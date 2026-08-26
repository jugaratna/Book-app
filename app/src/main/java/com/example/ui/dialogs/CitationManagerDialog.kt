package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CitationEntry
import com.example.data.model.CitationSourceType
import com.example.data.model.CitationStyle
import com.example.data.model.PredefinedMedicalCitations
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalRed
import com.example.ui.theme.MedicalBluePrimary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CitationManagerDialog(
    initialCitations: List<CitationEntry>,
    currentStyle: CitationStyle,
    onDismiss: () -> Unit,
    onInsertCitationKey: (inTextKey: String) -> Unit,
    onUpdateBibliographyInDoc: (formattedBibliography: String, updatedCitations: List<CitationEntry>, style: CitationStyle) -> Unit
) {
    val context = LocalContext.current
    var selectedStyle by remember { mutableStateOf(currentStyle) }
    val citations = remember {
        mutableStateListOf<CitationEntry>().apply {
            if (initialCitations.isNotEmpty()) {
                addAll(initialCitations)
            } else {
                addAll(PredefinedMedicalCitations.sampleCitations.take(3))
            }
        }
    }

    var activeTab by remember { mutableIntStateOf(0) } // 0: Document Citations, 1: Add / Edit Citation, 2: Landmark Library
    var editingCitation by remember { mutableStateOf<CitationEntry?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var styleDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MedicalBluePrimary.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                tint = MedicalBluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Medical Citation Manager",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Format references in Vancouver, APA 7th, AMA, Harvard & NLM",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Citation Style Selector Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Active Citation Style:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${selectedStyle.displayName} — ${selectedStyle.description}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = styleDropdownExpanded,
                            onExpandedChange = { styleDropdownExpanded = !styleDropdownExpanded }
                        ) {
                            OutlinedButton(
                                onClick = { styleDropdownExpanded = true },
                                modifier = Modifier
                                    .menuAnchor()
                                    .testTag("citation_style_selector_btn")
                            ) {
                                Text(selectedStyle.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = styleDropdownExpanded)
                            }

                            ExposedDropdownMenu(
                                expanded = styleDropdownExpanded,
                                onDismissRequest = { styleDropdownExpanded = false }
                            ) {
                                CitationStyle.values().forEach { style ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(style.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(style.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        },
                                        onClick = {
                                            selectedStyle = style
                                            styleDropdownExpanded = false
                                        },
                                        trailingIcon = {
                                            if (selectedStyle == style) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Citations, Add/Edit, Landmark Library
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Document Citations (${citations.size})", fontSize = 12.5.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = {
                            if (activeTab != 1) editingCitation = null
                            activeTab = 1
                        },
                        text = { Text(if (editingCitation != null) "Edit Citation" else "Add New", fontSize = 12.5.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Landmark Library", fontSize = 12.5.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.LibraryBooks, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (activeTab) {
                        0 -> DocumentCitationsTab(
                            citations = citations,
                            currentStyle = selectedStyle,
                            onInsertKey = { entry ->
                                onInsertCitationKey(entry.getInTextKey(selectedStyle))
                                Toast.makeText(context, "Inserted ${entry.getInTextKey(selectedStyle)} into document", Toast.LENGTH_SHORT).show()
                            },
                            onEdit = { entry ->
                                editingCitation = entry
                                activeTab = 1
                            },
                            onDelete = { entry ->
                                citations.remove(entry)
                                // re-index citation numbers
                                val reindexed = citations.mapIndexed { idx, item ->
                                    item.copy(citationNumber = idx + 1)
                                }
                                citations.clear()
                                citations.addAll(reindexed)
                            },
                            onCopySingle = { entry ->
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Citation", entry.format(selectedStyle))
                                cm.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied citation to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )

                        1 -> AddEditCitationTab(
                            initialCitation = editingCitation,
                            citationCount = citations.size,
                            currentStyle = selectedStyle,
                            onSave = { savedEntry ->
                                val existingIndex = citations.indexOfFirst { it.id == savedEntry.id }
                                if (existingIndex >= 0) {
                                    citations[existingIndex] = savedEntry
                                } else {
                                    val newNum = citations.size + 1
                                    citations.add(savedEntry.copy(citationNumber = newNum))
                                }
                                editingCitation = null
                                activeTab = 0
                                Toast.makeText(context, "Citation saved successfully", Toast.LENGTH_SHORT).show()
                            },
                            onCancel = {
                                editingCitation = null
                                activeTab = 0
                            }
                        )

                        2 -> LandmarkLibraryTab(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onAddLandmark = { landmark ->
                                if (!citations.any { it.title.equals(landmark.title, ignoreCase = true) }) {
                                    val newNum = citations.size + 1
                                    citations.add(landmark.copy(citationNumber = newNum))
                                    Toast.makeText(context, "Added '${landmark.title}' to citations", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Citation already in your list", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Bottom Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val bibliographyText = buildString {
                                append("## References\n\n")
                                citations.forEach { citation ->
                                    append("${citation.format(selectedStyle)}\n\n")
                                }
                            }
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Bibliography", bibliographyText)
                            cm.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied formatted bibliography (${selectedStyle.name}) to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Bibliography", fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val bibliographyText = buildString {
                                    append("\n\n## References\n\n")
                                    citations.forEach { citation ->
                                        append("${citation.format(selectedStyle)}\n\n")
                                    }
                                }
                                onUpdateBibliographyInDoc(bibliographyText, citations.toList(), selectedStyle)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                            modifier = Modifier.testTag("apply_references_to_doc_btn")
                        ) {
                            Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Inject References Section into Doc", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentCitationsTab(
    citations: List<CitationEntry>,
    currentStyle: CitationStyle,
    onInsertKey: (CitationEntry) -> Unit,
    onEdit: (CitationEntry) -> Unit,
    onDelete: (CitationEntry) -> Unit,
    onCopySingle: (CitationEntry) -> Unit
) {
    if (citations.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("No citations in document yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Add manual entries or import landmark clinical studies from the Library tab.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(citations, key = { it.id }) { citation ->
                CitationItemCard(
                    citation = citation,
                    currentStyle = currentStyle,
                    onInsertKey = { onInsertKey(citation) },
                    onEdit = { onEdit(citation) },
                    onDelete = { onDelete(citation) },
                    onCopy = { onCopySingle(citation) }
                )
            }
        }
    }
}

@Composable
private fun CitationItemCard(
    citation: CitationEntry,
    currentStyle: CitationStyle,
    onInsertKey: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // In-Text Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MedicalBluePrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "In-Text Key: ${citation.getInTextKey(currentStyle)}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalBluePrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Evidence Level Tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ClinicalGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = citation.evidenceLevel,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = ClinicalGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Formatted Reference Preview
            Text(
                text = citation.format(currentStyle),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            if (citation.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${citation.notes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onInsertKey,
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Insert ${citation.getInTextKey(currentStyle)}", fontSize = 11.5.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ClinicalRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddEditCitationTab(
    initialCitation: CitationEntry?,
    citationCount: Int,
    currentStyle: CitationStyle,
    onSave: (CitationEntry) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initialCitation?.title ?: "") }
    var authors by remember { mutableStateOf(initialCitation?.authors ?: "") }
    var journal by remember { mutableStateOf(initialCitation?.journalOrPublisher ?: "") }
    var year by remember { mutableStateOf(initialCitation?.year ?: "2024") }
    var volume by remember { mutableStateOf(initialCitation?.volume ?: "") }
    var issue by remember { mutableStateOf(initialCitation?.issue ?: "") }
    var pages by remember { mutableStateOf(initialCitation?.pages ?: "") }
    var doi by remember { mutableStateOf(initialCitation?.doi ?: "") }
    var pmid by remember { mutableStateOf(initialCitation?.pmid ?: "") }
    var sourceType by remember { mutableStateOf(initialCitation?.sourceType ?: CitationSourceType.JOURNAL_ARTICLE) }
    var evidenceLevel by remember { mutableStateOf(initialCitation?.evidenceLevel ?: "Level 1A (High-Quality RCT/Review)") }
    var notes by remember { mutableStateOf(initialCitation?.notes ?: "") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (initialCitation != null) "Edit Reference Details" else "Enter New Medical Reference",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Source Type Selector Chips
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CitationSourceType.values().forEach { st ->
                FilterChip(
                    selected = sourceType == st,
                    onClick = { sourceType = st },
                    label = { Text(st.displayName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MedicalBluePrimary.copy(alpha = 0.15f),
                        selectedLabelColor = MedicalBluePrimary
                    )
                )
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Article / Chapter Title *") },
            placeholder = { Text("e.g. SGLT2 inhibitors in heart failure...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        OutlinedTextField(
            value = authors,
            onValueChange = { authors = it },
            label = { Text("Authors (Standard NLM / Index Medicus format) *") },
            placeholder = { Text("e.g. Smith JA, Miller RB, Garcia EK") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = journal,
                onValueChange = { journal = it },
                label = { Text("Journal / Publisher *") },
                placeholder = { Text("e.g. N Engl J Med") },
                modifier = Modifier.weight(2f),
                singleLine = true
            )

            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Year *") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = volume,
                onValueChange = { volume = it },
                label = { Text("Vol") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = issue,
                onValueChange = { issue = it },
                label = { Text("Issue") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = pages,
                onValueChange = { pages = it },
                label = { Text("Pages") },
                placeholder = { Text("120-135") },
                modifier = Modifier.weight(1.5f),
                singleLine = true
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = doi,
                onValueChange = { doi = it },
                label = { Text("DOI") },
                placeholder = { Text("10.1056/NEJMoa...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = pmid,
                onValueChange = { pmid = it },
                label = { Text("PubMed PMID") },
                placeholder = { Text("34567890") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Clinical Relevance / High-Yield Summary Note") },
            placeholder = { Text("Key finding or guideline recommendation...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
        )

        // Live Preview Box
        val previewEntry = CitationEntry(
            id = initialCitation?.id ?: "preview",
            citationNumber = initialCitation?.citationNumber ?: (citationCount + 1),
            title = if (title.isBlank()) "Pathophysiology and Management of Acute Disease" else title,
            authors = if (authors.isBlank()) "Smith JA, Doe AB" else authors,
            journalOrPublisher = if (journal.isBlank()) "N Engl J Med" else journal,
            year = year,
            volume = volume,
            issue = issue,
            pages = pages,
            doi = doi,
            pmid = pmid,
            sourceType = sourceType,
            evidenceLevel = evidenceLevel,
            notes = notes
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Live Style Preview (${currentStyle.name}):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedicalBluePrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = previewEntry.format(currentStyle),
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Save & Cancel Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (title.isNotBlank() && authors.isNotBlank() && journal.isNotBlank()) {
                        onSave(previewEntry)
                    }
                },
                enabled = title.isNotBlank() && authors.isNotBlank() && journal.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                modifier = Modifier.testTag("save_citation_btn")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Reference")
            }
        }
    }
}

@Composable
private fun LandmarkLibraryTab(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddLandmark: (CitationEntry) -> Unit
) {
    val filteredLandmarks = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            PredefinedMedicalCitations.sampleCitations
        } else {
            PredefinedMedicalCitations.sampleCitations.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.authors.contains(searchQuery, ignoreCase = true) ||
                it.journalOrPublisher.contains(searchQuery, ignoreCase = true) ||
                it.notes.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search landmark trials (e.g., ESC, ATLS, Sepsis, GOLD)...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredLandmarks) { landmark ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MedicalBluePrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = landmark.sourceType.displayName,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    fontSize = 10.5.sp,
                                    color = MedicalBluePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = { onAddLandmark(landmark) },
                                colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import to Doc", fontSize = 11.5.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(landmark.title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${landmark.authors} (${landmark.year}). ${landmark.journalOrPublisher}.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        if (landmark.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = landmark.notes,
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
