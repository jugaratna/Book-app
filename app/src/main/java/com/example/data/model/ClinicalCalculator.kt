package com.example.data.model

import java.util.UUID

enum class CalculatorCategory(val displayName: String, val iconName: String) {
    CARDIOVASCULAR("Cardiovascular", "Favorite"),
    PULMONARY_ICU("Pulmonary & Critical Care", "Air"),
    NEPHROLOGY("Nephrology & Renal", "WaterDrop"),
    HEPATOLOGY("Gastro & Hepatology", "LocalHospital"),
    NEUROLOGY("Neurology & Emergency", "Psychology"),
    HEMATOLOGY("Hematology & Oncology", "Bloodtype")
}

enum class RiskSeverity(val label: String, val colorHex: Long) {
    LOW("Low Risk", 0xFF16A34A),
    MODERATE("Moderate Risk", 0xFFD97706),
    HIGH("High Risk", 0xFFDC2626),
    CRITICAL("Critical / Emergency", 0xFF991B1B)
}

data class CalculationResult(
    val calculatorId: String,
    val calculatorName: String,
    val scoreValue: String,
    val riskCategory: RiskSeverity,
    val interpretation: String,
    val recommendations: List<String>,
    val evidenceReference: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMarkdownBlock(): String {
        return """
            > **[CLINICAL SCORING: $calculatorName]**
            > **Score / Result:** $scoreValue (${riskCategory.label})
            > **Clinical Interpretation:** $interpretation
            > **Management Recommendations:**
            >${recommendations.joinToString("\n") { "> - $it" }}
            > *Evidence Base:* $evidenceReference
        """.trimIndent()
    }
}

data class CalculatorQuestion(
    val id: String,
    val prompt: String,
    val points: Double,
    val explanation: String = ""
)
