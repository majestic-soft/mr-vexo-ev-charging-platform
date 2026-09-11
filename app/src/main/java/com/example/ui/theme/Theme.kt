package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val UltimateAdminColorScheme = lightColorScheme(
  primary = UltimateEmerald,
  onPrimary = Color.White,
  primaryContainer = UltimateGreenBg,
  onPrimaryContainer = UltimateGreenText,
  secondary = UltimateSlate700,
  onSecondary = Color.White,
  secondaryContainer = UltimateSlate100,
  onSecondaryContainer = UltimateSlate900,
  tertiary = UltimateEmeraldDark,
  onTertiary = Color.White,
  background = UltimateBackground,
  onBackground = UltimateSlate900,
  surface = UltimateCardBg,
  onSurface = UltimateSlate900,
  surfaceVariant = UltimateSlate50,
  onSurfaceVariant = UltimateSlate700,
  outline = UltimateSurfaceBorder,
  error = StatusDangerText,
  errorContainer = StatusDangerBg,
  onError = Color.White,
  onErrorContainer = StatusDangerText
)

@Composable
fun UltimateAdminTheme(
  content: @Composable () -> Unit
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = Color.Transparent.toArgb()
      window.navigationBarColor = Color.Transparent.toArgb()
      WindowCompat.getInsetsController(window, view).apply {
        isAppearanceLightStatusBars = true
        isAppearanceLightNavigationBars = true
      }
    }
  }

  MaterialTheme(
    colorScheme = UltimateAdminColorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  UltimateAdminTheme(content = content)
}
