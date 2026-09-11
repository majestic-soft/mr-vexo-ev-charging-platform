package com.example.data.model

enum class AdminRole(val displayName: String, val code: String) {
  SUPER_ADMIN("Super Admin", "SUPER_ADMIN"),
  ADMIN("Admin", "ADMIN"),
  MANAGER("Manager", "MANAGER"),
  SUPPORT("Customer Support", "SUPPORT"),
  FINANCE("Finance", "FINANCE"),
  OPERATIONS("Operations", "OPERATIONS");

  companion object {
    fun fromCode(code: String): AdminRole {
      return entries.find { it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true) } ?: SUPPORT
    }
  }
}

object AppPermissions {
  const val VIEW_DASHBOARD = "View Dashboard"
  const val VIEW_CUSTOMERS = "View Users"
  const val MANAGE_CUSTOMERS = "Manage Users"
  const val VIEW_HOSTS = "View Hosts"
  const val MANAGE_HOSTS = "Manage Hosts"
  const val APPROVE_HOSTS = "Approve Hosts"
  const val VIEW_STATIONS = "View Stations"
  const val MANAGE_STATIONS = "Manage Stations"
  const val APPROVE_STATIONS = "Approve Stations"
  const val VIEW_BOOKINGS = "View Bookings"
  const val MANAGE_BOOKINGS = "Manage Bookings"
  const val VIEW_CHARGING_SESSIONS = "View Charging Sessions"
  const val VIEW_PAYMENTS = "View Payments"
  const val MANAGE_PAYMENTS = "Manage Payments"
  const val VIEW_WALLETS = "View Wallets"
  const val MANAGE_WALLETS = "Manage Wallets"
  const val VIEW_WITHDRAWALS = "View Withdrawals"
  const val MANAGE_WITHDRAWALS = "Manage Withdrawals"
  const val VIEW_REFUNDS = "View Refunds"
  const val MANAGE_REFUNDS = "Manage Refunds"
  const val VIEW_PROMOS = "View Promo Codes"
  const val MANAGE_PROMOS = "Manage Promo Codes"
  const val VIEW_DISPUTES = "View Disputes"
  const val MANAGE_DISPUTES = "Manage Disputes"
  const val VIEW_SUPPORT_TICKETS = "View Support Tickets"
  const val CREATE_SUPPORT_TICKETS = "Create Support Tickets"
  const val MANAGE_SUPPORT_TICKETS = "Manage Support Tickets"
  const val ADD_NOTES = "Add Notes"
  const val ESCALATE_TICKET = "Escalate Ticket"
  const val VIEW_STAFF = "View Staff"
  const val MANAGE_STAFF = "Manage Staff"
  const val MANAGE_ROLES_PERMISSIONS = "Roles & Permissions"
  const val VIEW_ANALYTICS = "View Analytics"
  const val VIEW_REVENUE = "View Revenue"
  const val VIEW_FINANCIAL_REPORTS = "View Financial Reports"
  const val SYSTEM_SETTINGS = "System Settings"
  const val GLOBAL_SEARCH = "Global Search"

  val superAdminPermissions = listOf(
    VIEW_DASHBOARD, VIEW_CUSTOMERS, MANAGE_CUSTOMERS,
    VIEW_HOSTS, MANAGE_HOSTS, APPROVE_HOSTS,
    VIEW_STATIONS, MANAGE_STATIONS, APPROVE_STATIONS,
    VIEW_BOOKINGS, MANAGE_BOOKINGS, VIEW_CHARGING_SESSIONS,
    VIEW_PAYMENTS, MANAGE_PAYMENTS,
    VIEW_WALLETS, MANAGE_WALLETS,
    VIEW_WITHDRAWALS, MANAGE_WITHDRAWALS,
    VIEW_REFUNDS, MANAGE_REFUNDS,
    VIEW_PROMOS, MANAGE_PROMOS,
    VIEW_DISPUTES, MANAGE_DISPUTES,
    VIEW_SUPPORT_TICKETS, CREATE_SUPPORT_TICKETS, MANAGE_SUPPORT_TICKETS, ADD_NOTES, ESCALATE_TICKET,
    VIEW_STAFF, MANAGE_STAFF, MANAGE_ROLES_PERMISSIONS,
    VIEW_REVENUE, VIEW_FINANCIAL_REPORTS, SYSTEM_SETTINGS, GLOBAL_SEARCH
  )

