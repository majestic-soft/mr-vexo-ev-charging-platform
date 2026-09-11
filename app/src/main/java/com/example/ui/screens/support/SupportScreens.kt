package com.example.ui.screens.support

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminRole
import com.example.data.model.AppPermissions
import com.example.data.model.Dispute
import com.example.data.model.SupportTicket
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun SupportHubScreen(
  viewModel: AdminViewModel,
  onDisputeClick: (String) -> Unit,
  onTicketClick: (String) -> Unit
) {
  val currentAdmin by viewModel.currentAdmin.collectAsState()
  var selectedTab by remember { mutableIntStateOf(1) } // Default to Support Tickets
  val tabs = listOf("Disputes & Refunds", "Support Tickets")

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
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (currentAdmin?.role == AdminRole.SUPPORT) "My Support Queue" else "Support & Dispute Hub",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = UltimateNavy
            )
            Text(
              text = if (currentAdmin?.role == AdminRole.SUPPORT) "Authorized for assigned tickets and escalations only" else "Role: ${currentAdmin?.role?.displayName ?: ""}",
              fontSize = 11.sp,
              color = UltimateSlate500
            )
          }
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
        DisputesListTab(viewModel = viewModel, onDisputeClick = onDisputeClick)
      } else {
        TicketsListTab(viewModel = viewModel, onTicketClick = onTicketClick)
      }
    }
  }
}

@Composable
fun DisputesListTab(
  viewModel: AdminViewModel,
  onDisputeClick: (String) -> Unit
) {
  val disputes by viewModel.disputes.collectAsState()
  val query by viewModel.disputeSearchQuery.collectAsState()

  val filteredDisputes = remember(disputes, query) {
    if (query.isBlank()) disputes else {
      disputes.filter {
        it.disputeCode.contains(query, ignoreCase = true) ||
          it.customerName.contains(query, ignoreCase = true) ||
          it.bookingCode.contains(query, ignoreCase = true)
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
        onQueryChange = { viewModel.setDisputeSearchQuery(it) },
        placeholderText = "Search disputes by ID, customer, booking...",
        modifier = Modifier.padding(vertical = 8.dp)
      )
    }

    if (filteredDisputes.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
          modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
          Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = UltimateEmerald, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("No Active Disputes Found", fontWeight = FontWeight.Bold, color = UltimateNavy)
            Text("All EV charging transactions are settled cleanly.", fontSize = 12.sp, color = UltimateSlate500)
          }
        }
      }
    } else {
      items(filteredDisputes, key = { it.id }) { dispute ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
          border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onDisputeClick(dispute.id) }
            .testTag("dispute_item_${dispute.disputeCode}")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(dispute.disputeCode, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
              StatusBadge(status = dispute.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("${dispute.customerName} • Booking #${dispute.bookingCode}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = UltimateNavy)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Reason: ${dispute.reason}", style = MaterialTheme.typography.bodySmall, color = UltimateSlate500)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Disputed Amount: ₹${dispute.disputedAmount} • ${dispute.createdAt}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = UltimateEmerald)
          }
        }
      }
    }
  }
}

@Composable
fun TicketsListTab(
  viewModel: AdminViewModel,
  onTicketClick: (String) -> Unit
) {
  val accessibleTickets by viewModel.accessibleTickets.collectAsState()
  val query by viewModel.ticketSearchQuery.collectAsState()
  val currentAdmin by viewModel.currentAdmin.collectAsState()

  val filteredTickets = remember(accessibleTickets, query) {
    if (query.isBlank()) accessibleTickets else {
      accessibleTickets.filter {
        it.ticketCode.contains(query, ignoreCase = true) ||
          it.subject.contains(query, ignoreCase = true) ||
          it.customerName.contains(query, ignoreCase = true)
      }
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    item {
      // Role scope notification badge
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = UltimateGreenBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateEmerald.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Filled.Security, contentDescription = null, tint = UltimateEmerald, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (currentAdmin?.role == AdminRole.SUPPORT) "Isolated Scope: Showing ${accessibleTickets.size} tickets assigned to you." else "RBAC Filter: Showing ${accessibleTickets.size} tickets for ${currentAdmin?.role?.displayName}.",
            fontSize = 11.sp,
            color = UltimateEmerald,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    item {
      EnterpriseSearchField(
        query = query,
        onQueryChange = { viewModel.setTicketSearchQuery(it) },
        placeholderText = "Search tickets by subject, user, ticket #...",
        modifier = Modifier.padding(vertical = 8.dp)
      )
    }

    if (filteredTickets.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
          modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
          Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(Icons.Outlined.SupportAgent, contentDescription = null, tint = UltimateSlate400, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("No Tickets in Your Queue", fontWeight = FontWeight.Bold, color = UltimateNavy)
            Text("Tickets assigned to your staff profile will appear here.", fontSize = 12.sp, color = UltimateSlate500)
          }
        }
      }
    } else {
      items(filteredTickets, key = { it.id }) { ticket ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
          border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (ticket.escalatedToManager) Color(0xFFFCA5A5) else UltimateSurfaceBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onTicketClick(ticket.id) }
            .testTag("ticket_item_${ticket.ticketCode}")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(ticket.ticketCode, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
                if (ticket.escalatedToManager) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFFEE2E2)
                  ) {
                    Text(
                      text = "ESCALATED",
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFDC2626),
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                  }
                }
              }
              StatusBadge(status = ticket.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(ticket.subject, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = UltimateNavy)
            Spacer(modifier = Modifier.height(2.dp))
            Text("${ticket.customerName} • Priority: ${ticket.priority}", style = MaterialTheme.typography.bodySmall, color = UltimateSlate500)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Assigned to: ${ticket.assignedStaffName} • ${ticket.updatedAt}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = UltimateEmerald)
          }
        }
      }
    }
  }
}

