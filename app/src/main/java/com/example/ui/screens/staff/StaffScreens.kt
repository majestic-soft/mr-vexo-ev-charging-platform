package com.example.ui.screens.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminViewModel

fun getRoleBackground(role: AdminRole): Color = when (role) {
  AdminRole.SUPER_ADMIN -> Color(0xFFEDE7F6)
  AdminRole.ADMIN -> Color(0xFFF3E5F5)
  AdminRole.MANAGER -> Color(0xFFE0F2F1)
  AdminRole.SUPPORT -> Color(0xFFE3F2FD)
  AdminRole.FINANCE -> Color(0xFFFFF8E1)
  AdminRole.OPERATIONS -> Color(0xFFE8F5E9)
}

fun getRoleTextColor(role: AdminRole): Color = when (role) {
  AdminRole.SUPER_ADMIN -> Color(0xFF6A1B9A)
  AdminRole.ADMIN -> Color(0xFF4A148C)
  AdminRole.MANAGER -> Color(0xFF00695C)
  AdminRole.SUPPORT -> Color(0xFF1565C0)
  AdminRole.FINANCE -> Color(0xFFE65100)
  AdminRole.OPERATIONS -> Color(0xFF2E7D32)
}

@Composable
fun StaffInfoRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  text: String = "",
  label: String? = null,
  value: String? = null,
  avatarLetter: String? = null
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = UltimateSlate500,
      modifier = Modifier.size(18.dp)
    )

    Spacer(modifier = Modifier.width(12.dp))

    if (label != null && value != null) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = UltimateSlate500,
        modifier = Modifier.width(90.dp)
      )

      if (avatarLetter != null) {
        Box(
          modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(UltimateGreenBg),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = avatarLetter,
            color = UltimateEmerald,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
      }

      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = UltimateNavy
      )
    } else {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = UltimateNavy
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StaffListScreen(
  viewModel: AdminViewModel,
  onStaffClick: (String) -> Unit,
  onAddStaffClick: () -> Unit,
  onRolesPermissionsClick: () -> Unit,
  onDepartmentsClick: () -> Unit,
  onActivityLogsClick: () -> Unit
) {
  val currentAdmin by viewModel.currentAdmin.collectAsState()
  val staffList by viewModel.filteredStaffList.collectAsState()
  val allStaff by viewModel.staffList.collectAsState()
  val searchQuery by viewModel.staffSearchQuery.collectAsState()
  val selectedDept by viewModel.staffFilterDepartment.collectAsState()
  val selectedStatus by viewModel.staffFilterStatus.collectAsState()
  val selectedRole by viewModel.staffFilterRole.collectAsState()
  val metrics by viewModel.metrics.collectAsState()
  val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsState()
  val unreadNotifs by viewModel.unreadNotificationsCount.collectAsState()
  val statusMessage by viewModel.statusMessage.collectAsState()

  var showTimeFilterDialog by remember { mutableStateOf(false) }
  var showDepartmentFilterDialog by remember { mutableStateOf(false) }
  var showRoleSwitcherDialog by remember { mutableStateOf(false) }
  var staffToSuspendOrActivate by remember { mutableStateOf<AdminStaffUser?>(null) }
  var editingPermissionsStaff by remember { mutableStateOf<AdminStaffUser?>(null) }
  var securityWarningMessage by remember { mutableStateOf<String?>(null) }

  val departments = listOf("All", "Support", "Operations", "Finance", "Executive")
  val statusFilters = listOf("All", "Active", "Suspended", "Inactive")
  val roleFilters = listOf("All", "Super Admin", "Admin", "Manager", "Customer Support", "Finance", "Operations")

  val isSuperAdmin = currentAdmin?.role == AdminRole.SUPER_ADMIN

  Scaffold(
    containerColor = UltimateBackground,
    topBar = {
      AdminHeaderTopBar(
        unreadNotificationCount = unreadNotifs,
        onMenuClick = onDepartmentsClick,
        onNotificationClick = { /* Notifications */ },
        onSearchClick = { viewModel.setStaffSearchQuery("") }
      )
    },
    snackbarHost = {
      if (statusMessage != null) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (statusMessage?.contains("Denied", ignoreCase = true) == true ||
              statusMessage?.contains("Error", ignoreCase = true) == true
            ) Color(0xFFB71C1C) else Color(0xFF1B5E20)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (statusMessage?.contains("Denied", ignoreCase = true) == true)
                Icons.Default.Security else Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = statusMessage ?: "",
              color = Color.White,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              modifier = Modifier.weight(1f)
            )
            IconButton(
              onClick = { viewModel.clearStatusMessage() },
              modifier = Modifier.size(24.dp)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(bottom = 16.dp)
    ) {
      // Welcome user header matching mockup
      item {
        AdminUserWelcomeHeader(
          currentAdmin = currentAdmin,
          onSwitchRoleClick = { showRoleSwitcherDialog = true }
        )
      }

      // Security / Super Admin Status Pill
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSuperAdmin) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
          ),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSuperAdmin) Color(0xFFA5D6A7) else Color(0xFFFFCC80)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isSuperAdmin) Icons.Outlined.VerifiedUser else Icons.Outlined.Lock,
              contentDescription = null,
              tint = if (isSuperAdmin) Color(0xFF2E7D32) else Color(0xFFE65100),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isSuperAdmin) "Super Admin Management Console" else "Restricted Admin Viewer",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSuperAdmin) Color(0xFF1B5E20) else Color(0xFFBF360C)
              )
              Text(
                text = if (isSuperAdmin)
                  "You have full authority to assign roles, modify Firestore permissions & suspend accounts."
                else
                  "Logged in as ${currentAdmin?.role?.displayName}. Administrative write actions require Super Admin privileges.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = if (isSuperAdmin) Color(0xFF2E7D32) else Color(0xFFD84315)
              )
            }
          }
        }
      }

      // Dark Green Overview Card
      item {
        DarkGreenOverviewCard(
          totalStaff = metrics.totalStaff,
          activeStaff = metrics.activeStaff,
          departmentsCount = metrics.totalDepartments,
          rolesCount = metrics.totalRoles,
          selectedTimeFilter = selectedTimeFilter,
          onTimeFilterClick = { showTimeFilterDialog = true },
          onViewDepartments = onDepartmentsClick,
          onViewRoles = onRolesPermissionsClick
        )
      }

      // Quick Actions
      item {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
          Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = UltimateNavy
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            QuickActionButton(
              icon = Icons.Outlined.PersonAdd,
              label = "Add Admin",
              backgroundColor = Color(0xFFE8F5E9),
              iconTint = Color(0xFF2E7D32),
              onClick = {
                if (isSuperAdmin || viewModel.hasPermission(AppPermissions.MANAGE_STAFF)) {
                  onAddStaffClick()
                } else {
                  securityWarningMessage = "Action Denied: You need Super Admin role or 'Manage Staff' permission to register new administrators."
                }
              }
            )

            QuickActionButton(
              icon = Icons.Outlined.Security,
              label = "Roles &\nPermissions",
              backgroundColor = Color(0xFFEDE7F6),
              iconTint = Color(0xFF6A1B9A),
              onClick = onRolesPermissionsClick
            )

            QuickActionButton(
              icon = Icons.Outlined.Apartment,
              label = "Departments",
              backgroundColor = Color(0xFFFFF3E0),
              iconTint = Color(0xFFE65100),
              onClick = onDepartmentsClick
            )

            QuickActionButton(
              icon = Icons.Outlined.Schedule,
              label = "Activity Logs",
              backgroundColor = Color(0xFFE3F2FD),
              iconTint = Color(0xFF1565C0),
              onClick = onActivityLogsClick
            )
          }
        }
      }

      // Staff Members header row with counts
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Admin Users Directory",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = UltimateNavy
            )
            Text(
              text = "${staffList.size} of ${allStaff.size} administrators displayed",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = UltimateSlate500
            )
          }

          Text(
            text = "Reset Filters",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = UltimateEmerald,
            modifier = Modifier.clickable {
              viewModel.setStaffSearchQuery("")
              viewModel.setStaffFilterDepartment("All")
              viewModel.setStaffFilterStatus("All")
              viewModel.setStaffFilterRole("All")
            }
          )
        }
      }

      // Search & Filter field
      item {
        EnterpriseSearchField(
          query = searchQuery,
          onQueryChange = { viewModel.setStaffSearchQuery(it) },
          placeholderText = "Search by name, email, role, or ID...",
          onFilterClick = { showDepartmentFilterDialog = true },
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
      }

      // Status Filter Segment Chips
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
          Text(
            text = "Status:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = UltimateSlate600
          )
          Spacer(modifier = Modifier.height(4.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(statusFilters) { status ->
              FilterChip(
                selected = selectedStatus == status,
                onClick = { viewModel.setStaffFilterStatus(status) },
                label = {
                  Text(
                    text = when (status) {
                      "All" -> "All Statuses"
                      "Active" -> "🟢 Active"
                      "Suspended" -> "🔴 Suspended"
                      "Inactive" -> "⚪ Inactive"
                      else -> status
                    },
                    fontSize = 11.sp
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = UltimateGreenBg,
                  selectedLabelColor = UltimateEmerald
                )
              )
            }
          }
        }
      }

      // Role Filter Segment Chips
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
          Text(
            text = "Role Filter:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = UltimateSlate600
          )
          Spacer(modifier = Modifier.height(4.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(roleFilters) { role ->
              FilterChip(
                selected = selectedRole == role,
                onClick = { viewModel.setStaffFilterRole(role) },
                label = { Text(role, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = Color(0xFFEDE7F6),
                  selectedLabelColor = Color(0xFF6A1B9A)
                )
              )
            }
          }
        }
      }

      // Active department chips if filtered
      if (selectedDept != "All") {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Department: ",
              style = MaterialTheme.typography.bodySmall,
              color = UltimateSlate500
            )
            AssistChip(
              onClick = { viewModel.setStaffFilterDepartment("All") },
              label = { Text(selectedDept) },
              trailingIcon = {
                Icon(
                  Icons.Default.Close,
                  contentDescription = "Clear",
                  modifier = Modifier.size(14.dp)
                )
              }
            )
          }
        }
      }

      // Staff Cards List
      if (staffList.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Outlined.SearchOff,
                contentDescription = null,
                tint = UltimateSlate400,
                modifier = Modifier.size(40.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No admin users match the selected filters.",
                style = MaterialTheme.typography.bodyMedium,
                color = UltimateSlate500
              )
            }
          }
        }
      } else {
        items(staffList, key = { it.id }) { staff ->
          AdminManagementCard(
            staff = staff,
            isCurrentAdminSuperAdmin = isSuperAdmin,
            onCardClick = { onStaffClick(staff.id) },
            onEditPermissionsClick = {
              if (isSuperAdmin) {
                editingPermissionsStaff = staff
              } else {
                securityWarningMessage = "Access Denied: Only Super Admins are authorized to reassign roles or modify Firestore permissions."
              }
            },
            onToggleStatusClick = {
              if (isSuperAdmin) {
                if (staff.id == currentAdmin?.id) {
                  securityWarningMessage = "Self-Protection Rule: You cannot suspend your own active Super Admin account."
                } else {
                  staffToSuspendOrActivate = staff
                }
              } else {
                securityWarningMessage = "Access Denied: Suspending or activating admin accounts requires Super Admin privileges."
              }
            }
          )
        }
      }
    }
  }

  // Security Warning Dialog
  if (securityWarningMessage != null) {
    AlertDialog(
      onDismissRequest = { securityWarningMessage = null },
      icon = {
        Icon(
          Icons.Default.Security,
          contentDescription = "Security Alert",
          tint = Color(0xFFD32F2F),
          modifier = Modifier.size(32.dp)
        )
      },
      title = { Text("Permission Restriction") },
      text = {
        Text(
          text = securityWarningMessage ?: "",
          style = MaterialTheme.typography.bodyMedium,
          color = UltimateNavy
        )
      },
      confirmButton = {
        Button(
          onClick = { securityWarningMessage = null },
          colors = ButtonDefaults.buttonColors(containerColor = UltimateNavy)
        ) {
          Text("Understood")
        }
      }
    )
  }

  // Suspend / Activate Confirmation Dialog
  if (staffToSuspendOrActivate != null) {
    val targetStaff = staffToSuspendOrActivate!!
    val isCurrentlyActive = targetStaff.status.equals("Active", ignoreCase = true)
    AlertDialog(
      onDismissRequest = { staffToSuspendOrActivate = null },
      icon = {
        Icon(
          imageVector = if (isCurrentlyActive) Icons.Outlined.Block else Icons.Outlined.CheckCircle,
          contentDescription = null,
          tint = if (isCurrentlyActive) StatusDangerText else UltimateEmerald,
          modifier = Modifier.size(36.dp)
        )
      },
      title = {
        Text(if (isCurrentlyActive) "Suspend Admin Account?" else "Activate Admin Account?")
      },
      text = {
        Column {
          Text(
            text = if (isCurrentlyActive)
              "Suspending ${targetStaff.name} (${targetStaff.staffCode}) will immediately revoke their ability to log in and block all Firestore write operations."
            else
              "Activating ${targetStaff.name} (${targetStaff.staffCode}) will restore their full administrative access with their configured permissions.",
            style = MaterialTheme.typography.bodyMedium,
            color = UltimateNavy
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Target Role: ${targetStaff.role.displayName} • ${targetStaff.department}",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = UltimateSlate600
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val success = if (isCurrentlyActive) {
              viewModel.suspendAdminAccount(targetStaff.id)
            } else {
              viewModel.activateAdminAccount(targetStaff.id)
            }
            staffToSuspendOrActivate = null
            if (!success) {
              securityWarningMessage = "Operation failed: Validated Super Admin authorization is required."
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isCurrentlyActive) StatusDangerText else UltimateEmerald
          )
        ) {
          Text(if (isCurrentlyActive) "Confirm Suspension" else "Confirm Activation")
        }
      },
      dismissButton = {
        TextButton(onClick = { staffToSuspendOrActivate = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Edit Permissions Dialog Modal
  if (editingPermissionsStaff != null) {
    EditStaffPermissionsDialog(
      staffId = editingPermissionsStaff!!.id,
      viewModel = viewModel,
      onDismiss = { editingPermissionsStaff = null }
    )
  }

  // Time filter modal
  if (showTimeFilterDialog) {
    AlertDialog(
      onDismissRequest = { showTimeFilterDialog = false },
      title = { Text("Filter Overview Range") },
      text = {
        Column {
          listOf("Today", "Last 7 Days", "This Month", "Last 30 Days", "This Quarter").forEach { filter ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.setTimeFilter(filter)
                  showTimeFilterDialog = false
                }
                .padding(vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = selectedTimeFilter == filter,
                onClick = {
                  viewModel.setTimeFilter(filter)
                  showTimeFilterDialog = false
                }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(filter, style = MaterialTheme.typography.bodyLarge)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showTimeFilterDialog = false }) { Text("Close") }
      }
    )
  }

  // Department filter modal
  if (showDepartmentFilterDialog) {
    AlertDialog(
      onDismissRequest = { showDepartmentFilterDialog = false },
      title = { Text("Filter by Department") },
      text = {
        Column {
          departments.forEach { dept ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.setStaffFilterDepartment(dept)
                  showDepartmentFilterDialog = false
                }
                .padding(vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = selectedDept == dept,
                onClick = {
                  viewModel.setStaffFilterDepartment(dept)
                  showDepartmentFilterDialog = false
                }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(dept, style = MaterialTheme.typography.bodyLarge)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showDepartmentFilterDialog = false }) { Text("Close") }
      }
    )
  }

  // Role Switcher Dialog for rapid testing of RBAC
  if (showRoleSwitcherDialog) {
    AlertDialog(
      onDismissRequest = { showRoleSwitcherDialog = false },
      title = { Text("Switch Active Admin Session") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          Text(
            text = "Select any admin account to test role validation and UI segregation:",
            style = MaterialTheme.typography.bodySmall,
            color = UltimateSlate500
          )
          Spacer(modifier = Modifier.height(12.dp))
          allStaff.forEach { admin ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  viewModel.selectAdminUser(admin)
                  showRoleSwitcherDialog = false
                }
                .background(if (currentAdmin?.id == admin.id) UltimateGreenBg else Color.Transparent)
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(getRoleTextColor(admin.role)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = admin.avatarLetter.ifBlank { "A" },
                  color = Color.White,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = admin.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = UltimateNavy
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  StatusBadge(status = admin.status)
                }
                Text(
                  text = "${admin.role.displayName} • ${admin.department}",
                  style = MaterialTheme.typography.bodySmall,
                  color = UltimateSlate500
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showRoleSwitcherDialog = false }) { Text("Done") }
      }
    )
  }
}

@Composable
fun AdminManagementCard(
  staff: AdminStaffUser,
  isCurrentAdminSuperAdmin: Boolean,
  onCardClick: () -> Unit,
  onEditPermissionsClick: () -> Unit,
  onToggleStatusClick: () -> Unit
) {
  val isSuspended = staff.status.equals("Suspended", ignoreCase = true)

  val avatarBg = getRoleBackground(staff.role)
  val avatarTextColor = getRoleTextColor(staff.role)

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSuspended) Color(0xFFFFF5F5) else UltimateCardBg
    ),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isSuspended) Color(0xFFFFCDD2) else UltimateSurfaceBorder
    ),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clickable { onCardClick() }
      .testTag("admin_card_${staff.staffCode}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Circle Avatar
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(avatarBg),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = staff.avatarLetter.ifBlank { staff.name.take(1) },
            color = avatarTextColor,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = staff.name,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = if (isSuspended) UltimateSlate600 else UltimateNavy
            )
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(status = staff.status)
          }

          Spacer(modifier = Modifier.height(3.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Role Tag
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(avatarBg)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = staff.role.displayName,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold
                ),
                color = avatarTextColor
              )
            }

            if (staff.department.isNotBlank()) {
              Text(
                text = " • ${staff.department}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = UltimateSlate500
              )
            }
          }

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = "${staff.email} • ID: ${staff.staffCode}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = UltimateSlate500
          )
        }

        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = "Details",
          tint = UltimateSlate300,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = UltimateSurfaceBorder.copy(alpha = 0.6f))
      Spacer(modifier = Modifier.height(8.dp))

      // Bottom Row: Permissions Count & Super Admin Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.Key,
            contentDescription = null,
            tint = UltimateEmerald,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${staff.permissions.size} Firestore Permissions",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = UltimateSlate600
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          if (isCurrentAdminSuperAdmin) {
            // Edit Role/Permissions Button
            TextButton(
              onClick = onEditPermissionsClick,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Icon(Icons.Outlined.Security, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6A1B9A))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Permissions", fontSize = 11.sp, color = Color(0xFF6A1B9A))
            }

            // Suspend/Activate Quick Toggle
            TextButton(
              onClick = onToggleStatusClick,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Icon(
                imageVector = if (isSuspended) Icons.Outlined.CheckCircle else Icons.Outlined.Block,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (isSuspended) UltimateEmerald else StatusDangerText
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isSuspended) "Activate" else "Suspend",
                fontSize = 11.sp,
                color = if (isSuspended) UltimateEmerald else StatusDangerText
              )
            }
          } else {
            Text(
              text = "View Only",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = UltimateSlate400
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StaffDetailScreen(
  staffId: String,
  viewModel: AdminViewModel,
  onBackClick: () -> Unit,
  onEditPermissionsClick: (String) -> Unit
) {
  val staffList by viewModel.staffList.collectAsState()
  val staff = staffList.find { it.id == staffId } ?: staffList.firstOrNull()
  val currentAdmin by viewModel.currentAdmin.collectAsState()

  var showSuspendConfirm by remember { mutableStateOf(false) }
  var showResetPasswordSuccess by remember { mutableStateOf(false) }
  var securityWarningMessage by remember { mutableStateOf<String?>(null) }

  val isSuperAdmin = currentAdmin?.role == AdminRole.SUPER_ADMIN

  if (staff == null) {
    AccessRestrictedScreen(requiredPermission = "View Staff", onBackClick = onBackClick)
    return
  }

  val isSuspended = staff.status.equals("Suspended", ignoreCase = true)

  Scaffold(
    containerColor = UltimateBackground,
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBackClick) {
          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = UltimateNavy
          )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
          text = "Admin Profile & Access",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = UltimateNavy
        )
        Spacer(modifier = Modifier.weight(1.3f))
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Activation Status Banner
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isSuspended) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
        ),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isSuspended) Color(0xFFFFCDD2) else Color(0xFFA5D6A7)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isSuspended) Icons.Default.Warning else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isSuspended) StatusDangerText else Color(0xFF2E7D32),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (isSuspended) "Account Suspended" else "Account Active & Operational",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = if (isSuspended) StatusDangerText else Color(0xFF1B5E20)
            )
            Text(
              text = if (isSuspended)
                "This admin account is blocked from login and cannot perform Firestore writes."
              else
                "Active in Firestore. Authenticated sessions have authority based on assigned permissions.",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = if (isSuspended) Color(0xFFC62828) else Color(0xFF2E7D32)
            )
          }
          if (isSuperAdmin) {
            Button(
              onClick = { showSuspendConfirm = true },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isSuspended) UltimateEmerald else StatusDangerText
              ),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.height(32.dp)
            ) {
              Text(if (isSuspended) "Activate" else "Suspend", fontSize = 11.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Header Card with Avatar & ID
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(getRoleBackground(staff.role)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = staff.avatarLetter.ifBlank { "A" },
                color = getRoleTextColor(staff.role),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
              )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = staff.name,
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                  color = UltimateNavy
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = staff.status)
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = staff.role.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = getRoleTextColor(staff.role)
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "ID: ${staff.staffCode} • Dept: ${staff.department}",
                style = MaterialTheme.typography.bodySmall,
                color = UltimateSlate500
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))
          HorizontalDivider(color = UltimateSurfaceBorder)
          Spacer(modifier = Modifier.height(16.dp))

          // Detail rows
          StaffInfoRow(icon = Icons.Outlined.Mail, text = staff.email)
          Spacer(modifier = Modifier.height(12.dp))
          StaffInfoRow(icon = Icons.Outlined.Phone, text = staff.phone)
          Spacer(modifier = Modifier.height(12.dp))
          StaffInfoRow(
            icon = Icons.Outlined.CalendarToday,
            label = "Joined On",
            value = staff.joinedOn.ifBlank { "08 May 2025, 10:30 AM" }
          )
          Spacer(modifier = Modifier.height(12.dp))
          StaffInfoRow(
            icon = Icons.Outlined.Person,
            label = "Manager",
            value = staff.managerName.ifBlank { "Ankit Verma (Super Admin)" },
            avatarLetter = "A"
          )
          Spacer(modifier = Modifier.height(12.dp))
          StaffInfoRow(
            icon = Icons.Outlined.Shield,
            label = "Department",
            value = staff.department
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Role & Firestore Permissions Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Role & Firestore Permissions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = UltimateNavy
              )
              Text(
                text = "Validated against RBAC security rules",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = UltimateSlate500
              )
            }

            if (isSuperAdmin) {
              FilledTonalButton(
                onClick = { onEditPermissionsClick(staff.id) },
                colors = ButtonDefaults.filledTonalButtonColors(
                  containerColor = Color(0xFFEDE7F6),
                  contentColor = Color(0xFF6A1B9A)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Modify", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Assigned Role:",
              style = MaterialTheme.typography.bodyMedium,
              color = UltimateSlate600
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(getRoleBackground(staff.role))
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = staff.role.displayName,
                color = getRoleTextColor(staff.role),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Active Firestore Permissions (${staff.permissions.size}):",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = UltimateNavy
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Permissions wrap flow
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            staff.permissions.forEach { perm ->
              PermissionChip(label = perm)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Assigned Access Domain Summary Card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Domain Scope Summary",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = UltimateNavy
          )

          Spacer(modifier = Modifier.height(14.dp))

          val accessItems = listOf(
            Triple(Icons.Outlined.People, "Users & Hosts", staff.assignedAccess["Users"] ?: "Full Access"),
            Triple(Icons.Outlined.EvStation, "EV Stations", if (staff.permissions.contains(AppPermissions.MANAGE_STATIONS)) "Full Management" else "View Only"),
            Triple(Icons.Outlined.EventNote, "Bookings & Sessions", staff.assignedAccess["Bookings"] ?: "View, Update Notes"),
            Triple(Icons.Outlined.AccountBalanceWallet, "Payments & Wallets", if (staff.permissions.contains(AppPermissions.MANAGE_PAYMENTS)) "Manage & Process" else "Restricted"),
            Triple(Icons.Outlined.ConfirmationNumber, "Support Tickets", staff.assignedAccess["Support Tickets"] ?: "Full Support"),
            Triple(Icons.Outlined.Security, "Admin Operations", if (staff.role == AdminRole.SUPER_ADMIN) "Super Admin Authority" else "Restricted")
          )

          accessItems.forEachIndexed { index, item ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = item.first,
                contentDescription = null,
                tint = UltimateSlate500,
                modifier = Modifier.size(20.dp)
              )

              Spacer(modifier = Modifier.width(12.dp))

              Text(
                text = item.second,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = UltimateNavy
              )

              Spacer(modifier = Modifier.weight(1f))

              Text(
                text = item.third,
                style = MaterialTheme.typography.bodySmall,
                color = UltimateSlate500
              )
            }

            if (index < accessItems.size - 1) {
              HorizontalDivider(color = UltimateSurfaceBorder.copy(alpha = 0.5f))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Admin Action Buttons
      Text(
        text = "Super Admin Actions",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = UltimateNavy
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Button(
          onClick = {
            if (isSuperAdmin) {
              onEditPermissionsClick(staff.id)
            } else {
              securityWarningMessage = "Access Denied: Only Super Admins can alter role assignments or permissions."
            }
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = UltimateEmerald),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Outlined.Security, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Edit Permissions")
        }

        OutlinedButton(
          onClick = {
            viewModel.resetStaffPassword(staff.id)
            showResetPasswordSuccess = true
          },
          shape = RoundedCornerShape(14.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = UltimateNavy),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Reset Pass")
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedButton(
        onClick = {
          if (isSuperAdmin) {
            if (staff.id == currentAdmin?.id) {
              securityWarningMessage = "Self-Protection Rule: You cannot suspend your own active Super Admin session."
            } else {
              showSuspendConfirm = true
            }
          } else {
            securityWarningMessage = "Access Denied: Suspending or activating accounts requires Super Admin privileges."
          }
        },
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isSuspended) UltimateEmerald else StatusDangerText.copy(alpha = 0.6f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = if (isSuspended) UltimateEmerald else StatusDangerText
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          imageVector = if (isSuspended) Icons.Outlined.CheckCircle else Icons.Outlined.Block,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isSuspended) "Activate Admin Account" else "Suspend Admin Account")
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }

  // Security Alert Modal
  if (securityWarningMessage != null) {
    AlertDialog(
      onDismissRequest = { securityWarningMessage = null },
      icon = {
        Icon(
          Icons.Default.Security,
          contentDescription = "Security Alert",
          tint = Color(0xFFD32F2F),
          modifier = Modifier.size(32.dp)
        )
      },
      title = { Text("Permission Restriction") },
      text = { Text(securityWarningMessage ?: "") },
      confirmButton = {
        Button(onClick = { securityWarningMessage = null }) { Text("Dismiss") }
      }
    )
  }

  // Suspend Confirm Modal
  if (showSuspendConfirm) {
    AlertDialog(
      onDismissRequest = { showSuspendConfirm = false },
      title = { Text(if (isSuspended) "Activate Admin Account?" else "Suspend Admin Account?") },
      text = {
        Text(
          if (isSuspended)
            "Activating this account will restore administrative permissions for ${staff.name} in Firestore."
          else
            "Suspending this account will immediately revoke access and block ${staff.name} from performing any administrative actions."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (isSuspended) {
              viewModel.activateAdminAccount(staff.id)
            } else {
              viewModel.suspendAdminAccount(staff.id)
            }
            showSuspendConfirm = false
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isSuspended) UltimateEmerald else StatusDangerText
          )
        ) {
          Text("Confirm")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSuspendConfirm = false }) { Text("Cancel") }
      }
    )
  }

  if (showResetPasswordSuccess) {
    AlertDialog(
      onDismissRequest = { showResetPasswordSuccess = false },
      title = { Text("Password Reset Dispatched") },
      text = {
        Text("A secure password reset link has been dispatched to ${staff.email}.")
      },
      confirmButton = {
        Button(onClick = { showResetPasswordSuccess = false }) { Text("Done") }
      }
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditStaffPermissionsDialog(
  staffId: String,
  viewModel: AdminViewModel,
  onDismiss: () -> Unit
) {
  val staffList by viewModel.staffList.collectAsState()
  val staff = staffList.find { it.id == staffId } ?: return
  val currentAdmin by viewModel.currentAdmin.collectAsState()

  var selectedRole by remember { mutableStateOf(staff.role) }
  var selectedDept by remember { mutableStateOf(staff.department) }
  var permissions by remember { mutableStateOf(staff.permissions.toMutableSet()) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val isSuperAdmin = currentAdmin?.role == AdminRole.SUPER_ADMIN

  val userPermissions = listOf(
    AppPermissions.VIEW_CUSTOMERS,
    AppPermissions.MANAGE_CUSTOMERS,
    AppPermissions.VIEW_HOSTS,
    AppPermissions.MANAGE_HOSTS
  )

  val stationPermissions = listOf(
    AppPermissions.VIEW_STATIONS,
    AppPermissions.MANAGE_STATIONS,
    AppPermissions.APPROVE_STATIONS,
    AppPermissions.VIEW_CHARGING_SESSIONS
  )

  val bookingPermissions = listOf(
    AppPermissions.VIEW_BOOKINGS,
    AppPermissions.MANAGE_BOOKINGS,
    AppPermissions.VIEW_PROMOS,
    AppPermissions.MANAGE_PROMOS
  )

  val financePermissions = listOf(
    AppPermissions.VIEW_PAYMENTS,
    AppPermissions.MANAGE_PAYMENTS,
    AppPermissions.VIEW_WALLETS,
    AppPermissions.MANAGE_WALLETS,
    AppPermissions.VIEW_WITHDRAWALS,
    AppPermissions.MANAGE_WITHDRAWALS
  )

  val supportPermissions = listOf(
    AppPermissions.VIEW_SUPPORT_TICKETS,
    AppPermissions.CREATE_SUPPORT_TICKETS,
    AppPermissions.MANAGE_SUPPORT_TICKETS,
    AppPermissions.ADD_NOTES,
    AppPermissions.VIEW_DISPUTES,
    AppPermissions.MANAGE_DISPUTES
  )

  val systemPermissions = listOf(
    AppPermissions.VIEW_STAFF,
    AppPermissions.MANAGE_STAFF,
    AppPermissions.MANAGE_ROLES_PERMISSIONS,
    AppPermissions.VIEW_ANALYTICS,
    AppPermissions.SYSTEM_SETTINGS
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = Color(0xFF6A1B9A),
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("Admin Role & Firestore Permissions")
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          text = "Configuring permissions for: ${staff.name} (${staff.staffCode})",
          style = MaterialTheme.typography.bodySmall,
          color = UltimateSlate500
        )

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = errorMessage ?: "",
            color = StatusDangerText,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Select Role:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          AdminRole.entries.forEach { role ->
            FilterChip(
              selected = selectedRole == role,
              onClick = {
                selectedRole = role
                when (role) {
                  AdminRole.SUPER_ADMIN -> permissions = AppPermissions.superAdminPermissions.toMutableSet()
                  AdminRole.ADMIN -> permissions = AppPermissions.adminDefaultPermissions.toMutableSet()
                  AdminRole.MANAGER -> permissions = AppPermissions.managerDefaultPermissions.toMutableSet()
                  AdminRole.SUPPORT -> permissions = AppPermissions.supportDefaultPermissions.toMutableSet()
                  AdminRole.FINANCE -> permissions = AppPermissions.financeDefaultPermissions.toMutableSet()
                  AdminRole.OPERATIONS -> permissions = AppPermissions.operationsDefaultPermissions.toMutableSet()
                }
              },
              label = { Text(role.displayName, fontSize = 11.sp) }
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Presets Row
        Text("Permission Presets:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = UltimateSlate600)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          OutlinedButton(
            onClick = { permissions = AppPermissions.superAdminPermissions.toMutableSet() },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text("Super Admin", fontSize = 10.sp)
          }
          OutlinedButton(
            onClick = { permissions = AppPermissions.adminDefaultPermissions.toMutableSet() },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text("Admin Default", fontSize = 10.sp)
          }
          OutlinedButton(
            onClick = { permissions = AppPermissions.managerDefaultPermissions.toMutableSet() },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text("Manager Default", fontSize = 10.sp)
          }
          OutlinedButton(
            onClick = { permissions = AppPermissions.supportDefaultPermissions.toMutableSet() },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text("Support Default", fontSize = 10.sp)
          }
          OutlinedButton(
            onClick = { permissions = AppPermissions.financeDefaultPermissions.toMutableSet() },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text("Finance Default", fontSize = 10.sp)
          }
          OutlinedButton(
            onClick = { permissions = AppPermissions.operationsDefaultPermissions.toMutableSet() },
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text("Operations Default", fontSize = 10.sp)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text("Department:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = selectedDept,
          onValueChange = { selectedDept = it },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Granular Permissions (${permissions.size} selected):",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = UltimateNavy
        )
        Text(
          text = "Persisted directly to Firestore `admins/${staff.id}`",
          style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
          color = UltimateSlate500
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Categories of Permissions
        PermissionCategorySection(
          categoryName = "👥 Users & Hosts Management",
          permissionList = userPermissions,
          activePermissions = permissions,
          onToggle = { perm ->
            if (permissions.contains(perm)) permissions.remove(perm) else permissions.add(perm)
          }
        )

        PermissionCategorySection(
          categoryName = "⚡ EV Stations & Infrastructure",
          permissionList = stationPermissions,
          activePermissions = permissions,
          onToggle = { perm ->
            if (permissions.contains(perm)) permissions.remove(perm) else permissions.add(perm)
          }
        )

        PermissionCategorySection(
          categoryName = "📋 Bookings & Charging Sessions",
          permissionList = bookingPermissions,
          activePermissions = permissions,
          onToggle = { perm ->
            if (permissions.contains(perm)) permissions.remove(perm) else permissions.add(perm)
          }
        )

        PermissionCategorySection(
          categoryName = "💳 Payments, Wallets & Financials",
          permissionList = financePermissions,
          activePermissions = permissions,
          onToggle = { perm ->
            if (permissions.contains(perm)) permissions.remove(perm) else permissions.add(perm)
          }
        )

        PermissionCategorySection(
          categoryName = "🎧 Support Tickets & Disputes",
          permissionList = supportPermissions,
          activePermissions = permissions,
          onToggle = { perm ->
            if (permissions.contains(perm)) permissions.remove(perm) else permissions.add(perm)
          }
        )

        PermissionCategorySection(
          categoryName = "🛡️ System Security & Staff Management",
          permissionList = systemPermissions,
          activePermissions = permissions,
          onToggle = { perm ->
            if (permissions.contains(perm)) permissions.remove(perm) else permissions.add(perm)
          }
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (!isSuperAdmin) {
            errorMessage = "Action Blocked: Super Admin privileges are required to modify admin permissions."
            return@Button
          }
          val success = viewModel.changeAdminRoleAndPermissions(
            staffId = staff.id,
            newRole = selectedRole,
            newPermissions = permissions.toList(),
            newDepartment = selectedDept
          )
          if (success) {
            onDismiss()
          } else {
            errorMessage = "Permission validation failed. Ensure you are authorized as Super Admin."
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = UltimateEmerald)
      ) {
        Text("Save & Sync to Firestore")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun PermissionCategorySection(
  categoryName: String,
  permissionList: List<String>,
  activePermissions: Set<String>,
  onToggle: (String) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(UltimateSlate100.copy(alpha = 0.5f))
      .padding(10.dp)
  ) {
    Text(
      text = categoryName,
      style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
      color = UltimateNavy
    )
    Spacer(modifier = Modifier.height(6.dp))
    permissionList.forEach { perm ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onToggle(perm) }
          .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Checkbox(
          checked = activePermissions.contains(perm),
          onCheckedChange = { onToggle(perm) }
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = perm, style = MaterialTheme.typography.bodySmall)
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddStaffDialog(
  viewModel: AdminViewModel,
  onDismiss: () -> Unit
) {
  val currentAdmin by viewModel.currentAdmin.collectAsState()
  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var department by remember { mutableStateOf("Support") }
  var role by remember { mutableStateOf(AdminRole.SUPPORT) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val isSuperAdmin = currentAdmin?.role == AdminRole.SUPER_ADMIN

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Register New Admin User") },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = StatusDangerText,
            style = MaterialTheme.typography.bodySmall
          )
          Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Full Name") },
          placeholder = { Text("e.g. Rahul Sharma") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("Company Email") },
          placeholder = { Text("name@ultimate.com") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number") },
          placeholder = { Text("+91 98765 43210") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = department,
          onValueChange = { department = it },
          label = { Text("Department") },
          placeholder = { Text("Support, Operations, Finance") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Select Role:", style = MaterialTheme.typography.titleSmall)
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          AdminRole.entries.forEach { r ->
            FilterChip(
              selected = role == r,
              onClick = { role = r },
              label = { Text(r.displayName, fontSize = 11.sp) }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank() || email.isBlank()) {
            errorMessage = "Name and email are required."
            return@Button
          }
          if (role == AdminRole.SUPER_ADMIN && !isSuperAdmin) {
            errorMessage = "Only an existing Super Admin can create another Super Admin account."
            return@Button
          }
          val defaultPerms = when (role) {
            AdminRole.SUPER_ADMIN -> AppPermissions.superAdminPermissions
            AdminRole.ADMIN -> AppPermissions.adminDefaultPermissions
            AdminRole.MANAGER -> AppPermissions.managerDefaultPermissions
            AdminRole.SUPPORT -> AppPermissions.supportDefaultPermissions
            AdminRole.FINANCE -> AppPermissions.financeDefaultPermissions
            AdminRole.OPERATIONS -> AppPermissions.operationsDefaultPermissions
          }
          viewModel.addStaff(
            name = name,
            email = email,
            phone = phone,
            role = role,
            department = department,
            permissions = defaultPerms,
            managerName = "${currentAdmin?.name ?: "Super Admin"} (${currentAdmin?.role?.displayName ?: "Super Admin"})"
          )
          onDismiss()
        },
        colors = ButtonDefaults.buttonColors(containerColor = UltimateEmerald)
      ) {
        Text("Create & Provision Admin")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
