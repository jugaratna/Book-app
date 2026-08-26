package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "saved_files")
data class SavedFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val fileType: String, // "PPT", "PDF", "DOCX"
    val description: String = "",
    val content: String = "", // Full text or HTML
    val slidesJson: String = "", // For PPT decks, stored as JSON string for complete slide-by-slide editing
    val fileSize: String = "1.2 MB",
    val documentId: Long = 0,
    val driveLink: String = "", // Optional Google Drive URL
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
        return sdf.format(Date(updatedAt))
    }
}
