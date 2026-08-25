package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "source_materials")
data class SourceMaterial(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long = 0, // 0 for global knowledge base, or attached to specific document
    val title: String,
    val fileType: String, // "PDF", "DOCX", "IMAGE", "TXT", "XRAY", "CT_SCAN", "GUIDELINE"
    val rawText: String,
    val extractedSummary: String = "",
    val extractedKeyPoints: String = "",
    val extractedClassifications: String = "",
    val extractedTables: String = "",
    val fileSize: String = "1.2 MB",
    val isProcessed: Boolean = true,
    val uploadedAt: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
        return sdf.format(Date(uploadedAt))
    }
}

@Entity(tableName = "document_versions")
data class DocumentVersion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val versionNumber: Int,
    val changeDescription: String,
    val contentSnapshot: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
