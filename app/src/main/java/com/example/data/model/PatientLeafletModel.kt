package com.example.data.model

enum class LeafletLanguage(val code: String, val displayName: String, val flagEmoji: String) {
    ENGLISH("en", "English", "🇺🇸"),
    SPANISH("es", "Español (Spanish)", "🇪🇸"),
    FRENCH("fr", "Français (French)", "🇫🇷"),
    ARABIC("ar", "العربية (Arabic)", "🇸🇦"),
    HINDI("hi", "हिन्दी (Hindi)", "🇮🇳"),
    MANDARIN("zh", "中文 (Mandarin)", "🇨🇳"),
    GERMAN("de", "Deutsch (German)", "🇩🇪"),
    PORTUGUESE("pt", "Português (Portuguese)", "🇧🇷")
}

enum class ReadingLevel(val label: String, val description: String) {
    SIMPLIFIED("6th Grade (Very Simple)", "Short sentences, common everyday words, clear bullet points"),
    STANDARD("8th Grade (Standard Patient)", "Clear medical explanations with definitions"),
    ADVANCED("High School (In-depth)", "Detailed explanation of disease mechanisms & drug actions")
}

data class PatientInformationLeaflet(
    val title: String,
    val language: LeafletLanguage,
    val readingLevel: ReadingLevel,
    val conditionSummary: String,
    val symptomsToWatch: List<String>,
    val treatmentPlan: List<String>,
    val medicationInstructions: List<String>,
    val emergencyRedFlags: List<String>,
    val questionsForDoctor: List<String>,
    val lifestyleAdvice: List<String>,
    val generatedTimestamp: Long = System.currentTimeMillis()
) {
    fun toMarkdownBlock(): String {
        return buildString {
            appendLine("## 🏥 Patient Information Leaflet: $title")
            appendLine("**Language:** ${language.flagEmoji} ${language.displayName} | **Reading Level:** ${readingLevel.label}\n")
            appendLine("### What is this condition?")
            appendLine(conditionSummary)
            appendLine()
            if (symptomsToWatch.isNotEmpty()) {
                appendLine("### Common Symptoms & Signs")
                symptomsToWatch.forEach { appendLine("- $it") }
                appendLine()
            }
            if (treatmentPlan.isNotEmpty()) {
                appendLine("### Your Treatment & Care Plan")
                treatmentPlan.forEach { appendLine("- $it") }
                appendLine()
            }
            if (medicationInstructions.isNotEmpty()) {
                appendLine("### Medication Instructions & Schedule")
                medicationInstructions.forEach { appendLine("- 💊 $it") }
                appendLine()
            }
            if (emergencyRedFlags.isNotEmpty()) {
                appendLine("### 🚨 Emergency Warning Signs (Call 911 / Go to ER)")
                emergencyRedFlags.forEach { appendLine("- **$it**") }
                appendLine()
            }
            if (lifestyleAdvice.isNotEmpty()) {
                appendLine("### Daily Lifestyle & Home Recovery")
                lifestyleAdvice.forEach { appendLine("- $it") }
                appendLine()
            }
            if (questionsForDoctor.isNotEmpty()) {
                appendLine("### Questions to Ask Your Healthcare Team")
                questionsForDoctor.forEach { appendLine("- $it") }
                appendLine()
            }
        }
    }
}
