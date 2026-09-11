package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminRole
import com.example.data.model.AdminStaffUser
import com.example.ui.theme.*

@Composable
fun AdminHeaderTopBar(
  title: String = "ULTIMATE",
  subtitle: String = "Super Admin Mode",
  currentRole: String = "Super Admin Mode",
  avatarInitials: String = "JD",
  unreadNotificationCount: Int = 0,
  onMenuClick: () -> Unit = {},
  onNotificationClick: () -> Unit = {},
  onSearchClick: () -> Unit = {}
) {
  Surface(
    color = Color.White,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Brand Title & Mode indicator
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = (-0.5).sp,
              fontSize = 22.sp
            ),
            color = UltimateNavy
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onMenuClick() }
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(UltimateEmerald)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = currentRole.uppercase(),
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
              ),
              color = UltimateEmerald
            )
          }
        }

        // Action icons (Notification + Avatar)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Notification circle button
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(UltimateSlate100)
              .border(1.dp, UltimateSurfaceBorder, CircleShape)
              .clickable { onNotificationClick() }
              .testTag("top_bar_notif_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Outlined.Notifications,
              contentDescription = "Notifications",
              tint = UltimateSlate500,
              modifier = Modifier.size(20.dp)
            )

            if (unreadNotificationCount > 0) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .align(Alignment.TopEnd)
                  .offset(x = (-2).dp, y = 2.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFEF4444))
                  .border(2.dp, Color.White, CircleShape)
              )
            }
          }

          // User Avatar pill / circle
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(UltimateEmerald)
              .border(2.dp, Color.White, CircleShape)
              .clickable { onMenuClick() }
              .testTag("top_bar_avatar_btn"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = avatarInitials,
              color = Color.White,
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            )
          }
        }
      }

      // Bottom crisp border
      HorizontalDivider(color = UltimateSurfaceBorder, thickness = 1.dp)
    }
  }
}

@Composable
fun AdminUserWelcomeHeader(
  currentAdmin: AdminStaffUser?,
  onSwitchRoleClick: () -> Unit = {}
) {
  Surface(
    color = Color.White,
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onSwitchRoleClick() }
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(UltimateGreenBg),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = currentAdmin?.avatarLetter?.ifBlank { "JD" } ?: "JD",
          color = UltimateEmerald,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = currentAdmin?.name ?: "John Doe",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = UltimateNavy,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(UltimateEmerald)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "${currentAdmin?.role?.displayName ?: "Super Admin"} • ${currentAdmin?.department ?: "Management"}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = UltimateSlate500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(UltimateSlate100)
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = "Switch Role",
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
          color = UltimateSlate600
        )
      }
    }
  }
}

@Composable
fun DarkGreenOverviewCard(
  totalStaff: Int = 28,
  activeStaff: Int = 24,
  departmentsCount: Int = 6,
  rolesCount: Int = 7,
  selectedTimeFilter: String = "This Month",
  onTimeFilterClick: () -> Unit = {},
  onViewDepartments: () -> Unit = {},
  onViewRoles: () -> Unit = {}
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .clip(RoundedCornerShape(24.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF042B1D),
            Color(0xFF0A442E),
            Color(0xFF063322)
          )
        )
      )
      .padding(20.dp)
  ) {
    Column {
      // Header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Overview",
          color = Color.White,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .clickable { onTimeFilterClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = selectedTimeFilter,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
          )
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Select Time",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 2x2 Grid stats
      Row(modifier = Modifier.fillMaxWidth()) {
        // Col 1: Total Staff
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.People,
              contentDescription = null,
              tint = UltimateElectricGreen.copy(alpha = 0.9f),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Total Staff",
              color = Color.White.copy(alpha = 0.8f),
              style = MaterialTheme.typography.bodySmall
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = totalStaff.toString(),
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "↑ 4",
            color = UltimateElectricGreen,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
          )
        }

        // Col 2: Active Staff
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.CheckCircle,
              contentDescription = null,
              tint = Color(0xFF64B5F6),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Active Staff",
              color = Color.White.copy(alpha = 0.8f),
              style = MaterialTheme.typography.bodySmall
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = activeStaff.toString(),
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "↑ 3",
            color = UltimateElectricGreen,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.8.dp)
      Spacer(modifier = Modifier.height(14.dp))

      Row(modifier = Modifier.fillMaxWidth()) {
        // Col 3: Departments
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.Apartment,
              contentDescription = null,
              tint = Color(0xFFB39DDB),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Departments",
              color = Color.White.copy(alpha = 0.8f),
              style = MaterialTheme.typography.bodySmall
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = departmentsCount.toString(),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "View all",
            color = UltimateElectricGreen,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.clickable { onViewDepartments() }
          )
        }

        // Col 4: Roles
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Outlined.Shield,
              contentDescription = null,
              tint = Color(0xFFFFB74D),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Roles",
              color = Color.White.copy(alpha = 0.8f),
              style = MaterialTheme.typography.bodySmall
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = rolesCount.toString(),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "View all",
            color = UltimateElectricGreen,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.clickable { onViewRoles() }
          )
        }
      }
    }
  }
}

@Composable
fun QuickActionButton(
  icon: ImageVector,
  label: String,
  backgroundColor: Color,
  iconTint: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clickable { onClick() }
      .padding(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(54.dp)
        .clip(CircleShape)
        .background(backgroundColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = iconTint,
        modifier = Modifier.size(24.dp)
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
      color = UltimateNavy,
      textAlign = TextAlign.Center,
      maxLines = 2,
      lineHeight = 14.sp
    )
  }
}

