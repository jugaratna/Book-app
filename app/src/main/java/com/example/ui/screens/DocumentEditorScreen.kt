package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Toc
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.DocumentExportHelper
import com.example.ui.components.FlowchartVisualizer
import com.example.ui.components.MedicalTableView
import com.example.ui.components.VersionHistoryDialog
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.DocuMedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentEditorScreen(
    viewModel: DocuMedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDoc by viewModel.selectedDocument.collectAsState()
    val content by viewModel.editorContent.collectAsState()
    val title by viewModel.editorTitle.collectAsState()
    val versions by viewModel.versions.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val aiStatusMessage by viewModel.aiStatusMessage.collectAsState()

    var showTocDrawer by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }
    var isPreviewMode by remember { mutableStateOf(false) }
    var aiTransformMenuExpanded by remember { mutableStateOf(false) }
    var showSaveToast by remember { mutableStateOf(false) }

    if (selectedDoc == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No document selected", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = { viewModel.navigateTo(AppNavTab.LIBRARY) }) {
                    Text("Return to Library")
                }
            }
        }
        return
    }

    val currentDoc = selectedDoc!!
    val toc = remember(content) { DocumentExportHelper.generateTableOfContents(content) }
    val wordCount = remember(content) { content.split(Regex("\\s+")).filter { it.isNotBlank() }.size }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.navigateTo(AppNavTab.LIBRARY) }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Library")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            BasicTextField(
                                value = title,
                                onValueChange = { viewModel.editorTitle.value = it },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MedicalBluePrimary),
                                singleLine = true,
                                modifier = Modifier.width(180.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MedicalBluePrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${currentDoc.specialty} · v${currentDoc.version}",
                                        color = MedicalBluePrimary,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$wordCount words",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Action Icons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Toggle Preview / Edit
                        IconButton(onClick = { isPreviewMode = !isPreviewMode }) {
                            Icon(
                                imageVector = if (isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                                contentDescription = "Toggle Preview Mode",
                                tint = if (isPreviewMode) MedicalTealPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // TOC Toggle
                        IconButton(onClick = { showTocDrawer = !showTocDrawer }) {
                            Icon(
                                imageVector = Icons.Default.Toc,
                                contentDescription = "Table of Contents",
                                tint = if (showTocDrawer) MedicalBluePrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Version History
                        IconButton(onClick = { showVersionDialog = true }) {
                            Icon(imageVector = Icons.Default.History, contentDescription = "Version History")
                        }

                        // Save Button
                        IconButton(onClick = {
                            viewModel.saveCurrentDocument("Manual Save")
                            showSaveToast = true
                        }) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Save Document", tint = MedicalBluePrimary)
                        }

                        // Share / Export Shortcut
                        IconButton(onClick = { viewModel.navigateTo(AppNavTab.EXPORT_PREVIEW) }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Export")
                        }
                    }
                }

                // AI Transformation Status / Activity Bar
                if (isAiGenerating) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MedicalBluePrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = MedicalBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = aiStatusMessage,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MedicalBluePrimary
                        )
                    }
                }
            }
        }

        // Quick Medical Formatting Toolbar
        val toolScroll = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .horizontalScroll(toolScroll)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FormatChip(label = "H1", icon = Icons.Default.Title) {
                viewModel.updateEditorContent("$content\n\n# ")
            }
            FormatChip(label = "H2", icon = Icons.Default.Title) {
                viewModel.updateEditorContent("$content\n\n## ")
            }
            FormatChip(label = "H3", icon = Icons.Default.FormatSize) {
                viewModel.updateEditorContent("$content\n\n### ")
            }
            FormatChip(label = "Clinical Pearl", icon = Icons.Default.Lightbulb, color = MedicalBluePrimary) {
                viewModel.updateEditorContent("$content\n\n[KEY_POINT: Enter high-yield clinical fact here]")
            }
            FormatChip(label = "Red Flag / Alert", icon = Icons.Default.Warning, color = Color(0xFFDC2626)) {
                viewModel.updateEditorContent("$content\n\n[WARNING: Enter contraindication or emergency red flag here]")
            }
            FormatChip(label = "Evidence Grade", icon = Icons.Default.CheckCircle, color = ClinicalGreen) {
                viewModel.updateEditorContent("$content\n\n[EVIDENCE_LEVEL: Level A Evidence (ACC/AHA 2025)]")
            }
            FormatChip(label = "Bullet", icon = Icons.AutoMirrored.Filled.FormatListBulleted) {
                viewModel.updateEditorContent("$content\n* ")
            }
            FormatChip(label = "Table", icon = Icons.Default.TableChart) {
                val sampleTable = """
| Classification | Diagnostic Criteria | Recommended Treatment |
| :--- | :--- | :--- |
| **Stage 1** | Early localized findings | Conservative / Medication |
| **Stage 2** | Displaced / Progressive | Surgical Fixation / Interventional |
                """.trimIndent()
                viewModel.updateEditorContent("$content\n\n$sampleTable\n")
            }
            FormatChip(label = "Algorithm", icon = Icons.Default.Timeline) {
                val sampleFlowchart = """
[STEP 1] Initial Clinical Triage -> Emergency Assessment
[STEP 2] Diagnostic Radiographs & Laboratory Testing -> Risk Stratification
[STEP 3A] Stable -> Medical Optimization & Observation
[STEP 3B] Unstable -> Immediate Surgical Decompression / Intervention
[STEP 4] Postoperative Rehabilitation & DVT Prophylaxis
                """.trimIndent()
                viewModel.updateEditorContent("$content\n\n$sampleFlowchart\n")
            }

            // AI Transform Dropdown
            Box {
                FilledTonalButton(
                    onClick = { aiTransformMenuExpanded = true },
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Assistant", fontSize = 11.sp)
                }
                DropdownMenu(
                    expanded = aiTransformMenuExpanded,
                    onDismissRequest = { aiTransformMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Rewrite in Academic Medical Style") },
                        onClick = {
                            aiTransformMenuExpanded = false
                            viewModel.applyAiTransform("Rewrite in rigorous, publication-grade academic medical prose with formal terminology.")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Make Concise & High-Yield") },
                        onClick = {
                            aiTransformMenuExpanded = false
                            viewModel.applyAiTransform("Make concise, removing fluff while preserving all numbers, drugs, and classification criteria.")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Simplify for Medical Students") },
                        onClick = {
                            aiTransformMenuExpanded = false
                            viewModel.applyAiTransform("Explain clearly for undergraduate medical students with anatomical and physiological first principles.")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Audit for Medical Consistency & Errors") },
                        onClick = {
                            aiTransformMenuExpanded = false
                            viewModel.applyAiTransform("Audit document: verify anatomical terms, check for safety flags, and standardize formatting.")
                        }
                    )
                }
            }
        }

        // Main Editor Canvas
        Row(modifier = Modifier.fillMaxSize()) {
            // TOC Outline Drawer
            AnimatedVisibility(visible = showTocDrawer) {
                Column(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "TABLE OF CONTENTS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MedicalBluePrimary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    val tocScroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(tocScroll),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (toc.isEmpty()) {
                            Text("Add # Headings to generate outline automatically.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            toc.forEach { item ->
                                val paddingStart = when (item.level) {
                                    1 -> 0.dp
                                    2 -> 12.dp
                                    else -> 24.dp
                                }
                                Text(
                                    text = "${item.number} ${item.title}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (item.level == 1) FontWeight.Bold else FontWeight.Normal,
                                        color = if (item.level == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier
                                        .padding(start = paddingStart)
                                        .fillMaxWidth()
                                        .clickable {
                                            // Navigation helper
                                        }
                                )
                            }
                        }
                    }
                }
            }

            // Editor / Live Preview Pane
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                if (isPreviewMode) {
                    // Rich Formatted Preview
                    val previewScroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(previewScroll)
                            .padding(bottom = 40.dp)
                    ) {
                        RenderRichDocumentContent(content)
                    }
                } else {
                    // Plain Text/Markdown Live Editor
                    OutlinedTextField(
                        value = content,
                        onValueChange = { viewModel.updateEditorContent(it) },
                        placeholder = { Text("Start typing your medical notes or paste clinical findings...") },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }

    if (showVersionDialog) {
        VersionHistoryDialog(
            versions = versions,
            currentContent = content,
            onDismiss = { showVersionDialog = false },
            onRestoreVersion = { version ->
                viewModel.restoreVersion(version)
                showVersionDialog = false
            }
        )
    }
}

@Composable
fun FormatChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(30.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@Composable
fun RenderRichDocumentContent(content: String) {
    val blocks = content.lines()
    var inTableBlock = false
    var currentTableText = ""
    var inFlowchartBlock = false
    var currentFlowchartText = ""

    blocks.forEach { line ->
        val trimmed = line.trim()
        when {
            trimmed.startsWith("# ") -> {
                Text(
                    text = trimmed.removePrefix("#").trim(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = MedicalBluePrimary),
                    modifier = Modifier.padding(top = 18.dp, bottom = 6.dp)
                )
            }
            trimmed.startsWith("## ") -> {
                Text(
                    text = trimmed.removePrefix("##").trim(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0F2B48)),
                    modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)
                )
            }
            trimmed.startsWith("### ") -> {
                Text(
                    text = trimmed.removePrefix("###").trim(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF1E3A5F)),
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                )
            }
            trimmed.startsWith("[KEY_POINT:") -> {
                val text = trimmed.removePrefix("[KEY_POINT:").removeSuffix("]").trim()
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedicalBluePrimary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("CLINICAL PEARL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MedicalBluePrimary))
                        Text(text, style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp), color = Color(0xFF1E3A8A))
                    }
                }
            }
            trimmed.startsWith("[WARNING:") -> {
                val text = trimmed.removePrefix("[WARNING:").removeSuffix("]").trim()
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("RED FLAG / CONTRAINDICATION", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)))
                        Text(text, style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp), color = Color(0xFF991B1B))
                    }
                }
            }
            trimmed.startsWith("[EVIDENCE_LEVEL:") -> {
                val text = trimmed.removePrefix("[EVIDENCE_LEVEL:").removeSuffix("]").trim()
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ClinicalGreen.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("EVIDENCE GRADE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ClinicalGreen))
                        Text(text, style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp), color = Color(0xFF065F46))
                    }
                }
            }
            trimmed.startsWith("[STEP ") -> {
                FlowchartVisualizer(flowchartText = trimmed, modifier = Modifier.padding(vertical = 6.dp))
            }
            trimmed.startsWith("|") && trimmed.endsWith("|") -> {
                MedicalTableView(markdownTable = trimmed, modifier = Modifier.padding(vertical = 6.dp))
            }
            trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                Row(modifier = Modifier.padding(vertical = 2.dp, horizontal = 6.dp)) {
                    Text("•", fontWeight = FontWeight.Bold, color = MedicalBluePrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(trimmed.substring(2), style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp))
                }
            }
            trimmed.isBlank() -> {
                Spacer(modifier = Modifier.height(8.dp))
            }
            else -> {
                Text(
                    text = trimmed,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
