package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = JobPulseBlue,
    onPrimary = Color.White,
    primaryContainer = JobPulseBlueDark,
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = JobPulseTealLight,
    onSecondary = Color(0xFF003831),
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = Color(0xFF70F7E3),
    tertiary = JobPulseAmber,
    onTertiary = Color.White,
    background = JobPulseBgDark,
    onBackground = JobPulseTextPrimaryDark,
    surface = JobPulseSurfaceDark,
    onSurface = JobPulseTextPrimaryDark,
    surfaceVariant = JobPulseSurfaceVariantDark,
    onSurfaceVariant = JobPulseTextSecondaryDark,
    outline = JobPulseOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = JobPulseBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = JobPulseBlueDark,
    secondary = JobPulseTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF004D40),
    tertiary = JobPulseAmber,
    onTertiary = Color.White,
    background = JobPulseBgLight,
    onBackground = JobPulseTextPrimaryLight,
    surface = JobPulseSurfaceLight,
    onSurface = JobPulseTextPrimaryLight,
    surfaceVariant = JobPulseSurfaceVariantLight,
    onSurfaceVariant = JobPulseTextSecondaryLight,
    outline = JobPulseOutlineLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
