package com.example.ui.screens.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.screens.staff.StaffInfoRow
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun UsersHubScreen(
  viewModel: AdminViewModel,
  onCustomerClick: (String) -> Unit,
  onHostClick: (String) -> Unit,
  onBackClick: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Customers", "Host Network")

  Scaffold(
    containerColor = UltimateBackground,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(UltimateCardBg)
          .statusBarsPadding()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "User Directory",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = UltimateNavy,
            modifier = Modifier.weight(1f)
          )
        }

        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = UltimateCardBg,
          contentColor = UltimateEmerald,
          divider = { HorizontalDivider(color = UltimateSurfaceBorder) }
        ) {
          tabs.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = {
                Text(
                  text = title,
                  style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                  )
                )
              }
            )
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (selectedTab == 0) {
        CustomerListContent(viewModel = viewModel, onCustomerClick = onCustomerClick)
      } else {
        HostListContent(viewModel = viewModel, onHostClick = onHostClick)
      }
    }
  }
}

@Composable
fun CustomerListContent(
  viewModel: AdminViewModel,
  onCustomerClick: (String) -> Unit
) {
  val customers by viewModel.customers.collectAsState()
  val query by viewModel.customerSearchQuery.collectAsState()

  val filteredCustomers = remember(customers, query) {
    if (query.isBlank()) customers else {
      customers.filter {
        it.name.contains(query, ignoreCase = true) ||
          it.email.contains(query, ignoreCase = true) ||
          it.phone.contains(query, ignoreCase = true) ||
          it.customerCode.contains(query, ignoreCase = true)
      }
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    item {
      EnterpriseSearchField(
        query = query,
        onQueryChange = { viewModel.setCustomerSearchQuery(it) },
        placeholderText = "Search customers by name, phone, email...",
        modifier = Modifier.padding(vertical = 8.dp)
      )
    }

    if (filteredCustomers.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("No customers found.", color = UltimateSlate500)
        }
      }
    } else {
      items(filteredCustomers, key = { it.id }) { customer ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onCustomerClick(customer.id) }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = customer.name.take(1),
                color = Color(0xFF1565C0),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = customer.name,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = UltimateNavy
                )
                Spacer(modifier = Modifier.width(6.dp))
                StatusBadge(status = customer.status)
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${customer.customerCode} • Joined ${customer.registrationDate}",
                style = MaterialTheme.typography.bodySmall,
                color = UltimateSlate500
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${customer.totalBookings} Bookings • ₹${customer.walletBalance} Wallet",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = UltimateEmerald
              )
            }

            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = UltimateSlate300,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun CustomerDetailScreen(
  customerId: String,
  viewModel: AdminViewModel,
  onBackClick: () -> Unit
) {
  val customers by viewModel.customers.collectAsState()
  val customer = customers.find { it.id == customerId } ?: customers.firstOrNull()
  val bookings by viewModel.bookings.collectAsState()
  val customerBookings = remember(bookings, customerId) {
    bookings.filter { it.customerId == customerId }
  }

  var showSuspendDialog by remember { mutableStateOf(false) }

  if (customer == null) {
    AccessRestrictedScreen(requiredPermission = "View Users", onBackClick = onBackClick)
    return
  }

  Scaffold(
    containerColor = UltimateBackground,
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(UltimateCardBg)
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBackClick) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = UltimateNavy)
        }
        Text(
          text = "Customer Profile",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = UltimateNavy,
          modifier = Modifier.weight(1f)
        )
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
      // Profile card
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
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = customer.name.take(1),
                color = Color(0xFF1565C0),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = customer.name,
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                  color = UltimateNavy
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = customer.status)
              }
              Text(customer.customerCode, style = MaterialTheme.typography.bodySmall, color = UltimateSlate500)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = UltimateSurfaceBorder)
          Spacer(modifier = Modifier.height(14.dp))

          StaffInfoRow(icon = Icons.Outlined.Mail, text = customer.email)
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.Phone, text = customer.phone)
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.CalendarToday, label = "Registered On", value = customer.registrationDate)
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.VerifiedUser, label = "KYC Verification", value = if (customer.kycVerified) "Verified (Aadhaar & DL)" else "Pending Documents")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Stats 3-tile Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        CustomerStatCard(modifier = Modifier.weight(1f), label = "Wallet Balance", value = "₹${customer.walletBalance}")
        CustomerStatCard(modifier = Modifier.weight(1f), label = "Total Spent", value = "₹${customer.totalSpent}")
        CustomerStatCard(modifier = Modifier.weight(1f), label = "Energy Consumed", value = "${customer.totalEnergyConsumedKwh} kWh")
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Booking history list
      Text("Recent Bookings (${customerBookings.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
      Spacer(modifier = Modifier.height(8.dp))

      if (customerBookings.isEmpty()) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
            Text("No recent charging sessions for this user.", color = UltimateSlate500)
          }
        }
      } else {
        customerBookings.forEach { booking ->
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(booking.stationName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
                StatusBadge(status = booking.bookingStatus)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text("${booking.bookingCode} • ${booking.date} • ${booking.slotTime}", style = MaterialTheme.typography.bodySmall, color = UltimateSlate500)
              Spacer(modifier = Modifier.height(4.dp))
              Text("₹${booking.totalAmount} • ${booking.energyDeliveredKwh} kWh delivered", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = UltimateEmerald)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Admin Action Button
      if (viewModel.hasPermission(AppPermissions.MANAGE_CUSTOMERS)) {
        Button(
          onClick = { showSuspendDialog = true },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (customer.status == "Active") StatusDangerText else UltimateEmerald
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = if (customer.status == "Active") Icons.Outlined.Block else Icons.Outlined.CheckCircle,
            contentDescription = null
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(if (customer.status == "Active") "Suspend Customer Account" else "Activate Customer Account")
        }
      }
    }
  }

  if (showSuspendDialog) {
    AlertDialog(
      onDismissRequest = { showSuspendDialog = false },
      title = { Text(if (customer.status == "Active") "Suspend Customer?" else "Activate Customer?") },
      text = { Text("Are you sure you want to change the status of ${customer.name} (${customer.customerCode})?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.toggleCustomerStatus(customer.id)
            showSuspendDialog = false
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (customer.status == "Active") StatusDangerText else UltimateEmerald
          )
        ) {
          Text("Confirm")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSuspendDialog = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun CustomerStatCard(
  modifier: Modifier = Modifier,
  label: String,
  value: String
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = UltimateSlate500)
      Spacer(modifier = Modifier.height(4.dp))
      Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
    }
  }
}

@Composable
fun HostListContent(
  viewModel: AdminViewModel,
  onHostClick: (String) -> Unit
) {
  val hosts by viewModel.hosts.collectAsState()
  val query by viewModel.hostSearchQuery.collectAsState()

  val filteredHosts = remember(hosts, query) {
    if (query.isBlank()) hosts else {
      hosts.filter {
        it.name.contains(query, ignoreCase = true) ||
          it.email.contains(query, ignoreCase = true) ||
          it.phone.contains(query, ignoreCase = true) ||
          it.hostCode.contains(query, ignoreCase = true)
      }
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    item {
      EnterpriseSearchField(
        query = query,
        onQueryChange = { viewModel.setHostSearchQuery(it) },
        placeholderText = "Search hosts by name, phone, code...",
        modifier = Modifier.padding(vertical = 8.dp)
      )
    }

    items(filteredHosts, key = { it.id }) { host ->
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 5.dp)
          .clickable { onHostClick(host.id) }
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(Color(0xFFFFF3E0)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = host.name.take(1),
              color = Color(0xFFE65100),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = host.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = UltimateNavy
              )
              Spacer(modifier = Modifier.width(6.dp))
              StatusBadge(status = host.verificationStatus)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${host.totalStations} Stations • ${host.totalBookings} Total Bookings",
              style = MaterialTheme.typography.bodySmall,
              color = UltimateSlate500
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "₹${host.totalEarnings} Earnings • ₹${host.withdrawableBalance} Withdrawable",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = UltimateEmerald
            )
          }

          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = UltimateSlate300,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun HostDetailScreen(
  hostId: String,
  viewModel: AdminViewModel,
  onBackClick: () -> Unit
) {
  val hosts by viewModel.hosts.collectAsState()
  val host = hosts.find { it.id == hostId } ?: hosts.firstOrNull()
  val stations by viewModel.stations.collectAsState()
  val hostStations = remember(stations, hostId) {
    stations.filter { it.hostId == hostId }
  }

  if (host == null) {
    AccessRestrictedScreen(requiredPermission = "View Hosts", onBackClick = onBackClick)
    return
  }

  Scaffold(
    containerColor = UltimateBackground,
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(UltimateCardBg)
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onBackClick) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = UltimateNavy)
        }
        Text(
          text = "Host Account & KYC",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = UltimateNavy,
          modifier = Modifier.weight(1f)
        )
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
      // Host Card
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
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF3E0)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = host.name.take(1),
                color = Color(0xFFE65100),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = host.name,
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                  color = UltimateNavy
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = host.verificationStatus)
              }
              Text(host.hostCode, style = MaterialTheme.typography.bodySmall, color = UltimateSlate500)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = UltimateSurfaceBorder)
          Spacer(modifier = Modifier.height(14.dp))

          StaffInfoRow(icon = Icons.Outlined.Mail, text = host.email)
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.Phone, text = host.phone)
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.AccountBalance, label = "Bank Account", value = "${host.bankName} (${host.accountNumber})")
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.Star, label = "Host Rating", value = "${host.rating} ★ (${host.totalBookings} ratings)")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // KYC Approval Banner if pending
      if (host.verificationStatus == "Pending Review" && viewModel.hasPermission(AppPermissions.MANAGE_HOSTS)) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = StatusPendingBg),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "KYC Verification Pending Review",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = StatusPendingText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Host has submitted GSTIN, Bank mandate, and electricity meter utility bills for verification.",
              style = MaterialTheme.typography.bodySmall,
              color = StatusPendingText
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Button(
                onClick = { viewModel.approveHost(host.id) },
                colors = ButtonDefaults.buttonColors(containerColor = UltimateEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("Approve KYC")
              }
              OutlinedButton(
                onClick = { viewModel.rejectHost(host.id) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDangerText),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("Reject")
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      // Host Stations list
      Text("Managed Charging Stations (${hostStations.size})", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
      Spacer(modifier = Modifier.height(8.dp))

      hostStations.forEach { station ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(station.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
              StatusBadge(status = station.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(station.address, style = MaterialTheme.typography.bodySmall, color = UltimateSlate500)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${station.powerRatingKw} kW • ₹${station.pricePerKwh}/kWh • ${station.totalChargingSessions} sessions", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = UltimateEmerald)
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Suspend/Activate Host button
      if (viewModel.hasPermission(AppPermissions.MANAGE_HOSTS)) {
        Button(
          onClick = { viewModel.toggleHostStatus(host.id) },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (host.status == "Active") StatusDangerText else UltimateEmerald
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(if (host.status == "Active") "Suspend Host Account" else "Activate Host Account")
        }
      }
    }
  }
}