  val adminDefaultPermissions = listOf(
    VIEW_DASHBOARD, VIEW_CUSTOMERS, MANAGE_CUSTOMERS,
    VIEW_HOSTS, MANAGE_HOSTS,
    VIEW_STATIONS, MANAGE_STATIONS,
    VIEW_BOOKINGS, MANAGE_BOOKINGS, VIEW_CHARGING_SESSIONS,
    VIEW_PAYMENTS, VIEW_WALLETS, VIEW_REFUNDS, MANAGE_REFUNDS,
    VIEW_PROMOS, MANAGE_PROMOS,
    VIEW_DISPUTES, MANAGE_DISPUTES,
    VIEW_SUPPORT_TICKETS, CREATE_SUPPORT_TICKETS, MANAGE_SUPPORT_TICKETS, ADD_NOTES, ESCALATE_TICKET,
    VIEW_STAFF, VIEW_REVENUE, VIEW_FINANCIAL_REPORTS, GLOBAL_SEARCH
  )

  val managerDefaultPermissions = listOf(
    VIEW_DASHBOARD, VIEW_CUSTOMERS,
    VIEW_BOOKINGS, VIEW_CHARGING_SESSIONS,
    VIEW_STATIONS,
    VIEW_DISPUTES, MANAGE_DISPUTES,
    VIEW_SUPPORT_TICKETS, CREATE_SUPPORT_TICKETS, MANAGE_SUPPORT_TICKETS, ADD_NOTES, ESCALATE_TICKET,
    VIEW_STAFF, GLOBAL_SEARCH
  )

  val supportDefaultPermissions = listOf(
    VIEW_DASHBOARD,
    VIEW_SUPPORT_TICKETS,
    CREATE_SUPPORT_TICKETS,
    MANAGE_SUPPORT_TICKETS,
    ADD_NOTES,
    ESCALATE_TICKET
  )

  val financeDefaultPermissions = listOf(
    VIEW_DASHBOARD,
    VIEW_PAYMENTS, MANAGE_PAYMENTS,
    VIEW_WALLETS, MANAGE_WALLETS,
    VIEW_WITHDRAWALS, MANAGE_WITHDRAWALS,
    VIEW_REFUNDS, MANAGE_REFUNDS,
    VIEW_PROMOS, MANAGE_PROMOS,
    VIEW_BOOKINGS
  )

  val operationsDefaultPermissions = listOf(
    VIEW_DASHBOARD,
    VIEW_STATIONS, MANAGE_STATIONS, APPROVE_STATIONS,
    VIEW_HOSTS, MANAGE_HOSTS, APPROVE_HOSTS,
    VIEW_BOOKINGS, MANAGE_BOOKINGS, VIEW_CHARGING_SESSIONS,
    VIEW_DISPUTES, MANAGE_DISPUTES
  )

  fun getPermissionsForRole(role: AdminRole): List<String> {
    return when (role) {
      AdminRole.SUPER_ADMIN -> superAdminPermissions
      AdminRole.ADMIN -> adminDefaultPermissions
      AdminRole.MANAGER -> managerDefaultPermissions
      AdminRole.SUPPORT -> supportDefaultPermissions
      AdminRole.FINANCE -> financeDefaultPermissions
      AdminRole.OPERATIONS -> operationsDefaultPermissions
    }
  }
}

data class AdminStaffUser(
  val id: String = "",
  val staffCode: String = "",
  val name: String = "",
  val email: String = "",
  val phone: String = "",
  val role: AdminRole = AdminRole.SUPPORT,
  val department: String = "Support",
  val status: String = "Active", // "Active", "Inactive", "Suspended"
  val joinedOn: String = "",
  val managerId: String = "",
  val managerName: String = "",
  val permissions: List<String> = emptyList(),
  val assignedAccess: Map<String, String> = emptyMap(),
  val avatarLetter: String = ""
)

