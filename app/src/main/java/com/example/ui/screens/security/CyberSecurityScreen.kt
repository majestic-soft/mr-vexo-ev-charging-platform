package com.example.ui.screens.security

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SecurityThreatEvent
import com.example.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberSecurityScreen(
  viewModel: AdminViewModel,
  onNavigateBack: () -> Unit
) {
  val securityStatus by viewModel.securityStatus.collectAsState()
  val threatEvents by viewModel.securityThreatEvents.collectAsState()
  val isLockedDown by viewModel.isPlatformLockedDown.collectAsState()
  val systemSettings by viewModel.systemSettings.collectAsState()
  val isSuperAdmin by viewModel.isSuperAdmin.collectAsState()

  var isScanning by remember { mutableStateOf(false) }
  var showLockdownDialog by remember { mutableStateOf(false) }
  var lockdownReason by remember { mutableStateOf("Manual emergency security protocol trigger") }
  val coroutineScope = rememberCoroutineScope()

  val infiniteTransition = rememberInfiniteTransition(label = "RadarScan")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "PulseAlpha"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isLockedDown) Color(0xFFEF4444) else Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Cyber Defense & Security (SOC)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = if (isLockedDown) "PLATFORM LOCKDOWN ACTIVE" else "Unhackable Multi-Layer Armor Active",
                style = MaterialTheme.typography.labelSmall,
                color = if (isLockedDown) Color(0xFFEF4444) else Color(0xFF10B981)
              )
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              coroutineScope.launch {
                isScanning = true
                delay(1200)
                viewModel.runDeepSecurityScan()
                isScanning = false
              }
            },
            enabled = !isScanning,
            modifier = Modifier.testTag("scan_security_top_button")
          ) {
            if (isScanning) {
              CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
              Icon(Icons.Default.Security, contentDescription = "Run Audit", tint = MaterialTheme.colorScheme.primary)
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item { Spacer(modifier = Modifier.height(4.dp)) }

      // 1. Hero Military-Grade Shield Banner
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
          )
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.radialGradient(
                  colors = listOf(
                    if (isLockedDown) Color(0x33EF4444) else Color(0x3310B981),
                    Color(0xFF0F172A)
                  )
                )
              )
              .padding(20.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = CircleShape,
                    color = (if (isLockedDown) Color(0xFFEF4444) else Color(0xFF10B981)).copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = if (isLockedDown) Icons.Default.Shield else Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = if (isLockedDown) Color(0xFFEF4444) else Color(0xFF10B981),
                        modifier = Modifier.size(26.dp)
                      )
                    }
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Text(
                      text = securityStatus.overallRating,
                      style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                      ),
                      color = Color.White
                    )
                    Text(
                      text = securityStatus.threatLevel,
                      style = MaterialTheme.typography.labelMedium,
                      color = (if (isLockedDown) Color(0xFFFCA5A5) else Color(0xFF6EE7B7)).copy(alpha = pulseAlpha)
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFF1E293B)
                ) {
                  Text(
                    text = "ZERO-TRUST",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    ),
                    color = Color(0xFF38BDF8)
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))
              Divider(color = Color(0xFF334155))
              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column {
                  Text("CRYPTO INTEGRITY", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                  Text(securityStatus.cryptoLevel, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text("LAST AUDIT", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                  Text(securityStatus.lastScanTime, style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = {
                  coroutineScope.launch {
                    isScanning = true
                    delay(1000)
                    viewModel.runDeepSecurityScan()
                    isScanning = false
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("run_deep_security_audit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isLockedDown) Color(0xFFEF4444) else Color(0xFF0EA5E9)
                ),
                enabled = !isScanning
              ) {
                if (isScanning) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text("Executing 8-Point Defense Audit...", color = Color.White)
                } else {
                  Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Run 8-Layer Deep Security Audit", fontWeight = FontWeight.Bold, color = Color.White)
                }
              }
            }
          }
        }
      }

      // 2. Notification Sound & Acoustic Studio
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.VolumeUp,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = "Notification Sound & Audio Feedback",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                  )
                  Text(
                    text = "Synthesized real-time alerts & haptic sounds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
              Switch(
                checked = systemSettings.notificationSoundEnabled,
                onCheckedChange = { isEnabled ->
                  viewModel.updateSystemSettings(systemSettings.copy(notificationSoundEnabled = isEnabled))
                },
                modifier = Modifier.testTag("notification_sound_switch")
              )
            }

            if (systemSettings.notificationSoundEnabled) {
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "SOUND STYLE PRESET",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(8.dp))

              val soundStyles = listOf("High-Tech Chime", "Alert Beep", "Subtle Pulse", "Sonic Alarm")
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                soundStyles.forEach { style ->
                  val isSelected = systemSettings.notificationSoundStyle == style
                  FilterChip(
                    selected = isSelected,
                    onClick = {
                      viewModel.updateSystemSettings(systemSettings.copy(notificationSoundStyle = style))
                      viewModel.testNotificationSound(style)
                    },
                    label = { Text(style, fontSize = 11.sp) },
                    modifier = Modifier.testTag("sound_style_$style")
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              OutlinedButton(
                onClick = {
                  viewModel.testNotificationSound(systemSettings.notificationSoundStyle)
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("test_sound_button"),
                shape = RoundedCornerShape(10.dp)
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test '${systemSettings.notificationSoundStyle}' Sound")
              }
            }
          }
        }
      }

      // 3. Multi-Layer Defense Matrix
      item {
        Text(
          text = "UNHACKABLE DEFENSE MATRIX (ACTIVE)",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )
      }

      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          DefenseMatrixCard(
            title = "Root & Jailbreak Neutralization",
            desc = "Scans kernel /system/bin/su, /data/local/su, and Magisk binaries. Prevents escalated privilege bypass.",
            statusText = securityStatus.rootStatus,
            icon = Icons.Default.Lock,
            isSecured = !securityStatus.rootStatus.contains("DETECTED")
          )

          DefenseMatrixCard(
            title = "Anti-Hooking & Memory Shield",
            desc = "Inspects /proc/self/maps for Frida Gadget, Xposed and Substrate dynamic instrumentation hooks.",
            statusText = securityStatus.fridaHookStatus,
            icon = Icons.Default.Code,
            isSecured = true
          )

          DefenseMatrixCard(
            title = "Screen Privacy Guard (FLAG_SECURE)",
            desc = "Prevents OS screen recording, screenshots, and recents snapshot snooping of financial EV data.",
            statusText = securityStatus.screenCaptureGuard,
            icon = Icons.Default.VisibilityOff,
            isSecured = systemSettings.screenCaptureShieldEnabled
          )

          DefenseMatrixCard(
            title = "Brute-Force & Rate-Limit Defense",
            desc = "Monitors failed authentication attempts with progressive lockouts and incident notifications.",
            statusText = "BRUTE-FORCE ARMOR ACTIVE",
            icon = Icons.Default.Password,
            isSecured = true
          )
        }
      }

      // 4. Emergency Executive Killswitch
      if (isSuperAdmin) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isLockedDown) Color(0xFFFEF2F2) else Color(0xFFFFFBEB)
            ),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isLockedDown) Color(0xFFEF4444) else Color(0xFFF59E0B)
            )
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (isLockedDown) Icons.Default.Warning else Icons.Default.Shield,
                  contentDescription = null,
                  tint = if (isLockedDown) Color(0xFFDC2626) else Color(0xFFD97706)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = if (isLockedDown) "PLATFORM IN EMERGENCY LOCKDOWN" else "Emergency Platform Lockdown Killswitch",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isLockedDown) Color(0xFF991B1B) else Color(0xFF92400E)
                  )
                  Text(
                    text = if (isLockedDown) "All write operations and API calls are currently halted." else "Super Admin executive action to freeze all operations instantly during an intrusion.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLockedDown) Color(0xFFB91C1C) else Color(0xFFB45309)
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              if (isLockedDown) {
                Button(
                  onClick = { viewModel.liftEmergencyLockdown() },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lift_lockdown_button"),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Default.LockOpen, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Lift Lockdown & Resume Platform", fontWeight = FontWeight.Bold)
                }
              } else {
                OutlinedButton(
                  onClick = { showLockdownDialog = true },
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("engage_lockdown_button"),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color(0xFFDC2626))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Engage Emergency Platform Lockdown", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }
              }
            }
          }
        }
      }

      // 5. Threat Audit & Incident Log
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "LIVE DEFENSE AUDIT STREAM (${threatEvents.size})",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      if (threatEvents.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
          ) {
            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
              Text("No threat incidents recorded. System verified clean.", style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      } else {
        items(threatEvents, key = { it.id }) { event ->
          ThreatEventItem(event)
        }
      }

      item { Spacer(modifier = Modifier.height(24.dp)) }
    }
  }

  if (showLockdownDialog) {
    AlertDialog(
      onDismissRequest = { showLockdownDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Confirm Platform Lockdown", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column {
          Text("Are you sure you want to engage Emergency Lockdown? This will immediately halt all charging station writes, withdrawal processing, and restrict logins until lifted.")
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = lockdownReason,
            onValueChange = { lockdownReason = it },
            label = { Text("Reason for Lockdown") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.triggerEmergencyLockdown(lockdownReason)
            showLockdownDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
          Text("Engage Lockdown", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLockdownDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun DefenseMatrixCard(
  title: String,
  desc: String,
  statusText: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isSecured: Boolean
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = CircleShape,
        color = (if (isSecured) Color(0xFF10B981) else Color(0xFFEF4444)).copy(alpha = 0.15f),
        modifier = Modifier.size(40.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSecured) Color(0xFF10B981) else Color(0xFFEF4444),
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = desc,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = statusText,
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          ),
          color = if (isSecured) Color(0xFF059669) else Color(0xFFDC2626)
        )
      }
    }
  }
}

@Composable
fun ThreatEventItem(event: SecurityThreatEvent) {
  val badgeColor = when (event.severity) {
    "CRITICAL" -> Color(0xFFDC2626)
    "HIGH" -> Color(0xFFEA580C)
    "MEDIUM" -> Color(0xFFD97706)
    else -> Color(0xFF10B981)
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .padding(top = 4.dp)
          .size(8.dp)
          .clip(CircleShape)
          .background(badgeColor)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = event.threatType,
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          )
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = badgeColor.copy(alpha = 0.15f)
          ) {
            Text(
              text = event.severity,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = badgeColor
            )
          }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = event.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Source: ${event.source}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = event.timestamp,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}
