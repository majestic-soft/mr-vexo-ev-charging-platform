package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.model.AdminRole
import com.example.data.model.AppPermissions
import com.example.ui.components.AccessRestrictedScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.bookings.BookingDetailScreen
import com.example.ui.screens.bookings.BookingListScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.finance.FinanceHubScreen
import com.example.ui.screens.security.CyberSecurityScreen
import com.example.ui.screens.settings.*
import com.example.ui.screens.staff.*
import com.example.ui.screens.stations.StationDetailScreen
import com.example.ui.screens.stations.StationListScreen
import com.example.ui.screens.support.DisputeDetailScreen
import com.example.ui.screens.support.SupportHubScreen
import com.example.ui.screens.support.TicketDetailScreen
import com.example.ui.screens.users.CustomerDetailScreen
import com.example.ui.screens.users.HostDetailScreen
import com.example.ui.screens.users.UsersHubScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      UltimateAdminTheme {
        val viewModel: AdminViewModel = viewModel()
        val settings by viewModel.systemSettings.collectAsState()

        // Unhackable Screen Privacy Shield: Dynamic FLAG_SECURE management
        LaunchedEffect(settings.screenCaptureShieldEnabled) {
          if (settings.screenCaptureShieldEnabled) {
            window.setFlags(
              WindowManager.LayoutParams.FLAG_SECURE,
              WindowManager.LayoutParams.FLAG_SECURE
            )
          } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
          }
        }

        AdminAppRoot(viewModel = viewModel)
      }
    }
  }
}

sealed class AdminScreen(val route: String, val title: String, val icon: ImageVector) {
  object Dashboard : AdminScreen("dashboard", "Overview", Icons.Outlined.Dashboard)
  object Staff : AdminScreen("staff", "Staff & RBAC", Icons.Outlined.Badge)
  object Users : AdminScreen("users", "Users & Hosts", Icons.Outlined.People)
  object Stations : AdminScreen("stations", "Stations", Icons.Outlined.EvStation)
  object Finance : AdminScreen("finance", "Finance", Icons.Outlined.AccountBalanceWallet)
  object Support : AdminScreen("support", "Support Queue", Icons.Outlined.SupportAgent)
  object Bookings : AdminScreen("bookings", "Bookings", Icons.Outlined.ConfirmationNumber)
  object Notifications : AdminScreen("notifications", "Alerts", Icons.Outlined.Notifications)
  object Settings : AdminScreen("settings", "Settings", Icons.Outlined.Settings)
}

