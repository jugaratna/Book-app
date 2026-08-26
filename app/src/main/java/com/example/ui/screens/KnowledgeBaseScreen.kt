package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SourceMaterial
import com.example.ui.components.AddSourceDialog
import com.example.ui.components.SourceUploadType
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.DocuMedViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun KnowledgeBaseScreen(
    viewModel: DocuMedViewModel,
    modifier: Modifier = Modifier
) {
    val sources by viewModel.sourceMaterials.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val selectedDoc by viewModel.selectedDocument.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddSourceDialog by remember { mutableStateOf(false) }
    var initialDialogTab by remember { mutableStateOf(SourceUploadType.PDF) }
    var selectedSourceForDetail by remember { mutableStateOf<SourceMaterial?>(null) }

    var filterType by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var chatInput by remember { mutableStateOf("") }

    val filteredSources = sources.filter { source ->
        val matchType = when (filterType) {
            "All" -> true
            "PDF" -> source.fileType.equals("PDF", ignoreCase = true)
            "Web Link" -> source.fileType.equals("WEB_LINK", ignoreCase = true)
            "Word" -> source.fileType.equals("DOCX", ignoreCase = true) || source.fileType.equals("DOC", ignoreCase = true)
            "Excel" -> source.fileType.equals("EXCEL", ignoreCase = true) || source.fileType.equals("CSV", ignoreCase = true) || source.fileType.equals("XLSX", ignoreCase = true)
            "Text" -> source.fileType.equals("TXT", ignoreCase = true) || source.fileType.equals("GUIDELINE", ignoreCase = true)
            "Scans" -> source.fileType.equals("XRAY", ignoreCase = true) || source.fileType.equals("CT_SCAN", ignoreCase = true) || source.fileType.equals("IMAGE", ignoreCase = true)
            else -> true
        }
        val matchQuery = searchQuery.isBlank() ||
                source.title.contains(searchQuery, ignoreCase = true) ||
                source.rawText.contains(searchQuery, ignoreCase = true) ||
                source.extractedSummary.contains(searchQuery, ignoreCase = true)
        matchType && matchQuery
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MedicalBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Medical Knowledge Base",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${sources.size} Uploaded Source Documents & Clinical Guidelines",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            initialDialogTab = SourceUploadType.PDF
                            showAddSourceDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Source", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Ask My Documents (Q&A)", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Source Files (${sources.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
        }

        if (selectedTab == 0) {
            // Tab 0: "Ask My Documents" Conversational Multi-Source RAG
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Quick Source Upload Strip for instant access
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Add Source Documents for AI Retrieval:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${sources.size} active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MedicalTealPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                QuickUploadChip(label = "+ PDF", icon = Icons.Default.PictureAsPdf, color = Color(0xFFDC2626)) {
                                    initialDialogTab = SourceUploadType.PDF
                                    showAddSourceDialog = true
                                }
                            }
                            item {
                                QuickUploadChip(label = "+ Photo / Scan", icon = Icons.Default.Image, color = Color(0xFF0284C7)) {
                                    initialDialogTab = SourceUploadType.PHOTO
                                    showAddSourceDialog = true
                                }
                            }
                            item {
                                QuickUploadChip(label = "+ Web Link", icon = Icons.Default.Language, color = Color(0xFF2563EB)) {
                                    initialDialogTab = SourceUploadType.WEB
                                    showAddSourceDialog = true
                                }
                            }
                            item {
                                QuickUploadChip(label = "+ Word (.docx)", icon = Icons.Default.Description, color = Color(0xFF1D4ED8)) {
                                    initialDialogTab = SourceUploadType.WORD
                                    showAddSourceDialog = true
                                }
                            }
                            item {
                                QuickUploadChip(label = "+ Excel (.xlsx)", icon = Icons.Default.TableChart, color = Color(0xFF059669)) {
                                    initialDialogTab = SourceUploadType.EXCEL
                                    showAddSourceDialog = true
                                }
                            }
                            item {
                                QuickUploadChip(label = "+ Direct Text", icon = Icons.Default.EditNote, color = Color(0xFFD97706)) {
                                    initialDialogTab = SourceUploadType.TEXT
                                    showAddSourceDialog = true
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chat List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chatMessages) { (sender, message) ->
                        val isUser = sender == "User"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 12.dp
                                ),
                                color = if (isUser) MedicalBluePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null,
                                modifier = Modifier.fillMaxWidth(0.88f)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isUser) Icons.Default.QuestionAnswer else Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = if (isUser) Color.White.copy(alpha = 0.8f) else MedicalTealPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = sender,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUser) Color.White else MedicalTealPrimary
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = message,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            lineHeight = 18.sp,
                                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (isAiGenerating) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MedicalBluePrimary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Cross-referencing sources and synthesizing answer...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Query Prompt Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Ask across all uploaded PDFs, Word, Excel & Web links...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                val q = chatInput
                                chatInput = ""
                                viewModel.askKnowledgeBase(q)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MedicalBluePrimary)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        } else {
            // Tab 1: Source Files Library with Filters and Quick Add Cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick Add Source Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MedicalBluePrimary.copy(alpha = 0.06f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedicalBluePrimary.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Upload & Ingest Medical Evidence",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Add source materials in any of the 5 supported clinical formats:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SourceActionBtn("PDF (.pdf)", Icons.Default.PictureAsPdf, Color(0xFFDC2626)) {
                                    initialDialogTab = SourceUploadType.PDF
                                    showAddSourceDialog = true
                                }
                                SourceActionBtn("Photo / Scans", Icons.Default.Image, Color(0xFF0284C7)) {
                                    initialDialogTab = SourceUploadType.PHOTO
                                    showAddSourceDialog = true
                                }
                                SourceActionBtn("Web Link", Icons.Default.Language, Color(0xFF2563EB)) {
                                    initialDialogTab = SourceUploadType.WEB
                                    showAddSourceDialog = true
                                }
                                SourceActionBtn("Word (.docx)", Icons.Default.Description, Color(0xFF1D4ED8)) {
                                    initialDialogTab = SourceUploadType.WORD
                                    showAddSourceDialog = true
                                }
                                SourceActionBtn("Excel (.xlsx/.csv)", Icons.Default.TableChart, Color(0xFF059669)) {
                                    initialDialogTab = SourceUploadType.EXCEL
                                    showAddSourceDialog = true
                                }
                                SourceActionBtn("Direct Text", Icons.Default.EditNote, Color(0xFFD97706)) {
                                    initialDialogTab = SourceUploadType.TEXT
                                    showAddSourceDialog = true
                                }
                            }
                        }
                    }
                }

                // Search & Filter Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search sources by title, guideline, or text...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Format Filter Chips
                        val filterCategories = listOf("All", "PDF", "Web Link", "Word", "Excel", "Text", "Scans")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(filterCategories) { cat ->
                                FilterChip(
                                    selected = filterType == cat,
                                    onClick = { filterType = cat },
                                    label = { Text(cat, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }

                if (filteredSources.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank() || filterType != "All") "No sources match filter." else "No source materials uploaded yet.",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Upload PDF, Word, Excel, Web links, or Text to empower clinical AI synthesis.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { showAddSourceDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add First Source")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredSources) { source ->
                        SourceMaterialCard(
                            source = source,
                            onClick = { selectedSourceForDetail = source },
                            onDelete = { viewModel.deleteSource(source) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Modal: Add Source Multi-Modal Dialog
    if (showAddSourceDialog) {
        AddSourceDialog(
            viewModel = viewModel,
            initialTab = initialDialogTab,
            onDismiss = { showAddSourceDialog = false },
            onSourceAdded = { showAddSourceDialog = false }
        )
    }

    // Modal: Source Details & Actions Dialog
    selectedSourceForDetail?.let { source ->
        SourceDetailDialog(
            source = source,
            viewModel = viewModel,
            onDismiss = { selectedSourceForDetail = null }
        )
    }
}

@Composable
fun QuickUploadChip(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = color))
        }
    }
}

@Composable
fun SourceActionBtn(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SourceMaterialCard(
    source: SourceMaterial,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (typeIcon, typeColor, typeBadge) = when (source.fileType) {
        "PDF" -> Triple(Icons.Default.PictureAsPdf, Color(0xFFDC2626), "PDF")
        "DOCX", "DOC" -> Triple(Icons.Default.Description, Color(0xFF1D4ED8), "WORD")
        "EXCEL", "CSV", "XLSX" -> Triple(Icons.Default.TableChart, Color(0xFF059669), "EXCEL")
        "WEB_LINK" -> Triple(Icons.Default.Language, Color(0xFF2563EB), "WEB LINK")
        "XRAY", "CT_SCAN", "IMAGE" -> Triple(Icons.Default.Image, MedicalTealPrimary, "IMAGING")
        else -> Triple(Icons.Default.EditNote, Color(0xFFD97706), "TEXT")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(typeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = typeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = typeBadge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = typeColor,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "${source.fileSize} · ${source.getFormattedDate()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = source.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }

            if (source.extractedSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = source.extractedSummary,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (source.extractedTables.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Contains structured tabular dataset", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun SourceDetailDialog(
    source: SourceMaterial,
    viewModel: DocuMedViewModel,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(680.dp)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 10.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = source.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Format: ${source.fileType} · Size: ${source.fileSize} · Uploaded: ${source.getFormattedDate()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Body content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (source.extractedSummary.isNotBlank()) {
                        Text("AI Clinical Summary:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MedicalTealPrimary.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = source.extractedSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Text("Extracted Document Content & Notes:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = source.rawText,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 17.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Action Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(source.rawText))
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Text")
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                viewModel.appendContentToEditor("\n\n### Clinical Reference: ${source.title}\n${source.rawText}")
                                onDismiss()
                                viewModel.navigateTo(AppNavTab.EDITOR)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary)
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Insert into Editor")
                        }
                    }
                }
            }
        }
    }
}