@Composable
fun EnterpriseSearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  placeholderText: String = "Search staff by name, email or role...",
  onFilterClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(52.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(UltimateCardBg)
      .border(1.dp, UltimateSurfaceBorder, RoundedCornerShape(16.dp))
      .padding(horizontal = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Default.Search,
      contentDescription = "Search",
      tint = UltimateSlate500,
      modifier = Modifier.size(20.dp)
    )

    Spacer(modifier = Modifier.width(8.dp))

    TextField(
      value = query,
      onValueChange = onQueryChange,
      placeholder = {
        Text(
          text = placeholderText,
          style = MaterialTheme.typography.bodyMedium,
          color = UltimateSlate500,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      },
      singleLine = true,
      colors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
      ),
      modifier = Modifier
        .weight(1f)
        .testTag("search_input_field")
    )

    if (query.isNotEmpty()) {
      IconButton(
        onClick = { onQueryChange("") },
        modifier = Modifier.size(28.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Clear,
          contentDescription = "Clear",
          tint = UltimateSlate500,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    Box(
      modifier = Modifier
        .height(24.dp)
        .width(1.dp)
        .background(UltimateSurfaceBorder)
    )

    Spacer(modifier = Modifier.width(6.dp))

    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .clickable { onFilterClick() }
        .padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Outlined.FilterList,
        contentDescription = "Filter",
        tint = UltimateNavy,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "Filter",
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        color = UltimateNavy
      )
    }
  }
}

@Composable
fun StaffListItemCard(
  staff: AdminStaffUser,
  onClick: () -> Unit
) {
  val avatarBg = when (staff.staffCode) {
    "STF1001" -> Color(0xFFE8F5E9)
    "STF1002" -> Color(0xFFE3F2FD)
    "STF1003" -> Color(0xFFEDE7F6)
    "STF1004" -> Color(0xFFFFF3E0)
    "STF1005" -> Color(0xFFE0F2F1)
    "STF1006" -> Color(0xFFFCE4EC)
    else -> Color(0xFFE8F5E9)
  }

  val avatarTextColor = when (staff.staffCode) {
    "STF1001" -> Color(0xFF2E7D32)
    "STF1002" -> Color(0xFF1565C0)
    "STF1003" -> Color(0xFF6A1B9A)
    "STF1004" -> Color(0xFFE65100)
    "STF1005" -> Color(0xFF00695C)
    "STF1006" -> Color(0xFFC2185B)
    else -> Color(0xFF2E7D32)
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = UltimateCardBg),
    border = androidx.compose.foundation.BorderStroke(1.dp, UltimateSurfaceBorder),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clickable { onClick() }
      .testTag("staff_card_${staff.staffCode}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
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
            color = UltimateNavy
          )
          Spacer(modifier = Modifier.width(8.dp))
          StatusBadge(status = staff.status)
        }

        Spacer(modifier = Modifier.height(3.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = staff.role.displayName,
            style = MaterialTheme.typography.bodySmall.copy(
              color = if (staff.staffCode == "STF1002") Color(0xFF1976D2) else UltimateSlate500
            )
          )
          if (staff.department.isNotBlank()) {
            Text(
              text = "  •  ${staff.department}",
              style = MaterialTheme.typography.bodySmall,
              color = UltimateSlate500
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "ID: ${staff.staffCode}",
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
  }
}

@Composable
fun StatusBadge(status: String) {
  val (bg, textColor) = when (status.lowercase()) {
    "active", "approved", "verified", "completed", "paid", "success" -> Pair(StatusActiveBg, StatusActiveText)
    "pending", "pending review", "under review", "assigned", "waiting" -> Pair(StatusPendingBg, StatusPendingText)
    "suspended", "rejected", "cancelled", "failed", "offline" -> Pair(StatusDangerBg, StatusDangerText)
    "in progress", "charging" -> Pair(StatusInfoBg, StatusInfoText)
    else -> Pair(UltimateSlate100, UltimateSlate700)
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(bg)
      .padding(horizontal = 8.dp, vertical = 2.dp)
  ) {
    Text(
      text = status,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun PermissionChip(
  label: String,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null
) {
  Row(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFFE8F5E9))
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
      .padding(horizontal = 10.dp, vertical = 7.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = Icons.Default.CheckCircle,
      contentDescription = null,
      tint = Color(0xFF2E7D32),
      modifier = Modifier.size(15.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      color = Color(0xFF1B5E20),
      style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun ModuleNavRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(UltimateSlate100),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = UltimateEmerald,
        modifier = Modifier.size(20.dp)
      )
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = UltimateNavy
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = UltimateSlate500
      )
    }

    Icon(
      imageVector = Icons.Default.ChevronRight,
      contentDescription = null,
      tint = UltimateSlate300,
      modifier = Modifier.size(18.dp)
    )
  }
}

@Composable
fun AccessRestrictedScreen(
  requiredPermission: String,
  onBackClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(UltimateBackground)
      .statusBarsPadding()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(80.dp)
        .clip(CircleShape)
        .background(StatusDangerBg),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Outlined.Lock,
        contentDescription = "Restricted",
        tint = StatusDangerText,
        modifier = Modifier.size(40.dp)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Access Restricted",
      style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
      color = UltimateNavy
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "Your current role does not have permission to view or manage '$requiredPermission'. Contact your Super Admin or Manager for permissions.",
      style = MaterialTheme.typography.bodyMedium,
      color = UltimateSlate500,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = onBackClick,
      colors = ButtonDefaults.buttonColors(containerColor = UltimateEmerald),
      shape = RoundedCornerShape(14.dp)
    ) {
      Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Go Back")
    }
  }
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


