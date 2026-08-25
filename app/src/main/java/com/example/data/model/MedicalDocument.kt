package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "medical_documents")
data class MedicalDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val docType: String, // "Textbook Chapter", "Clinical Protocol", "Lecture Notes", "Case Report", "OSCE Station", "Question Bank", "Research Paper", "Study Guide"
    val specialty: String, // "Orthopedics", "Cardiology", "Neurology", "General Surgery", "Pediatrics", "Internal Medicine", "Emergency Medicine"
    val targetAudience: String = "Postgraduate", // "Undergraduate", "Postgraduate", "Specialist", "Patient Education"
    val content: String,
    val authors: String = "Medical Author / AI Co-Pilot",
    val institution: String = "DocuMed Academic Studio",
    val wordCount: Int = 0,
    val version: Int = 1,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
        return sdf.format(Date(updatedAt))
    }
}

data class TocItem(
    val level: Int, // 1 for H1, 2 for H2, 3 for H3
    val number: String, // e.g. "1.0", "1.1", "1.1.1"
    val title: String,
    val rawLine: String
)

data class MedicalReference(
    val id: Int,
    val citationKey: String, // e.g. "[1]"
    val authors: String,
    val title: String,
    val journalOrBook: String,
    val year: String,
    val doi: String = "",
    val pmid: String = "",
    val url: String = "",
    val isVerified: Boolean = true
)

data class MedicalTable(
    val id: String,
    val tableNumber: Int,
    val caption: String,
    val headers: List<String>,
    val rows: List<List<String>>
)

data class FlowchartNode(
    val id: String,
    val stepNumber: Int,
    val title: String,
    val description: String,
    val decisionTag: String = "", // e.g. "Initial Assessment", "Decision Point", "Surgical Path", "Conservative Path", "Outcome"
    val nextStepId: String? = null
)

data class MedicalFigure(
    val id: String,
    val figureNumber: Int,
    val title: String,
    val caption: String,
    val category: String, // "Anatomical", "Radiology (X-Ray/CT)", "Surgical Step", "Clinical Algorithm", "Pathology"
    val sourceOrReference: String = "DocuMed Clinical Archive",
    val imageUrl: String? = null
)

data class McqItem(
    val id: String,
    val questionNumber: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val learningObjective: String,
    val selectedOptionIndex: Int = -1
)

data class VivaItem(
    val question: String,
    val modelAnswer: String,
    val highYieldKeywords: List<String>
)

data class OsceStation(
    val stationTitle: String,
    val clinicalScenario: String,
    val candidateInstructions: String,
    val subQuestions: List<String>,
    val expectedAnswers: List<String>,
    val markingScheme: List<String>
)

data class FlashcardItem(
    val id: String,
    val front: String,
    val back: String,
    val category: String,
    val isMastered: Boolean = false
)
