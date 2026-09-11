package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
  viewModel: AdminViewModel,
  onLoginSuccess: () -> Unit = {}
) {
  var email by remember { mutableStateOf("ankit.verma@ultimate.com") }
  var password by remember { mutableStateOf("••••••••") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  val demoAccounts = listOf(
    DemoAccount(
      role = AdminRole.SUPER_ADMIN,
      name = "Ankit Verma",
      email = "ankit.verma@ultimate.com",
      badge = "Super Admin",
      badgeColor = Color(0xFFEF4444),
      description = "Full Unrestricted Access & RBAC Controls"
    ),
    DemoAccount(
      role = AdminRole.ADMIN,
      name = "Rajesh Kulkarni",
      email = "rajesh.kulkarni@ultimate.com",
      badge = "Admin",
      badgeColor = Color(0xFF8B5CF6),
      description = "General Ops, Users & Station Controls"
    ),
    DemoAccount(
      role = AdminRole.MANAGER,
      name = "Vikram Patel",
      email = "vikram.patel@ultimate.com",
      badge = "Manager",
      badgeColor = Color(0xFF3B82F6),
      description = "Support Team Lead & Escalation Queue"
    ),
    DemoAccount(
      role = AdminRole.SUPPORT,
      name = "Rahul Sharma",
      email = "rahul.sharma@ultimate.com",
      badge = "Customer Support",
      badgeColor = UltimateEmerald,
      description = "Assigned Tickets & User Inquiries Only"
    ),
    DemoAccount(
      role = AdminRole.FINANCE,
      name = "Arjun Mehta",
      email = "arjun.mehta@ultimate.com",
      badge = "Finance",
      badgeColor = Color(0xFFD97706),
      description = "Payouts, Wallets, Settlements & Reports"
    ),
    DemoAccount(
      role = AdminRole.OPERATIONS,
      name = "Neha Gupta",
      email = "neha.gupta@ultimate.com",
      badge = "Operations",
      badgeColor = Color(0xFF0D9488),
      description = "Charging Bays, Grid Approvals & Telemetry"
    )
  )

  fun executeLogin(targetEmail: String) {
    isLoading = true
    errorMessage = null
    val result = viewModel.login(targetEmail, password)
    isLoading = false
    result.onSuccess {
      onLoginSuccess()
    }.onFailure { err ->
      errorMessage = err.message ?: "Authentication failed."
    }
  }

  Scaffold(
    containerColor = UltimateBackground
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // App Logo Header
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = UltimateEmerald,
        shadowElevation = 8.dp,
        modifier = Modifier.size(72.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Filled.ElectricBolt,
            contentDescription = "Ultimate EV Logo",
            tint = Color.White,
            modifier = Modifier.size(42.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "ULTIMATE EV ADMIN",
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        color = UltimateSlate900,
        letterSpacing = 1.sp
      )

      Text(
        text = "Role-Based Access Control & Operations Portal",
        fontSize = 13.sp,
        color = UltimateSlate500,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Login Card
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, UltimateSurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(24.dp)
        ) {
          Text(
            text = "Sign In to Your Workspace",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = UltimateSlate900
          )
          Text(
            text = "Enter authorized credentials to load assigned role permissions.",
            fontSize = 12.sp,
            color = UltimateSlate500,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
          )

          // Error Banner
          if (errorMessage != null) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFFFEF2F2),
              border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Filled.ErrorOutline,
                  contentDescription = "Error",
                  tint = Color(0xFFDC2626),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = errorMessage ?: "",
                  fontSize = 12.sp,
                  color = Color(0xFFB91C1C),
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          // Email Input
          Text(
            text = "Staff Email Address",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = UltimateSlate700,
            modifier = Modifier.padding(bottom = 6.dp)
          )
          OutlinedTextField(
            value = email,
            onValueChange = {
              email = it
              errorMessage = null
            },
            placeholder = { Text("e.g. name@ultimate.com", fontSize = 14.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.Email, contentDescription = null, tint = UltimateSlate400)
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = UltimateEmerald,
              unfocusedBorderColor = UltimateSurfaceBorder
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_email_input")
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Password Input
          Text(
            text = "Password",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = UltimateSlate700,
            modifier = Modifier.padding(bottom = 6.dp)
          )
          OutlinedTextField(
            value = password,
            onValueChange = {
              password = it
              errorMessage = null
            },
            placeholder = { Text("••••••••", fontSize = 14.sp) },
            leadingIcon = {
              Icon(Icons.Outlined.Lock, contentDescription = null, tint = UltimateSlate400)
            },
            trailingIcon = {
              IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                  imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                  contentDescription = "Toggle password visibility",
                  tint = UltimateSlate400
                )
              }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = UltimateEmerald,
              unfocusedBorderColor = UltimateSurfaceBorder
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_password_input")
          )

          Spacer(modifier = Modifier.height(24.dp))

          // Primary Login Button
          Button(
            onClick = { executeLogin(email) },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UltimateEmerald),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("login_submit_button")
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp
              )
            } else {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Filled.Login,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Sign In to Admin Console",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Demo Role Switcher Section for RBAC Verification
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = UltimateSurfaceBorder)
        Text(
          text = "  TEST ALL 6 ROLES  ",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = UltimateSlate400,
          letterSpacing = 1.sp
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = UltimateSurfaceBorder)
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Tap any role to immediately sign in and verify strict RBAC boundaries & isolated modules:",
        fontSize = 12.sp,
        color = UltimateSlate600,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 12.dp)
      )

      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        demoAccounts.forEach { account ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, UltimateSurfaceBorder),
            onClick = {
              email = account.email
              executeLogin(account.email)
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("demo_login_${account.role.name.lowercase()}")
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Surface(
                shape = CircleShape,
                color = account.badgeColor.copy(alpha = 0.12f),
                modifier = Modifier.size(42.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = account.name.take(1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = account.badgeColor
                  )
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = account.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = UltimateSlate900
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = account.badgeColor.copy(alpha = 0.12f)
                  ) {
                    Text(
                      text = account.badge,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = account.badgeColor,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                  text = account.description,
                  fontSize = 11.sp,
                  color = UltimateSlate500,
                  modifier = Modifier.padding(top = 2.dp)
                )
              }

              Icon(
                imageVector = Icons.Outlined.ArrowForwardIos,
                contentDescription = "Login as ${account.name}",
                tint = UltimateSlate400,
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "Enterprise Security Shield • Firebase Auth & Rule Enforcement Active",
        fontSize = 11.sp,
        color = UltimateSlate400,
        textAlign = TextAlign.Center
      )
    }
  }
}

private data class DemoAccount(
  val role: AdminRole,
  val name: String,
  val email: String,
  val badge: String,
  val badgeColor: Color,
  val description: String
)
