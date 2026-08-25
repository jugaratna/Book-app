package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DocumentVersion
import com.example.data.model.SourceMaterial
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceMaterialDao {
    @Query("SELECT * FROM source_materials ORDER BY uploadedAt DESC")
    fun getAllSources(): Flow<List<SourceMaterial>>

    @Query("SELECT * FROM source_materials WHERE documentId = :docId OR documentId = 0 ORDER BY uploadedAt DESC")
    fun getSourcesForDocument(docId: Long): Flow<List<SourceMaterial>>

    @Query("SELECT * FROM source_materials WHERE documentId = :docId OR documentId = 0")
    suspend fun getSourcesForDocumentOnce(docId: Long): List<SourceMaterial>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: SourceMaterial): Long

    @Delete
    suspend fun deleteSource(source: SourceMaterial)

    @Query("DELETE FROM source_materials WHERE id = :id")
    suspend fun deleteSourceById(id: Long)
}

@Dao
interface VersionDao {
    @Query("SELECT * FROM document_versions WHERE documentId = :docId ORDER BY versionNumber DESC")
    fun getVersionsForDocument(docId: Long): Flow<List<DocumentVersion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: DocumentVersion): Long

    @Query("DELETE FROM document_versions WHERE documentId = :docId")
    suspend fun deleteVersionsForDocument(docId: Long)
}
