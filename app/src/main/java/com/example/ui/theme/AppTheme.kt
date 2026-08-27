package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppTheme(
    val displayName: String,
    val description: String,
    val primaryColor: Color,
    val isDark: Boolean = false
) {
    MEDICAL_LIGHT(
        displayName = "Clinical Light",
        description = "Clean, professional light theme with medical blue accents",
        primaryColor = MedicalBluePrimary,
        isDark = false
    ),
    MEDICAL_DARK(
        displayName = "Clinical Dark",
        description = "Dark mode with deep navy tones and bright teal accents",
        primaryColor = MedicalTealAccent,
        isDark = true
    ),
    OCEAN_DARK(
        displayName = "Ocean Dark",
        description = "Deep ocean-inspired dark theme with subtle blue gradients",
        primaryColor = Color(0xFF1E40AF),
        isDark = true
    ),
    FOREST_LIGHT(
        displayName = "Forest Light",
        description = "Nature-inspired light theme with green clinical accents",
        primaryColor = ClinicalGreen,
        isDark = false
    )
}