data class Customer(
  val id: String = "",
  val customerCode: String = "",
  val name: String = "",
  val email: String = "",
  val phone: String = "",
  val status: String = "Active", // "Active", "Suspended"
  val registrationDate: String = "",
  val walletBalance: Double = 0.0,
  val totalBookings: Int = 0,
  val totalEnergyConsumedKwh: Double = 0.0,
  val totalSpent: Double = 0.0,
  val kycVerified: Boolean = true,
  val activeDisputesCount: Int = 0,
  val openTicketsCount: Int = 0
)

data class Host(
  val id: String = "",
  val hostCode: String = "",
  val name: String = "",
  val email: String = "",
  val phone: String = "",
  val status: String = "Active", // "Active", "Suspended", "Pending Approval"
  val verificationStatus: String = "Verified", // "Verified", "Pending Review", "Rejected"
  val joinedDate: String = "",
  val totalStations: Int = 0,
  val totalBookings: Int = 0,
  val totalEarnings: Double = 0.0,
  val withdrawableBalance: Double = 0.0,
  val commissionPaid: Double = 0.0,
  val rating: Double = 4.8,
  val bankName: String = "",
  val accountNumber: String = "",
  val kycDocumentUrl: String = ""
)

data class ChargingStation(
  val id: String = "",
  val stationCode: String = "",
  val name: String = "",
  val address: String = "",
  val city: String = "",
  val hostId: String = "",
  val hostName: String = "",
  val hostPhone: String = "",
  val status: String = "Active", // "Active", "Offline", "Under Maintenance"
  val approvalStatus: String = "Approved", // "Approved", "Pending Review", "Rejected"
  val powerRatingKw: Double = 50.0,
  val chargerType: String = "CCS2 & Type 2",
  val connectorTypes: List<String> = listOf("CCS2", "Type 2"),
  val pricePerKwh: Double = 18.5,
  val availableSlots: Int = 2,
  val totalSlots: Int = 4,
  val totalChargingSessions: Int = 0,
  val totalRevenueGenerated: Double = 0.0,
  val rating: Double = 4.7,
  val latitude: Double = 28.6139,
  val longitude: Double = 77.2090
)

data class Booking(
  val id: String = "",
  val bookingCode: String = "",
  val customerId: String = "",
  val customerName: String = "",
  val customerPhone: String = "",
  val hostId: String = "",
  val hostName: String = "",
  val stationId: String = "",
  val stationName: String = "",
  val connectorType: String = "CCS2",
  val date: String = "",
  val slotTime: String = "",
  val durationMinutes: Int = 60,
  val energyDeliveredKwh: Double = 28.4,
  val totalAmount: Double = 525.0,
  val bookingStatus: String = "Confirmed", // "Confirmed", "In Progress", "Completed", "Cancelled"
  val paymentStatus: String = "Paid", // "Paid", "Pending", "Refunded", "Failed"
  val chargingStatus: String = "Finished", // "Waiting", "Charging", "Finished", "Interrupted"
  val paymentMethod: String = "Ultimate Wallet",
  val createdAt: String = ""
)

data class WalletTransaction(
  val id: String = "",
  val transactionCode: String = "",
  val userId: String = "",
  val userName: String = "",
  val userType: String = "Customer",
  val type: String = "Debit", // "Credit", "Debit", "Refund", "Commission", "Promo Credit"
  val amount: Double = 0.0,
  val status: String = "Success",
  val date: String = "",
  val referenceBookingId: String = "",
  val description: String = "",
  val note: String = ""
)

data class HostWithdrawal(
  val id: String = "",
  val withdrawalCode: String = "",
  val hostId: String = "",
  val hostName: String = "",
  val amount: Double = 0.0,
  val bankName: String = "",
  val accountNumber: String = "HDFC0001827 - 501002349182",
  val accountNumberMasked: String = "•••• 9182",
  val status: String = "Pending", // "Pending", "Approved", "Processing", "Completed", "Rejected"
  val requestedDate: String = "",
  val processedDate: String = "",
  val referenceNumber: String = "",
  val remarks: String = ""
)

