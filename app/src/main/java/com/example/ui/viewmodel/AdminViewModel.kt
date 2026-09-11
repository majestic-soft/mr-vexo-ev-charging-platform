package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.FirebaseAdminRepository
import com.example.ui.util.SoundManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private data class StaffFilters(
  val query: String,
  val dept: String,
  val status: String,
  val role: String
)

class AdminViewModel(
  private val repository: FirebaseAdminRepository = FirebaseAdminRepository()
) : ViewModel() {

  // Current Admin & Permissions
  val currentAdmin: StateFlow<AdminStaffUser?> = repository.currentAdmin

  val isSuperAdmin: StateFlow<Boolean> = repository.currentAdmin.map { it?.role == AdminRole.SUPER_ADMIN }.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), false
  )
  val isAdmin: StateFlow<Boolean> = repository.currentAdmin.map { it?.role == AdminRole.ADMIN }.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), false
  )
  val isManager: StateFlow<Boolean> = repository.currentAdmin.map { it?.role == AdminRole.MANAGER }.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), false
  )
  val isSupport: StateFlow<Boolean> = repository.currentAdmin.map { it?.role == AdminRole.SUPPORT }.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), false
  )
  val isFinance: StateFlow<Boolean> = repository.currentAdmin.map { it?.role == AdminRole.FINANCE }.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), false
  )
  val isOperations: StateFlow<Boolean> = repository.currentAdmin.map { it?.role == AdminRole.OPERATIONS }.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), false
  )
  val canViewRevenue: StateFlow<Boolean> = repository.currentAdmin.map { 
    it?.role == AdminRole.SUPER_ADMIN || it?.role == AdminRole.ADMIN 
  }.stateIn(
    viewModelScope, SharingStarted.WhileSubscribed(5000), false
  )

  // Master Data Collections
  val staffList: StateFlow<List<AdminStaffUser>> = repository.staffList
  val customers: StateFlow<List<Customer>> = repository.customers
  val hosts: StateFlow<List<Host>> = repository.hosts
  val stations: StateFlow<List<ChargingStation>> = repository.stations
  val bookings: StateFlow<List<Booking>> = repository.bookings
  val transactions: StateFlow<List<WalletTransaction>> = repository.transactions
  val withdrawals: StateFlow<List<HostWithdrawal>> = repository.withdrawals
  val promos: StateFlow<List<PromoCode>> = repository.promos
  val couponUsageHistory: StateFlow<List<CouponUsageRecord>> = repository.couponUsageHistory
  val disputes: StateFlow<List<Dispute>> = repository.disputes
  val tickets: StateFlow<List<SupportTicket>> = repository.tickets
  val notifications: StateFlow<List<AdminNotification>> = repository.notifications
  val metrics: StateFlow<DashboardMetrics> = repository.metrics
  val systemSettings: StateFlow<SystemSettings> = repository.systemSettings
  val selectedTimeFilter: StateFlow<String> = repository.selectedTimeFilter
  val statusMessage: StateFlow<String?> = repository.statusMessage
  val isLiveSimulationActive: StateFlow<Boolean> = repository.isLiveSimulationActive

  // Enterprise Cyber Defense Shield State
  val securityStatus: StateFlow<SecurityShieldStatus> = repository.securityStatus
  val securityThreatEvents: StateFlow<List<SecurityThreatEvent>> = repository.securityThreatEvents
  val isPlatformLockedDown: StateFlow<Boolean> = repository.isPlatformLockedDown
  val failedLoginAttempts: StateFlow<Int> = repository.failedLoginAttempts

  // App Lock & Security State
  private val _isAppLocked = MutableStateFlow(false)
  val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

  private val _pinAttempt = MutableStateFlow("")
  val pinAttempt: StateFlow<String> = _pinAttempt.asStateFlow()

  private val _pinError = MutableStateFlow<String?>(null)
  val pinError: StateFlow<String?> = _pinError.asStateFlow()

  private var previousNotificationCount = 0

  init {
    // Reactive notification sound trigger
    viewModelScope.launch {
      notifications.collect { list ->
        if (previousNotificationCount > 0 && list.size > previousNotificationCount) {
          if (systemSettings.value.notificationSoundEnabled) {
            SoundManager.playNotificationSound(null, systemSettings.value.notificationSoundStyle)
          }
        }
        previousNotificationCount = list.size
      }
    }
  }

  fun toggleLiveSimulation() { repository.toggleLiveSimulation() }
  fun setProductionMode(isLive: Boolean) { 
    repository.setProductionMode(isLive) 
    if (systemSettings.value.notificationSoundEnabled) {
      if (isLive) SoundManager.playSuccessSound() else SoundManager.playWarningAlert()
    }
  }
  fun refreshLiveFleetData() {
    repository.refreshLiveFleetData()
    if (systemSettings.value.notificationSoundEnabled) {
      SoundManager.playSuccessSound()
    }
  }
  fun broadcastSystemAlert(title: String, message: String, targetRole: String = "ALL") {
    repository.broadcastSystemAlert(title, message, targetRole)
    if (systemSettings.value.notificationSoundEnabled) {
      SoundManager.playNotificationSound(null, systemSettings.value.notificationSoundStyle)
    }
  }
  fun simulateLiveEvent(type: String) { 
    repository.simulateLiveEvent(type)
    if (systemSettings.value.notificationSoundEnabled) {
      SoundManager.playNotificationSound(null, systemSettings.value.notificationSoundStyle)
    }
  }

  // Security Operations Center (SOC) controls
  fun runDeepSecurityScan() {
    repository.runSecurityScan()
    if (systemSettings.value.notificationSoundEnabled) {
      SoundManager.playSuccessSound()
    }
  }

  fun triggerEmergencyLockdown(reason: String) {
    repository.triggerEmergencyLockdown(reason)
    SoundManager.playSecurityAlarmSound()
  }

  fun liftEmergencyLockdown() {
    repository.liftEmergencyLockdown()
    SoundManager.playSuccessSound()
  }

  fun testNotificationSound(style: String = systemSettings.value.notificationSoundStyle) {
    SoundManager.playNotificationSound(null, style)
  }

  // Search & Filter State for Screens
  private val _staffSearchQuery = MutableStateFlow("")
  val staffSearchQuery: StateFlow<String> = _staffSearchQuery.asStateFlow()

  private val _staffFilterDepartment = MutableStateFlow("All")
  val staffFilterDepartment: StateFlow<String> = _staffFilterDepartment.asStateFlow()

  private val _staffFilterStatus = MutableStateFlow("All")
  val staffFilterStatus: StateFlow<String> = _staffFilterStatus.asStateFlow()

  private val _staffFilterRole = MutableStateFlow("All")
  val staffFilterRole: StateFlow<String> = _staffFilterRole.asStateFlow()

  private val _customerSearchQuery = MutableStateFlow("")
  val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

  private val _hostSearchQuery = MutableStateFlow("")
  val hostSearchQuery: StateFlow<String> = _hostSearchQuery.asStateFlow()

  private val _stationSearchQuery = MutableStateFlow("")
  val stationSearchQuery: StateFlow<String> = _stationSearchQuery.asStateFlow()

  private val _bookingSearchQuery = MutableStateFlow("")
  val bookingSearchQuery: StateFlow<String> = _bookingSearchQuery.asStateFlow()

  private val _ticketSearchQuery = MutableStateFlow("")
  val ticketSearchQuery: StateFlow<String> = _ticketSearchQuery.asStateFlow()

  private val _disputeSearchQuery = MutableStateFlow("")
  val disputeSearchQuery: StateFlow<String> = _disputeSearchQuery.asStateFlow()

  private val _globalSearchQuery = MutableStateFlow("")
  val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

  // Selected Detail IDs for Master-Detail views
  private val _selectedStaffId = MutableStateFlow<String?>("stf_1001")
  val selectedStaffId: StateFlow<String?> = _selectedStaffId.asStateFlow()

  private val _selectedCustomerId = MutableStateFlow<String?>(null)
  val selectedCustomerId: StateFlow<String?> = _selectedCustomerId.asStateFlow()

  private val _selectedHostId = MutableStateFlow<String?>(null)
  val selectedHostId: StateFlow<String?> = _selectedHostId.asStateFlow()

  private val _selectedStationId = MutableStateFlow<String?>(null)
  val selectedStationId: StateFlow<String?> = _selectedStationId.asStateFlow()

  private val _selectedBookingId = MutableStateFlow<String?>(null)
  val selectedBookingId: StateFlow<String?> = _selectedBookingId.asStateFlow()

  private val _selectedDisputeId = MutableStateFlow<String?>(null)
  val selectedDisputeId: StateFlow<String?> = _selectedDisputeId.asStateFlow()

  private val _selectedTicketId = MutableStateFlow<String?>(null)
  val selectedTicketId: StateFlow<String?> = _selectedTicketId.asStateFlow()

  // Coupon Simulator State for Admin UI
  private val _couponTestCode = MutableStateFlow("ULTIMATE100")
  val couponTestCode: StateFlow<String> = _couponTestCode.asStateFlow()

  private val _couponTestAmount = MutableStateFlow("500")
  val couponTestAmount: StateFlow<String> = _couponTestAmount.asStateFlow()

  private val _couponTestResult = MutableStateFlow<CouponValidationResult?>(null)
  val couponTestResult: StateFlow<CouponValidationResult?> = _couponTestResult.asStateFlow()

  // Unread Notifications Count (Filtered by Role)
  val unreadNotificationsCount: StateFlow<Int> = combine(notifications, currentAdmin) { list, admin ->
    val accessible = repository.getAccessibleNotifications()
    accessible.count { !it.read }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4)

  // Filtered Staff Members matching RBAC permissions
  val filteredStaffList: StateFlow<List<AdminStaffUser>> = combine(
    staffList,
    currentAdmin,
    combine(staffSearchQuery, staffFilterDepartment, staffFilterStatus, staffFilterRole) { q, d, s, r ->
      StaffFilters(q, d, s, r)
    }
  ) { _, _, filters ->
    val accessible = repository.getAccessibleStaff()
    accessible.filter { staff ->
      val matchesQuery = filters.query.isBlank() ||
        staff.name.contains(filters.query, ignoreCase = true) ||
        staff.email.contains(filters.query, ignoreCase = true) ||
        staff.staffCode.contains(filters.query, ignoreCase = true) ||
        staff.department.contains(filters.query, ignoreCase = true) ||
        staff.role.displayName.contains(filters.query, ignoreCase = true)

      val matchesDept = filters.dept == "All" || staff.department.equals(filters.dept, ignoreCase = true)
      val matchesStatus = filters.status == "All" || staff.status.equals(filters.status, ignoreCase = true)
      val matchesRole = filters.role == "All" || staff.role.displayName.equals(filters.role, ignoreCase = true) || staff.role.name.equals(filters.role, ignoreCase = true)

      matchesQuery && matchesDept && matchesStatus && matchesRole
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Accessible Data Streams based on active admin role
  val accessibleTickets: StateFlow<List<SupportTicket>> = combine(tickets, currentAdmin) { _, _ ->
    repository.getAccessibleTickets()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val accessibleCustomers: StateFlow<List<Customer>> = combine(customers, currentAdmin) { _, _ ->
    repository.getAccessibleCustomers()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val accessibleBookings: StateFlow<List<Booking>> = combine(bookings, currentAdmin) { _, _ ->
    repository.getAccessibleBookings()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val accessibleStations: StateFlow<List<ChargingStation>> = combine(stations, currentAdmin) { _, _ ->
    repository.getAccessibleStations()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val accessibleDisputes: StateFlow<List<Dispute>> = combine(disputes, currentAdmin) { _, _ ->
    repository.getAccessibleDisputes()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val accessibleNotifications: StateFlow<List<AdminNotification>> = combine(notifications, currentAdmin) { _, _ ->
    repository.getAccessibleNotifications()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun setStaffSearchQuery(query: String) { _staffSearchQuery.value = query }
  fun setStaffFilterDepartment(dept: String) { _staffFilterDepartment.value = dept }
  fun setStaffFilterStatus(status: String) { _staffFilterStatus.value = status }
  fun setStaffFilterRole(role: String) { _staffFilterRole.value = role }

  fun setCustomerSearchQuery(query: String) { _customerSearchQuery.value = query }
  fun setHostSearchQuery(query: String) { _hostSearchQuery.value = query }
  fun setStationSearchQuery(query: String) { _stationSearchQuery.value = query }
  fun setBookingSearchQuery(query: String) { _bookingSearchQuery.value = query }
  fun setTicketSearchQuery(query: String) { _ticketSearchQuery.value = query }
  fun setDisputeSearchQuery(query: String) { _disputeSearchQuery.value = query }
  fun setGlobalSearchQuery(query: String) { _globalSearchQuery.value = query }

  fun selectStaff(staffId: String?) { _selectedStaffId.value = staffId }
  fun selectCustomer(customerId: String?) { _selectedCustomerId.value = customerId }
  fun selectHost(hostId: String?) { _selectedHostId.value = hostId }
  fun selectStation(stationId: String?) { _selectedStationId.value = stationId }
  fun selectBooking(bookingId: String?) { _selectedBookingId.value = bookingId }
  fun selectDispute(disputeId: String?) { _selectedDisputeId.value = disputeId }
  fun selectTicket(ticketId: String?) { _selectedTicketId.value = ticketId }

  fun setCouponTestCode(code: String) { _couponTestCode.value = code }
  fun setCouponTestAmount(amount: String) { _couponTestAmount.value = amount }

  fun testApplyCoupon() {
    val amt = _couponTestAmount.value.toDoubleOrNull() ?: 0.0
    val result = repository.validateAndApplyCoupon(
      code = _couponTestCode.value,
      userId = "cust_201",
      bookingAmount = amt
    )
    _couponTestResult.value = result
  }

  fun setTimeFilter(filter: String) { repository.setTimeFilter(filter) }
  fun clearStatusMessage() { repository.clearStatusMessage() }

  // RBAC Permission Check
  fun hasPermission(permission: String): Boolean = repository.checkPermission(permission)
  fun canViewRevenue(): Boolean {
    val role = currentAdmin.value?.role ?: return false
    return role == AdminRole.SUPER_ADMIN || role == AdminRole.ADMIN
  }

  // Authentication operations
  fun login(email: String, pass: String): Result<AdminStaffUser> = repository.login(email, pass)
  fun loginAsRole(role: AdminRole) { repository.loginAsRole(role) }
  fun switchRoleForTesting(role: AdminRole) { repository.loginAsRole(role) }
  fun selectAdminUser(user: AdminStaffUser) { repository.selectAdminUser(user) }
  fun logout() { repository.logout() }

  // Staff / Admin Management operations
  fun addStaff(name: String, email: String, phone: String, role: AdminRole, department: String, permissions: List<String>, managerName: String) {
    repository.addStaffMember(name, email, phone, role, department, permissions, managerName)
  }

  fun updateStaffPermissions(staffId: String, newRole: AdminRole, newPermissions: List<String>, newDepartment: String) {
    repository.updateStaffPermissions(staffId, newRole, newPermissions, newDepartment)
  }

  fun changeAdminRoleAndPermissions(staffId: String, newRole: AdminRole, newPermissions: List<String>, newDepartment: String): Boolean {
    return repository.changeAdminRoleAndPermissions(staffId, newRole, newPermissions, newDepartment)
  }

  fun setAdminAccountStatus(staffId: String, status: String): Boolean {
    return repository.setAdminAccountStatus(staffId, status)
  }

  fun suspendAdminAccount(staffId: String): Boolean {
    return repository.suspendAdminAccount(staffId)
  }

  fun activateAdminAccount(staffId: String): Boolean {
    return repository.activateAdminAccount(staffId)
  }

  fun toggleStaffStatus(staffId: String) { repository.toggleStaffStatus(staffId) }
  fun resetStaffPassword(staffId: String) { repository.resetStaffPassword(staffId) }

  // Customer operations
  fun toggleCustomerStatus(customerId: String) { 
    repository.toggleCustomerStatus(customerId) 
    SoundManager.playWarningAlert()
  }

  // Host operations
  fun approveHost(hostId: String) { 
    repository.approveOrRejectHost(hostId, true) 
    SoundManager.playSuccessSound()
  }
  fun rejectHost(hostId: String) { 
    repository.approveOrRejectHost(hostId, false) 
    SoundManager.playWarningAlert()
  }
  fun toggleHostStatus(hostId: String) { 
    repository.toggleHostStatus(hostId) 
    SoundManager.playWarningAlert()
  }

  // Station operations
  fun approveStation(stationId: String) { 
    repository.approveOrRejectStation(stationId, true) 
    SoundManager.playSuccessSound()
  }
  fun rejectStation(stationId: String) { 
    repository.approveOrRejectStation(stationId, false) 
    SoundManager.playWarningAlert()
  }
  fun toggleStationStatus(stationId: String, newStatus: String) { 
    repository.toggleStationStatus(stationId, newStatus) 
    SoundManager.playSuccessSound()
  }

  // Booking operations
  fun updateBookingStatus(bookingId: String, bookingStatus: String, chargingStatus: String) {
    repository.updateBookingStatus(bookingId, bookingStatus, chargingStatus)
    SoundManager.playSuccessSound()
  }

  // Withdrawal operations
  fun approveWithdrawal(withdrawalId: String, remarks: String) { 
    repository.processWithdrawal(withdrawalId, true, remarks) 
    SoundManager.playSuccessSound()
  }
  fun rejectWithdrawal(withdrawalId: String, remarks: String) { 
    repository.processWithdrawal(withdrawalId, false, remarks) 
    SoundManager.playWarningAlert()
  }

  // Dispute operations
  fun updateDisputeStatus(disputeId: String, newStatus: String, notes: String, refundAmount: Double = 0.0) {
    repository.updateDisputeStatus(disputeId, newStatus, notes, refundAmount)
    SoundManager.playSuccessSound()
  }
  fun assignDisputeStaff(disputeId: String, staffId: String, staffName: String) {
    repository.assignDisputeStaff(disputeId, staffId, staffName)
    SoundManager.playSuccessSound()
  }

  // Support ticket operations
  fun addTicketReply(ticketId: String, message: String, isInternalNote: Boolean) {
    repository.addTicketReply(ticketId, message, isInternalNote)
    SoundManager.playSuccessSound()
  }
  fun updateTicketStatus(ticketId: String, newStatus: String) {
    repository.updateTicketStatus(ticketId, newStatus)
    SoundManager.playSuccessSound()
  }
  fun assignTicketStaff(ticketId: String, staffId: String, staffName: String) {
    repository.assignTicketStaff(ticketId, staffId, staffName)
    SoundManager.playSuccessSound()
  }
  fun escalateTicketToManager(ticketId: String, reason: String): Boolean {
    val res = repository.escalateTicketToManager(ticketId, reason)
    if (res) SoundManager.playWarningAlert()
    return res
  }
  fun resolveDispute(disputeId: String, refundApproved: Boolean, refundAmount: Double, resolutionNotes: String): Boolean {
    val res = repository.resolveDispute(disputeId, refundApproved, refundAmount, resolutionNotes)
    if (res) SoundManager.playSuccessSound()
    return res
  }

  // Promo operations
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
    repository.createPromoCode(code, title, discountType, discountPercent, discountAmount, maxDiscount, minBookingAmount, usageLimit, perUserLimit, startDate, endDate)
    SoundManager.playSuccessSound()
  }
  fun togglePromoStatus(promoId: String) { 
    repository.togglePromoStatus(promoId) 
    SoundManager.playSuccessSound()
  }

  // Settings
  fun updateSystemSettings(settings: SystemSettings) { 
    repository.updateSystemSettings(settings) 
    SoundManager.playSuccessSound()
  }

  // Notifications
  fun markNotificationAsRead(id: String) { repository.markNotificationAsRead(id) }
  fun markAllNotificationsAsRead() { 
    repository.markAllNotificationsAsRead() 
    SoundManager.playSuccessSound()
  }

  // App Lock Security
  fun lockApp() {
    _isAppLocked.value = true
    _pinAttempt.value = ""
    _pinError.value = null
    SoundManager.playSecurityAlarmSound()
  }

  fun appendPinDigit(digit: String) {
    if (_pinAttempt.value.length < 4) {
      val newPin = _pinAttempt.value + digit
      _pinAttempt.value = newPin
      SoundManager.playKeypadClick()
      if (newPin.length == 4) {
        verifyPin(newPin)
      }
    }
  }

  fun deletePinDigit() {
    if (_pinAttempt.value.isNotEmpty()) {
      _pinAttempt.value = _pinAttempt.value.dropLast(1)
      _pinError.value = null
      SoundManager.playKeypadClick()
    }
  }

  private fun verifyPin(pin: String) {
    val correctPin = systemSettings.value.pinCode
    if (pin == correctPin || pin == "1234") {
      _isAppLocked.value = false
      _pinAttempt.value = ""
      _pinError.value = null
      SoundManager.playUnlockChime()
    } else {
      _pinError.value = "Incorrect PIN. Try 1234."
      _pinAttempt.value = ""
      SoundManager.playWarningAlert()
    }
  }
}
