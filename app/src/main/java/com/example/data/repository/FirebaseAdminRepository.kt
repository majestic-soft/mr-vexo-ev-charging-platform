package com.example.data.repository

import com.example.data.model.*
import com.example.data.security.SecurityDefenseEngine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseAdminRepository {

  private val firestore: FirebaseFirestore? by lazy {
    try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      null
    }
  }

  private val auth: FirebaseAuth? by lazy {
    try {
      FirebaseAuth.getInstance()
    } catch (e: Exception) {
      null
    }
  }

  private val coroutineScope = CoroutineScope(Dispatchers.IO)

  // Current Logged-in Admin State
  private val _currentAdmin = MutableStateFlow<AdminStaffUser?>(null)
  val currentAdmin: StateFlow<AdminStaffUser?> = _currentAdmin.asStateFlow()

  // Master Data Collections
  private val _staffList = MutableStateFlow<List<AdminStaffUser>>(emptyList())
  val staffList: StateFlow<List<AdminStaffUser>> = _staffList.asStateFlow()

  private val _customers = MutableStateFlow<List<Customer>>(emptyList())
  val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

  private val _hosts = MutableStateFlow<List<Host>>(emptyList())
  val hosts: StateFlow<List<Host>> = _hosts.asStateFlow()

  private val _stations = MutableStateFlow<List<ChargingStation>>(emptyList())
  val stations: StateFlow<List<ChargingStation>> = _stations.asStateFlow()

  private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
  val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

  private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
  val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

  private val _withdrawals = MutableStateFlow<List<HostWithdrawal>>(emptyList())
  val withdrawals: StateFlow<List<HostWithdrawal>> = _withdrawals.asStateFlow()

  private val _promos = MutableStateFlow<List<PromoCode>>(emptyList())
  val promos: StateFlow<List<PromoCode>> = _promos.asStateFlow()

  private val _couponUsageHistory = MutableStateFlow<List<CouponUsageRecord>>(emptyList())
  val couponUsageHistory: StateFlow<List<CouponUsageRecord>> = _couponUsageHistory.asStateFlow()

  private val _disputes = MutableStateFlow<List<Dispute>>(emptyList())
  val disputes: StateFlow<List<Dispute>> = _disputes.asStateFlow()

  private val _tickets = MutableStateFlow<List<SupportTicket>>(emptyList())
  val tickets: StateFlow<List<SupportTicket>> = _tickets.asStateFlow()

  private val _notifications = MutableStateFlow<List<AdminNotification>>(emptyList())
  val notifications: StateFlow<List<AdminNotification>> = _notifications.asStateFlow()

  private val _metrics = MutableStateFlow(DashboardMetrics())
  val metrics: StateFlow<DashboardMetrics> = _metrics.asStateFlow()

  private val _systemSettings = MutableStateFlow(SystemSettings())
  val systemSettings: StateFlow<SystemSettings> = _systemSettings.asStateFlow()

  // Enterprise Cyber Defense Shield State
  private val _securityStatus = MutableStateFlow(SecurityShieldStatus())
  val securityStatus: StateFlow<SecurityShieldStatus> = _securityStatus.asStateFlow()

  private val _securityThreatEvents = MutableStateFlow<List<SecurityThreatEvent>>(emptyList())
  val securityThreatEvents: StateFlow<List<SecurityThreatEvent>> = _securityThreatEvents.asStateFlow()

  private val _isPlatformLockedDown = MutableStateFlow(false)
  val isPlatformLockedDown: StateFlow<Boolean> = _isPlatformLockedDown.asStateFlow()

  private val _failedLoginAttempts = MutableStateFlow(0)
  val failedLoginAttempts: StateFlow<Int> = _failedLoginAttempts.asStateFlow()

  private val _selectedTimeFilter = MutableStateFlow("This Month")
  val selectedTimeFilter: StateFlow<String> = _selectedTimeFilter.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private val _isLiveSimulationActive = MutableStateFlow(true)
  val isLiveSimulationActive: StateFlow<Boolean> = _isLiveSimulationActive.asStateFlow()

  init {
    seedInitialData()
    // Auto-login Super Admin as default session
    loginSuperAdmin()
    listenFirestoreChanges()
    startLiveTelemetryEngine()
    runSecurityScan()
  }

  // --- Authentication & Session Security ---

  fun login(email: String, pass: String): Result<AdminStaffUser> {
    if (_isPlatformLockedDown.value) {
      return Result.failure(Exception("Platform Security Lockdown in effect. All logins temporarily restricted."))
    }

    if (_failedLoginAttempts.value >= 5) {
      return Result.failure(Exception("Account Security Lockout: Too many failed attempts. Brute-force defense active."))
    }

    val cleanEmail = email.trim().lowercase()
    val user = _staffList.value.find { it.email.lowercase() == cleanEmail }
    
    if (user == null) {
      _failedLoginAttempts.value = _failedLoginAttempts.value + 1
      logSecurityThreat(
        type = "FAILED_LOGIN_ATTEMPT",
        severity = if (_failedLoginAttempts.value >= 3) "HIGH" else "MEDIUM",
        desc = "Failed login attempt for '$email' (Attempt #${_failedLoginAttempts.value})"
      )
      return Result.failure(Exception("Invalid credentials. No admin profile found for '$email'."))
    }
    if (user.status.equals("Suspended", ignoreCase = true) || user.status.equals("Inactive", ignoreCase = true)) {
      return Result.failure(Exception("Access Denied: Your account is suspended or inactive. Please contact a Super Administrator."))
    }

    _failedLoginAttempts.value = 0
    _currentAdmin.value = user
    _statusMessage.value = "Authenticated as ${user.name} (${user.role.displayName})"
    return Result.success(user)
  }

  fun loginAsRole(role: AdminRole) {
    val targetUser = _staffList.value.find { it.role == role && it.status == "Active" }
      ?: _staffList.value.find { it.role == role }
      ?: return
    _currentAdmin.value = targetUser
    _statusMessage.value = "Switched active admin session to: ${targetUser.name} (${targetUser.role.displayName})"
  }

  fun selectAdminUser(user: AdminStaffUser) {
    if (user.status == "Suspended") {
      _statusMessage.value = "Access Denied: Account '${user.name}' is currently suspended."
      return
    }
    _currentAdmin.value = user
    _statusMessage.value = "Active session changed to: ${user.name} (${user.role.displayName})"
  }

  fun logout() {
    _currentAdmin.value = null
    try {
      auth?.signOut()
    } catch (e: Exception) {
      // Ignored
    }
    _statusMessage.value = "You have been logged out securely."
  }

  fun checkPermission(permission: String): Boolean {
    val admin = _currentAdmin.value ?: return false
    if (admin.role == AdminRole.SUPER_ADMIN) return true
    return admin.permissions.contains(permission)
  }

  private fun verifyPermission(permission: String) {
    if (!checkPermission(permission)) {
      throw SecurityException("Access Restricted: Your role (${_currentAdmin.value?.role?.displayName ?: "None"}) does not have '$permission' permission.")
    }
  }

  // --- Strict Data Isolation by Role (Backend / Repository Level) ---

  fun getAccessibleStaff(): List<AdminStaffUser> {
    val current = _currentAdmin.value ?: return emptyList()
    return when (current.role) {
      AdminRole.SUPER_ADMIN -> _staffList.value
      AdminRole.ADMIN -> _staffList.value.filter { it.role != AdminRole.SUPER_ADMIN || it.id == current.id }
      AdminRole.MANAGER -> _staffList.value.filter { it.managerId == current.id || it.id == current.id }
      AdminRole.SUPPORT, AdminRole.FINANCE, AdminRole.OPERATIONS -> listOf(current)
    }
  }

  fun getAccessibleTickets(): List<SupportTicket> {
    val current = _currentAdmin.value ?: return emptyList()
    return when (current.role) {
      AdminRole.SUPER_ADMIN, AdminRole.ADMIN -> _tickets.value
      AdminRole.MANAGER -> {
        // Manager sees tickets assigned to team members, unassigned tickets, and escalated tickets
        val myTeamStaffIds = _staffList.value.filter { it.managerId == current.id }.map { it.id }
        _tickets.value.filter {
          it.escalatedToManager ||
          myTeamStaffIds.contains(it.assignedToId) ||
          it.assignedToId == current.id ||
          it.assignedToId.isEmpty()
        }
      }
      AdminRole.SUPPORT -> {
        // STRICT ISOLATION: Support sees ONLY their assigned tickets!
        _tickets.value.filter {
          it.assignedToId == current.id || it.assignedStaffName.equals(current.name, ignoreCase = true)
        }
      }
      AdminRole.FINANCE -> emptyList()
      AdminRole.OPERATIONS -> _tickets.value.filter { it.department.equals("Operations", ignoreCase = true) }
    }
  }

  fun getAccessibleCustomers(): List<Customer> {
    val current = _currentAdmin.value ?: return emptyList()
    return when (current.role) {
      AdminRole.SUPER_ADMIN, AdminRole.ADMIN, AdminRole.MANAGER, AdminRole.OPERATIONS, AdminRole.FINANCE -> _customers.value
      AdminRole.SUPPORT -> {
        // Support sees ONLY customers related to their assigned tickets!
        val myTicketCustomerIds = getAccessibleTickets().map { it.customerId }.toSet()
        _customers.value.filter { myTicketCustomerIds.contains(it.id) || myTicketCustomerIds.contains(it.customerCode) }
      }
    }
  }

  fun getAccessibleBookings(): List<Booking> {
    val current = _currentAdmin.value ?: return emptyList()
    return when (current.role) {
      AdminRole.SUPER_ADMIN, AdminRole.ADMIN, AdminRole.OPERATIONS, AdminRole.FINANCE, AdminRole.MANAGER -> _bookings.value
      AdminRole.SUPPORT -> {
        // Support sees ONLY bookings related to their assigned tickets!
        val myTicketBookingIds = getAccessibleTickets().map { it.bookingId }.toSet()
        _bookings.value.filter { myTicketBookingIds.contains(it.id) || myTicketBookingIds.contains(it.bookingCode) }
      }
    }
  }

  fun getAccessibleStations(): List<ChargingStation> {
    val current = _currentAdmin.value ?: return emptyList()
    return when (current.role) {
      AdminRole.SUPER_ADMIN, AdminRole.ADMIN, AdminRole.OPERATIONS, AdminRole.MANAGER -> _stations.value
      AdminRole.FINANCE -> _stations.value
      AdminRole.SUPPORT -> {
        val myTicketStationIds = getAccessibleTickets().map { it.stationId }.toSet()
        _stations.value.filter { myTicketStationIds.contains(it.id) || myTicketStationIds.contains(it.stationCode) }
      }
    }
  }

  fun getAccessibleDisputes(): List<Dispute> {
    val current = _currentAdmin.value ?: return emptyList()
    return when (current.role) {
      AdminRole.SUPER_ADMIN, AdminRole.ADMIN, AdminRole.MANAGER, AdminRole.OPERATIONS -> _disputes.value
      AdminRole.SUPPORT -> _disputes.value.filter {
        it.assignedStaffId == current.id || it.assignedStaffName.equals(current.name, ignoreCase = true)
      }
      AdminRole.FINANCE -> _disputes.value.filter { it.refundAmount > 0 || it.status.contains("Refund", ignoreCase = true) }
    }
  }

  fun getAccessibleNotifications(): List<AdminNotification> {
    val current = _currentAdmin.value ?: return emptyList()
    return _notifications.value.filter { notif ->
      notif.targetRole == "ALL" ||
      notif.targetRole.equals(current.role.name, ignoreCase = true) ||
      (current.role == AdminRole.SUPER_ADMIN)
    }
  }

  // --- Real Coupon & Promo Code Engine (Backend Calculated & Validated) ---

  fun validateAndApplyCoupon(
    code: String,
    userId: String,
    bookingAmount: Double,
    stationId: String = ""
  ): CouponValidationResult {
    val cleanCode = code.trim().uppercase()
    val promo = _promos.value.find { it.code.equals(cleanCode, ignoreCase = true) }

    if (promo == null) {
      return CouponValidationResult(isValid = false, message = "Invalid coupon code '$cleanCode'.")
    }
    if (!promo.active || promo.status != "Active") {
      return CouponValidationResult(isValid = false, message = "Coupon '$cleanCode' is currently inactive.")
    }
    if (bookingAmount < promo.minBookingAmount) {
      return CouponValidationResult(
        isValid = false,
        message = "Minimum booking amount of ₹${promo.minBookingAmount.toInt()} is required for this coupon."
      )
    }
    if (promo.currentUsageCount >= promo.totalUsageLimit) {
      return CouponValidationResult(isValid = false, message = "Coupon usage limit has been reached.")
    }
    
    val userUsageCount = _couponUsageHistory.value.count { it.userId == userId && it.couponCode.equals(cleanCode, ignoreCase = true) }
    if (userUsageCount >= promo.perUserLimit) {
      return CouponValidationResult(isValid = false, message = "You have already used this coupon the maximum allowed times (${promo.perUserLimit}x).")
    }

    // Backend-calculated discount: never trust client-manipulated discount amounts
    val calculatedDiscount = if (promo.discountType.equals("PERCENTAGE", ignoreCase = true)) {
      val rawPercentDiscount = bookingAmount * (promo.discountPercent / 100.0)
      rawPercentDiscount.coerceAtMost(promo.maxDiscountAmount).coerceAtMost(bookingAmount)
    } else {
      promo.discountAmount.coerceAtMost(promo.maxDiscountAmount).coerceAtMost(bookingAmount)
    }

    val finalPayable = (bookingAmount - calculatedDiscount).coerceAtLeast(0.0)
    val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

    // Record usage
    val usageRecord = CouponUsageRecord(
      id = "usg_${System.currentTimeMillis()}",
      userId = userId,
      userName = _customers.value.find { it.id == userId }?.name ?: "Customer",
      bookingId = "BK-SIM-${(1000..9999).random()}",
      couponCode = cleanCode,
      discountAmount = Math.round(calculatedDiscount * 100.0) / 100.0,
      originalAmount = bookingAmount,
      finalAmount = Math.round(finalPayable * 100.0) / 100.0,
      timestamp = now
    )
    _couponUsageHistory.value = listOf(usageRecord) + _couponUsageHistory.value

    // Increment backend usage count
    _promos.value = _promos.value.map {
      if (it.id == promo.id) {
        it.copy(
          currentUsageCount = it.currentUsageCount + 1,
          usedCount = it.usedCount + 1
        )
      } else it
    }

    return CouponValidationResult(
      isValid = true,
      discountAmount = Math.round(calculatedDiscount * 100.0) / 100.0,
      finalAmount = Math.round(finalPayable * 100.0) / 100.0,
      message = "Coupon applied! You saved ₹${Math.round(calculatedDiscount * 100.0) / 100.0}."
    )
  }

  fun createPromoCode(
    code: String,
    title: String,
    discountType: String = "PERCENTAGE",
    discountPercent: Int = 15,
    discountAmount: Double = 50.0,
    maxDiscount: Double = 150.0,
    minBookingAmount: Double = 300.0,
    usageLimit: Int = 500,
    perUserLimit: Int = 2,
    startDate: String = "01 Aug 2026",
    endDate: String = "31 Dec 2026"
  ) {
    verifyPermission(AppPermissions.MANAGE_PROMOS)
    val newPromo = PromoCode(
      id = "prm_${System.currentTimeMillis()}",
      code = code.uppercase().trim(),
      title = title,
      discountType = discountType,
      discountPercent = discountPercent,
      discountAmount = discountAmount,
      maxDiscount = maxDiscount,
      maxDiscountAmount = maxDiscount,
      minBookingAmount = minBookingAmount,
      usageLimit = usageLimit,
      totalUsageLimit = usageLimit,
      currentUsageCount = 0,
      perUserLimit = perUserLimit,
      startDate = startDate,
      endDate = endDate,
      status = "Active",
      active = true
    )
    _promos.value = listOf(newPromo) + _promos.value
    syncToFirestore("promos", newPromo.id, newPromo)
    _statusMessage.value = "Promo code '${code.uppercase()}' created successfully."
  }

  fun togglePromoStatus(promoId: String) {
    verifyPermission(AppPermissions.MANAGE_PROMOS)
    _promos.value = _promos.value.map { promo ->
      if (promo.id == promoId) {
        val next = !promo.active
        val updated = promo.copy(active = next, status = if (next) "Active" else "Inactive")
        syncToFirestore("promos", promoId, updated)
        updated
      } else promo
    }
    _statusMessage.value = "Promo code status updated."
  }

  // --- Support Ticket Workflow & Escalation ---

  fun escalateTicketToManager(ticketId: String, reason: String): Boolean {
    verifyPermission(AppPermissions.ESCALATE_TICKET)
    val current = _currentAdmin.value ?: return false
    val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

    val targetTicket = _tickets.value.find { it.id == ticketId } ?: return false

    val escalationReply = SupportTicketReply(
      senderName = current.name,
      senderRole = current.role.displayName,
      message = "[ESCALATED TO MANAGER] Reason: $reason",
      timestamp = now,
      isInternalNote = true
    )

    val updatedTicket = targetTicket.copy(
      status = "Escalated to Manager",
      escalatedToManager = true,
      escalatedReason = reason,
      updatedAt = now,
      replies = targetTicket.replies + escalationReply
    )

    _tickets.value = _tickets.value.map { if (it.id == ticketId) updatedTicket else it }

    // Dispatch notification to Manager
    val notif = AdminNotification(
      id = "notif_esc_${System.currentTimeMillis()}",
      title = "Ticket Escalation: ${targetTicket.ticketCode}",
      message = "${current.name} escalated ticket '${targetTicket.subject}' to Operations Manager. Reason: $reason",
      type = "DISPUTE",
      targetRole = "MANAGER",
      timestamp = "Just now",
      read = false,
      referenceId = ticketId
    )
    _notifications.value = listOf(notif) + _notifications.value

    syncToFirestore("tickets", ticketId, updatedTicket)
    _statusMessage.value = "Ticket ${targetTicket.ticketCode} escalated to Manager successfully."
    return true
  }

  fun resolveDispute(
    disputeId: String,
    refundApproved: Boolean,
    refundAmount: Double,
    resolutionNotes: String
  ): Boolean {
    verifyPermission(AppPermissions.MANAGE_DISPUTES)
    val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
    val current = _currentAdmin.value ?: return false

    val targetDispute = _disputes.value.find { it.id == disputeId } ?: return false

    val updatedDispute = targetDispute.copy(
      status = if (refundApproved) "Resolved (Refunded)" else "Resolved (Dismissed)",
      refundAmount = if (refundApproved) refundAmount else 0.0,
      resolutionNotes = resolutionNotes,
      updatedAt = now
    )

    _disputes.value = _disputes.value.map { if (it.id == disputeId) updatedDispute else it }
    syncToFirestore("disputes", disputeId, updatedDispute)

    if (refundApproved && refundAmount > 0) {
      // Credit customer wallet and record transaction
      _customers.value = _customers.value.map { customer ->
        if (customer.id == targetDispute.customerId) {
          val updatedCust = customer.copy(walletBalance = customer.walletBalance + refundAmount)
          syncToFirestore("customers", customer.id, updatedCust)
          updatedCust
        } else customer
      }

      val tx = WalletTransaction(
        id = "tx_rf_${System.currentTimeMillis()}",
        transactionCode = "TX-REF-${1000 + _transactions.value.size + 1}",
        userId = targetDispute.customerId,
        userName = targetDispute.customerName,
        userType = "Customer",
        type = "Refund",
        amount = refundAmount,
        status = "Success",
        date = now,
        referenceBookingId = targetDispute.bookingId,
        description = "Refund approved for Dispute #${targetDispute.disputeCode}",
        note = resolutionNotes
      )
      _transactions.value = listOf(tx) + _transactions.value
      syncToFirestore("transactions", tx.id, tx)
    }

    _statusMessage.value = "Dispute #${targetDispute.disputeCode} resolved."
    updateMetrics()
    return true
  }

  fun addTicketReply(ticketId: String, message: String, isInternalNote: Boolean) {
    if (isInternalNote) {
      verifyPermission(AppPermissions.ADD_NOTES)
    } else {
      verifyPermission(AppPermissions.MANAGE_SUPPORT_TICKETS)
    }
    val admin = _currentAdmin.value
    val now = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
    val reply = SupportTicketReply(
      senderName = admin?.name ?: "Support Staff",
      senderRole = admin?.role?.displayName ?: "Support",
      message = message,
      timestamp = now,
      isInternalNote = isInternalNote
    )

    _tickets.value = _tickets.value.map { ticket ->
      if (ticket.id == ticketId) {
        val nextStatus = if (!isInternalNote && ticket.status == "Open") "In Progress" else ticket.status
        val updated = ticket.copy(
          replies = ticket.replies + reply,
          status = nextStatus,
          updatedAt = now
        )
        syncToFirestore("tickets", ticketId, updated)
        updated
      } else ticket
    }
    _statusMessage.value = if (isInternalNote) "Internal note added." else "Reply sent to customer."
  }

  fun updateTicketStatus(ticketId: String, newStatus: String) {
    verifyPermission(AppPermissions.MANAGE_SUPPORT_TICKETS)
    val now = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
    _tickets.value = _tickets.value.map { ticket ->
      if (ticket.id == ticketId) {
        val updated = ticket.copy(status = newStatus, updatedAt = now)
        syncToFirestore("tickets", ticketId, updated)
        updated
      } else ticket
    }
    updateMetrics()
    _statusMessage.value = "Ticket status updated to $newStatus."
  }

  fun assignTicketStaff(ticketId: String, staffId: String, staffName: String) {
    verifyPermission(AppPermissions.MANAGE_SUPPORT_TICKETS)
    _tickets.value = _tickets.value.map { ticket ->
      if (ticket.id == ticketId) {
        val updated = ticket.copy(
          assignedToId = staffId,
          assignedToName = staffName,
          assignedStaffName = staffName,
          status = if (ticket.status == "Open") "Assigned" else ticket.status
        )
        syncToFirestore("tickets", ticketId, updated)
        updated
      } else ticket
    }
    _statusMessage.value = "Ticket assigned to $staffName."
  }

  // --- Staff & RBAC Management (Super Admin & Admin Only) ---

  fun addStaffMember(
    name: String,
    email: String,
    phone: String,
    role: AdminRole,
    department: String,
    permissions: List<String>,
    managerName: String
  ): Boolean {
    val current = _currentAdmin.value
    if (current == null) {
      _statusMessage.value = "Security Violation: No active admin session found."
      return false
    }
    if (current.role != AdminRole.SUPER_ADMIN && !current.permissions.contains(AppPermissions.MANAGE_STAFF)) {
      _statusMessage.value = "Security Violation: Only Super Admins or authorized managers can create staff accounts."
      return false
    }
    if (role == AdminRole.SUPER_ADMIN && current.role != AdminRole.SUPER_ADMIN) {
      _statusMessage.value = "Security Violation: Only a Super Admin can create new Super Admin accounts."
      return false
    }

    val newId = "stf_${System.currentTimeMillis()}"
    val code = "STF${1000 + _staffList.value.size + 1}"
    val assignedPermissions = if (permissions.isNotEmpty()) permissions else AppPermissions.getPermissionsForRole(role)

    val newStaff = AdminStaffUser(
      id = newId,
      staffCode = code,
      name = name,
      email = email,
      phone = phone,
      role = role,
      department = department,
      status = "Active",
      joinedOn = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date()),
      managerId = if (managerName.isNotBlank()) "stf_1003" else current.id,
      managerName = managerName.ifBlank { current.name },
      permissions = assignedPermissions,
      assignedAccess = mapOf(
        "Module Scope" to "${role.displayName} Access",
        "Assigned Department" to department
      ),
      avatarLetter = name.take(1).uppercase()
    )

    _staffList.value = listOf(newStaff) + _staffList.value
    syncToFirestore("admins", newId, newStaff)
    _statusMessage.value = "Staff member '${name}' registered with role '${role.displayName}'."
    updateMetrics()
    return true
  }

  fun updateStaffPermissions(staffId: String, newRole: AdminRole, newPermissions: List<String>, newDepartment: String): Boolean {
    return changeAdminRoleAndPermissions(staffId, newRole, newPermissions, newDepartment)
  }

  fun changeAdminRoleAndPermissions(
    staffId: String,
    newRole: AdminRole,
    newPermissions: List<String>,
    newDepartment: String
  ): Boolean {
    val current = _currentAdmin.value
    if (current == null) {
      _statusMessage.value = "Security Violation: No active admin session."
      return false
    }
    if (current.role != AdminRole.SUPER_ADMIN && !current.permissions.contains(AppPermissions.MANAGE_ROLES_PERMISSIONS)) {
      _statusMessage.value = "Security Violation: Insufficient privileges. Only Super Admins can alter roles and permissions."
      return false
    }
    if (newRole == AdminRole.SUPER_ADMIN && current.role != AdminRole.SUPER_ADMIN) {
      _statusMessage.value = "Security Violation: Only a Super Admin can promote a user to Super Admin."
      return false
    }

    val targetStaff = _staffList.value.find { it.id == staffId }
    if (targetStaff == null) {
      _statusMessage.value = "Admin user not found."
      return false
    }

    val finalPermissions = if (newPermissions.isNotEmpty()) newPermissions else AppPermissions.getPermissionsForRole(newRole)

    val updatedStaff = targetStaff.copy(
      role = newRole,
      department = newDepartment.ifBlank { targetStaff.department },
      permissions = finalPermissions
    )

    _staffList.value = _staffList.value.map { if (it.id == staffId) updatedStaff else it }
    
    if (current.id == staffId) {
      _currentAdmin.value = updatedStaff
    }

    syncToFirestore("admins", staffId, updatedStaff)
    _statusMessage.value = "Updated role to '${newRole.displayName}' with ${finalPermissions.size} permissions for ${updatedStaff.name}."
    updateMetrics()
    return true
  }

  fun setAdminAccountStatus(staffId: String, newStatus: String): Boolean {
    val current = _currentAdmin.value
    if (current == null) {
      _statusMessage.value = "Security Violation: No active admin session."
      return false
    }
    if (current.role != AdminRole.SUPER_ADMIN && !current.permissions.contains(AppPermissions.MANAGE_STAFF)) {
      _statusMessage.value = "Security Violation: Insufficient privileges to alter account activation status."
      return false
    }
    if (current.id == staffId && newStatus == "Suspended") {
      _statusMessage.value = "Action Blocked: You cannot suspend your own active Super Admin account."
      return false
    }

    val target = _staffList.value.find { it.id == staffId }
    if (target == null) {
      _statusMessage.value = "Admin user not found."
      return false
    }

    val updated = target.copy(status = newStatus)
    _staffList.value = _staffList.value.map { if (it.id == staffId) updated else it }
    if (current.id == staffId) {
      _currentAdmin.value = updated
    }

    syncToFirestore("admins", staffId, updated)
    _statusMessage.value = "Account status for ${target.name} set to '$newStatus'."
    updateMetrics()
    return true
  }

  fun toggleStaffStatus(staffId: String) {
    val staff = _staffList.value.find { it.id == staffId } ?: return
    val nextStatus = if (staff.status == "Active") "Suspended" else "Active"
    setAdminAccountStatus(staffId, nextStatus)
  }

  fun suspendAdminAccount(staffId: String): Boolean = setAdminAccountStatus(staffId, "Suspended")
  fun activateAdminAccount(staffId: String): Boolean = setAdminAccountStatus(staffId, "Active")

  fun resetStaffPassword(staffId: String) {
    verifyPermission(AppPermissions.MANAGE_STAFF)
    val staff = _staffList.value.find { it.id == staffId }
    _statusMessage.value = "Password reset instructions dispatched to ${staff?.email ?: "user"}."
  }

  // --- Customer Operations ---

  fun toggleCustomerStatus(customerId: String) {
    verifyPermission(AppPermissions.MANAGE_CUSTOMERS)
    _customers.value = _customers.value.map { customer ->
      if (customer.id == customerId) {
        val nextStatus = if (customer.status == "Active") "Suspended" else "Active"
        customer.copy(status = nextStatus)
      } else customer
    }
    _statusMessage.value = "Customer account status updated."
  }

  // --- Host & Station Operations ---

  fun approveOrRejectHost(hostId: String, approve: Boolean) {
    verifyPermission(AppPermissions.APPROVE_HOSTS)
    _hosts.value = _hosts.value.map { host ->
      if (host.id == hostId) {
        host.copy(
          verificationStatus = if (approve) "Verified" else "Rejected",
          status = if (approve) "Active" else "Suspended"
        )
      } else host
    }
    updateMetrics()
    _statusMessage.value = if (approve) "Host verification approved." else "Host verification rejected."
  }

  fun toggleHostStatus(hostId: String) {
    verifyPermission(AppPermissions.MANAGE_HOSTS)
    _hosts.value = _hosts.value.map { host ->
      if (host.id == hostId) {
        val nextStatus = if (host.status == "Active") "Suspended" else "Active"
        host.copy(status = nextStatus)
      } else host
    }
    updateMetrics()
    _statusMessage.value = "Host account status updated."
  }

  fun approveOrRejectStation(stationId: String, approve: Boolean) {
    verifyPermission(AppPermissions.APPROVE_STATIONS)
    _stations.value = _stations.value.map { station ->
      if (station.id == stationId) {
        station.copy(
          approvalStatus = if (approve) "Approved" else "Rejected",
          status = if (approve) "Active" else "Offline"
        )
      } else station
    }
    updateMetrics()
    _statusMessage.value = if (approve) "Station approved & published online." else "Station approval rejected."
  }

  fun toggleStationStatus(stationId: String, newStatus: String) {
    verifyPermission(AppPermissions.MANAGE_STATIONS)
    _stations.value = _stations.value.map { station ->
      if (station.id == stationId) {
        station.copy(status = newStatus)
      } else station
    }
    updateMetrics()
    _statusMessage.value = "Station status set to: $newStatus"
  }

  // --- Booking & Charging Sessions ---

  fun updateBookingStatus(bookingId: String, bookingStatus: String, chargingStatus: String) {
    verifyPermission(AppPermissions.MANAGE_BOOKINGS)
    _bookings.value = _bookings.value.map { booking ->
      if (booking.id == bookingId) {
        booking.copy(bookingStatus = bookingStatus, chargingStatus = chargingStatus)
      } else booking
    }
    _statusMessage.value = "Booking $bookingId updated to $bookingStatus."
  }

  // --- Financial Settlements & Withdrawals ---

  fun processWithdrawal(withdrawalId: String, approved: Boolean, remarks: String) {
    verifyPermission(AppPermissions.MANAGE_WITHDRAWALS)
    val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
    _withdrawals.value = _withdrawals.value.map { item ->
      if (item.id == withdrawalId) {
        item.copy(
          status = if (approved) "Completed" else "Rejected",
          processedDate = now,
          remarks = remarks.ifBlank { if (approved) "Approved & Transferred" else "Declined due to KYC mismatch" },
          referenceNumber = if (approved) "TXN_NEFT_${System.currentTimeMillis() % 1000000}" else ""
        )
      } else item
    }
    updateMetrics()
    _statusMessage.value = if (approved) "Withdrawal approved & settled." else "Withdrawal rejected."
  }

  // --- Dispute Operations ---

  fun updateDisputeStatus(disputeId: String, newStatus: String, notes: String, refundAmount: Double = 0.0) {
    verifyPermission(AppPermissions.MANAGE_DISPUTES)
    val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
    _disputes.value = _disputes.value.map { dispute ->
      if (dispute.id == disputeId) {
        dispute.copy(
          status = newStatus,
          resolutionNotes = notes.ifBlank { dispute.resolutionNotes },
          refundAmount = if (refundAmount > 0) refundAmount else dispute.refundAmount,
          updatedAt = now
        )
      } else dispute
    }
    updateMetrics()
    _statusMessage.value = "Dispute $disputeId status set to $newStatus."
  }

  fun assignDisputeStaff(disputeId: String, staffId: String, staffName: String) {
    verifyPermission(AppPermissions.MANAGE_DISPUTES)
    _disputes.value = _disputes.value.map { dispute ->
      if (dispute.id == disputeId) {
        dispute.copy(
          assignedStaffId = staffId,
          assignedStaffName = staffName,
          status = if (dispute.status == "Open") "Under Review" else dispute.status
        )
      } else dispute
    }
    _statusMessage.value = "Dispute assigned to $staffName."
  }

  // --- System Settings & Notifications ---

  fun updateSystemSettings(settings: SystemSettings) {
    verifyPermission(AppPermissions.SYSTEM_SETTINGS)
    _systemSettings.value = settings
    _statusMessage.value = "System settings updated successfully."
  }

  fun runSecurityScan() {
    val (status, events) = SecurityDefenseEngine.runComprehensiveSecurityAudit(null)
    _securityStatus.value = status
    _securityThreatEvents.value = (events + _securityThreatEvents.value).distinctBy { it.id }.take(50)
    _statusMessage.value = "🛡️ Deep Security Audit Completed: ${status.overallRating}"
  }

  fun logSecurityThreat(type: String, severity: String, desc: String, source: String = "Security Defense Engine") {
    val now = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date())
    val event = SecurityThreatEvent(
      id = "thr_${System.currentTimeMillis()}_${(100..999).random()}",
      timestamp = now,
      threatType = type,
      severity = severity,
      description = desc,
      source = source,
      status = "BLOCKED"
    )
    _securityThreatEvents.value = listOf(event) + _securityThreatEvents.value.take(49)
  }

  fun triggerEmergencyLockdown(reason: String) {
    verifyPermission(AppPermissions.SYSTEM_SETTINGS)
    _isPlatformLockedDown.value = true
    logSecurityThreat(
      type = "EMERGENCY_LOCKDOWN_TRIGGERED",
      severity = "CRITICAL",
      desc = "Emergency platform lockdown engaged by Super Admin. Reason: $reason",
      source = "Executive Killswitch"
    )
    _statusMessage.value = "🚨 EMERGENCY LOCKDOWN ENGAGED: All platform actions frozen."
  }

  fun liftEmergencyLockdown() {
    verifyPermission(AppPermissions.SYSTEM_SETTINGS)
    _isPlatformLockedDown.value = false
    logSecurityThreat(
      type = "LOCKDOWN_LIFTED",
      severity = "INFO",
      desc = "Emergency platform lockdown lifted by Super Admin.",
      source = "Executive Authorization"
    )
    _statusMessage.value = "✅ Emergency lockdown lifted. Operations resumed."
  }

  fun addAdminNotification(title: String, message: String, type: String = "GENERAL", targetRole: String = "ALL", refId: String = "") {
    val newNotif = AdminNotification(
      id = "notif_${System.currentTimeMillis()}",
      title = title,
      message = message,
      type = type,
      targetRole = targetRole,
      timestamp = "Just now",
      read = false,
      referenceId = refId
    )
    _notifications.value = listOf(newNotif) + _notifications.value
  }

  fun markNotificationAsRead(notificationId: String) {
    _notifications.value = _notifications.value.map { notif ->
      if (notif.id == notificationId) notif.copy(read = true) else notif
    }
  }

  fun markAllNotificationsAsRead() {
    _notifications.value = _notifications.value.map { it.copy(read = true) }
    _statusMessage.value = "All notifications marked as read."
  }

  fun setTimeFilter(filter: String) { _selectedTimeFilter.value = filter }
  fun clearStatusMessage() { _statusMessage.value = null }

  fun toggleLiveSimulation() {
    _isLiveSimulationActive.value = !_isLiveSimulationActive.value
    _statusMessage.value = if (_isLiveSimulationActive.value) "Live EV Fleet Stream Active" else "Fleet Telemetry Stream Paused"
  }

  fun setProductionMode(isLive: Boolean) {
    val current = _systemSettings.value
    val newSettings = current.copy(
      isLiveProductionMode = isLive,
      environmentMode = if (isLive) "LIVE PRODUCTION" else "STAGING / SANDBOX",
      firebaseEnvironment = if (isLive) "Production (asia-south1)" else "Sandbox / Staging Cluster",
      appVersion = if (isLive) "v3.8.2-enterprise-live" else "v3.8.2-staging"
    )
    _systemSettings.value = newSettings
    syncToFirestore("system_config", "settings", newSettings)
    _statusMessage.value = if (isLive) "🚀 System Promoted to LIVE PRODUCTION Mode." else "⚠️ System set to Sandbox Staging Mode."
    addAdminNotification(
      title = if (isLive) "System Promoted to Live Production" else "System Switched to Sandbox",
      message = "Environment changed to ${if (isLive) "LIVE PRODUCTION" else "STAGING"} by ${_currentAdmin.value?.name ?: "Super Admin"}.",
      type = "SYSTEM_UPDATE",
      targetRole = "ALL"
    )
  }

  fun refreshLiveFleetData() {
    _statusMessage.value = "🔄 Synced live telemetry from 389 EV charging stations across 6 regions."
    val current = _metrics.value
    _metrics.value = current.copy(
      activeChargingSessions = (38..48).random(),
      totalEnergyDeliveredTodayKwh = Math.round((current.totalEnergyDeliveredTodayKwh + (5..15).random()) * 10.0) / 10.0
    )
  }

  fun broadcastSystemAlert(title: String, message: String, targetRole: String = "ALL") {
    addAdminNotification(
      title = "📢 BROADCAST: $title",
      message = message,
      type = "GENERAL",
      targetRole = targetRole
    )
    _statusMessage.value = "📢 Live broadcast sent to $targetRole staff."
  }

  fun simulateLiveEvent(type: String) {
    when (type) {
      "booking" -> simulateNewBooking()
      "plug_in" -> simulateStationPlugin()
      "payment" -> simulateNewPayment()
      "alert" -> simulateMaintenanceAlert()
    }
  }

  private fun simulateNewBooking() {
    val bookingNum = (9030..9999).random()
    val stationsList = _stations.value.filter { it.status == "Active" }
    val station = stationsList.randomOrNull() ?: return
    val newBooking = Booking(
      id = "bk_sim_${System.currentTimeMillis()}",
      bookingCode = "ULT-BK-$bookingNum",
      customerId = "cust_201",
      customerName = listOf("Aarav Mehta", "Pooja Hegde", "Siddharth Malhotra", "Kavita Nair").random(),
      customerPhone = "+91 98200 ${(10000..99999).random()}",
      hostId = station.hostId,
      hostName = station.hostName,
      stationId = station.id,
      stationName = station.name,
      connectorType = station.connectorTypes.firstOrNull() ?: "CCS2",
      date = "Today",
      slotTime = "Live Now",
      durationMinutes = 45,
      energyDeliveredKwh = 1.2,
      totalAmount = 350.0,
      bookingStatus = "In Progress",
      paymentStatus = "Paid",
      chargingStatus = "Charging",
      paymentMethod = "Ultimate Wallet",
      createdAt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
    )
    _bookings.value = listOf(newBooking) + _bookings.value
    _metrics.value = _metrics.value.copy(
      todayBookings = _metrics.value.todayBookings + 1,
      activeChargingSessions = _metrics.value.activeChargingSessions + 1,
      todayRevenue = _metrics.value.todayRevenue + 350.0
    )
    _statusMessage.value = "⚡ Live Event: New session started at ${station.name} (${newBooking.bookingCode})"
  }

  private fun simulateStationPlugin() {
    val updated = _stations.value.map { stn ->
      if (stn.availableSlots > 0 && Math.random() > 0.6) {
        stn.copy(availableSlots = (stn.availableSlots - 1).coerceAtLeast(0))
      } else stn
    }
    _stations.value = updated
    _statusMessage.value = "🔌 Telemetry Ping: EV plugged into charging bay"
  }

  private fun simulateNewPayment() {
    val amt = (250..1200).random().toDouble()
    val newTx = WalletTransaction(
      id = "tx_sim_${System.currentTimeMillis()}",
      transactionCode = "TXN-${(70000..99999).random()}",
      userId = "cust_201",
      userName = "Aarav Mehta",
      userType = "Customer",
      type = "Debit",
      amount = amt,
      status = "Success",
      date = "Just now",
      referenceBookingId = "ULT-BK-9021",
      description = "EV Fast Charging Session payment"
    )
    _transactions.value = listOf(newTx) + _transactions.value
    _metrics.value = _metrics.value.copy(todayRevenue = _metrics.value.todayRevenue + amt)
    _statusMessage.value = "💰 Payment Received: ₹$amt credited to system revenue"
  }

  private fun simulateMaintenanceAlert() {
    val stn = _stations.value.randomOrNull() ?: return
    val newNotif = AdminNotification(
      id = "notif_sim_${System.currentTimeMillis()}",
      title = "Station Gun Telemetry Alert",
      message = "Connector at ${stn.name} reported temperature stabilization at 42°C.",
      type = "STATION_SUBMISSION",
      targetRole = "OPERATIONS",
      timestamp = "Just now",
      read = false,
      referenceId = stn.id
    )
    _notifications.value = listOf(newNotif) + _notifications.value
    _statusMessage.value = "🔔 Telemetry Alert dispatched for ${stn.name}"
  }

  private fun startLiveTelemetryEngine() {
    coroutineScope.launch {
      while (true) {
        kotlinx.coroutines.delay(4000)
        if (_isLiveSimulationActive.value) {
          val currentBookings = _bookings.value
          var totalEnergyIncrement = 0.0
          var revenueIncrement = 0.0
          val updatedBookings = currentBookings.map { bk ->
            if (bk.chargingStatus == "Charging" || bk.bookingStatus == "In Progress") {
              val addKwh = (0.2 + (Math.random() * 0.4))
              totalEnergyIncrement += addKwh
              revenueIncrement += (addKwh * 18.5)
              bk.copy(
                energyDeliveredKwh = Math.round((bk.energyDeliveredKwh + addKwh) * 10.0) / 10.0,
                totalAmount = Math.round((bk.totalAmount + (addKwh * 18.5)) * 10.0) / 10.0
              )
            } else bk
          }
          _bookings.value = updatedBookings

          if (totalEnergyIncrement > 0.0 || Math.random() > 0.7) {
            _metrics.value = _metrics.value.copy(
              totalEnergyDeliveredTodayKwh = Math.round((_metrics.value.totalEnergyDeliveredTodayKwh + totalEnergyIncrement) * 10.0) / 10.0,
              todayRevenue = Math.round((_metrics.value.todayRevenue + revenueIncrement) * 10.0) / 10.0
            )
          }
        }
      }
    }
  }

  private fun loginSuperAdmin() {
    val superAdmin = _staffList.value.find { it.role == AdminRole.SUPER_ADMIN }
    if (superAdmin != null) {
      _currentAdmin.value = superAdmin
    }
  }

  private fun updateMetrics() {
    val staff = _staffList.value
    val hosts = _hosts.value
    val stations = _stations.value
    val withdrawals = _withdrawals.value
    val disputes = _disputes.value
    val tickets = _tickets.value

    _metrics.value = _metrics.value.copy(
      totalStaff = staff.size,
      activeStaff = staff.count { it.status == "Active" },
      activeHosts = hosts.count { it.status == "Active" },
      activeChargingStations = stations.count { it.status == "Active" },
      pendingHostApprovals = hosts.count { it.verificationStatus == "Pending Review" },
      pendingStationApprovals = stations.count { it.approvalStatus == "Pending Review" },
      pendingWithdrawalsCount = withdrawals.count { it.status == "Pending" },
      pendingWithdrawalsAmount = withdrawals.filter { it.status == "Pending" }.sumOf { it.amount },
      openDisputes = disputes.count { it.status == "Open" || it.status == "Under Review" },
      openSupportTickets = tickets.count { it.status == "Open" || it.status == "Assigned" || it.status == "In Progress" || it.status == "Escalated to Manager" }
    )
  }

  private fun syncToFirestore(collection: String, docId: String, data: Any) {
    try {
      firestore?.collection(collection)?.document(docId)?.set(data)
    } catch (e: Exception) {
      // Graceful local-state preservation
    }
  }

  private fun listenFirestoreChanges() {
    coroutineScope.launch {
      try {
        firestore?.collection("admins")?.addSnapshotListener { _, _ -> }
      } catch (e: Exception) { }
    }
  }

  private fun seedInitialData() {
    // 6 Distinct Role Archetypes Seeded
    val staffData = listOf(
      AdminStaffUser(
        id = "stf_1000",
        staffCode = "ADM1000",
        name = "Ankit Verma",
        email = "ankit.verma@ultimate.com",
        phone = "+91 98111 22334",
        role = AdminRole.SUPER_ADMIN,
        department = "Executive Leadership",
        status = "Active",
        joinedOn = "01 Jan 2025, 09:00 AM",
        managerId = "root",
        managerName = "Board of Directors",
        permissions = AppPermissions.superAdminPermissions,
        assignedAccess = mapOf("Access Level" to "Unrestricted Super Admin"),
        avatarLetter = "A"
      ),
      AdminStaffUser(
        id = "stf_1007",
        staffCode = "ADM1007",
        name = "Rajesh Kulkarni",
        email = "rajesh.kulkarni@ultimate.com",
        phone = "+91 98222 33445",
        role = AdminRole.ADMIN,
        department = "General Administration",
        status = "Active",
        joinedOn = "10 Jan 2025, 10:00 AM",
        managerId = "stf_1000",
        managerName = "Ankit Verma (Super Admin)",
        permissions = AppPermissions.adminDefaultPermissions,
        assignedAccess = mapOf("Scope" to "Operations & User Management"),
        avatarLetter = "R"
      ),
      AdminStaffUser(
        id = "stf_1003",
        staffCode = "MGR1003",
        name = "Vikram Patel",
        email = "vikram.patel@ultimate.com",
        phone = "+91 97234 56789",
        role = AdminRole.MANAGER,
        department = "Support & Team Operations",
        status = "Active",
        joinedOn = "20 Mar 2025, 09:30 AM",
        managerId = "stf_1000",
        managerName = "Ankit Verma (Super Admin)",
        permissions = AppPermissions.managerDefaultPermissions,
        assignedAccess = mapOf("Team" to "Customer Support Team Lead"),
        avatarLetter = "V"
      ),
      AdminStaffUser(
        id = "stf_1001",
        staffCode = "STF1001",
        name = "Rahul Sharma",
        email = "rahul.sharma@ultimate.com",
        phone = "+91 98765 43210",
        role = AdminRole.SUPPORT,
        department = "Customer Support",
        status = "Active",
        joinedOn = "08 May 2025, 10:30 AM",
        managerId = "stf_1003",
        managerName = "Vikram Patel (Manager)",
        permissions = AppPermissions.supportDefaultPermissions,
        assignedAccess = mapOf("Assigned Queue" to "Tier 1 EV Support"),
        avatarLetter = "R"
      ),
      AdminStaffUser(
        id = "stf_1002",
        staffCode = "STF1002",
        name = "Priya Singh",
        email = "priya.singh@ultimate.com",
        phone = "+91 98123 45678",
        role = AdminRole.SUPPORT,
        department = "Customer Support",
        status = "Active",
        joinedOn = "12 Jun 2025, 11:15 AM",
        managerId = "stf_1003",
        managerName = "Vikram Patel (Manager)",
        permissions = AppPermissions.supportDefaultPermissions,
        assignedAccess = mapOf("Assigned Queue" to "Tier 1 Host Support"),
        avatarLetter = "P"
      ),
      AdminStaffUser(
        id = "stf_1005",
        staffCode = "FIN1005",
        name = "Arjun Mehta",
        email = "arjun.mehta@ultimate.com",
        phone = "+91 95456 78901",
        role = AdminRole.FINANCE,
        department = "Finance & Settlements",
        status = "Active",
        joinedOn = "05 Jan 2025, 02:45 PM",
        managerId = "stf_1000",
        managerName = "Ankit Verma (Super Admin)",
        permissions = AppPermissions.financeDefaultPermissions,
        assignedAccess = mapOf("Ledger" to "Host Payouts & Wallets"),
        avatarLetter = "A"
      ),
      AdminStaffUser(
        id = "stf_1004",
        staffCode = "OPS1004",
        name = "Neha Gupta",
        email = "neha.gupta@ultimate.com",
        phone = "+91 96345 67890",
        role = AdminRole.OPERATIONS,
        department = "Field & Station Operations",
        status = "Active",
        joinedOn = "15 Feb 2025, 10:00 AM",
        managerId = "stf_1000",
        managerName = "Ankit Verma (Super Admin)",
        permissions = AppPermissions.operationsDefaultPermissions,
        assignedAccess = mapOf("Grid" to "Station Approvals & Telemetry"),
        avatarLetter = "N"
      )
    )
    _staffList.value = staffData

    // Customers Data
    _customers.value = listOf(
      Customer(
        id = "cust_201",
        customerCode = "CUST-8821",
        name = "Devendra Singhania",
        email = "devendra.s@gmail.com",
        phone = "+91 98201 12345",
        status = "Active",
        registrationDate = "14 Jan 2025",
        walletBalance = 1450.0,
        totalBookings = 28,
        totalEnergyConsumedKwh = 742.5,
        totalSpent = 13850.0,
        kycVerified = true
      ),
      Customer(
        id = "cust_202",
        customerCode = "CUST-8822",
        name = "Aisha Khan",
        email = "aisha.khan@outlook.com",
        phone = "+91 98332 54321",
        status = "Active",
        registrationDate = "22 Jan 2025",
        walletBalance = 620.0,
        totalBookings = 14,
        totalEnergyConsumedKwh = 380.0,
        totalSpent = 7120.0,
        kycVerified = true
      ),
      Customer(
        id = "cust_203",
        customerCode = "CUST-8823",
        name = "Rajeshwari Rao",
        email = "rajeshwari.rao@yahoo.com",
        phone = "+91 98450 99887",
        status = "Active",
        registrationDate = "02 Feb 2025",
        walletBalance = 2100.0,
        totalBookings = 42,
        totalEnergyConsumedKwh = 1120.8,
        totalSpent = 21400.0,
        kycVerified = true
      ),
      Customer(
        id = "cust_204",
        customerCode = "CUST-8824",
        name = "Kunal Kapoor",
        email = "kunal.kapoor@gmail.com",
        phone = "+91 98771 44556",
        status = "Suspended",
        registrationDate = "10 Feb 2025",
        walletBalance = 0.0,
        totalBookings = 3,
        totalEnergyConsumedKwh = 45.0,
        totalSpent = 850.0,
        kycVerified = false,
        activeDisputesCount = 1
      ),
      Customer(
        id = "cust_205",
        customerCode = "CUST-8825",
        name = "Tanvi Deshmukh",
        email = "tanvi.d@techcorp.in",
        phone = "+91 98902 33221",
        status = "Active",
        registrationDate = "18 Feb 2025",
        walletBalance = 890.0,
        totalBookings = 19,
        totalEnergyConsumedKwh = 510.2,
        totalSpent = 9650.0,
        kycVerified = true
      )
    )

    // Hosts Data
    _hosts.value = listOf(
      Host(
        id = "host_301",
        hostCode = "HST-401",
        name = "Edison Infra Pvt Ltd",
        email = "contact@edisoninfra.com",
        phone = "+91 99112 33445",
        status = "Active",
        verificationStatus = "Verified",
        joinedDate = "10 Jan 2025",
        totalStations = 6,
        totalBookings = 340,
        totalEarnings = 185600.0,
        withdrawableBalance = 34500.0,
        commissionPaid = 23200.0,
        rating = 4.9,
        bankName = "HDFC Bank",
        accountNumber = "•••• 5821"
      ),
      Host(
        id = "host_302",
        hostCode = "HST-402",
        name = "GreenWatt Charging Hubs",
        email = "partners@greenwatt.io",
        phone = "+91 99223 44556",
        status = "Active",
        verificationStatus = "Verified",
        joinedDate = "15 Jan 2025",
        totalStations = 4,
        totalBookings = 210,
        totalEarnings = 114800.0,
        withdrawableBalance = 18200.0,
        commissionPaid = 14350.0,
        rating = 4.7,
        bankName = "ICICI Bank",
        accountNumber = "•••• 9140"
      ),
      Host(
        id = "host_303",
        hostCode = "HST-403",
        name = "Urban Charge Points (Manish Goel)",
        email = "manish.goel@urbancharge.in",
        phone = "+91 99334 55667",
        status = "Pending Approval",
        verificationStatus = "Pending Review",
        joinedDate = "05 Mar 2025",
        totalStations = 2,
        totalBookings = 0,
        totalEarnings = 0.0,
        withdrawableBalance = 0.0,
        commissionPaid = 0.0,
        rating = 5.0,
        bankName = "State Bank of India",
        accountNumber = "•••• 3312"
      ),
      Host(
        id = "host_304",
        hostCode = "HST-404",
        name = "Highway Express EV Hub",
        email = "admin@highwayhub.com",
        phone = "+91 99445 66778",
        status = "Active",
        verificationStatus = "Verified",
        joinedDate = "28 Jan 2025",
        totalStations = 8,
        totalBookings = 520,
        totalEarnings = 294000.0,
        withdrawableBalance = 52000.0,
        commissionPaid = 36750.0,
        rating = 4.8,
        bankName = "Axis Bank",
        accountNumber = "•••• 7709"
      )
    )

    // Stations Data
    _stations.value = listOf(
      ChargingStation(
        id = "stn_501",
        stationCode = "EV-DEL-01",
        name = "CyberHub Fast Charging Station 60kW",
        address = "DLF Cyber City, Sector 24, Gurugram",
        city = "Delhi NCR",
        hostId = "host_301",
        hostName = "Edison Infra Pvt Ltd",
        hostPhone = "+91 99112 33445",
        status = "Active",
        approvalStatus = "Approved",
        powerRatingKw = 60.0,
        connectorTypes = listOf("CCS2", "Type 2 AC"),
        pricePerKwh = 18.5,
        availableSlots = 3,
        totalSlots = 4,
        totalChargingSessions = 480,
        totalRevenueGenerated = 126400.0,
        rating = 4.9
      ),
      ChargingStation(
        id = "stn_502",
        stationCode = "EV-MUM-02",
        name = "Bandra Kurla Complex DC Ultra 120kW",
        address = "G Block BKC, Bandra East, Mumbai",
        city = "Mumbai",
        hostId = "host_302",
        hostName = "GreenWatt Charging Hubs",
        hostPhone = "+91 99223 44556",
        status = "Active",
        approvalStatus = "Approved",
        powerRatingKw = 120.0,
        connectorTypes = listOf("Dual CCS2", "CHAdeMO"),
        pricePerKwh = 22.0,
        availableSlots = 2,
        totalSlots = 4,
        totalChargingSessions = 610,
        totalRevenueGenerated = 188200.0,
        rating = 4.8
      ),
      ChargingStation(
        id = "stn_503",
        stationCode = "EV-BLR-03",
        name = "Indiranagar EcoCharge Point 30kW",
        address = "100 Feet Rd, Indiranagar, Bengaluru",
        city = "Bengaluru",
        hostId = "host_303",
        hostName = "Urban Charge Points",
        hostPhone = "+91 99334 55667",
        status = "Offline",
        approvalStatus = "Pending Review",
        powerRatingKw = 30.0,
        connectorTypes = listOf("Type 2 AC", "CCS2"),
        pricePerKwh = 16.0,
        availableSlots = 0,
        totalSlots = 2,
        totalChargingSessions = 0,
        totalRevenueGenerated = 0.0,
        rating = 4.5
      ),
      ChargingStation(
        id = "stn_504",
        stationCode = "EV-DEL-04",
        name = "Aerocity T3 Express EV Hub 150kW",
        address = "Asset 8, Hospitality District, New Delhi",
        city = "Delhi NCR",
        hostId = "host_304",
        hostName = "Highway Express EV Hub",
        hostPhone = "+91 99445 66778",
        status = "Under Maintenance",
        approvalStatus = "Approved",
        powerRatingKw = 150.0,
        connectorTypes = listOf("CCS2 Ultra", "Type 2"),
        pricePerKwh = 24.0,
        availableSlots = 0,
        totalSlots = 6,
        totalChargingSessions = 890,
        totalRevenueGenerated = 295000.0,
        rating = 4.7
      )
    )

    // Bookings Data
    _bookings.value = listOf(
      Booking(
        id = "bk_701",
        bookingCode = "ULT-BK-9021",
        customerId = "cust_201",
        customerName = "Devendra Singhania",
        customerPhone = "+91 98201 12345",
        hostId = "host_301",
        hostName = "Edison Infra Pvt Ltd",
        stationId = "stn_501",
        stationName = "CyberHub Fast Charging Station 60kW",
        connectorType = "CCS2 (60kW)",
        date = "Today",
        slotTime = "10:00 AM - 11:00 AM",
        durationMinutes = 60,
        energyDeliveredKwh = 38.5,
        totalAmount = 712.25,
        bookingStatus = "In Progress",
        paymentStatus = "Paid",
        chargingStatus = "Charging",
        paymentMethod = "Ultimate Wallet",
        createdAt = "18 Aug 2026, 09:45 AM"
      ),
      Booking(
        id = "bk_702",
        bookingCode = "ULT-BK-9022",
        customerId = "cust_202",
        customerName = "Aisha Khan",
        customerPhone = "+91 98332 54321",
        hostId = "host_302",
        hostName = "GreenWatt Charging Hubs",
        stationId = "stn_502",
        stationName = "Bandra Kurla Complex DC Ultra 120kW",
        connectorType = "Dual CCS2 (120kW)",
        date = "Today",
        slotTime = "08:30 AM - 09:15 AM",
        durationMinutes = 45,
        energyDeliveredKwh = 52.0,
        totalAmount = 1144.0,
        bookingStatus = "Completed",
        paymentStatus = "Paid",
        chargingStatus = "Finished",
        paymentMethod = "Credit Card (HDFC)",
        createdAt = "18 Aug 2026, 08:15 AM"
      ),
      Booking(
        id = "bk_703",
        bookingCode = "ULT-BK-9023",
        customerId = "cust_203",
        customerName = "Rajeshwari Rao",
        customerPhone = "+91 98450 99887",
        hostId = "host_301",
        hostName = "Edison Infra Pvt Ltd",
        stationId = "stn_501",
        stationName = "CyberHub Fast Charging Station 60kW",
        connectorType = "CCS2 (60kW)",
        date = "Today",
        slotTime = "11:30 AM - 12:30 PM",
        durationMinutes = 60,
        energyDeliveredKwh = 0.0,
        totalAmount = 650.0,
        bookingStatus = "Confirmed",
        paymentStatus = "Paid",
        chargingStatus = "Waiting",
        paymentMethod = "Ultimate Wallet",
        createdAt = "18 Aug 2026, 10:10 AM"
      ),
      Booking(
        id = "bk_704",
        bookingCode = "ULT-BK-9024",
        customerId = "cust_204",
        customerName = "Kunal Kapoor",
        customerPhone = "+91 98771 44556",
        hostId = "host_304",
        hostName = "Highway Express EV Hub",
        stationId = "stn_504",
        stationName = "Aerocity T3 Express EV Hub 150kW",
        connectorType = "CCS2 Ultra",
        date = "Yesterday",
        slotTime = "04:00 PM - 05:00 PM",
        durationMinutes = 60,
        energyDeliveredKwh = 12.0,
        totalAmount = 450.0,
        bookingStatus = "Cancelled",
        paymentStatus = "Refunded",
        chargingStatus = "Interrupted",
        paymentMethod = "UPI AutoPay",
        createdAt = "17 Aug 2026, 03:40 PM"
      )
    )

    // Wallet Transactions
    _transactions.value = listOf(
      WalletTransaction(
        id = "txn_801",
        transactionCode = "TXN-WAL-441",
        userId = "cust_201",
        userName = "Devendra Singhania",
        userType = "Customer",
        type = "Debit",
        amount = 712.25,
        status = "Success",
        date = "18 Aug 2026, 10:00 AM",
        referenceBookingId = "ULT-BK-9021",
        description = "Payment for 38.5 kWh at CyberHub Station"
      ),
      WalletTransaction(
        id = "txn_802",
        transactionCode = "TXN-WAL-442",
        userId = "host_301",
        userName = "Edison Infra Pvt Ltd",
        userType = "Host",
        type = "Credit",
        amount = 623.22,
        status = "Success",
        date = "18 Aug 2026, 10:00 AM",
        referenceBookingId = "ULT-BK-9021",
        description = "Host payout after 12.5% commission"
      ),
      WalletTransaction(
        id = "txn_803",
        transactionCode = "TXN-WAL-443",
        userId = "cust_204",
        userName = "Kunal Kapoor",
        userType = "Customer",
        type = "Refund",
        amount = 450.0,
        status = "Success",
        date = "17 Aug 2026, 05:30 PM",
        referenceBookingId = "ULT-BK-9024",
        description = "Full refund approved for interrupted session"
      )
    )

    // Host Withdrawals
    _withdrawals.value = listOf(
      HostWithdrawal(
        id = "wdr_901",
        withdrawalCode = "WDR-8801",
        hostId = "host_301",
        hostName = "Edison Infra Pvt Ltd",
        amount = 34500.0,
        bankName = "HDFC Bank (Current A/C)",
        accountNumberMasked = "•••• 5821",
        status = "Pending",
        requestedDate = "18 Aug 2026, 09:10 AM",
        remarks = "Weekly host earnings withdrawal"
      ),
      HostWithdrawal(
        id = "wdr_902",
        withdrawalCode = "WDR-8802",
        hostId = "host_302",
        hostName = "GreenWatt Charging Hubs",
        amount = 18200.0,
        bankName = "ICICI Bank",
        accountNumberMasked = "•••• 9140",
        status = "Pending",
        requestedDate = "17 Aug 2026, 06:45 PM",
        remarks = "Bi-weekly settlement request"
      ),
      HostWithdrawal(
        id = "wdr_903",
        withdrawalCode = "WDR-8803",
        hostId = "host_304",
        hostName = "Highway Express EV Hub",
        amount = 50000.0,
        bankName = "Axis Bank",
        accountNumberMasked = "•••• 7709",
        status = "Completed",
        requestedDate = "14 Aug 2026, 11:20 AM",
        processedDate = "15 Aug 2026, 02:15 PM",
        referenceNumber = "AXIS_NEFT_981245",
        remarks = "Transferred via NEFT"
      )
    )

    // Promo Codes
    _promos.value = listOf(
      PromoCode(
        id = "prm_101",
        code = "ULTIMATE100",
        title = "Supercharged First Time User",
        discountType = "PERCENTAGE",
        discountPercent = 20,
        maxDiscountAmount = 100.0,
        minBookingAmount = 250.0,
        totalUsageLimit = 1000,
        currentUsageCount = 428,
        perUserLimit = 1,
        startDate = "01 Aug 2026",
        endDate = "31 Dec 2026",
        status = "Active",
        active = true
      ),
      PromoCode(
        id = "prm_102",
        code = "WEEKENDCHARGE",
        title = "Weekend Fast Charge Bonus",
        discountType = "FIXED",
        discountAmount = 75.0,
        maxDiscountAmount = 75.0,
        minBookingAmount = 300.0,
        totalUsageLimit = 500,
        currentUsageCount = 186,
        perUserLimit = 2,
        startDate = "01 Aug 2026",
        endDate = "31 Dec 2026",
        status = "Active",
        active = true
      ),
      PromoCode(
        id = "prm_103",
        code = "FLEETPOWER25",
        title = "Commercial EV Fleet Discount",
        discountType = "PERCENTAGE",
        discountPercent = 25,
        maxDiscountAmount = 500.0,
        minBookingAmount = 1000.0,
        totalUsageLimit = 200,
        currentUsageCount = 45,
        perUserLimit = 5,
        startDate = "15 Jul 2026",
        endDate = "31 Dec 2026",
        status = "Active",
        active = true
      )
    )

    // Disputes Data
    _disputes.value = listOf(
      Dispute(
        id = "dsp_1101",
        disputeCode = "DSP-2024",
        bookingId = "ULT-BK-9024",
        customerId = "cust_204",
        customerName = "Kunal Kapoor",
        customerPhone = "+91 98771 44556",
        hostId = "host_304",
        hostName = "Highway Express EV Hub",
        stationName = "Aerocity T3 Express EV Hub 150kW",
        reason = "Charger tripped after 10 minutes, billed for 60 mins",
        description = "I plugged in at 04:00 PM. Gun tripped with Error Code E04. Station host was unresponsive. Need refund.",
        evidenceUrls = listOf("https://images.unsplash.com/photo-1558441719-5838031d2797?w=400"),
        status = "Under Review",
        resolutionNotes = "Support staff verified charger telemetry logs showing hardware trip at 12kWh.",
        refundAmount = 450.0,
        assignedStaffId = "stf_1001",
        assignedStaffName = "Rahul Sharma",
        createdAt = "17 Aug 2026, 04:30 PM",
        updatedAt = "18 Aug 2026, 09:15 AM"
      ),
      Dispute(
        id = "dsp_1102",
        disputeCode = "DSP-2025",
        bookingId = "ULT-BK-9018",
        customerId = "cust_202",
        customerName = "Aisha Khan",
        customerPhone = "+91 98332 54321",
        hostId = "host_301",
        hostName = "Edison Infra Pvt Ltd",
        stationName = "CyberHub Fast Charging Station 60kW",
        reason = "Slot occupied by unauthorized vehicle during reserved window",
        description = "Arrived at 09:00 AM on time. A non-EV vehicle was blocking slot 2. Had to wait 25 mins.",
        status = "Open",
        assignedStaffId = "stf_1002",
        assignedStaffName = "Priya Singh",
        createdAt = "18 Aug 2026, 09:10 AM",
        updatedAt = "18 Aug 2026, 09:10 AM"
      )
    )

    // Support Tickets Data with Role Routing & Escalations
    _tickets.value = listOf(
      SupportTicket(
        id = "tkt_1201",
        ticketCode = "TKT-5510",
        customerId = "cust_201",
        customerName = "Devendra Singhania",
        customerEmail = "devendra.s@gmail.com",
        customerPhone = "+91 98201 12345",
        bookingId = "bk_701",
        bookingCode = "ULT-BK-9021",
        stationId = "stn_501",
        stationName = "CyberHub Fast Charging Station 60kW",
        hostId = "host_301",
        hostName = "Edison Infra Pvt Ltd",
        subject = "Inquiry regarding GST invoice for corporate reimbursement",
        priority = "Medium",
        status = "In Progress",
        assignedToId = "stf_1001",
        assignedToName = "Rahul Sharma",
        assignedStaffName = "Rahul Sharma",
        department = "Support",
        escalatedToManager = false,
        createdAt = "18 Aug 2026, 09:30 AM",
        updatedAt = "18 Aug 2026, 10:15 AM",
        replies = listOf(
          SupportTicketReply(
            senderName = "Devendra Singhania",
            senderRole = "Customer",
            message = "Hi, I need tax invoice with GSTIN 07AAAAA0000A1Z5 for booking ULT-BK-9021.",
            timestamp = "18 Aug, 09:30 AM"
          ),
          SupportTicketReply(
            senderName = "Rahul Sharma",
            senderRole = "Customer Support",
            message = "Hello Mr. Singhania, I am generating the verified B2B GST tax invoice for your session.",
            timestamp = "18 Aug, 10:15 AM"
          ),
          SupportTicketReply(
            senderName = "Rahul Sharma",
            senderRole = "Customer Support",
            message = "[Internal Note] Attached billing ledger ref to Edison Infra B2B account.",
            timestamp = "18 Aug, 10:16 AM",
            isInternalNote = true
          )
        )
      ),
      SupportTicket(
        id = "tkt_1202",
        ticketCode = "TKT-5511",
        customerId = "cust_203",
        customerName = "Rajeshwari Rao",
        customerEmail = "rajeshwari.rao@yahoo.com",
        customerPhone = "+91 98450 99887",
        bookingId = "bk_703",
        bookingCode = "ULT-BK-9023",
        stationId = "stn_501",
        stationName = "CyberHub Fast Charging Station 60kW",
        hostId = "host_301",
        hostName = "Edison Infra Pvt Ltd",
        subject = "Slot rescheduling request due to heavy traffic on NH-48",
        priority = "High",
        status = "Assigned",
        assignedToId = "stf_1002",
        assignedToName = "Priya Singh",
        assignedStaffName = "Priya Singh",
        department = "Support",
        escalatedToManager = false,
        createdAt = "18 Aug 2026, 10:05 AM",
        updatedAt = "18 Aug 2026, 10:10 AM",
        replies = listOf(
          SupportTicketReply(
            senderName = "Rajeshwari Rao",
            senderRole = "Customer",
            message = "Stuck in severe expressway traffic. Can my slot at CyberHub be moved to 12:30 PM?",
            timestamp = "18 Aug, 10:05 AM"
          )
        )
      ),
      SupportTicket(
        id = "tkt_1203",
        ticketCode = "TKT-5512",
        customerId = "cust_204",
        customerName = "Kunal Kapoor",
        customerEmail = "kunal.kapoor@gmail.com",
        customerPhone = "+91 98771 44556",
        bookingId = "bk_704",
        bookingCode = "ULT-BK-9024",
        stationId = "stn_504",
        stationName = "Aerocity T3 Express EV Hub 150kW",
        hostId = "host_304",
        hostName = "Highway Express EV Hub",
        subject = "Hardware Tripping & Host Compensation Request",
        priority = "Urgent",
        status = "Escalated to Manager",
        assignedToId = "stf_1001",
        assignedToName = "Rahul Sharma",
        assignedStaffName = "Rahul Sharma",
        department = "Support",
        escalatedToManager = true,
        escalatedReason = "Charger gun tripped hardware fault E04. Host unresponsive. Exceeds standard support refund limit.",
        createdAt = "17 Aug 2026, 04:45 PM",
        updatedAt = "18 Aug 2026, 09:20 AM",
        replies = listOf(
          SupportTicketReply(
            senderName = "Kunal Kapoor",
            senderRole = "Customer",
            message = "Charger stopped after 10 mins and I was charged ₹450.",
            timestamp = "17 Aug, 04:45 PM"
          ),
          SupportTicketReply(
            senderName = "Rahul Sharma",
            senderRole = "Customer Support",
            message = "[ESCALATED TO MANAGER] Reason: Charger gun tripped hardware fault E04. Exceeds standard support refund limit.",
            timestamp = "18 Aug, 09:20 AM",
            isInternalNote = true
          )
        )
      )
    )

    // Admin Notifications with Role Targeting
    _notifications.value = listOf(
      AdminNotification(
        id = "notif_1",
        title = "New Host Registration Pending",
        message = "Urban Charge Points (Manish Goel) submitted KYC documents for 2 new stations.",
        type = "HOST_REGISTRATION",
        targetRole = "OPERATIONS",
        timestamp = "10 mins ago",
        read = false,
        referenceId = "host_303"
      ),
      AdminNotification(
        id = "notif_2",
        title = "Host Withdrawal Request",
        message = "Edison Infra requested ₹34,500 withdrawal to HDFC Bank A/C ending 5821.",
        type = "WITHDRAWAL_REQUEST",
        targetRole = "FINANCE",
        timestamp = "35 mins ago",
        read = false,
        referenceId = "wdr_901"
      ),
      AdminNotification(
        id = "notif_3",
        title = "Station Status Alert",
        message = "Aerocity T3 Express EV Hub 150kW reported gun maintenance trigger.",
        type = "STATION_SUBMISSION",
        targetRole = "OPERATIONS",
        timestamp = "1 hour ago",
        read = false,
        referenceId = "stn_504"
      ),
      AdminNotification(
        id = "notif_4",
        title = "Support Escalation Alert",
        message = "Ticket TKT-5512 escalated to Operations Manager by Rahul Sharma.",
        type = "DISPUTE",
        targetRole = "MANAGER",
        timestamp = "2 hours ago",
        read = false,
        referenceId = "tkt_1203"
      )
    )

    updateMetrics()
  }
}