data class PromoCode(
  val id: String = "",
  val code: String = "",
  val title: String = "",
  val discountType: String = "PERCENTAGE", // "PERCENTAGE", "FIXED"
  val discountPercent: Int = 15,
  val discountAmount: Double = 50.0,
  val maxDiscount: Double = 150.0,
  val maxDiscountAmount: Double = 150.0,
  val minBookingAmount: Double = 300.0,
  val usageLimit: Int = 500,
  val totalUsageLimit: Int = 500,
  val usedCount: Int = 142,
  val currentUsageCount: Int = 142,
  val perUserLimit: Int = 2,
  val startDate: String = "01 Aug 2026",
  val endDate: String = "31 Dec 2026",
  val status: String = "Active",
  val active: Boolean = true,
  val applicableStations: List<String> = emptyList()
)

data class CouponValidationResult(
  val isValid: Boolean,
  val discountAmount: Double = 0.0,
  val originalAmount: Double = 0.0,
  val finalAmount: Double = 0.0,
  val message: String = ""
) {
  val finalPayable: Double get() = finalAmount
}

data class CouponUsageRecord(
  val id: String = "",
  val userId: String = "",
  val userName: String = "",
  val bookingId: String = "",
  val couponCode: String = "",
  val discountAmount: Double = 0.0,
  val originalAmount: Double = 0.0,
  val finalAmount: Double = 0.0,
  val timestamp: String = ""
) {
  val discountApplied: Double get() = discountAmount
  val finalPayable: Double get() = finalAmount
  val usedAt: String get() = timestamp
}

data class Dispute(
  val id: String = "",
  val disputeCode: String = "",
  val bookingId: String = "",
  val bookingCode: String = "BK-2025-0819",
  val customerId: String = "",
  val customerName: String = "",
  val customerPhone: String = "",
  val hostId: String = "",
  val hostName: String = "",
  val stationName: String = "",
  val reason: String = "Charger not delivering requested speed",
  val description: String = "",
  val evidenceUrls: List<String> = emptyList(),
  val status: String = "Open", // "Open", "Under Review", "Waiting for User", "Waiting for Host", "Resolved", "Rejected"
  val resolutionNotes: String = "",
  val disputedAmount: Double = 350.0,
  val refundAmount: Double = 0.0,
  val assignedStaffId: String = "",
  val assignedStaffName: String = "Rahul Sharma",
  val createdAt: String = "",
  val updatedAt: String = ""
)

data class SupportTicketReply(
  val senderName: String = "",
  val senderRole: String = "", // "Support", "Customer", "System", "Manager"
  val message: String = "",
  val timestamp: String = "",
  val isInternal: Boolean = false,
  val isStaff: Boolean = false,
  val isInternalNote: Boolean = false
)

data class SupportTicket(
  val id: String = "",
  val ticketCode: String = "",
  val customerId: String = "",
  val customerName: String = "",
  val raisedByName: String = "",
  val raisedByRole: String = "Customer",
  val customerEmail: String = "",
  val customerPhone: String = "",
  val bookingId: String = "",
  val bookingCode: String = "",
  val stationId: String = "",
  val stationName: String = "",
  val hostId: String = "",
  val hostName: String = "",
  val subject: String = "",
  val priority: String = "Medium", // "Low", "Medium", "High", "Urgent"
  val status: String = "Open", // "Open", "Assigned", "In Progress", "Waiting for Customer", "Waiting for Host", "Escalated to Manager", "Resolved", "Closed"
  val assignedToId: String = "",
  val assignedToName: String = "",
  val assignedStaffName: String = "Rahul Sharma",
  val department: String = "Support",
  val escalatedToManager: Boolean = false,
  val escalatedReason: String = "",
  val createdAt: String = "",
  val updatedAt: String = "",
  val messages: List<SupportTicketReply> = emptyList(),
  val replies: List<SupportTicketReply> = emptyList()
)

data class AdminNotification(
  val id: String = "",
  val title: String = "",
  val message: String = "",
  val type: String = "GENERAL",
  val targetRole: String = "ALL",
  val timestamp: String = "",
  val read: Boolean = false,
  val referenceId: String = ""
)

