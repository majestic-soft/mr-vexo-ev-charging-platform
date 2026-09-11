package com.example.ui.screens.finance

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
import com.example.data.model.AppPermissions
import com.example.data.model.CouponUsageRecord
import com.example.data.model.HostWithdrawal
import com.example.data.model.PromoCode
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminViewModel

@Composable
fun FinanceHubScreen(
  viewModel: AdminViewModel,
  onBackClick: () -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Withdrawals", "Promos & Coupons", "Wallet Ledgers", "Redemptions")

  var showAddPromoDialog by remember { mutableStateOf(false) }

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
            text = "Finance & Settlements",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = UltimateNavy,
            modifier = Modifier.weight(1f)
          )

          if (selectedTab == 1 && viewModel.hasPermission(AppPermissions.MANAGE_PROMOS)) {
            IconButton(onClick = { showAddPromoDialog = true }) {
              Icon(Icons.Default.Add, contentDescription = "Add Promo", tint = UltimateEmerald)
            }
          }
        }

        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          containerColor = UltimateCardBg,
          contentColor = UltimateEmerald,
          edgePadding = 16.dp,
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
      when (selectedTab) {
        0 -> WithdrawalsTab(viewModel = viewModel)
        1 -> PromosTab(viewModel = viewModel)
        2 -> WalletLedgerTab(viewModel = viewModel)
        3 -> CouponRedemptionsTab(viewModel = viewModel)
      }
    }
  }

  if (showAddPromoDialog) {
    CreatePromoDialog(viewModel = viewModel, onDismiss = { showAddPromoDialog = false })
  }
}

@Composable
fun WithdrawalsTab(viewModel: AdminViewModel) {
  val withdrawals by viewModel.withdrawals.collectAsState()
  var selectedWithdrawalForAction by remember { mutableStateOf<HostWithdrawal?>(null) }
  var isApproveAction by remember { mutableStateOf(true) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    items(withdrawals, key = { it.id }) { item ->
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 5.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(item.hostName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
            StatusBadge(status = item.status)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text("${item.withdrawalCode} • ${item.bankName} (${item.accountNumber})", style = MaterialTheme.typography.bodySmall, color = UltimateSlate500)
          Spacer(modifier = Modifier.height(4.dp))
          Text("₹${String.format("%,.2f", item.amount)} requested on ${item.requestedDate}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)

          if (item.status == "Pending" && viewModel.hasPermission(AppPermissions.MANAGE_WITHDRAWALS)) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = {
                  selectedWithdrawalForAction = item
                  isApproveAction = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = UltimateEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("Approve Payout")
              }

              OutlinedButton(
                onClick = {
                  selectedWithdrawalForAction = item
                  isApproveAction = false
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDangerText),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("Reject")
              }
            }
          }
        }
      }
    }
  }

  if (selectedWithdrawalForAction != null) {
    val item = selectedWithdrawalForAction!!
    var remarks by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { selectedWithdrawalForAction = null },
      title = { Text(if (isApproveAction) "Approve Host Payout" else "Reject Host Payout") },
      text = {
        Column {
          Text("Host: ${item.hostName}")
          Text("Amount: ₹${item.amount}")
          Text("Bank Account: ${item.bankName} (${item.accountNumber})")
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = remarks,
            onValueChange = { remarks = it },
            label = { Text("Transfer Reference / Remarks") },
            placeholder = { Text(if (isApproveAction) "e.g. UTR #982183921" else "e.g. KYC Mismatch") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (isApproveAction) {
              viewModel.approveWithdrawal(item.id, remarks.ifBlank { "Approved by Admin" })
            } else {
              viewModel.rejectWithdrawal(item.id, remarks.ifBlank { "Rejected by Admin" })
            }
            selectedWithdrawalForAction = null
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isApproveAction) UltimateEmerald else StatusDangerText
          )
        ) {
          Text("Submit")
        }
      },
      dismissButton = {
        TextButton(onClick = { selectedWithdrawalForAction = null }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun PromosTab(viewModel: AdminViewModel) {
  val promos by viewModel.promos.collectAsState()
  val testCode by viewModel.couponTestCode.collectAsState()
  val testAmount by viewModel.couponTestAmount.collectAsState()
  val testResult by viewModel.couponTestResult.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Backend Validation Engine Simulator Card
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateEmerald.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = UltimateEmerald, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "BACKEND COUPON VALIDATION ENGINE",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = UltimateEmerald,
              letterSpacing = 0.5.sp
            )
          }

          Text(
            text = "Calculates discounts strictly on the backend/server. Never trusts client amounts.",
            fontSize = 11.sp,
            color = UltimateSlate500,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = testCode,
              onValueChange = { viewModel.setCouponTestCode(it) },
              label = { Text("Promo Code", fontSize = 11.sp) },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1.2f).testTag("coupon_test_code_input")
            )
            OutlinedTextField(
              value = testAmount,
              onValueChange = { viewModel.setCouponTestAmount(it) },
              label = { Text("Amount (₹)", fontSize = 11.sp) },
              singleLine = true,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(0.8f).testTag("coupon_test_amount_input")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = { viewModel.testApplyCoupon() },
            colors = ButtonDefaults.buttonColors(containerColor = UltimateEmerald),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("btn_verify_coupon_backend")
          ) {
            Icon(Icons.Filled.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Validate & Calculate Server-Side", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          if (testResult != null) {
            val res = testResult!!
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (res.isValid) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (res.isValid) Color(0xFFA7F3D0) else Color(0xFFFECACA)
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (res.isValid) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = if (res.isValid) UltimateEmerald else Color(0xFFDC2626),
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (res.isValid) "Coupon Valid & Discount Applied" else "Validation Rejected",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (res.isValid) UltimateEmerald else Color(0xFFB91C1C)
                  )
                }

                Text(
                  text = res.message,
                  fontSize = 11.sp,
                  color = if (res.isValid) UltimateSlate700 else Color(0xFFB91C1C),
                  modifier = Modifier.padding(top = 4.dp)
                )

                if (res.isValid) {
                  Spacer(modifier = Modifier.height(6.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Original: ₹${res.originalAmount}", fontSize = 11.sp, color = UltimateSlate500)
                    Text("Discount: -₹${res.discountAmount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = UltimateEmerald)
                    Text("Final Payable: ₹${res.finalAmount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = UltimateNavy)
                  }
                }
              }
            }
          }
        }
      }
    }

    item {
      Text(
        text = "ACTIVE PROMOTIONS & COUPONS",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = UltimateSlate500,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(vertical = 6.dp)
      )
    }

    items(promos, key = { it.id }) { promo ->
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 5.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9))
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = promo.code,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2E7D32)
              )
            }
            StatusBadge(status = promo.status)
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(promo.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = UltimateNavy)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            if (promo.discountType == "PERCENTAGE") "${promo.discountPercent}% OFF up to ₹${promo.maxDiscount} • Min Order ₹${promo.minBookingAmount}" else "Flat ₹${promo.discountAmount} OFF • Min Order ₹${promo.minBookingAmount}",
            style = MaterialTheme.typography.bodySmall,
            color = UltimateSlate500
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text("Used ${promo.usedCount} of ${promo.usageLimit} times • Max ${promo.perUserLimit} per user • Valid till ${promo.endDate}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = UltimateSlate500)

          if (viewModel.hasPermission(AppPermissions.MANAGE_PROMOS)) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
              onClick = { viewModel.togglePromoStatus(promo.id) },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(if (promo.status == "Active") "Deactivate Coupon" else "Activate Coupon")
            }
          }
        }
      }
    }
  }
}

