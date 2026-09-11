package com.example.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppPermissions
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun DashboardScreen(
  viewModel: AdminViewModel,
  onNavigateToUsers: () -> Unit,
  onNavigateToHosts: () -> Unit,
  onNavigateToStations: () -> Unit,
  onNavigateToBookings: () -> Unit,
  onNavigateToDisputes: () -> Unit,
  onNavigateToSupport: () -> Unit,
  onNavigateToWithdrawals: () -> Unit,
  onNavigateToStaff: () -> Unit,
  onNotificationsClick: () -> Unit,
  onSearchClick: () -> Unit
) {
  val currentAdmin by viewModel.currentAdmin.collectAsState()
  val canViewRevenue by viewModel.canViewRevenue.collectAsState()
  val metrics by viewModel.metrics.collectAsState()
  val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsState()
  val unreadNotifs by viewModel.unreadNotificationsCount.collectAsState()
  val isLiveSimActive by viewModel.isLiveSimulationActive.collectAsState()

  var showTimeFilterDialog by remember { mutableStateOf(false) }

  Scaffold(
    containerColor = UltimateBackground,
    topBar = {
      AdminHeaderTopBar(
        unreadNotificationCount = unreadNotifs,
        onMenuClick = onNavigateToStaff,
        onNotificationClick = onNotificationsClick,
        onSearchClick = onSearchClick
      )
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(bottom = 24.dp)
    ) {
      // Welcome user banner
      item {
        AdminUserWelcomeHeader(
          currentAdmin = currentAdmin,
          onSwitchRoleClick = onNavigateToStaff
        )
      }

      // Live Enterprise Telemetry & Real-Time Production Fleet Operations Bar
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("production_live_telemetry_banner")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "LIVE PRODUCTION NETWORK",
                  style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                  ),
                  color = Color(0xFF34D399)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Color(0xFF1E293B)
                ) {
                  Text(
                    text = "OCPP 2.0.1",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF10B981).copy(alpha = 0.2f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "99.9% Uptime",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "${metrics.activeChargingSessions} Fast-Charging Bays actively dispensing ~${String.format("%.1f", metrics.totalEnergyDeliveredTodayKwh)} kWh across ${metrics.activeChargingStations} stations in real-time.",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
              color = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Production Grid Highlights
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text("Grid Latency", fontSize = 9.sp, color = Color(0xFF94A3B8))
                    Text("18ms (Live)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                  }
                }
              }

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text("Cluster", fontSize = 9.sp, color = Color(0xFF94A3B8))
                    Text("asia-south1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                  }
                }
              }

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text("Cyber Armor", fontSize = 9.sp, color = Color(0xFF94A3B8))
                    Text("A+ Armed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Live Fleet Action Controls
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              FilledTonalButton(
                onClick = { viewModel.refreshLiveFleetData() },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                  containerColor = Color(0xFF334155),
                  contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(34.dp)
              ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sync Live Telemetry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              OutlinedButton(
                onClick = onNavigateToStations,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(34.dp)
              ) {
                Icon(Icons.Default.EvStation, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View 389 Chargers", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Top 2-Column KPI Cards (Sleek Interface)
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Card 1: Today's Revenue (Super Admin & Admin Only) vs Energy Delivered (Other Roles)
          if (canViewRevenue) {
            Surface(
              color = Color.White,
              shape = RoundedCornerShape(18.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
              modifier = Modifier
                .weight(1f)
                .clickable { onNavigateToWithdrawals() }
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "TODAY'S REVENUE",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                  ),
                  color = UltimateSlate500
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "₹${String.format("%,.2f", metrics.todayRevenue)}",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                  ),
                  color = UltimateNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(UltimateGreenBg.copy(alpha = 0.8f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = UltimateEmerald,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "+12.4%",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = UltimateEmerald
                  )
                }
              }
            }
          } else {
            Surface(
              color = Color.White,
              shape = RoundedCornerShape(18.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
              modifier = Modifier
                .weight(1f)
                .clickable { onNavigateToStations() }
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "ENERGY DELIVERED",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                  ),
                  color = UltimateSlate500
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "${String.format("%.1f", metrics.totalEnergyDeliveredTodayKwh)} kWh",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                  ),
                  color = UltimateNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(UltimateGreenBg.copy(alpha = 0.8f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = UltimateEmerald,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "Clean Energy Today",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = UltimateEmerald
                  )
                }
              }
            }
          }

          // Card 2: Active Sessions
          Surface(
            color = Color.White,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
            modifier = Modifier
              .weight(1f)
              .clickable { onNavigateToStations() }
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "ACTIVE SESSIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.6.sp
                ),
                color = UltimateSlate500
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "${metrics.activeChargingSessions}",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp
                ),
                color = UltimateNavy
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "across ${metrics.activeChargingStations} stations",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = UltimateSlate500
              )
            }
          }
        }
      }

      // Live Activity Card (Sleek Interface Bar Chart)
      item {
        Surface(
          color = Color.White,
          shape = RoundedCornerShape(18.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Live Activity",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = UltimateNavy
              )

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(50.dp))
                  .background(UltimateSlate100)
                  .padding(horizontal = 10.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "Last 24h",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  color = UltimateSlate600
                )
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sleek Bar Heights
            val barFractions = listOf(0.40f, 0.65f, 0.45f, 0.90f, 0.55f, 0.75f, 0.40f)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .padding(horizontal = 8.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.Bottom
            ) {
              barFractions.forEachIndexed { index, fraction ->
                val isPeak = index == 3
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                    .background(
                      if (isPeak) UltimateEmerald else Color(0xFFDCFCE7)
                    )
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time markers
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              listOf("08:00", "12:00", "16:00", "20:00").forEach { time ->
                Text(
                  text = time,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                  ),
                  color = UltimateSlate400
                )
              }
            }
          }
        }
      }

      // Sleek Dark "Alerts Center" Card (#1E293B)
      item {
        val urgentCount = metrics.pendingHostApprovals + metrics.openDisputes + metrics.openSupportTickets
        Surface(
          color = UltimateSlate800,
          shape = RoundedCornerShape(18.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top
            ) {
              Column {
                Text(
                  text = "ALERTS CENTER",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                  ),
                  color = UltimateSlate400
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Action Required",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                  ),
                  color = Color.White
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFFEF4444))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "$urgentCount Urgent",
                  color = Color.White,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Alert Rows inside Dark Card
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              // Item 1: Host Approvals
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.White.copy(alpha = 0.06f))
                  .clickable { onNavigateToHosts() }
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(UltimateEmerald.copy(alpha = 0.25f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = UltimateEmerald,
                    modifier = Modifier.size(16.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${metrics.pendingHostApprovals} New Host Approvals",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 12.sp
                    ),
                    color = Color.White
                  )
                  Text(
                    text = "Verification pending",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = UltimateSlate400
                  )
                }

                Icon(
                  imageVector = Icons.Default.ChevronRight,
                  contentDescription = null,
                  tint = UltimateSlate500,
                  modifier = Modifier.size(16.dp)
                )
              }

              // Item 2: Support Tickets & Disputes
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.White.copy(alpha = 0.06f))
                  .clickable { onNavigateToSupport() }
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF59E0B).copy(alpha = 0.25f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(16.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${metrics.openSupportTickets} Open Support Tickets",
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 12.sp
                    ),
                    color = Color.White
                  )
                  Text(
                    text = "High priority response needed",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = UltimateSlate400
                  )
                }

                Icon(
                  imageVector = Icons.Default.ChevronRight,
                  contentDescription = null,
                  tint = UltimateSlate500,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }

      // Key Metrics 2x2 Grid
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
          Text(
            text = "Marketplace Health",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = UltimateNavy
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            MetricTile(
              modifier = Modifier.weight(1f),
              title = "Total Users",
              value = metrics.totalCustomers.toString(),
              change = "+24 today",
              icon = Icons.Outlined.People,
              iconBg = Color(0xFFE3F2FD),
              iconTint = Color(0xFF1565C0),
              onClick = onNavigateToUsers
            )

            MetricTile(
              modifier = Modifier.weight(1f),
              title = "Active Hosts",
              value = metrics.activeHosts.toString(),
              change = "${metrics.pendingHostApprovals} pending",
              icon = Icons.Outlined.Business,
              iconBg = Color(0xFFFFF3E0),
              iconTint = Color(0xFFE65100),
              onClick = onNavigateToHosts
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            MetricTile(
              modifier = Modifier.weight(1f),
              title = "Stations Live",
              value = metrics.activeChargingStations.toString(),
              change = "98.2% Uptime",
              icon = Icons.Outlined.EvStation,
              iconBg = Color(0xFFE8F5E9),
              iconTint = Color(0xFF2E7D32),
              onClick = onNavigateToStations
            )

            MetricTile(
              modifier = Modifier.weight(1f),
              title = "Today's Bookings",
              value = metrics.todayBookings.toString(),
              change = "↑ 12% vs avg",
              icon = Icons.Outlined.EventNote,
              iconBg = Color(0xFFEDE7F6),
              iconTint = Color(0xFF6A1B9A),
              onClick = onNavigateToBookings
            )
          }
        }
      }

      // Interactive Trend Charts
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Charging Sessions Trend",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = UltimateNavy
                )
                Text(
                  text = "Live hourly throughput across cities",
                  style = MaterialTheme.typography.bodySmall,
                  color = UltimateSlate500
                )
              }

              Icon(
                imageVector = Icons.Outlined.ShowChart,
                contentDescription = null,
                tint = UltimateEmerald,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Custom Smooth Line Canvas
            Canvas(
              modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
            ) {
              val points = listOf(18f, 32f, 25f, 48f, 42f, 65f, 58f, 85f, 78f, 95f, 88f, 110f)
              val maxVal = points.maxOrNull() ?: 120f
              val stepX = size.width / (points.size - 1)
              val path = Path()
              val fillPath = Path()

              points.forEachIndexed { index, value ->
                val x = index * stepX
                val y = size.height - (value / maxVal) * (size.height * 0.8f) - 10f
                if (index == 0) {
                  path.moveTo(x, y)
                  fillPath.moveTo(x, size.height)
                  fillPath.lineTo(x, y)
                } else {
                  path.lineTo(x, y)
                  fillPath.lineTo(x, y)
                }
              }

              fillPath.lineTo(size.width, size.height)
              fillPath.close()

              drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                  colors = listOf(
                    UltimateEmerald.copy(alpha = 0.25f),
                    UltimateEmerald.copy(alpha = 0.01f)
                  )
                )
              )

              drawPath(
                path = path,
                color = UltimateEmerald,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
              )

              points.forEachIndexed { index, value ->
                val x = index * stepX
                val y = size.height - (value / maxVal) * (size.height * 0.8f) - 10f
                drawCircle(
                  color = UltimateEmerald,
                  radius = 3.5.dp.toPx(),
                  center = Offset(x, y)
                )
                drawCircle(
                  color = Color.White,
                  radius = 2.dp.toPx(),
                  center = Offset(x, y)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              listOf("06:00", "09:00", "12:00", "15:00", "18:00", "21:00").forEach { hour ->
                Text(
                  text = hour,
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                  color = UltimateSlate500
                )
              }
            }
          }
        }
      }

      // Quick Module Directory
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
          Text(
            text = "Enterprise Modules",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = UltimateNavy
          )
          Spacer(modifier = Modifier.height(10.dp))

          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              ModuleNavRow(
                icon = Icons.Outlined.People,
                title = "Customers & Users",
                subtitle = "${metrics.totalCustomers} registered accounts",
                onClick = onNavigateToUsers
              )
              HorizontalDivider(color = UltimateSurfaceBorder.copy(alpha = 0.5f))
              ModuleNavRow(
                icon = Icons.Outlined.Business,
                title = "Host Network",
                subtitle = "${metrics.activeHosts} hosts, ${metrics.pendingHostApprovals} approvals",
                onClick = onNavigateToHosts
              )
              HorizontalDivider(color = UltimateSurfaceBorder.copy(alpha = 0.5f))
              ModuleNavRow(
                icon = Icons.Outlined.EvStation,
                title = "Charging Stations",
                subtitle = "${metrics.activeChargingStations} stations, 480 slots",
                onClick = onNavigateToStations
              )
              HorizontalDivider(color = UltimateSurfaceBorder.copy(alpha = 0.5f))
              ModuleNavRow(
                icon = Icons.Outlined.EventNote,
                title = "Bookings & Sessions",
                subtitle = "${metrics.todayBookings} bookings today",
                onClick = onNavigateToBookings
              )
              HorizontalDivider(color = UltimateSurfaceBorder.copy(alpha = 0.5f))
              ModuleNavRow(
                icon = Icons.Outlined.HelpOutline,
                title = "Disputes & Claims",
                subtitle = "${metrics.openDisputes} open dispute cases",
                onClick = onNavigateToDisputes
              )
              HorizontalDivider(color = UltimateSurfaceBorder.copy(alpha = 0.5f))
              ModuleNavRow(
                icon = Icons.Outlined.SupportAgent,
                title = "Support Tickets",
                subtitle = "${metrics.openSupportTickets} unresolved tickets",
                onClick = onNavigateToSupport
              )
            }
          }
        }
      }
    }
  }

  // Time filter dialog
  if (showTimeFilterDialog) {
    AlertDialog(
      onDismissRequest = { showTimeFilterDialog = false },
      title = { Text("Select Date Range") },
      text = {
        Column {
          listOf("Today", "Last 7 Days", "This Month", "Last 30 Days", "Custom Range").forEach { filter ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  viewModel.setTimeFilter(filter)
                  showTimeFilterDialog = false
                }
                .padding(vertical = 10.dp),
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
}

@Composable
fun ActionAlertChip(
  label: String,
  badgeColor: Color,
  textColor: Color,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(badgeColor)
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(textColor)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      color = textColor,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
    )
  }
}

@Composable
fun MetricTile(
  modifier: Modifier = Modifier,
  title: String,
  value: String,
  change: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconBg: Color,
  iconTint: Color,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
    modifier = modifier.clickable { onClick() }
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(iconBg),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
          )
        }

        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = UltimateSlate300,
          modifier = Modifier.size(16.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = title,
        style = MaterialTheme.typography.bodySmall,
        color = UltimateSlate500
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = UltimateNavy
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = change,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
        color = UltimateEmerald
      )
    }
  }
}

