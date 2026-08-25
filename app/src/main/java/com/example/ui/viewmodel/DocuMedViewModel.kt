package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.MedicalAiService
import com.example.data.local.DocuMedDatabase
import com.example.data.model.DocumentVersion
import com.example.data.model.FlashcardItem
import com.example.data.model.McqItem
import com.example.data.model.MedicalDocument
import com.example.data.model.OsceStation
import com.example.data.model.SourceMaterial
import com.example.data.model.VivaItem
import com.example.data.repository.DocuMedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab {
    LIBRARY,
    EDITOR,
    AI_STUDIO,
    KNOWLEDGE_BASE,
    EXPORT_PREVIEW
}

class DocuMedViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DocuMedDatabase.getDatabase(application, viewModelScope)
    private val repository = DocuMedRepository(
        database.documentDao(),
        database.sourceMaterialDao(),
        database.versionDao()
    )
    private val aiService = MedicalAiService()

    // Nav State
    private val _currentTab = MutableStateFlow(AppNavTab.LIBRARY)
    val currentTab: StateFlow<AppNavTab> = _currentTab

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val selectedSpecialty = MutableStateFlow("All")
    val selectedDocType = MutableStateFlow("All")

    // Active Document
    private val _selectedDocument = MutableStateFlow<MedicalDocument?>(null)
    val selectedDocument: StateFlow<MedicalDocument?> = _selectedDocument

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

    // Sources Knowledge Base
    val sourceMaterials: StateFlow<List<SourceMaterial>> = repository.allSources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Version History
    private val _versions = MutableStateFlow<List<DocumentVersion>>(emptyList())
    val versions: StateFlow<List<DocumentVersion>> = _versions

    // AI Operation States
    val isAiGenerating = MutableStateFlow(false)
    val aiStatusMessage = MutableStateFlow("")
    val generatedSummary = MutableStateFlow<String?>(null)
    val generatedMCQs = MutableStateFlow<List<McqItem>>(emptyList())
    val generatedViva = MutableStateFlow<List<VivaItem>>(emptyList())
    val generatedOSCE = MutableStateFlow<OsceStation?>(null)
    val generatedFlashcards = MutableStateFlow<List<FlashcardItem>>(emptyList())
    val generatedTable = MutableStateFlow<String?>(null)
    val generatedFlowchart = MutableStateFlow<String?>(null)
    val generatedImageAnalysis = MutableStateFlow<String?>(null)

    // Knowledge Base Chat ("Ask My Documents")
    private val _chatMessages = MutableStateFlow<List<Pair<String, String>>>(
        listOf(
            "DocuMed AI" to "Welcome to the DocuMed Multi-Document Knowledge Base. Ask any clinical question, compare treatment guidelines, or analyze contradictions across your uploaded PDFs, Word documents, and radiographs."
        )
    )
    val chatMessages: StateFlow<List<Pair<String, String>>> = _chatMessages

    fun navigateTo(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun selectDocument(doc: MedicalDocument) {
        _selectedDocument.value = doc
        editorContent.value = doc.content
        editorTitle.value = doc.title
        loadVersions(doc.id)
        _currentTab.value = AppNavTab.EDITOR
    }

    private fun loadVersions(docId: Long) {
        viewModelScope.launch {
            repository.getVersionsForDocument(docId).collect {
                _versions.value = it
            }
        }
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
# 1.0 Introduction
Enter introduction and clinical context here...

## 1.1 Anatomy & Pathophysiology
Key anatomical features and pathophysiology mechanisms...

[KEY_POINT: Clinical pearls and high-yield examination facts.]

## 1.2 Diagnostic & Classification Criteria
* Criterion 1
* Criterion 2

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

        val updated = current.copy(
            title = newTitle,
            content = newContent,
            wordCount = wordCount,
            version = newVersion,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDocument(updated)
            repository.saveVersionSnapshot(current.id, newVersion, snapshotDescription, newContent)
            _selectedDocument.value = updated
        }
    }

    fun updateEditorContent(newContent: String) {
        editorContent.value = newContent
    }

    fun appendContentToEditor(textToAppend: String) {
        val current = editorContent.value
        editorContent.value = if (current.isBlank()) textToAppend else "$current\n\n$textToAppend"
        saveCurrentDocument("Appended AI-generated section")
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

    // --- AI Operations ---

    fun generateChapterWithAi(
        topic: String,
        specialty: String,
        audience: String,
        sections: List<String>
    ) {
        viewModelScope.launch {
            isAiGenerating.value = true
            aiStatusMessage.value = "Analyzing clinical sources & drafting chapter on '$topic'..."
            try {
                val sources = repository.getSourcesForDocumentOnce(_selectedDocument.value?.id ?: 0)
                val chapterText = aiService.generateChapter(topic, specialty, audience, sections, sources)
                
                // If a document is active, we can create or replace
                createNewDocument(
                    title = "$topic: Complete Chapter",
                    docType = "Textbook Chapter",
                    specialty = specialty,
                    targetAudience = audience,
                    initialContent = chapterText
                )
                aiStatusMessage.value = "Chapter successfully generated with 17 clinical sections!"
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
            aiStatusMessage.value = "Synthesizing $summaryType..."
            try {
                val summary = aiService.generateSummary(content, summaryType, audience)
                generatedSummary.value = summary
                aiStatusMessage.value = "Summary generated successfully."
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
            aiStatusMessage.value = "Generating $count clinical vignette MCQs with rationale..."
            try {
                val mcqs = aiService.generateMCQs(content, count, difficulty)
                generatedMCQs.value = mcqs
                aiStatusMessage.value = "Generated ${mcqs.size} MCQs."
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
            aiStatusMessage.value = "Formulating $count Viva Voce questions & model answers..."
            try {
                val viva = aiService.generateViva(content, count)
                generatedViva.value = viva
                aiStatusMessage.value = "Viva Voce examination bank created."
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
            aiStatusMessage.value = "Constructing OSCE station for '$stationTopic'..."
            try {
                val osce = aiService.generateOSCE(content, stationTopic)
                generatedOSCE.value = osce
                aiStatusMessage.value = "OSCE Station created with 8-minute marking scheme."
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
            aiStatusMessage.value = "Extracting spaced-repetition flashcards..."
            try {
                val cards = aiService.generateFlashcards(content)
                generatedFlashcards.value = cards
                aiStatusMessage.value = "Generated ${cards.size} flashcards."
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
            aiStatusMessage.value = "Extracting tabular data & clinical classifications..."
            try {
                val table = aiService.generateTable(content, prompt)
                generatedTable.value = table
                aiStatusMessage.value = "Table generated."
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
            aiStatusMessage.value = "Generating clinical decision algorithm flowchart..."
            try {
                val flowchart = aiService.generateFlowchart(content, prompt)
                generatedFlowchart.value = flowchart
                aiStatusMessage.value = "Flowchart algorithm generated."
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
            aiStatusMessage.value = "Performing multi-modal radiological/clinical analysis..."
            try {
                val analysis = aiService.analyzeMedicalImage(category, description)
                generatedImageAnalysis.value = analysis
                aiStatusMessage.value = "Image analysis & educational question set ready."
            } catch (e: Exception) {
                aiStatusMessage.value = "Error: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun askKnowledgeBase(query: String) {
        if (query.isBlank()) return
        val currentChat = _chatMessages.value.toMutableList()
        currentChat.add("User" to query)
        _chatMessages.value = currentChat

        viewModelScope.launch {
            isAiGenerating.value = true
            aiStatusMessage.value = "Cross-referencing sources and synthesizing answer..."
            try {
                val sources = repository.allSources.stateIn(viewModelScope).value
                val docContent = _selectedDocument.value?.content ?: ""
                val answer = aiService.askMyDocuments(query, sources, docContent)
                _chatMessages.value = _chatMessages.value + ("DocuMed AI" to answer)
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
            aiStatusMessage.value = "Applying AI transformation: $command..."
            try {
                val transformed = aiService.transformText(current, command)
                editorContent.value = transformed
                saveCurrentDocument("Applied AI Transform: $command")
                aiStatusMessage.value = "Transformation applied."
            } catch (e: Exception) {
                aiStatusMessage.value = "Error: ${e.message}"
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    // --- Knowledge Base Uploads ---

    fun uploadSourceFile(title: String, fileType: String, rawText: String, summary: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val source = SourceMaterial(
                documentId = _selectedDocument.value?.id ?: 0,
                title = title,
                fileType = fileType,
                rawText = rawText,
                extractedSummary = summary.ifBlank { "Text extracted from $title ($fileType). Ready for synthesis and chapter creation." },
                extractedKeyPoints = "High-yield concepts parsed from $title.",
                fileSize = "${(rawText.length * 2 / 1024).coerceAtLeast(1)} KB"
            )
            repository.insertSource(source)
        }
    }

    fun deleteSource(source: SourceMaterial) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSource(source)
        }
    }
}
