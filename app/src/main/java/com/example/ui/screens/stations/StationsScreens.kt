package com.example.ui.screens.stations

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
import com.example.data.model.AppPermissions
import com.example.data.model.ChargingStation
import com.example.ui.components.*
import com.example.ui.screens.staff.StaffInfoRow
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun StationListScreen(
  viewModel: AdminViewModel,
  onStationClick: (String) -> Unit
) {
  val stations by viewModel.stations.collectAsState()
  val query by viewModel.stationSearchQuery.collectAsState()

  val filteredStations = remember(stations, query) {
    if (query.isBlank()) stations else {
      stations.filter {
        it.name.contains(query, ignoreCase = true) ||
          it.stationCode.contains(query, ignoreCase = true) ||
          it.address.contains(query, ignoreCase = true) ||
          it.hostName.contains(query, ignoreCase = true)
      }
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
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Charging Station Network",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = UltimateNavy,
            modifier = Modifier.weight(1f)
          )
        }
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
          onQueryChange = { viewModel.setStationSearchQuery(it) },
          placeholderText = "Search stations by name, city, host...",
          modifier = Modifier.padding(vertical = 8.dp)
        )
      }

      items(filteredStations, key = { it.id }) { station ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onStationClick(station.id) }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE8F5E9)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.EvStation,
                contentDescription = null,
                tint = UltimateEmerald,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = station.name,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = UltimateNavy
                )
                Spacer(modifier = Modifier.width(6.dp))
                StatusBadge(status = station.status)
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${station.powerRatingKw} kW (${station.chargerType}) • Host: ${station.hostName}",
                style = MaterialTheme.typography.bodySmall,
                color = UltimateSlate500
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "₹${station.pricePerKwh}/kWh • ${station.availableSlots}/${station.totalSlots} Slots Free",
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
fun StationDetailScreen(
  stationId: String,
  viewModel: AdminViewModel,
  onBackClick: () -> Unit
) {
  val stations by viewModel.stations.collectAsState()
  val station = stations.find { it.id == stationId } ?: stations.firstOrNull()

  if (station == null) {
    AccessRestrictedScreen(requiredPermission = "View Stations", onBackClick = onBackClick)
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
          text = "Station Details & Telemetry",
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
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFE8F5E9)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.EvStation,
                contentDescription = null,
                tint = UltimateEmerald,
                modifier = Modifier.size(30.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = station.name,
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                  color = UltimateNavy
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = station.status)
              }
              Text(station.stationCode, style = MaterialTheme.typography.bodySmall, color = UltimateSlate500)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = UltimateSurfaceBorder)
          Spacer(modifier = Modifier.height(14.dp))

          StaffInfoRow(icon = Icons.Outlined.LocationOn, text = station.address)
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.Person, label = "Managed by Host", value = station.hostName)
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.Bolt, label = "Charger Architecture", value = "${station.powerRatingKw} kW Fast DC (${station.chargerType})")
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.AttachMoney, label = "Tariff Rate", value = "₹${station.pricePerKwh} / kWh delivered")
          Spacer(modifier = Modifier.height(10.dp))
          StaffInfoRow(icon = Icons.Outlined.Power, label = "Live Slot Capacity", value = "${station.availableSlots} available out of ${station.totalSlots} total bays")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Station Approval Banner if Pending Review
      if (station.status == "Pending Review" && viewModel.hasPermission(AppPermissions.APPROVE_STATIONS)) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = StatusPendingBg),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Station Grid Verification Pending",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = StatusPendingText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Grid load certificate, electrical safety inspection, and geo-coordinates submitted by host.",
              style = MaterialTheme.typography.bodySmall,
              color = StatusPendingText
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Button(
                onClick = { viewModel.approveStation(station.id) },
                colors = ButtonDefaults.buttonColors(containerColor = UltimateEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("Approve & Go Live")
              }
              OutlinedButton(
                onClick = { viewModel.rejectStation(station.id) },
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

      // Operational Controls
      if (viewModel.hasPermission(AppPermissions.MANAGE_STATIONS)) {
        Text("Operational Controls", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = {
              val newStatus = if (station.status == "Active") "Offline" else "Active"
              viewModel.toggleStationStatus(station.id, newStatus)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text(if (station.status == "Active") "Force Take Offline" else "Bring Station Online")
          }
        }
      }
    }
  }
}