@Composable
fun TicketDetailScreen(
  ticketId: String,
  viewModel: AdminViewModel,
  onBackClick: () -> Unit
) {
  val tickets by viewModel.tickets.collectAsState()
  val currentAdmin by viewModel.currentAdmin.collectAsState()
  val ticket = tickets.find { it.id == ticketId } ?: tickets.firstOrNull()

  var replyMessage by remember { mutableStateOf("") }
  var isInternalNote by remember { mutableStateOf(false) }
  var showEscalateDialog by remember { mutableStateOf(false) }

  if (ticket == null) {
    AccessRestrictedScreen(requiredPermission = "View Support Tickets", onBackClick = onBackClick)
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
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Ticket #${ticket.ticketCode}",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = UltimateNavy
          )
          Text(
            text = ticket.status,
            fontSize = 11.sp,
            color = if (ticket.escalatedToManager) Color(0xFFDC2626) else UltimateEmerald,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Action: Escalate to Manager button (available to Support or Manager)
        if (!ticket.escalatedToManager) {
          FilledTonalButton(
            onClick = { showEscalateDialog = true },
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = Color(0xFFFEE2E2),
              contentColor = Color(0xFFDC2626)
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp).testTag("btn_escalate_ticket")
          ) {
            Icon(Icons.Outlined.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Escalate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(ticket.subject, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = UltimateNavy, modifier = Modifier.weight(1f))
                StatusBadge(status = ticket.status)
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text("Customer: ${ticket.customerName} (${ticket.customerEmail})", style = MaterialTheme.typography.bodySmall, color = UltimateSlate700)
              Text("Phone: ${ticket.customerPhone} • Station: ${ticket.stationName}", style = MaterialTheme.typography.bodySmall, color = UltimateSlate500)
              Spacer(modifier = Modifier.height(4.dp))
              Text("Priority: ${ticket.priority} • Assigned Specialist: ${ticket.assignedStaffName}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = UltimateEmerald)

              if (ticket.escalatedToManager && ticket.escalatedReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFFFEF2F2),
                  border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Escalation Reason: ${ticket.escalatedReason}", fontSize = 11.sp, color = Color(0xFFB91C1C), fontWeight = FontWeight.Medium)
                  }
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
          Text("Conversation & Action History", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
          Spacer(modifier = Modifier.height(8.dp))
        }

        items(ticket.replies) { msg ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (msg.isInternalNote) Color(0xFFFFFBEB) else if (msg.senderRole.contains("Support") || msg.senderRole.contains("Manager") || msg.senderRole.contains("Admin")) Color(0xFFECFDF5) else UltimateCardBg
            ),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (msg.isInternalNote) Color(0xFFFDE68A) else UltimateSurfaceBorder
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(msg.senderName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (msg.isInternalNote) Color(0xFFFEF3C7) else UltimateGreenBg
                  ) {
                    Text(
                      text = if (msg.isInternalNote) "INTERNAL NOTE" else msg.senderRole,
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (msg.isInternalNote) Color(0xFFD97706) else UltimateEmerald,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(msg.timestamp, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = UltimateSlate500)
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(msg.message, style = MaterialTheme.typography.bodyMedium, color = UltimateSlate700)
            }
          }
        }
      }

      // Quick Status Updater for Managers / Support
      Surface(
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Update Status:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = UltimateSlate600)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("In Progress", "Resolved", "Closed").forEach { st ->
              SuggestionChip(
                onClick = { viewModel.updateTicketStatus(ticket.id, st) },
                label = { Text(st, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                  containerColor = if (ticket.status == st) UltimateGreenBg else Color.Transparent
                ),
                modifier = Modifier.height(28.dp)
              )
            }
          }
        }
      }

      // Bottom reply bar
      Card(
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
              checked = isInternalNote,
              onCheckedChange = { isInternalNote = it }
            )
            Text("Private Staff Note (Hidden from Customer)", fontSize = 12.sp, color = UltimateSlate700)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = replyMessage,
              onValueChange = { replyMessage = it },
              placeholder = { Text(if (isInternalNote) "Add private internal note..." else "Send reply to customer...") },
              modifier = Modifier.weight(1f).testTag("ticket_reply_input"),
              shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
              onClick = {
                if (replyMessage.isNotBlank()) {
                  viewModel.addTicketReply(ticket.id, replyMessage, isInternalNote)
                  replyMessage = ""
                }
              },
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(UltimateEmerald)
                .testTag("ticket_send_reply_btn")
            ) {
              Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
          }
        }
      }
    }
  }

  // Escalation Dialog
  if (showEscalateDialog) {
    var reasonText by remember { mutableStateOf("Hardware fault requires higher-level authorization & host penalty assessment.") }
    AlertDialog(
      onDismissRequest = { showEscalateDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Color(0xFFDC2626))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Escalate Ticket to Manager")
        }
      },
      text = {
        Column {
          Text("This ticket will be marked as ESCALATED and routed directly to the Operations & Support Manager queue.")
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = reasonText,
            onValueChange = { reasonText = it },
            label = { Text("Escalation Reason & Summary") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.escalateTicketToManager(ticket.id, reasonText)
            showEscalateDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
          Text("Confirm Escalation")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEscalateDialog = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun DisputeDetailScreen(
  disputeId: String,
  viewModel: AdminViewModel,
  onBackClick: () -> Unit
) {
  val disputes by viewModel.accessibleDisputes.collectAsState()
  val dispute = disputes.find { it.id == disputeId }
  val currentAdmin by viewModel.currentAdmin.collectAsState()

  var resolutionNotes by remember { mutableStateOf("") }
  var refundAmount by remember { mutableStateOf(dispute?.disputedAmount?.toString() ?: "0.0") }
  var showRefundDialog by remember { mutableStateOf(false) }

  if (dispute == null) {
    Box(
      modifier = Modifier.fillMaxSize().background(UltimateBackground),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Dispute not found or access restricted.", color = UltimateSlate600)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBackClick) { Text("Go Back") }
      }
    }
    return
  }

  Scaffold(
    containerColor = UltimateBackground,
    topBar = {
      Surface(
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = UltimateNavy)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Dispute #${dispute.disputeCode}",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = UltimateNavy
            )
            Text(
              text = "Booking: ${dispute.bookingCode}",
              fontSize = 11.sp,
              color = UltimateSlate500
            )
          }
          StatusBadge(status = dispute.status)
        }
      }
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Summary Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Dispute Details",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = UltimateNavy
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = dispute.reason,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFDC2626)
          )
          if (dispute.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = dispute.description,
              fontSize = 13.sp,
              color = UltimateSlate700
            )
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = UltimateSurfaceBorder)
          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Disputed Amount", fontSize = 11.sp, color = UltimateSlate500)
              Text("₹${"%.2f".format(dispute.disputedAmount)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = UltimateNavy)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Assigned Handler", fontSize = 11.sp, color = UltimateSlate500)
              Text(dispute.assignedStaffName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = UltimateEmerald)
            }
          }
        }
      }

      // Parties involved
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Parties & Station",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = UltimateNavy
          )
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
              Text("Customer", fontSize = 11.sp, color = UltimateSlate500)
              Text(dispute.customerName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = UltimateNavy)
              Text(dispute.customerPhone, fontSize = 11.sp, color = UltimateSlate500)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Host & Station", fontSize = 11.sp, color = UltimateSlate500)
              Text(dispute.hostName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = UltimateNavy)
              Text(dispute.stationName, fontSize = 11.sp, color = UltimateSlate500)
            }
          }
        }
      }

      // Resolution Controls
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Dispute Resolution Actions",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = UltimateNavy
          )
          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = resolutionNotes,
            onValueChange = { resolutionNotes = it },
            label = { Text("Resolution Notes & Summary") },
            placeholder = { Text("Explain investigation outcome and actions taken...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3
          )

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = {
                viewModel.resolveDispute(
                  disputeId = dispute.id,
                  refundApproved = false,
                  refundAmount = 0.0,
                  resolutionNotes = resolutionNotes.ifBlank { "Dispute dismissed after reviewing charging telemetry logs." }
                )
                onBackClick()
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Dismiss Dispute", color = Color(0xFFDC2626), fontSize = 12.sp)
            }

            Button(
              onClick = { showRefundDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = UltimateEmerald),
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Authorize Refund", fontSize = 12.sp)
            }
          }
        }
      }
    }
  }

  if (showRefundDialog) {
    AlertDialog(
      onDismissRequest = { showRefundDialog = false },
      title = { Text("Authorize Customer Wallet Refund") },
      text = {
        Column {
          Text("Enter the refund amount to be credited back to the customer's wallet balance:")
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = refundAmount,
            onValueChange = { refundAmount = it },
            label = { Text("Refund Amount (₹)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amount = refundAmount.toDoubleOrNull() ?: dispute.disputedAmount
            viewModel.resolveDispute(
              disputeId = dispute.id,
              refundApproved = true,
              refundAmount = amount,
              resolutionNotes = resolutionNotes.ifBlank { "Refund of ₹$amount approved by ${currentAdmin?.name} (${currentAdmin?.role?.displayName})." }
            )
            showRefundDialog = false
            onBackClick()
          },
          colors = ButtonDefaults.buttonColors(containerColor = UltimateEmerald)
        ) {
          Text("Process Refund")
        }
      },
      dismissButton = {
        TextButton(onClick = { showRefundDialog = false }) { Text("Cancel") }
      }
    )
  }
}
