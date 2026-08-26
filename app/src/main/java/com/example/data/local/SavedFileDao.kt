package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SavedFile
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedFileDao {
    @Query("SELECT * FROM saved_files ORDER BY updatedAt DESC")
    fun getAllSavedFiles(): Flow<List<SavedFile>>

    @Query("SELECT * FROM saved_files ORDER BY updatedAt DESC")
    suspend fun getAllSavedFilesOnce(): List<SavedFile>

    @Query("SELECT * FROM saved_files WHERE id = :id LIMIT 1")
    fun getSavedFileById(id: Long): Flow<SavedFile?>

    @Query("SELECT * FROM saved_files WHERE id = :id LIMIT 1")
    suspend fun getSavedFileByIdOnce(id: Long): SavedFile?

    @Query("SELECT * FROM saved_files WHERE fileType = :type ORDER BY updatedAt DESC")
    fun getSavedFilesByType(type: String): Flow<List<SavedFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedFile(file: SavedFile): Long

    @Update
    suspend fun updateSavedFile(file: SavedFile)

    @Delete
    suspend fun deleteSavedFile(file: SavedFile)

    @Query("DELETE FROM saved_files WHERE id = :id")
    suspend fun deleteSavedFileById(id: Long)
}
