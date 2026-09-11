package com.example.ui.screens.bookings

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppPermissions
import com.example.data.model.Booking
import com.example.ui.components.*
import com.example.ui.screens.staff.StaffInfoRow
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun BookingListScreen(
  viewModel: AdminViewModel,
  onBookingClick: (String) -> Unit
) {
  val bookings by viewModel.bookings.collectAsState()
  val query by viewModel.bookingSearchQuery.collectAsState()
  var selectedStatusFilter by remember { mutableStateOf("All") }

  val statusFilters = listOf("All", "Confirmed", "Charging", "Completed", "Cancelled")

  val filteredBookings = remember(bookings, query, selectedStatusFilter) {
    bookings.filter { b ->
      val matchesQuery = query.isBlank() ||
        b.bookingCode.contains(query, ignoreCase = true) ||
        b.customerName.contains(query, ignoreCase = true) ||
        b.stationName.contains(query, ignoreCase = true)

      val matchesStatus = selectedStatusFilter == "All" ||
        b.bookingStatus.equals(selectedStatusFilter, ignoreCase = true)

      matchesQuery && matchesStatus
    }
  }

  Scaffold(
    containerColor = UltimateBackground,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(UltimateCardBg)
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Text(
          text = "Bookings & Charging Sessions",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = UltimateNavy
        )
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      item {
        EnterpriseSearchField(
          query = query,
          onQueryChange = { viewModel.setBookingSearchQuery(it) },
          placeholderText = "Search by booking ID, customer or station...",
          modifier = Modifier.padding(vertical = 6.dp)
        )
      }

      item {
        LazyRow(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(statusFilters) { status ->
            FilterChip(
              selected = selectedStatusFilter == status,
              onClick = { selectedStatusFilter = status },
              label = { Text(status) }
            )
          }
        }
      }

      if (filteredBookings.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("No bookings matching criteria.", color = UltimateSlate500)
          }
        }
      } else {
        items(filteredBookings, key = { it.id }) { booking ->
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 5.dp)
              .clickable { onBookingClick(booking.id) }
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
                  .background(
                    if (booking.bookingStatus == "Charging") Color(0xFFE8F5E9) else Color(0xFFEDE7F6)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (booking.bookingStatus == "Charging") Icons.Outlined.Bolt else Icons.Outlined.EventNote,
                  contentDescription = null,
                  tint = if (booking.bookingStatus == "Charging") UltimateEmerald else Color(0xFF6A1B9A),
                  modifier = Modifier.size(22.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = booking.bookingCode,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = UltimateNavy
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  StatusBadge(status = booking.bookingStatus)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "${booking.customerName} • ${booking.stationName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = UltimateSlate500
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "₹${booking.totalAmount} • ${booking.date} (${booking.slotTime})",
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
}

@Composable
fun BookingDetailScreen(
  bookingId: String,
  viewModel: AdminViewModel,
  onBackClick: () -> Unit
) {
  val bookings by viewModel.bookings.collectAsState()
  val booking = bookings.find { it.id == bookingId } ?: bookings.firstOrNull()

  var showCancelDialog by remember { mutableStateOf(false) }

  if (booking == null) {
    AccessRestrictedScreen(requiredPermission = "View Bookings", onBackClick = onBackClick)
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
          text = "Booking #${booking.bookingCode}",
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
            Text(booking.stationName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
            StatusBadge(status = booking.bookingStatus)
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = UltimateSurfaceBorder)
          Spacer(modifier = Modifier.height(14.dp))

          StaffInfoRow(icon = Icons.Outlined.Person, label = "Customer", value = booking.customerName)
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.CalendarToday, label = "Scheduled Slot", value = "${booking.date}, ${booking.slotTime}")
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.Bolt, label = "Energy Transferred", value = "${booking.energyDeliveredKwh} kWh")
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.Payment, label = "Payment Method", value = "${booking.paymentMethod} (${booking.paymentStatus})")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Financial breakdown card
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Financial Settlement Breakdown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
          Spacer(modifier = Modifier.height(12.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Billed Amount", color = UltimateSlate500)
            Text("₹${booking.totalAmount}", fontWeight = FontWeight.Bold, color = UltimateNavy)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Platform Commission (15%)", color = UltimateSlate500)
            Text("₹${String.format("%.2f", booking.totalAmount * 0.15)}", color = UltimateEmerald, fontWeight = FontWeight.SemiBold)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Host Payout Amount", color = UltimateSlate500)
            Text("₹${String.format("%.2f", booking.totalAmount * 0.85)}", color = UltimateNavy, fontWeight = FontWeight.Medium)
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      if (viewModel.hasPermission(AppPermissions.MANAGE_BOOKINGS) && booking.bookingStatus != "Cancelled") {
        OutlinedButton(
          onClick = { showCancelDialog = true },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDangerText),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Cancel Booking & Refund Customer")
        }
      }
    }
  }

  if (showCancelDialog) {
    AlertDialog(
      onDismissRequest = { showCancelDialog = false },
      title = { Text("Cancel & Refund Booking?") },
      text = { Text("Are you sure you want to cancel booking #${booking.bookingCode} and refund ₹${booking.totalAmount} to ${booking.customerName}'s wallet?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateBookingStatus(booking.id, "Cancelled", "Cancelled")
            showCancelDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusDangerText)
        ) {
          Text("Confirm Cancellation")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCancelDialog = false }) { Text("Close") }
      }
    )
  }
}
