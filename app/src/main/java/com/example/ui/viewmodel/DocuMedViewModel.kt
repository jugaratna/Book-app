package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.MedicalAiService
import com.example.data.export.DocumentExportHelper
import com.example.data.local.DocuMedDatabase
import com.example.data.model.AiEngine
import com.example.data.model.CitationEntry
import com.example.data.model.CitationStyle
import com.example.data.model.DocumentVersion
import com.example.data.model.FlashcardItem
import com.example.data.model.McqItem
import com.example.data.model.MedicalDocument
import com.example.data.model.MedicalPresentation
import com.example.data.model.MedicalTemplate
import com.example.data.model.OsceStation
import com.example.data.model.PredefinedMedicalCitations
import com.example.data.model.PresentationSlide
import com.example.data.model.SavedFile
import com.example.data.model.SourceMaterial
import com.example.data.model.VivaItem
import com.example.data.model.CalculationResult
import com.example.data.model.DrugMonograph
import com.example.data.model.DrugInteractionPair
import com.example.data.model.AnnotatedFigureData
import com.example.data.model.PatientInformationLeaflet
import com.example.data.model.LeafletLanguage
import com.example.data.model.ReadingLevel
import com.example.data.model.CollaboratorPresence
import com.example.data.model.CommentReply
import com.example.data.model.CommentStatus
import com.example.data.model.CommentType
import com.example.data.model.DocumentComment
import com.example.data.model.DocumentPermissionLevel
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.data.repository.CollaborationRepository
import com.example.data.repository.DocuMedRepository
import com.example.data.repository.DrugFormularyRepository
import com.example.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

enum class AppNavTab {
    LIBRARY,
    EDITOR,
    AI_STUDIO,
    SAVED_FILES,
    KNOWLEDGE_BASE,
    EXPORT_PREVIEW,
    SETTINGS
}

class DocuMedViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DocuMedDatabase.getDatabase(application, viewModelScope)
    private val repository = DocuMedRepository(
        database.documentDao(),
        database.sourceMaterialDao(),
        database.versionDao(),
        database.savedFileDao()
    )
    private val aiService = MedicalAiService()
    private val collaborationRepo = CollaborationRepository()

    // Nav State
    private val _currentTab = MutableStateFlow(AppNavTab.LIBRARY)
    val currentTab: StateFlow<AppNavTab> = _currentTab

    // AI Engine Selection
    val selectedAiEngine = MutableStateFlow(AiEngine.GEMINI)

    fun setSelectedAiEngine(engine: AiEngine) {
        selectedAiEngine.value = engine
    }

    // Google Drive Integration
    val googleDriveUrl = MutableStateFlow("https://drive.google.com/drive/my-drive")

    fun setGoogleDriveUrl(url: String) {
        if (url.isNotBlank()) {
            googleDriveUrl.value = url.trim()
        }
    }

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedSpecialty = MutableStateFlow("All")
    val selectedDocType = MutableStateFlow("All")

    // Active Document
    private val _selectedDocument = MutableStateFlow<MedicalDocument?>(null)
    val selectedDocument: StateFlow<MedicalDocument?> = _selectedDocument

    // Multi-User & Collaboration State
    val users: StateFlow<List<UserProfile>> = collaborationRepo.users
    val currentUser: StateFlow<UserProfile> = collaborationRepo.currentUser
    val activeCollaborators: StateFlow<List<CollaboratorPresence>> = collaborationRepo.activeCollaborators

    // In-document comments mapped to currently selected document
    val activeDocumentComments: StateFlow<List<DocumentComment>> = combine(
        _selectedDocument,
        collaborationRepo.documentComments
    ) { doc, commentsMap ->
        if (doc != null) commentsMap[doc.id] ?: emptyList() else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Role & Document Permission checks
    val canEditCurrentDocument: StateFlow<Boolean> = combine(
        currentUser,
        _selectedDocument
    ) { user, doc ->
        if (doc == null) false
        else user.hasPermissionForDocument(doc.id, DocumentPermissionLevel.FULL)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val canViewCurrentDocument: StateFlow<Boolean> = combine(
        currentUser,
        _selectedDocument
    ) { user, doc ->
        if (doc == null) false
        else user.hasPermissionForDocument(doc.id, DocumentPermissionLevel.VIEW)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentDocumentPermissionLevel: StateFlow<DocumentPermissionLevel> = combine(
        currentUser,
        _selectedDocument
    ) { user, doc ->
        if (doc == null) DocumentPermissionLevel.NONE
        else user.getEffectivePermission(doc.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DocumentPermissionLevel.FULL)

    // In-document editor content state
    val editorContent = MutableStateFlow("")
    val editorTitle = MutableStateFlow("")
    val editorSearchQuery = MutableStateFlow("")

    // All Documents with filter
    val documents: StateFlow<List<MedicalDocument>> = combine(
        repository.allDocuments,
        searchQuery,
        selectedSpecialty,
        selectedDocType
    ) { docs, query, specialty, type ->
        docs.filter { doc ->
            val matchQuery = query.isBlank() || doc.title.contains(query, ignoreCase = true) || doc.content.contains(query, ignoreCase = true)
            val matchSpecialty = specialty == "All" || doc.specialty.equals(specialty, ignoreCase = true)
            val matchType = type == "All" || doc.docType.equals(type, ignoreCase = true)
            matchQuery && matchSpecialty && matchType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Saved Files Hub (PPT, PDF, Word)
    val savedFilesFilter = MutableStateFlow("All") // "All", "PPT", "PDF", "DOCX"
    val savedFilesSearchQuery = MutableStateFlow("")

    val savedFiles: StateFlow<List<SavedFile>> = combine(
        repository.allSavedFiles,
        savedFilesFilter,
        savedFilesSearchQuery
    ) { files, filter, query ->
        files.filter { file ->
            val matchesFilter = filter == "All" || file.fileType.equals(filter, ignoreCase = true)
            val matchesQuery = query.isBlank() || file.title.contains(query, ignoreCase = true) || file.description.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active PPT Presentation Deck for Editing / Viewing
    val activeSlideDeckForEditing = MutableStateFlow<MedicalPresentation?>(null)
    val editingSavedFile = MutableStateFlow<SavedFile?>(null)

    // Document Versions for active document
    val versions: StateFlow<List<DocumentVersion>> = _selectedDocument
        .flatMapLatest { doc ->
            if (doc != null) repository.getVersionsForDocument(doc.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sources Knowledge Base
    val sourceMaterials: StateFlow<List<SourceMaterial>> = repository.allSources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Auto-save and sync status to local storage
    private val _lastSavedTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastSavedTimestamp: StateFlow<Long> = _lastSavedTimestamp

    private val _isAutoSaving = MutableStateFlow(false)
    val isAutoSaving: StateFlow<Boolean> = _isAutoSaving

    init {
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            combine(editorContent, editorTitle) { c, t -> c to t }
                .debounce(1500L)
                .collect { (content, title) ->
                    val current = _selectedDocument.value ?: return@collect
                    val newTitle = title.ifBlank { current.title }
                    if (content.isNotBlank() && (content != current.content || newTitle != current.title)) {
                        _isAutoSaving.value = true
                        val wordCount = content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                        val now = System.currentTimeMillis()
                        val updated = current.copy(
                            title = newTitle,
                            content = content,
                            wordCount = wordCount,
                            updatedAt = now
                        )
                        withContext(Dispatchers.IO) {
                            repository.updateDocument(updated)
                        }
                        _selectedDocument.value = updated
                        _lastSavedTimestamp.value = now
                        _isAutoSaving.value = false
                    }
                }
        }
    }

    // App Theme Management
    val appTheme = MutableStateFlow<AppTheme>(AppTheme.MEDICAL_LIGHT)

    fun setAppTheme(theme: AppTheme) {
        appTheme.value = theme
    }

    // AI Generation States
    val isAiGenerating = MutableStateFlow(false)
    val aiStatusMessage = MutableStateFlow("")

    val generatedSummary = MutableStateFlow("")
    val generatedMCQs = MutableStateFlow<List<McqItem>>(emptyList())
    val generatedViva = MutableStateFlow<List<VivaItem>>(emptyList())
    val generatedOSCE = MutableStateFlow<OsceStation?>(null)
    val generatedFlashcards = MutableStateFlow<List<FlashcardItem>>(emptyList())
    val generatedTable = MutableStateFlow("")
    val generatedFlowchart = MutableStateFlow("")
    val generatedImageAnalysis = MutableStateFlow("")
    val generatedPresentation = MutableStateFlow<MedicalPresentation?>(null)
    val generatedPatientLeaflet = MutableStateFlow<PatientInformationLeaflet?>(null)

    // Drug Formulary Repository
    val drugFormularyRepository = DrugFormularyRepository()

    // Knowledge Base Chat Messages
    private val _chatMessages = MutableStateFlow<List<Pair<String, String>>>(
        listOf(
            "DocuMed AI" to "Hello Doctor! I am your multi-source academic medical assistant. Ask me questions about your uploaded documents, clinical guidelines, surgical procedures, or case presentations."
        )
    )
    val chatMessages: StateFlow<List<Pair<String, String>>> = _chatMessages

    // Navigation
    fun navigateTo(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun selectDocument(doc: MedicalDocument) {
        _selectedDocument.value = doc
        editorContent.value = doc.content
        editorTitle.value = doc.title
        _lastSavedTimestamp.value = doc.updatedAt
        _currentTab.value = AppNavTab.EDITOR
    }

    fun createNewDocument(
        title: String,
        docType: String,
        specialty: String,
        targetAudience: String,
        initialContent: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val content = if (initialContent.isNotBlank()) initialContent else """
# 1.0 Clinical Overview of $title
A comprehensive medical guide for $specialty ($targetAudience).

## 1.1 Anatomy & Pathophysiology
[KEY_POINT: Essential clinical high-yield takeaway.]

## 1.2 Clinical Presentation & Physical Examination
* Primary Signs & Symptoms
* Diagnostic Pitfalls

## 1.3 Therapeutic Algorithm
* Conservative Management
* Surgical Interventions

[WARNING: Red flags, contraindications, and emergency indications.]

## 1.4 References
1. DocuMed Clinical Reference. 2026.
            """.trimIndent()

            val wordCount = content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
            val doc = MedicalDocument(
                title = title.ifBlank { "Untitled Medical Note" },
                docType = docType,
                specialty = specialty,
                targetAudience = targetAudience,
                content = content,
                wordCount = wordCount,
                version = 1
            )
            val id = repository.insertDocument(doc)
            repository.saveVersionSnapshot(id, 1, "Initial Document Creation", content)
            val created = repository.getDocumentByIdOnce(id)
            if (created != null) {
                _selectedDocument.value = created
                editorContent.value = created.content
                editorTitle.value = created.title
                _lastSavedTimestamp.value = created.updatedAt
                _currentTab.value = AppNavTab.EDITOR
            }
        }
    }

    fun saveCurrentDocument(snapshotDescription: String = "Autosave update") {
        val current = _selectedDocument.value ?: return
        val newContent = editorContent.value
        val newTitle = editorTitle.value.ifBlank { current.title }
        val wordCount = newContent.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val newVersion = current.version + 1
        val now = System.currentTimeMillis()

        val updated = current.copy(
            title = newTitle,
            content = newContent,
            wordCount = wordCount,
            version = newVersion,
            updatedAt = now
        )

        viewModelScope.launch(Dispatchers.IO) {
            _isAutoSaving.value = true
            repository.updateDocument(updated)
            repository.saveVersionSnapshot(current.id, newVersion, snapshotDescription, newContent)
            _selectedDocument.value = updated
            _lastSavedTimestamp.value = now
            _isAutoSaving.value = false
        }
    }

    fun updateEditorContent(newContent: String) {
        editorContent.value = newContent
    }

    // Citation Management
    val activeCitationStyle = MutableStateFlow<CitationStyle>(CitationStyle.VANCOUVER)
    val documentCitations = MutableStateFlow<List<CitationEntry>>(PredefinedMedicalCitations.sampleCitations.take(3))

    fun setCitationStyle(style: CitationStyle) {
        activeCitationStyle.value = style
    }

    fun insertCitationInText(citationKey: String) {
        val current = editorContent.value
        editorContent.value = if (current.isBlank()) citationKey else "$current $citationKey"
        saveCurrentDocument("Inserted citation $citationKey")
    }

    fun updateDocumentBibliography(formattedBibliography: String, updatedCitations: List<CitationEntry>, style: CitationStyle) {
        activeCitationStyle.value = style
        documentCitations.value = updatedCitations
        val current = editorContent.value
        // If references section already exists in doc, replace or append
        val referencesRegex = Regex("""\n*##\s*References[\s\S]*$""", RegexOption.IGNORE_CASE)
        val newContent = if (referencesRegex.containsMatchIn(current)) {
            current.replace(referencesRegex, formattedBibliography)
        } else {
            "$current$formattedBibliography"
        }
        editorContent.value = newContent
        saveCurrentDocument("Updated references in ${style.name} format")
    }

    // Template Library Operations
    fun createDocumentFromTemplate(template: MedicalTemplate) {
        createNewDocument(
            title = template.defaultTitle,
            docType = template.docType,
            specialty = template.specialty,
            targetAudience = template.defaultAudience,
            initialContent = template.templateContent
        )
    }

    fun insertTemplateIntoCurrentDoc(template: MedicalTemplate) {
        val current = editorContent.value
        editorContent.value = if (current.isBlank()) {
            template.templateContent
        } else {
            "$current\n\n---\n\n${template.templateContent}"
        }
        saveCurrentDocument("Applied template: ${template.title}")
    }

    fun applyVocabularyCorrections(correctedText: String) {
        editorContent.value = correctedText
        saveCurrentDocument("Applied medical vocabulary and safety corrections")
    }

    fun appendContentToEditor(textToAppend: String) {
        val current = editorContent.value
        editorContent.value = if (current.isBlank()) textToAppend else "$current\n\n$textToAppend"
        saveCurrentDocument("Appended AI-generated section")
    }

    fun insertPhotoIntoEditor(photoUriOrUrl: String, caption: String, clinicalNotes: String = "") {
        val cleanCaption = caption.ifBlank { "Clinical Diagnostic Figure" }
        val figureBlock = buildString {
            append("![${cleanCaption}](${photoUriOrUrl.trim()})\n")
            if (clinicalNotes.isNotBlank()) {
                append("[FIGURE: ${cleanCaption} | ${clinicalNotes.trim()} | ${photoUriOrUrl.trim()}]\n")
            } else {
                append("[FIGURE: ${cleanCaption} | Diagnostic Clinical Photo | ${photoUriOrUrl.trim()}]\n")
            }
        }
        appendContentToEditor(figureBlock)
    }

    fun restoreVersion(version: DocumentVersion) {
        editorContent.value = version.contentSnapshot
        saveCurrentDocument("Restored from Version v${version.versionNumber}")
    }

    fun duplicateDocument(doc: MedicalDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            val copy = doc.copy(
                id = 0,
                title = "${doc.title} (Copy)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                version = 1
            )
            val id = repository.insertDocument(copy)
            repository.saveVersionSnapshot(id, 1, "Cloned from ${doc.title}", copy.content)
        }
    }

    fun deleteDocument(doc: MedicalDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDocument(doc)
            if (_selectedDocument.value?.id == doc.id) {
                _selectedDocument.value = null
                editorContent.value = ""
                _currentTab.value = AppNavTab.LIBRARY
            }
        }
    }

    fun toggleFavorite(doc: MedicalDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = doc.copy(isFavorite = !doc.isFavorite, updatedAt = System.currentTimeMillis())
            repository.updateDocument(updated)
            if (_selectedDocument.value?.id == doc.id) {
                _selectedDocument.value = updated
            }
        }
    }

    // ==========================================
    // Saved Files Hub (PPT, PDF, Word) CRUD & Sync
    // ==========================================

    fun saveFileToHub(
        title: String,
        fileType: String, // "PPT", "PDF", "DOCX"
        description: String = "",
        content: String = "",
        slidesJson: String = "",
        driveLink: String = "",
        documentId: Long = _selectedDocument.value?.id ?: 0
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val calcSize = when (fileType) {
                "PPT" -> "1.8 MB"
                "PDF" -> "2.3 MB"
                "DOCX" -> "1.1 MB"
                else -> "1.5 MB"
            }
            val savedFile = SavedFile(
                title = title.ifBlank { "Untitled $fileType File" },
                fileType = fileType.uppercase(),
                description = description.ifBlank { "Generated with ${selectedAiEngine.value.displayName}" },
                content = content.ifBlank { editorContent.value },
                slidesJson = slidesJson,
                fileSize = calcSize,
                documentId = documentId,
                driveLink = driveLink.ifBlank { googleDriveUrl.value },
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertSavedFile(savedFile)
        }
    }

    fun updateSavedFile(file: SavedFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = file.copy(updatedAt = System.currentTimeMillis())
            repository.updateSavedFile(updated)
        }
    }

    fun deleteSavedFile(file: SavedFile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSavedFile(file)
            if (editingSavedFile.value?.id == file.id) {
                editingSavedFile.value = null
            }
        }
    }

    fun openPresentationDeckForEditing(presentation: MedicalPresentation, savedFile: SavedFile? = null) {
        activeSlideDeckForEditing.value = presentation
        editingSavedFile.value = savedFile
    }

    fun updateSlideDeck(presentation: MedicalPresentation) {
        activeSlideDeckForEditing.value = presentation
        generatedPresentation.value = presentation
        val file = editingSavedFile.value
        if (file != null) {
            val json = DocumentExportHelper.slidesToJson(presentation.slides)
            val updated = file.copy(
                title = presentation.title,
                slidesJson = json,
                updatedAt = System.currentTimeMillis()
            )
            viewModelScope.launch(Dispatchers.IO) {
                repository.updateSavedFile(updated)
                editingSavedFile.value = updated
            }
        }
    }

    fun saveCurrentPresentationToHub(
        presentation: MedicalPresentation,
        title: String = "",
        driveLink: String = ""
    ) {
        val presentationTitle = title.ifBlank { presentation.title }
        val json = DocumentExportHelper.slidesToJson(presentation.slides)
        saveFileToHub(
            title = presentationTitle,
            fileType = "PPT",
            description = "${presentation.slides.size}-Slide Deck with Clinical Pearls & Speaker Notes",
            content = editorContent.value,
            slidesJson = json,
            driveLink = driveLink
        )
    }

    fun saveDocumentAsPdfOrWordToHub(
        fileType: String, // "PDF" or "DOCX"
        title: String = "",
        driveLink: String = ""
    ) {
        val doc = _selectedDocument.value
        val fileTitle = title.ifBlank { doc?.title ?: editorTitle.value.ifBlank { "Medical $fileType Document" } }
        val fileContent = editorContent.value.ifBlank { doc?.content ?: "" }
        saveFileToHub(
            title = fileTitle,
            fileType = fileType,
            description = "Publication-ready ${doc?.docType ?: "Medical Document"} (${doc?.specialty ?: "Clinical"})",
            content = fileContent,
            driveLink = driveLink,
            documentId = doc?.id ?: 0
        )
    }

    // --- AI Operations with Engine Selector ---

    fun generateChapterWithAi(
        topic: String,
        specialty: String,
        audience: String,
        sections: List<String>
    ) {
        viewModelScope.launch {
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "Analyzing clinical sources & drafting with ${engine.displayName}..."
            try {
                val sources = repository.getSourcesForDocumentOnce(_selectedDocument.value?.id ?: 0)
                val chapterText = aiService.generateChapter(topic, specialty, audience, sections, sources, engine)
                
                createNewDocument(
                    title = "$topic: Complete Chapter",
                    docType = "Textbook Chapter",
                    specialty = specialty,
                    targetAudience = audience,
                    initialContent = chapterText
                )
                aiStatusMessage.value = "Chapter synthesized with ${engine.displayName}!"
            } catch (e: Exception) {
                aiStatusMessage.value = "Error generating chapter: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun generateSummaryWithAi(summaryType: String, audience: String) {
        viewModelScope.launch {
            val content = editorContent.value.ifBlank { _selectedDocument.value?.content ?: "" }
            if (content.isBlank()) return@launch
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "Synthesizing $summaryType with ${engine.displayName}..."
            try {
                val summary = aiService.generateSummary(content, summaryType, audience, engine)
                generatedSummary.value = summary
                aiStatusMessage.value = "Summary synthesized with ${engine.displayName}."
            } catch (e: Exception) {
                aiStatusMessage.value = "Error: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun generateMCQsWithAi(count: Int = 4, difficulty: String = "Postgraduate") {
        viewModelScope.launch {
            val content = editorContent.value.ifBlank { _selectedDocument.value?.content ?: "" }
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "Generating $count MCQs with ${engine.displayName}..."
            try {
                val mcqs = aiService.generateMCQs(content, count, difficulty, engine)
                generatedMCQs.value = mcqs
                aiStatusMessage.value = "Generated ${mcqs.size} MCQs via ${engine.displayName}."
            } catch (e: Exception) {
                aiStatusMessage.value = "Error: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun generateVivaWithAi(count: Int = 3) {
        viewModelScope.launch {
            val content = editorContent.value.ifBlank { _selectedDocument.value?.content ?: "" }
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "Formulating Viva questions with ${engine.displayName}..."
            try {
                val viva = aiService.generateViva(content, count, engine)
                generatedViva.value = viva
                aiStatusMessage.value = "Viva examination bank generated via ${engine.displayName}."
            } catch (e: Exception) {
                aiStatusMessage.value = "Error: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun generateOSCEWithAi(stationTopic: String) {
        viewModelScope.launch {
            val content = editorContent.value.ifBlank { _selectedDocument.value?.content ?: "" }
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "Constructing OSCE station for '$stationTopic' via ${engine.displayName}..."
            try {
                val osce = aiService.generateOSCE(content, stationTopic, engine)
                generatedOSCE.value = osce
                aiStatusMessage.value = "OSCE Station generated with 8-minute marking scheme via ${engine.displayName}."
            } catch (e: Exception) {
                aiStatusMessage.value = "Error: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun generateFlashcardsWithAi() {
        viewModelScope.launch {
            val content = editorContent.value.ifBlank { _selectedDocument.value?.content ?: "" }
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "Extracting flashcards via ${engine.displayName}..."
            try {
                val cards = aiService.generateFlashcards(content, engine)
                generatedFlashcards.value = cards
                aiStatusMessage.value = "Generated ${cards.size} flashcards via ${engine.displayName}."
            } catch (e: Exception) {
                aiStatusMessage.value = "Error: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun generateTableWithAi(prompt: String = "") {
        viewModelScope.launch {
            val content = editorContent.value.ifBlank { _selectedDocument.value?.content ?: "" }
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "Extracting tabular classifications via ${engine.displayName}..."
            try {
                val table = aiService.generateTable(content, prompt, engine)
                generatedTable.value = table
                aiStatusMessage.value = "Table generated via ${engine.displayName}."
            } catch (e: Exception) {
                aiStatusMessage.value = "Error: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun generateFlowchartWithAi(prompt: String = "") {
        viewModelScope.launch {
            val content = editorContent.value.ifBlank { _selectedDocument.value?.content ?: "" }
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "Generating clinical algorithm flowchart via ${engine.displayName}..."
            try {
                val flowchart = aiService.generateFlowchart(content, prompt, engine)
                generatedFlowchart.value = flowchart
                aiStatusMessage.value = "Flowchart algorithm synthesized via ${engine.displayName}."
            } catch (e: Exception) {
                aiStatusMessage.value = "Error: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun analyzeMedicalImageWithAi(category: String, description: String) {
        viewModelScope.launch {
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "Analyzing imaging with ${engine.displayName}..."
            try {
                val analysis = aiService.analyzeMedicalImage(category, description, engine)
                generatedImageAnalysis.value = analysis
                aiStatusMessage.value = "Analysis completed via ${engine.displayName}."
            } catch (e: Exception) {
                aiStatusMessage.value = "Error: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun generatePatientLeafletWithAi(
        medicalContent: String,
        targetLanguage: LeafletLanguage = LeafletLanguage.ENGLISH,
        readingLevel: ReadingLevel = ReadingLevel.STANDARD
    ) {
        viewModelScope.launch {
            val content = medicalContent.ifBlank { editorContent.value.ifBlank { _selectedDocument.value?.content ?: "" } }
            if (content.isBlank()) return@launch
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "Translating & adapting patient guide into ${targetLanguage.displayName}..."
            try {
                val leaflet = aiService.generatePatientLeaflet(content, targetLanguage, readingLevel, engine)
                generatedPatientLeaflet.value = leaflet
                aiStatusMessage.value = "Patient Leaflet created (${targetLanguage.displayName}) via ${engine.displayName}."
            } catch (e: Exception) {
                aiStatusMessage.value = "Error: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun insertCalculationIntoEditor(result: CalculationResult) {
        appendContentToEditor(result.toMarkdownBlock())
    }

    fun insertDrugMonographIntoEditor(drug: DrugMonograph) {
        val block = buildString {
            append("### 💊 Medication Monograph: ${drug.genericName} (${drug.brandNames})\n")
            append("**Class:** ${drug.category.displayName}\n")
            append("**Standard Adult Dose:** ${drug.standardAdultDose}\n")
            if (drug.pediatricDoseMgPerKg != null) {
                append("**Pediatric Dosing:** ${drug.pediatricDoseMgPerKg} mg/kg (${drug.pediatricFrequency ?: "divided"}) [Max: ${drug.pediatricMaxSingleDoseMg} mg]\n")
            }
            append("**Renal Adjustments:** ${drug.renalAdjustment}\n")
            append("**Hepatic Adjustments:** ${drug.hepaticAdjustment}\n")
            if (!drug.blackBoxWarning.isNullOrBlank()) {
                append("[WARNING: FDA Black Box Warning: ${drug.blackBoxWarning}]\n")
            }
            append("**Contraindications:** ${drug.contraindications.joinToString(", ")}\n")
            append("**Monitoring Parameters:** ${drug.monitoringParameters.joinToString(", ")}\n")
            append("**Pregnancy / Lactation:** ${drug.pregnancyCategory}\n")
        }
        appendContentToEditor(block)
    }

    fun insertInteractionReportIntoEditor(interactions: List<DrugInteractionPair>, drugNames: List<String>) {
        val block = buildString {
            append("### ⚠️ Drug-Drug Interaction Safety Report\n")
            append("**Regimen Analyzed:** ${drugNames.joinToString(", ")}\n\n")
            if (interactions.isEmpty()) {
                append("[KEY_POINT: No major pharmacokinetic or pharmacodynamic interactions identified in the standard clinical database for this combination.]\n")
            } else {
                interactions.forEach { pair ->
                    val tag = if (pair.severity == com.example.data.model.InteractionSeverity.MAJOR_AVOID) "WARNING" else "KEY_POINT"
                    append("[$tag: ${pair.severity.label}: ${pair.drug1Name} + ${pair.drug2Name} | Mechanism: ${pair.mechanism} | Clinical Impact: ${pair.clinicalEffect} | Action: ${pair.managementAction}]\n\n")
                }
            }
        }
        appendContentToEditor(block)
    }

    fun insertAnnotatedFigureIntoEditor(figure: AnnotatedFigureData) {
        val block = buildString {
            append("### 🔬 ${figure.title} (${figure.modality})\n")
            append("![${figure.title}](${figure.baseImageUri})\n")
            if (figure.pins.isNotEmpty()) {
                append("**Anatomical Structures Identified:**\n")
                figure.pins.forEach { pin ->
                    append("- **[Pin #${pin.pinNumber}]**: ${pin.label}\n")
                }
            }
            if (figure.measurements.isNotEmpty()) {
                append("**Diagnostic Measurements:**\n")
                figure.measurements.forEach { m ->
                    append("- ${m.label}: ${String.format("%.1f", m.measuredValueMm)} mm\n")
                }
            }
            if (figure.clinicalDescription.isNotBlank()) {
                append("[KEY_POINT: Clinical Interpretation: ${figure.clinicalDescription}]\n")
            }
            if (figure.comparisonImageUri != null) {
                append("\n**Diagnostic Comparison (${figure.comparisonTitle ?: "Normal Reference / Follow-up"}):**\n")
                append("![Comparison](${figure.comparisonImageUri})\n")
            }
        }
        appendContentToEditor(block)
    }

    fun exportFlashcardsToAnki(context: Context, deckTitle: String = "Clinical Flashcards") {
        val cards = generatedFlashcards.value
        if (cards.isEmpty()) return
        val file = DocumentExportHelper.exportFlashcardsToAnkiCsv(context, cards, deckTitle)
        DocumentExportHelper.shareAnkiDeck(context, file)
    }

    fun askKnowledgeBase(query: String) {
        if (query.isBlank()) return
        val currentChat = _chatMessages.value.toMutableList()
        currentChat.add("User" to query)
        _chatMessages.value = currentChat

        viewModelScope.launch {
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "Cross-referencing sources via ${engine.displayName}..."
            try {
                val sources = repository.allSources.stateIn(viewModelScope).value
                val docContent = _selectedDocument.value?.content ?: ""
                val answer = aiService.askMyDocuments(query, sources, docContent, engine)
                _chatMessages.value = _chatMessages.value + ("DocuMed AI (${engine.displayName})" to answer)
                aiStatusMessage.value = "Answer synthesized from knowledge base."
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + ("DocuMed AI" to "Error processing query: ${e.message}")
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun applyAiTransform(command: String) {
        viewModelScope.launch {
            val current = editorContent.value
            if (current.isBlank()) return@launch
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "Applying transformation via ${engine.displayName}..."
            try {
                val transformed = aiService.transformText(current, command, engine)
                editorContent.value = transformed
                saveCurrentDocument("Applied AI Transform (${engine.displayName}): $command")
                aiStatusMessage.value = "Transformation applied via ${engine.displayName}."
            } catch (e: Exception) {
                aiStatusMessage.value = "Error: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    // --- Knowledge Base Uploads ---

    val isSourceProcessing = MutableStateFlow(false)
    val sourceProcessingStatus = MutableStateFlow("")

    fun uploadSourceFile(
        title: String,
        fileType: String,
        rawText: String,
        summary: String = "",
        keyPoints: String = "",
        tables: String = "",
        fileSize: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val calcSize = if (fileSize.isNotBlank()) fileSize else "${(rawText.length * 2 / 1024).coerceAtLeast(1)} KB"
            val source = SourceMaterial(
                documentId = _selectedDocument.value?.id ?: 0,
                title = title,
                fileType = fileType,
                rawText = rawText,
                extractedSummary = summary.ifBlank { "Text extracted from $title ($fileType). Ready for synthesis and chapter creation." },
                extractedKeyPoints = keyPoints.ifBlank { "High-yield concepts parsed from $title." },
                extractedTables = tables,
                fileSize = calcSize
            )
            repository.insertSource(source)
        }
    }

    fun processAndUploadUri(
        context: Context,
        uri: Uri,
        onDone: (SourceMaterial) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isSourceProcessing.value = true
            sourceProcessingStatus.value = "Reading & parsing file..."
            try {
                val parsed = com.example.data.parser.SourceParserHelper.parseFileUri(context, uri)
                sourceProcessingStatus.value = "Indexing parsed text and generating clinical summary..."
                val source = SourceMaterial(
                    documentId = _selectedDocument.value?.id ?: 0,
                    title = parsed.fileName,
                    fileType = parsed.fileType,
                    rawText = parsed.extractedText,
                    extractedSummary = parsed.summary,
                    extractedKeyPoints = parsed.keyPoints,
                    extractedTables = parsed.tableData,
                    fileSize = parsed.fileSize
                )
                withContext(Dispatchers.IO) {
                    repository.insertSource(source)
                }
                sourceProcessingStatus.value = "Successfully added ${parsed.fileName} to Knowledge Base"
                onDone(source)
            } catch (e: Exception) {
                sourceProcessingStatus.value = "Error parsing file: ${e.message}"
                onError(e.message ?: "Failed to read file")
            } finally {
                isSourceProcessing.value = false
            }
        }
    }

    fun fetchAndUploadWebLink(
        url: String,
        onDone: (SourceMaterial) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isSourceProcessing.value = true
            sourceProcessingStatus.value = "Connecting to $url & extracting medical article content..."
            try {
                val parsed = com.example.data.parser.SourceParserHelper.fetchAndParseWebLink(url)
                val source = SourceMaterial(
                    documentId = _selectedDocument.value?.id ?: 0,
                    title = parsed.fileName,
                    fileType = "WEB_LINK",
                    rawText = parsed.extractedText,
                    extractedSummary = parsed.summary,
                    extractedKeyPoints = parsed.keyPoints,
                    extractedTables = parsed.tableData,
                    fileSize = parsed.fileSize
                )
                withContext(Dispatchers.IO) {
                    repository.insertSource(source)
                }
                sourceProcessingStatus.value = "Successfully imported web reference: ${parsed.fileName}"
                onDone(source)
            } catch (e: Exception) {
                sourceProcessingStatus.value = "Failed to fetch web link: ${e.message}"
                onError(e.message ?: "Failed to fetch web article")
            } finally {
                isSourceProcessing.value = false
            }
        }
    }

    fun deleteSource(source: SourceMaterial) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSource(source)
        }
    }

    // ==========================================
    // AI PowerPoint / Presentation Deck Generation
    // ==========================================

    fun generatePowerPointPresentation(
        audience: String = "Postgraduate Medical Residents & Fellows",
        slideCount: Int = 5,
        onComplete: ((MedicalPresentation) -> Unit)? = null
    ) {
        viewModelScope.launch {
            isAiGenerating.value = true
            val engine = selectedAiEngine.value
            aiStatusMessage.value = "AI (${engine.displayName}) synthesizing clinical slides, speaker notes & diagrams..."
            val content = editorContent.value.ifBlank { _selectedDocument.value?.content ?: "" }
            val title = editorTitle.value.ifBlank { _selectedDocument.value?.title ?: "Clinical Topic" }
            try {
                val presentation = aiService.generatePresentation(
                    content = content,
                    title = title,
                    audience = audience,
                    slideCount = slideCount,
                    engine = engine
                )
                generatedPresentation.value = presentation
                activeSlideDeckForEditing.value = presentation
                onComplete?.invoke(presentation)
            } catch (e: Exception) {
                val fallback = aiService.generatePresentation(content, title, audience, slideCount, engine)
                generatedPresentation.value = fallback
                activeSlideDeckForEditing.value = fallback
                onComplete?.invoke(fallback)
            } finally {
                isAiGenerating.value = false
                aiStatusMessage.value = ""
            }
        }
    }

    // ==========================================
    // Offline App Data Backup & Restore
    // ==========================================

    fun exportOfflineBackup(context: Context, onComplete: (File) -> Unit) {
        viewModelScope.launch {
            val docs = withContext(Dispatchers.IO) { repository.getAllDocumentsOnce() }
            val sources = withContext(Dispatchers.IO) { repository.getAllSourcesOnce() }
            val versions = withContext(Dispatchers.IO) { repository.getAllVersionsOnce() }

            val file = withContext(Dispatchers.IO) {
                DocumentExportHelper.exportAppDataBackupFile(context, docs, sources, versions)
            }
            onComplete(file)
        }
    }

    fun restoreOfflineBackup(
        context: Context,
        uri: Uri,
        onSuccess: (docCount: Int, srcCount: Int, verCount: Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val jsonString = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
                    } ?: throw IllegalStateException("Could not open backup file stream.")
                }

                val backupData = DocumentExportHelper.parseBackupData(jsonString)
                if (backupData.documents.isEmpty() && backupData.sources.isEmpty()) {
                    onError("The selected backup file contains no valid medical documents or sources.")
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    repository.restoreBackup(backupData.documents, backupData.sources, backupData.versions)
                }

                val currentDocId = _selectedDocument.value?.id
                if (currentDocId != null) {
                    val updated = withContext(Dispatchers.IO) { repository.getDocumentByIdOnce(currentDocId) }
                    if (updated != null) {
                        selectDocument(updated)
                    }
                }

                onSuccess(backupData.documents.size, backupData.sources.size, backupData.versions.size)
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Failed to parse and restore backup data.")
            }
        }
    }

    // ==========================================
    // Send / Share App APK
    // ==========================================

    fun shareAppApk(context: Context) {
        DocumentExportHelper.shareAppApkFile(context)
    }

    // ==========================================
    // Multi-User Collaboration & Role Management
    // ==========================================

    fun switchUser(userId: String) {
        collaborationRepo.switchUser(userId)
    }

    fun createNewUser(name: String, email: String, role: UserRole, specialty: String, title: String) {
        collaborationRepo.createUser(name, email, role, specialty, title)
    }

    fun updateUserRole(userId: String, newRole: UserRole) {
        collaborationRepo.updateUserRole(userId, newRole)
    }

    fun deleteUser(userId: String): Boolean {
        return collaborationRepo.deleteUser(userId)
    }

    fun setDocumentPermission(userId: String, docId: Long, permission: DocumentPermissionLevel) {
        collaborationRepo.setDocumentPermission(userId, docId, permission)
    }

    fun setBatchDocumentPermissions(userId: String, docIds: List<Long>, permission: DocumentPermissionLevel) {
        collaborationRepo.setBatchPermissions(userId, docIds, permission)
    }

    fun updateCollaboratorPresence(sectionTitle: String, isEditing: Boolean) {
        collaborationRepo.updateCurrentSectionPresence(sectionTitle, isEditing)
    }

    // ==========================================
    // Real-Time Comments & Annotations
    // ==========================================

    fun addDocumentComment(
        commentText: String,
        selectedText: String = "",
        sectionTitle: String = "General Chapter",
        commentType: CommentType = CommentType.GENERAL,
        suggestedReplacement: String = ""
    ) {
        val docId = _selectedDocument.value?.id ?: return
        val author = currentUser.value
        collaborationRepo.addComment(
            documentId = docId,
            author = author,
            commentText = commentText,
            selectedText = selectedText,
            sectionTitle = sectionTitle,
            commentType = commentType,
            suggestedReplacement = suggestedReplacement
        )
    }

    fun addCommentReply(commentId: String, replyText: String) {
        val docId = _selectedDocument.value?.id ?: return
        val author = currentUser.value
        collaborationRepo.addReply(
            commentId = commentId,
            documentId = docId,
            author = author,
            replyText = replyText
        )
    }

    fun resolveComment(commentId: String) {
        val docId = _selectedDocument.value?.id ?: return
        val resolver = currentUser.value
        collaborationRepo.resolveComment(commentId, docId, resolver)
    }

    fun reopenComment(commentId: String) {
        val docId = _selectedDocument.value?.id ?: return
        collaborationRepo.reopenComment(commentId, docId)
    }

    fun deleteComment(commentId: String) {
        val docId = _selectedDocument.value?.id ?: return
        collaborationRepo.deleteComment(commentId, docId)
    }

    fun applyCommentSuggestion(comment: DocumentComment) {
        val replacement = comment.suggestedReplacement
        if (replacement.isBlank()) return

        val current = editorContent.value
        if (comment.selectedText.isNotBlank() && current.contains(comment.selectedText)) {
            val updated = current.replaceFirst(comment.selectedText, replacement)
            updateEditorContent(updated)
        } else {
            // Append with section note
            val updated = current + "\n\n" + replacement
            updateEditorContent(updated)
        }
        // Auto-resolve comment after applying suggestion
        resolveComment(comment.id)
    }
}
