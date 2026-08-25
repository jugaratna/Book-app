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
    primary = MedicalBlueLight,
    onPrimary = MedicalNavy900,
    primaryContainer = MedicalNavy800,
    onPrimaryContainer = MedicalBlueLight,
    secondary = MedicalTealAccent,
    onSecondary = Color.Black,
    secondaryContainer = MedicalNavy700,
    onSecondaryContainer = MedicalTealSoft,
    tertiary = ClinicalAmber,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceCardDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderSubtleDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MedicalBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = MedicalTealPrimary,
    onSecondary = Color.White,
    secondaryContainer = MedicalTealSoft,
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = ClinicalAmber,
    background = BackgroundPaper,
    surface = SurfaceCardLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderSubtleLight,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
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

