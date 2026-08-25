package com.example.data.repository

import com.example.data.local.DocumentDao
import com.example.data.local.SourceMaterialDao
import com.example.data.local.VersionDao
import com.example.data.model.DocumentVersion
import com.example.data.model.MedicalDocument
import com.example.data.model.SourceMaterial
import kotlinx.coroutines.flow.Flow

class DocuMedRepository(
    private val documentDao: DocumentDao,
    private val sourceMaterialDao: SourceMaterialDao,
    private val versionDao: VersionDao
) {
    val allDocuments: Flow<List<MedicalDocument>> = documentDao.getAllDocuments()
    val favoriteDocuments: Flow<List<MedicalDocument>> = documentDao.getFavoriteDocuments()
    val allSources: Flow<List<SourceMaterial>> = sourceMaterialDao.getAllSources()

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
}