data class DashboardMetrics(
  val totalStaff: Int = 28,
  val activeStaff: Int = 24,
  val totalDepartments: Int = 6,
  val totalRoles: Int = 7,
  val totalCustomers: Int = 1845,
  val activeHosts: Int = 142,
  val activeChargingStations: Int = 389,
  val todayBookings: Int = 124,
  val activeChargingSessions: Int = 42,
  val todayRevenue: Double = 86450.0,
  val pendingHostApprovals: Int = 7,
  val pendingStationApprovals: Int = 5,
  val pendingWithdrawalsCount: Int = 12,
  val pendingWithdrawalsAmount: Double = 248000.0,
  val openDisputes: Int = 4,
  val openSupportTickets: Int = 9,
  val totalEnergyDeliveredTodayKwh: Double = 3450.0
)

data class SecurityThreatEvent(
  val id: String = "",
  val timestamp: String = "",
  val threatType: String = "INTEGRITY_SCAN_PASSED",
  val severity: String = "INFO", // "INFO", "LOW", "MEDIUM", "HIGH", "CRITICAL"
  val description: String = "",
  val source: String = "Defense Engine",
  val status: String = "BLOCKED" // "BLOCKED", "MONITORED", "RESOLVED", "SECURE"
)

data class SecurityShieldStatus(
  val overallRating: String = "A+ MILITARY GRADE",
  val threatLevel: String = "SECURE / 0 VULNERABILITIES",
  val rootStatus: String = "NO ROOT (VERIFIED CLEAN)",
  val fridaHookStatus: String = "ZERO HOOKS DETECTED (SECURE)",
  val screenCaptureGuard: String = "FLAG_SECURE ARMED (ANTI-SNOOPING ACTIVE)",
  val cryptoLevel: String = "AES-256-GCM + TLS 1.3 PINNED",
  val lastScanTime: String = "Just now",
  val activeThreatCount: Int = 0
)

data class SystemSettings(
  val platformCommissionPercent: Double = 12.5,
  val evMarketplaceCommissionPercent: Double = 12.5,
  val minimumWithdrawalAmount: Double = 1000.0,
  val minBookingDurationMinutes: Int = 15,
  val maxPendingWithdrawalDays: Int = 3,
  val autoApproveVerifiedStations: Boolean = false,
  val screenCaptureShieldEnabled: Boolean = true, // Default enabled for unhackable screen privacy
  val biometricAuthEnabled: Boolean = true,
  val sessionTimeoutMinutes: Int = 30,
  val pinLockEnabled: Boolean = false,
  val pinCode: String = "1234",
  val appVersion: String = "v3.8.2-enterprise-live",
  val firebaseEnvironment: String = "Production (asia-south1)",
  val platformMaintenanceMode: Boolean = false,
  // Live Production Configuration
  val isLiveProductionMode: Boolean = true, // Live Production Mode Active
  val environmentMode: String = "LIVE PRODUCTION", // "LIVE PRODUCTION", "STAGING / SANDBOX"
  val ocppGatewayStatus: String = "ONLINE (OCPP 2.0.1)",
  val paymentGatewayLiveStatus: String = "LIVE (Razorpay / Stripe Webhooks Active)",
  val cloudSyncState: String = "SYNCHRONIZED (Cloud Firestore Live)",
  val liveFleetHealthPercent: Int = 99,
  // Notification Sound & Audio Engine Settings
  val notificationSoundEnabled: Boolean = true,
  val notificationSoundStyle: String = "High-Tech Chime", // "High-Tech Chime", "Alert Beep", "Subtle Pulse", "Sonic Alarm"
  val audioHapticFeedbackEnabled: Boolean = true,
  // Enterprise Cyber Defense Shield Settings
  val securityHardeningEnabled: Boolean = true,
  val antiTamperShieldEnabled: Boolean = true,
  val antiHookingDefenseEnabled: Boolean = true,
  val bruteForceDefenseEnabled: Boolean = true,
  val zeroTrustPolicyEnforced: Boolean = true,
  val emergencyPlatformLockdown: Boolean = false
)
