package com.example.data.model

enum class DrugCategory(val displayName: String) {
    ANTIBIOTICS("Antibiotics & Anti-Infectives"),
    CARDIOVASCULAR("Cardiovascular & Antihypertensives"),
    ANTICOAGULANTS("Anticoagulants & Antiplatelets"),
    ANALGESICS_EMERGENCY("Analgesics & Emergency Drugs"),
    ENDOCRINE_METABOLIC("Endocrine & Diabetes"),
    PSYCHIATRIC_NEURO("Neurology & Psychiatry"),
    IMMUNOLOGY_ONCOLOGY("Immunology & Oncology")
}

enum class InteractionSeverity(val label: String, val colorHex: Long) {
    MAJOR_AVOID("Major / Contraindicated", 0xFFDC2626),
    MODERATE_MONITOR("Moderate / Adjust Dose", 0xFFD97706),
    MINOR_SAFE("Minor / Caution", 0xFF2563EB)
}

data class DrugMonograph(
    val id: String,
    val genericName: String,
    val brandNames: String,
    val category: DrugCategory,
    val standardAdultDose: String,
    val pediatricDoseMgPerKg: Double? = null,
    val pediatricMaxSingleDoseMg: Double? = null,
    val pediatricFrequency: String? = null,
    val renalAdjustment: String,
    val hepaticAdjustment: String,
    val blackBoxWarning: String? = null,
    val contraindications: List<String>,
    val commonAdverseEffects: List<String>,
    val monitoringParameters: List<String>,
    val pregnancyCategory: String = "Category B / C (Check clinical risk)"
)

data class DrugInteractionPair(
    val drug1Name: String,
    val drug2Name: String,
    val severity: InteractionSeverity,
    val mechanism: String,
    val clinicalEffect: String,
    val managementAction: String
)
