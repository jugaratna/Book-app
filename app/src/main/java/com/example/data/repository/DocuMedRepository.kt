package com.example.data.repository

import com.example.data.local.DocumentDao
import com.example.data.local.SavedFileDao
import com.example.data.local.SourceMaterialDao
import com.example.data.local.VersionDao
import com.example.data.model.DocumentVersion
import com.example.data.model.MedicalDocument
import com.example.data.model.SavedFile
import com.example.data.model.SourceMaterial
import kotlinx.coroutines.flow.Flow

class DocuMedRepository(
    private val documentDao: DocumentDao,
    private val sourceMaterialDao: SourceMaterialDao,
    private val versionDao: VersionDao,
    private val savedFileDao: SavedFileDao
) {
    // Documents
    val allDocuments: Flow<List<MedicalDocument>> = documentDao.getAllDocuments()
    val favoriteDocuments: Flow<List<MedicalDocument>> = documentDao.getFavoriteDocuments()
    val allSources: Flow<List<SourceMaterial>> = sourceMaterialDao.getAllSources()

    // Saved Files (PPT, PDF, Word)
    val allSavedFiles: Flow<List<SavedFile>> = savedFileDao.getAllSavedFiles()

    fun getDocumentById(id: Long): Flow<MedicalDocument?> = documentDao.getDocumentById(id)
    suspend fun getDocumentByIdOnce(id: Long): MedicalDocument? = documentDao.getDocumentByIdOnce(id)

    fun searchDocuments(query: String): Flow<List<MedicalDocument>> =
        if (query.isBlank()) documentDao.getAllDocuments() else documentDao.searchDocuments(query)

    fun getDocumentsBySpecialty(specialty: String): Flow<List<MedicalDocument>> =
        if (specialty == "All") documentDao.getAllDocuments() else documentDao.getDocumentsBySpecialty(specialty)

    fun getDocumentsByType(docType: String): Flow<List<MedicalDocument>> =
        if (docType == "All") documentDao.getAllDocuments() else documentDao.getDocumentsByType(docType)

    suspend fun insertDocument(doc: MedicalDocument): Long = documentDao.insertDocument(doc)

    suspend fun updateDocument(doc: MedicalDocument) = documentDao.updateDocument(doc)

    suspend fun deleteDocument(doc: MedicalDocument) {
        versionDao.deleteVersionsForDocument(doc.id)
        documentDao.deleteDocument(doc)
    }

    suspend fun deleteDocumentById(id: Long) {
        versionDao.deleteVersionsForDocument(id)
        documentDao.deleteDocumentById(id)
    }

    // Saved Files CRUD (PPT, PDF, Word)
    fun getSavedFilesByType(type: String): Flow<List<SavedFile>> =
        if (type == "All") savedFileDao.getAllSavedFiles() else savedFileDao.getSavedFilesByType(type)

    fun getSavedFileById(id: Long): Flow<SavedFile?> = savedFileDao.getSavedFileById(id)

    suspend fun getSavedFileByIdOnce(id: Long): SavedFile? = savedFileDao.getSavedFileByIdOnce(id)

    suspend fun insertSavedFile(file: SavedFile): Long = savedFileDao.insertSavedFile(file)

    suspend fun updateSavedFile(file: SavedFile) = savedFileDao.updateSavedFile(file)

    suspend fun deleteSavedFile(file: SavedFile) = savedFileDao.deleteSavedFile(file)

    suspend fun deleteSavedFileById(id: Long) = savedFileDao.deleteSavedFileById(id)

    // Source Materials
    fun getSourcesForDocument(docId: Long): Flow<List<SourceMaterial>> =
        sourceMaterialDao.getSourcesForDocument(docId)

    suspend fun getSourcesForDocumentOnce(docId: Long): List<SourceMaterial> =
        sourceMaterialDao.getSourcesForDocumentOnce(docId)

    suspend fun insertSource(source: SourceMaterial): Long =
        sourceMaterialDao.insertSource(source)

    suspend fun deleteSource(source: SourceMaterial) =
        sourceMaterialDao.deleteSource(source)

    suspend fun deleteSourceById(id: Long) =
        sourceMaterialDao.deleteSourceById(id)

    // Version History
    fun getVersionsForDocument(docId: Long): Flow<List<DocumentVersion>> =
        versionDao.getVersionsForDocument(docId)

    suspend fun saveVersionSnapshot(docId: Long, versionNumber: Int, description: String, content: String) {
        versionDao.insertVersion(
            DocumentVersion(
                documentId = docId,
                versionNumber = versionNumber,
                changeDescription = description,
                contentSnapshot = content
            )
        )
    }

    // Offline Backup & Restore Support
    suspend fun getAllDocumentsOnce(): List<MedicalDocument> = documentDao.getAllDocumentsOnce()
    suspend fun getAllSourcesOnce(): List<SourceMaterial> = sourceMaterialDao.getAllSourcesOnce()
    suspend fun getAllVersionsOnce(): List<DocumentVersion> = versionDao.getAllVersionsOnce()
    suspend fun getAllSavedFilesOnce(): List<SavedFile> = savedFileDao.getAllSavedFilesOnce()

    suspend fun restoreBackup(
        documents: List<MedicalDocument>,
        sources: List<SourceMaterial>,
        versions: List<DocumentVersion>,
        savedFiles: List<SavedFile> = emptyList()
    ) {
        documents.forEach { documentDao.insertDocument(it) }
        sources.forEach { sourceMaterialDao.insertSource(it) }
        versions.forEach { versionDao.insertVersion(it) }
        savedFiles.forEach { savedFileDao.insertSavedFile(it) }
    }
}