@Composable
fun AdminAppRoot(viewModel: AdminViewModel) {
  val currentAdmin by viewModel.currentAdmin.collectAsState()
  val isAppLocked by viewModel.isAppLocked.collectAsState()

  // If not logged in, show the Login Screen
  if (currentAdmin == null) {
    LoginScreen(viewModel = viewModel)
    return
  }

  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  var showAddStaffDialog by remember { mutableStateOf(false) }
  var editingStaffId by remember { mutableStateOf<String?>(null) }
  var showRoleSwitcherDialog by remember { mutableStateOf(false) }

  // Dynamically compute Bottom Navigation Items strictly by Role
  val bottomNavItems = remember(currentAdmin?.role) {
    when (currentAdmin?.role) {
      AdminRole.SUPER_ADMIN, AdminRole.ADMIN -> listOf(
        AdminScreen.Dashboard,
        AdminScreen.Staff,
        AdminScreen.Users,
        AdminScreen.Stations,
        AdminScreen.Finance
      )
      AdminRole.MANAGER -> listOf(
        AdminScreen.Dashboard,
        AdminScreen.Support,
        AdminScreen.Staff,
        AdminScreen.Stations,
        AdminScreen.Bookings
      )
      AdminRole.SUPPORT -> listOf(
        AdminScreen.Dashboard,
        AdminScreen.Support,
        AdminScreen.Bookings,
        AdminScreen.Notifications
      )
      AdminRole.FINANCE -> listOf(
        AdminScreen.Dashboard,
        AdminScreen.Finance,
        AdminScreen.Bookings,
        AdminScreen.Notifications
      )
      AdminRole.OPERATIONS -> listOf(
        AdminScreen.Dashboard,
        AdminScreen.Stations,
        AdminScreen.Bookings,
        AdminScreen.Support
      )
      null -> listOf(AdminScreen.Dashboard)
    }
  }

  val isTopLevelDestination = bottomNavItems.any { it.route == currentRoute }

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      containerColor = UltimateBackground,
      contentWindowInsets = WindowInsets(0.dp),
      topBar = {
        // Persistent Top Role & Security Bar
        Surface(
          color = Color.White,
          border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .statusBarsPadding()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable { showRoleSwitcherDialog = true }
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(UltimateGreenBg),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = currentAdmin?.name?.take(1) ?: "A",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = UltimateEmerald
                )
              }

              Spacer(modifier = Modifier.width(10.dp))

              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = currentAdmin?.name ?: "Staff Admin",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = UltimateNavy
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(Icons.Filled.ArrowDropDown, contentDescription = "Switch Account", tint = UltimateSlate500, modifier = Modifier.size(16.dp))
                }
                Text(
                  text = "${currentAdmin?.role?.displayName ?: ""} • ${currentAdmin?.department ?: ""}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = UltimateEmerald
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              // Cyber Security Shield Quick Access
              IconButton(
                onClick = { navController.navigate("cyber_security") },
                modifier = Modifier.size(32.dp).testTag("btn_cyber_shield_topbar")
              ) {
                Icon(
                  imageVector = Icons.Filled.Shield,
                  contentDescription = "Cyber Security",
                  tint = Color(0xFF10B981),
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(4.dp))

              // Switch role chip
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = UltimateSlate100,
                onClick = { showRoleSwitcherDialog = true },
                modifier = Modifier.testTag("btn_switch_role_topbar")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Outlined.SwapHoriz, contentDescription = null, tint = UltimateSlate700, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Role Switch", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = UltimateSlate700)
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              // Logout Button
              IconButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.size(32.dp).testTag("btn_logout")
              ) {
                Icon(Icons.Outlined.Logout, contentDescription = "Logout", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
              }
            }
          }
        }
      },
      bottomBar = {
        if (isTopLevelDestination) {
          Surface(
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder)
          ) {
            NavigationBar(
              containerColor = Color.White,
              tonalElevation = 0.dp,
              modifier = Modifier.navigationBarsPadding()
            ) {
              bottomNavItems.forEach { screen ->
                val selected = currentRoute == screen.route
                NavigationBarItem(
                  selected = selected,
                  onClick = {
                    navController.navigate(screen.route) {
                      popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                      }
                      launchSingleTop = true
                      restoreState = true
                    }
                  },
                  icon = {
                    Icon(
                      imageVector = screen.icon,
                      contentDescription = screen.title
                    )
                  },
                  label = {
                    Text(
                      text = screen.title,
                      fontSize = 10.sp,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                  },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = UltimateEmerald,
                    selectedTextColor = UltimateEmerald,
                    unselectedIconColor = UltimateSlate400,
                    unselectedTextColor = UltimateSlate400,
                    indicatorColor = UltimateGreenBg
                  ),
                  modifier = Modifier.testTag("nav_tab_${screen.route}")
                )
              }
            }
          }
        }
      }
    ) { innerPadding ->
      NavHost(
        navController = navController,
        startDestination = AdminScreen.Dashboard.route,
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        // Staff Dashboard Screen (Gated to Super Admin, Admin, Manager)
        composable(AdminScreen.Staff.route) {
          if (viewModel.hasPermission(AppPermissions.VIEW_STAFF)) {
            StaffListScreen(
              viewModel = viewModel,
              onStaffClick = { staffId -> navController.navigate("staff_detail/$staffId") },
              onAddStaffClick = { showAddStaffDialog = true },
              onRolesPermissionsClick = { navController.navigate("roles_matrix") },
              onDepartmentsClick = { navController.navigate("departments") },
              onActivityLogsClick = { navController.navigate("activity_logs") }
            )
          } else {
            AccessRestrictedScreen(
              requiredPermission = "Staff & RBAC Administration",
              onBackClick = { navController.navigate(AdminScreen.Dashboard.route) }
            )
          }
        }

        // Dashboard Screen
        composable(AdminScreen.Dashboard.route) {
          DashboardScreen(
            viewModel = viewModel,
            onNavigateToUsers = { navController.navigate(AdminScreen.Users.route) },
            onNavigateToHosts = { navController.navigate(AdminScreen.Users.route) },
            onNavigateToStations = { navController.navigate(AdminScreen.Stations.route) },
            onNavigateToBookings = { navController.navigate("bookings") },
            onNavigateToDisputes = { navController.navigate(AdminScreen.Support.route) },
            onNavigateToSupport = { navController.navigate(AdminScreen.Support.route) },
            onNavigateToWithdrawals = { navController.navigate(AdminScreen.Finance.route) },
            onNavigateToStaff = { navController.navigate(AdminScreen.Staff.route) },
            onNotificationsClick = { navController.navigate("notifications") },
            onSearchClick = { navController.navigate(AdminScreen.Staff.route) }
          )
        }

        // Users & Hosts Screen (Gated)
        composable(AdminScreen.Users.route) {
          if (viewModel.hasPermission(AppPermissions.VIEW_CUSTOMERS) || viewModel.hasPermission(AppPermissions.VIEW_HOSTS)) {
            UsersHubScreen(
              viewModel = viewModel,
              onCustomerClick = { customerId -> navController.navigate("customer_detail/$customerId") },
              onHostClick = { hostId -> navController.navigate("host_detail/$hostId") },
              onBackClick = { navController.popBackStack() }
            )
          } else {
            AccessRestrictedScreen(
              requiredPermission = "View Users & Hosts",
              onBackClick = { navController.navigate(AdminScreen.Dashboard.route) }
            )
          }
        }

        // Stations Screen (Gated)
        composable(AdminScreen.Stations.route) {
          if (viewModel.hasPermission(AppPermissions.VIEW_STATIONS)) {
            StationListScreen(
              viewModel = viewModel,
              onStationClick = { stationId -> navController.navigate("station_detail/$stationId") }
            )
          } else {
            AccessRestrictedScreen(
              requiredPermission = "View Charging Stations",
              onBackClick = { navController.navigate(AdminScreen.Dashboard.route) }
            )
          }
        }

        // Finance Screen (Gated)
        composable(AdminScreen.Finance.route) {
          if (viewModel.hasPermission(AppPermissions.VIEW_PAYMENTS) || viewModel.hasPermission(AppPermissions.MANAGE_PROMOS) || viewModel.hasPermission(AppPermissions.MANAGE_WITHDRAWALS)) {
            FinanceHubScreen(
              viewModel = viewModel,
              onBackClick = { navController.popBackStack() }
            )
          } else {
            AccessRestrictedScreen(
              requiredPermission = "Financial & Payout Hub",
              onBackClick = { navController.navigate(AdminScreen.Dashboard.route) }
            )
          }
        }

        // Support Hub Screen (Accessible to Support, Manager, Operations, Admin)
        composable(AdminScreen.Support.route) {
          if (viewModel.hasPermission(AppPermissions.VIEW_SUPPORT_TICKETS) || viewModel.hasPermission(AppPermissions.VIEW_DISPUTES)) {
            SupportHubScreen(
              viewModel = viewModel,
              onDisputeClick = { disputeId -> navController.navigate("dispute_detail/$disputeId") },
              onTicketClick = { ticketId -> navController.navigate("ticket_detail/$ticketId") }
            )
          } else {
            AccessRestrictedScreen(
              requiredPermission = "Support Tickets & Disputes",
              onBackClick = { navController.navigate(AdminScreen.Dashboard.route) }
            )
          }
        }

        // Settings Screen
        composable(AdminScreen.Settings.route) {
          if (viewModel.hasPermission(AppPermissions.SYSTEM_SETTINGS)) {
            SettingsHubScreen(
              viewModel = viewModel,
              onNavigateToRolesMatrix = { navController.navigate("roles_matrix") },
              onNavigateToDepartments = { navController.navigate("departments") },
              onNavigateToActivityLogs = { navController.navigate("activity_logs") },
              onNavigateToCyberSecurity = { navController.navigate("cyber_security") },
              onBackClick = { navController.popBackStack() }
            )
          } else {
            AccessRestrictedScreen(
              requiredPermission = "System Administration",
              onBackClick = { navController.navigate(AdminScreen.Dashboard.route) }
            )
          }
        }

        // Cyber Security Defense & SOC Screen (Unhackable Platform Armor)
        composable("cyber_security") {
          CyberSecurityScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
          )
        }

        // Bookings Screen
        composable("bookings") {
          if (viewModel.hasPermission(AppPermissions.VIEW_BOOKINGS)) {
            BookingListScreen(
              viewModel = viewModel,
              onBookingClick = { bookingId -> navController.navigate("booking_detail/$bookingId") }
            )
          } else {
            AccessRestrictedScreen(
              requiredPermission = "View Bookings",
              onBackClick = { navController.navigate(AdminScreen.Dashboard.route) }
            )
          }
        }

        // Notifications Screen
        composable("notifications") {
          NotificationsScreen(
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() }
          )
        }

        // Roles & Permissions Matrix (Super Admin only)
        composable("roles_matrix") {
          if (viewModel.hasPermission(AppPermissions.MANAGE_ROLES_PERMISSIONS)) {
            RolesAndPermissionsMatrixScreen(
              viewModel = viewModel,
              onBackClick = { navController.popBackStack() }
            )
          } else {
            AccessRestrictedScreen(
              requiredPermission = "Manage Roles & Permissions Matrix",
              onBackClick = { navController.popBackStack() }
            )
          }
        }

        // Departments Screen
        composable("departments") {
          if (viewModel.hasPermission(AppPermissions.VIEW_STAFF)) {
            DepartmentsScreen(
              viewModel = viewModel,
              onBackClick = { navController.popBackStack() }
            )
          } else {
            AccessRestrictedScreen(
              requiredPermission = "View Departments",
              onBackClick = { navController.popBackStack() }
            )
          }
        }

        // Activity Logs Screen
        composable("activity_logs") {
          if (viewModel.hasPermission(AppPermissions.SYSTEM_SETTINGS) || viewModel.hasPermission(AppPermissions.VIEW_STAFF)) {
            ActivityLogsScreen(
              viewModel = viewModel,
              onBackClick = { navController.popBackStack() }
            )
          } else {
            AccessRestrictedScreen(
              requiredPermission = "View System Activity Logs",
              onBackClick = { navController.popBackStack() }
            )
          }
        }

        // Staff Detail Screen
        composable(
          route = "staff_detail/{staffId}",
          arguments = listOf(navArgument("staffId") { type = NavType.StringType })
        ) { backStackEntry ->
          val staffId = backStackEntry.arguments?.getString("staffId") ?: "stf_1001"
          if (viewModel.hasPermission(AppPermissions.VIEW_STAFF)) {
            StaffDetailScreen(
              staffId = staffId,
              viewModel = viewModel,
              onBackClick = { navController.popBackStack() },
              onEditPermissionsClick = { id -> editingStaffId = id }
            )
          } else {
            AccessRestrictedScreen(
              requiredPermission = "View Staff Profile",
              onBackClick = { navController.popBackStack() }
            )
          }
        }

        // Customer Detail Screen
        composable(
          route = "customer_detail/{customerId}",
          arguments = listOf(navArgument("customerId") { type = NavType.StringType })
        ) { backStackEntry ->
          val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
          CustomerDetailScreen(
            customerId = customerId,
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() }
          )
        }

        // Host Detail Screen
        composable(
          route = "host_detail/{hostId}",
          arguments = listOf(navArgument("hostId") { type = NavType.StringType })
        ) { backStackEntry ->
          val hostId = backStackEntry.arguments?.getString("hostId") ?: ""
          HostDetailScreen(
            hostId = hostId,
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() }
          )
        }

        // Station Detail Screen
        composable(
          route = "station_detail/{stationId}",
          arguments = listOf(navArgument("stationId") { type = NavType.StringType })
        ) { backStackEntry ->
          val stationId = backStackEntry.arguments?.getString("stationId") ?: ""
          StationDetailScreen(
            stationId = stationId,
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() }
          )
        }

        // Booking Detail Screen
        composable(
          route = "booking_detail/{bookingId}",
          arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
        ) { backStackEntry ->
          val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
          BookingDetailScreen(
            bookingId = bookingId,
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() }
          )
        }

        // Dispute Detail Screen
        composable(
          route = "dispute_detail/{disputeId}",
          arguments = listOf(navArgument("disputeId") { type = NavType.StringType })
        ) { backStackEntry ->
          val disputeId = backStackEntry.arguments?.getString("disputeId") ?: ""
          DisputeDetailScreen(
            disputeId = disputeId,
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() }
          )
        }

        // Ticket Detail Screen
        composable(
          route = "ticket_detail/{ticketId}",
          arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
          val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
          TicketDetailScreen(
            ticketId = ticketId,
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() }
          )
        }
      }
    }

    // Role Switcher Dialog for rapid testing of all 6 roles
    if (showRoleSwitcherDialog) {
      AlertDialog(
        onDismissRequest = { showRoleSwitcherDialog = false },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = UltimateEmerald)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Switch Role (Test Accounts)")
          }
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Select an admin role to instantly verify RBAC isolation & navigation:", fontSize = 12.sp, color = UltimateSlate600)
            AdminRole.entries.forEach { r ->
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (currentAdmin?.role == r) UltimateGreenBg else Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (currentAdmin?.role == r) UltimateEmerald else UltimateSurfaceBorder
                ),
                onClick = {
                  viewModel.loginAsRole(r)
                  showRoleSwitcherDialog = false
                },
                modifier = Modifier.fillMaxWidth().testTag("switch_to_role_${r.name.lowercase()}")
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = r.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (currentAdmin?.role == r) UltimateEmerald else UltimateNavy,
                    modifier = Modifier.weight(1f)
                  )
                  if (currentAdmin?.role == r) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Active", tint = UltimateEmerald, modifier = Modifier.size(16.dp))
                  }
                }
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showRoleSwitcherDialog = false }) { Text("Close") }
        }
      )
    }

    // Modal dialogs
    if (showAddStaffDialog) {
      AddStaffDialog(
        viewModel = viewModel,
        onDismiss = { showAddStaffDialog = false }
      )
    }

    if (editingStaffId != null) {
      EditStaffPermissionsDialog(
        staffId = editingStaffId!!,
        viewModel = viewModel,
        onDismiss = { editingStaffId = null }
      )
    }

    // App Lock Overlay
    if (isAppLocked) {
      AppLockOverlayScreen(viewModel = viewModel)
    }
  }
}