@Composable
fun WalletLedgerTab(viewModel: AdminViewModel) {
  val transactions by viewModel.transactions.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    items(transactions, key = { it.id }) { tx ->
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(if (tx.type == "CREDIT") Color(0xFFE8F5E9) else Color(0xFFFCE4EC)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (tx.type == "CREDIT") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
              contentDescription = null,
              tint = if (tx.type == "CREDIT") Color(0xFF2E7D32) else Color(0xFFC2185B),
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(tx.userName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = UltimateNavy)
            Text("${tx.description} • ${tx.date}", style = MaterialTheme.typography.bodySmall, color = UltimateSlate500)
          }

          Text(
            text = "${if (tx.type == "CREDIT") "+" else "-"}₹${tx.amount}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = if (tx.type == "CREDIT") Color(0xFF2E7D32) else UltimateNavy
          )
        }
      }
    }
  }
}

@Composable
fun CouponRedemptionsTab(viewModel: AdminViewModel) {
  val history by viewModel.couponUsageHistory.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    items(history, key = { it.id }) { item ->
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
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
            Text(item.couponCode, fontWeight = FontWeight.Bold, color = UltimateNavy, fontSize = 13.sp)
            Text("-₹${item.discountApplied}", fontWeight = FontWeight.Bold, color = UltimateEmerald, fontSize = 13.sp)
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text("User #${item.userId} • Booking #${item.bookingId}", fontSize = 11.sp, color = UltimateSlate600)
          Text("Calculated on Server: Original ₹${item.originalAmount} → Final ₹${item.finalPayable} on ${item.usedAt}", fontSize = 10.sp, color = UltimateSlate500)
        }
      }
    }
  }
}

@Composable
fun CreatePromoDialog(
  viewModel: AdminViewModel,
  onDismiss: () -> Unit
) {
  var code by remember { mutableStateOf("") }
  var title by remember { mutableStateOf("") }
  var discountType by remember { mutableStateOf("PERCENTAGE") }
  var discountPercent by remember { mutableStateOf("20") }
  var maxDiscount by remember { mutableStateOf("150") }
  var minBookingAmount by remember { mutableStateOf("200") }
  var usageLimit by remember { mutableStateOf("1000") }
  var perUserLimit by remember { mutableStateOf("2") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create Promo Code") },
    text = {
      Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Coupon Code (e.g. MONSOON50)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Campaign Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = discountPercent, onValueChange = { discountPercent = it }, label = { Text("Discount Percentage (%)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = maxDiscount, onValueChange = { maxDiscount = it }, label = { Text("Max Discount Cap (₹)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = minBookingAmount, onValueChange = { minBookingAmount = it }, label = { Text("Min Booking Amount (₹)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = perUserLimit, onValueChange = { perUserLimit = it }, label = { Text("Max Usage Per User") }, singleLine = true, modifier = Modifier.fillMaxWidth())
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (code.isNotBlank()) {
            viewModel.createPromoCode(
              code = code,
              title = title.ifBlank { "$discountPercent% Discount Promo" },
              discountType = discountType,
              discountPercent = discountPercent.toIntOrNull() ?: 15,
              discountAmount = 50.0,
              maxDiscount = maxDiscount.toDoubleOrNull() ?: 100.0,
              minBookingAmount = minBookingAmount.toDoubleOrNull() ?: 150.0,
              usageLimit = usageLimit.toIntOrNull() ?: 500,
              perUserLimit = perUserLimit.toIntOrNull() ?: 2,
              startDate = "01 Aug 2026",
              endDate = "31 Dec 2026"
            )
            onDismiss()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = UltimateEmerald)
      ) {
        Text("Create Coupon")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
