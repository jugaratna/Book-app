package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MedicalDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM medical_documents ORDER BY updatedAt DESC")
    fun getAllDocuments(): Flow<List<MedicalDocument>>

    @Query("SELECT * FROM medical_documents ORDER BY updatedAt DESC")
    suspend fun getAllDocumentsOnce(): List<MedicalDocument>

    @Query("SELECT * FROM medical_documents WHERE id = :id LIMIT 1")
    fun getDocumentById(id: Long): Flow<MedicalDocument?>

    @Query("SELECT * FROM medical_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentByIdOnce(id: Long): MedicalDocument?

    @Query("SELECT * FROM medical_documents WHERE specialty = :specialty ORDER BY updatedAt DESC")
    fun getDocumentsBySpecialty(specialty: String): Flow<List<MedicalDocument>>

    @Query("SELECT * FROM medical_documents WHERE docType = :docType ORDER BY updatedAt DESC")
    fun getDocumentsByType(docType: String): Flow<List<MedicalDocument>>

    @Query("SELECT * FROM medical_documents WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteDocuments(): Flow<List<MedicalDocument>>

    @Query("SELECT * FROM medical_documents WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchDocuments(query: String): Flow<List<MedicalDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: MedicalDocument): Long

    @Update
    suspend fun updateDocument(doc: MedicalDocument)

    @Delete
    suspend fun deleteDocument(doc: MedicalDocument)

    @Query("DELETE FROM medical_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)
}
