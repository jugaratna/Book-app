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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Toc
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import com.example.ui.dialogs.ClinicalCalculatorsDialog
import com.example.ui.dialogs.DrugFormularyDialog
import com.example.ui.dialogs.MedicalImageAnnotationDialog
import com.example.ui.dialogs.PatientLeafletDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.export.DocumentExportHelper
import com.example.ui.components.FlowchartVisualizer
import com.example.ui.components.InsertPhotoDialog
import com.example.ui.components.MedicalTableView
import com.example.ui.components.PresentationViewDialog
import com.example.ui.components.VersionHistoryDialog
import com.example.data.model.CollaboratorPresence
import com.example.data.model.CommentType
import com.example.data.model.DocumentComment
import com.example.data.model.DocumentPermissionLevel
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.dialogs.AddCommentAnnotationDialog
import com.example.ui.dialogs.AdminUserManagementDialog
import com.example.ui.dialogs.DocumentCommentsDialog
import com.example.ui.dialogs.UserLoginSwitcherDialog
import com.example.ui.dialogs.UserRoleBadge
import com.example.ui.dialogs.CitationManagerDialog
import com.example.ui.dialogs.TemplateLibraryDialog
import com.example.ui.dialogs.VocabularyCheckerDialog
import com.example.ui.dialogs.VoiceDictationDialog
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.DocuMedViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val lastSavedTimestamp by viewModel.lastSavedTimestamp.collectAsState()
    val isAutoSaving by viewModel.isAutoSaving.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val aiStatusMessage by viewModel.aiStatusMessage.collectAsState()
    val generatedPresentation by viewModel.generatedPresentation.collectAsState()

    // Cursor-tracked textfield state
    var textFieldValue by remember(selectedDoc?.id) {
        mutableStateOf(TextFieldValue(content, TextRange(content.length)))
    }

    // Keep text field in sync if content was updated externally (e.g. AI generation / version restore)
    LaunchedEffect(content) {
        if (content != textFieldValue.text) {
            val oldSelection = textFieldValue.selection
            val safeStart = oldSelection.start.coerceIn(0, content.length)
            val safeEnd = oldSelection.end.coerceIn(0, content.length)
            textFieldValue = TextFieldValue(content, TextRange(safeStart, safeEnd))
        }
    }

    // Helper to insert formatting snippets at cursor or around highlighted selection
    fun insertSnippet(prefix: String, suffix: String = "", placeholder: String = "") {
        val currentText = textFieldValue.text
        val selection = textFieldValue.selection
        val hasSelection = !selection.collapsed
        val selectedText = if (hasSelection) {
            currentText.substring(selection.min, selection.max)
        } else ""

        val snippet = if (hasSelection) {
            "$prefix$selectedText$suffix"
        } else if (placeholder.isNotEmpty()) {
            "$prefix$placeholder$suffix"
        } else {
            "$prefix$suffix"
        }

        // Smart newline handling for block elements
        val isBlock = prefix.startsWith("#") || prefix.startsWith("[") || prefix.startsWith("*") || prefix.startsWith("|")
        val needsLeadingNewline = isBlock && selection.min > 0 && currentText[selection.min - 1] != '\n'
        val finalSnippet = if (needsLeadingNewline) "\n$snippet" else snippet

        val newText = currentText.replaceRange(selection.min, selection.max, finalSnippet)

        // Position cursor: if placeholder was inserted without prior selection, highlight the placeholder
        val newSelection = if (!hasSelection && placeholder.isNotEmpty()) {
            val start = selection.min + (if (needsLeadingNewline) 1 else 0) + prefix.length
            TextRange(start, start + placeholder.length)
        } else {
            val cursor = selection.min + finalSnippet.length
            TextRange(cursor, cursor)
        }

        textFieldValue = TextFieldValue(newText, newSelection)
        viewModel.updateEditorContent(newText)
    }

    val formattedLastSavedTime = remember(lastSavedTimestamp) {
        if (lastSavedTimestamp > 0) {
            SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date(lastSavedTimestamp))
        } else {
            "just now"
        }
    }

    var showTocDrawer by remember { mutableStateOf(false) }
    var showVersionDialog by remember { mutableStateOf(false) }
    var showInsertPhotoDialog by remember { mutableStateOf(false) }
    var showPresentationDialog by remember { mutableStateOf(false) }
    var showCitationDialog by remember { mutableStateOf(false) }
    var showVoiceDictationDialog by remember { mutableStateOf(false) }
    var showTemplateLibraryDialog by remember { mutableStateOf(false) }
    var showVocabularyDialog by remember { mutableStateOf(false) }
    var showCalculatorsDialog by remember { mutableStateOf(false) }
    var showDrugFormularyDialog by remember { mutableStateOf(false) }
    var showPatientLeafletDialog by remember { mutableStateOf(false) }
    var showImageAnnotationDialog by remember { mutableStateOf(false) }
    var showCommentsDialog by remember { mutableStateOf(false) }
    var showAddCommentDialog by remember { mutableStateOf(false) }
    var showUserLoginDialog by remember { mutableStateOf(false) }
    var showAdminManagementDialog by remember { mutableStateOf(false) }
    var commentInitialSelectedText by remember { mutableStateOf("") }
    var isPreviewMode by remember { mutableStateOf(false) }
    var aiTransformMenuExpanded by remember { mutableStateOf(false) }
    var showSaveToast by remember { mutableStateOf(false) }

    val activeCitationStyle by viewModel.activeCitationStyle.collectAsState()
    val documentCitations by viewModel.documentCitations.collectAsState()

    // Multi-User Collaboration States
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.users.collectAsState()
    val allDocs by viewModel.documents.collectAsState()
    val activeCollaborators by viewModel.activeCollaborators.collectAsState()
    val activeComments by viewModel.activeDocumentComments.collectAsState()
    val canEditDoc by viewModel.canEditCurrentDocument.collectAsState()
    val canViewDoc by viewModel.canViewCurrentDocument.collectAsState()
    val currentDocPermission by viewModel.currentDocumentPermissionLevel.collectAsState()

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
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
                                modifier = Modifier.widthIn(min = 140.dp, max = 240.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .horizontalScroll(rememberScrollState())
                            ) {
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
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))

                                // Subtle 'Auto-saved' indicator with timestamp of last local storage sync
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isAutoSaving) MedicalBluePrimary.copy(alpha = 0.08f) else ClinicalGreen.copy(alpha = 0.08f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isAutoSaving) Icons.Default.Sync else Icons.Default.CloudDone,
                                            contentDescription = "Auto-save status",
                                            tint = if (isAutoSaving) MedicalBluePrimary else ClinicalGreen,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = if (isAutoSaving) "Saving..." else "Auto-saved $formattedLastSavedTime",
                                            color = if (isAutoSaving) MedicalBluePrimary else ClinicalGreen,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Action Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // User Profile Pill (Tap to Switch Account / Log In)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(currentUser.avatarColorHex).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(currentUser.avatarColorHex).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { showUserLoginDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(currentUser.avatarColorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser.avatarInitials,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = currentUser.name.split(" ").firstOrNull() ?: currentUser.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(currentUser.avatarColorHex)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                UserRoleBadge(role = currentUser.role)
                            }
                        }

                        // Comments & Annotations button with Badge
                        IconButton(onClick = { showCommentsDialog = true }) {
                            Box {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Comment,
                                    contentDescription = "Document Comments",
                                    tint = if (activeComments.any { it.status == com.example.data.model.CommentStatus.OPEN }) ClinicalAmber else MedicalBluePrimary
                                )
                                if (activeComments.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(if (activeComments.any { it.status == com.example.data.model.CommentStatus.OPEN }) ClinicalAmber else ClinicalGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${activeComments.size}",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Admin Access Control (if admin)
                        if (currentUser.role == UserRole.ADMIN) {
                            IconButton(onClick = { showAdminManagementDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Admin Roles & Permissions",
                                    tint = Color(0xFF7C3AED)
                                )
                            }
                        }

                        // Voice Dictation
                        IconButton(onClick = { showVoiceDictationDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Dictation",
                                tint = Color(0xFFE11D48)
                            )
                        }

                        // Citations & References
                        IconButton(onClick = { showCitationDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = "Citation Manager",
                                tint = MedicalBluePrimary
                            )
                        }

                        // Medical Template Library
                        IconButton(onClick = { showTemplateLibraryDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Medical Template Library",
                                tint = MedicalTealPrimary
                            )
                        }

                        // Smart Vocabulary & Safety Checker
                        IconButton(onClick = { showVocabularyDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Spellcheck,
                                contentDescription = "Vocabulary & Safety Check",
                                tint = Color(0xFF7C3AED)
                            )
                        }

                        // AI PowerPoint Presentation Deck
                        IconButton(onClick = {
                            viewModel.generatePowerPointPresentation()
                            showPresentationDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Slideshow,
                                contentDescription = "AI PowerPoint Slide Deck",
                                tint = Color(0xFF0284C7)
                            )
                        }

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

                        // Insert Photo / Scan
                        IconButton(onClick = { showInsertPhotoDialog = true }) {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = "Insert Photo / Scan", tint = Color(0xFF0284C7))
                        }

                        // Save Button (disabled if view-only)
                        if (canEditDoc) {
                            IconButton(onClick = {
                                viewModel.saveCurrentDocument("Manual Save")
                                showSaveToast = true
                            }) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = "Save Document", tint = MedicalBluePrimary)
                            }
                        }

                        // Settings Shortcut
                        IconButton(onClick = { viewModel.navigateTo(AppNavTab.SETTINGS) }) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
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

        // Read-Only / Reviewer Banner if not permitted to edit
        if (!canEditDoc) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = MedicalTealPrimary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedicalTealPrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MedicalTealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Review & Comment Mode (Read-Only)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MedicalTealPrimary
                            )
                            Text(
                                text = "Logged in as ${currentUser.name} (${currentUser.role.badgeLabel}). You can add comments, highlight annotations, and propose draft suggestions.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            val selMin = textFieldValue.selection.min
                            val selMax = textFieldValue.selection.max
                            val selected = if (selMin < selMax && selMax <= textFieldValue.text.length) {
                                textFieldValue.text.substring(selMin, selMax)
                            } else ""
                            commentInitialSelectedText = selected
                            showAddCommentDialog = true
                        },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Feedback, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Note", fontSize = 11.sp)
                    }
                }
            }
        }

        // Quick Medical Formatting Toolbar (Cursor & Selection Aware)
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
            FormatChip(label = "Feedback", icon = Icons.Default.Feedback, color = MedicalBluePrimary) {
                val selMin = textFieldValue.selection.min
                val selMax = textFieldValue.selection.max
                val selected = if (selMin < selMax && selMax <= textFieldValue.text.length) {
                    textFieldValue.text.substring(selMin, selMax)
                } else ""
                commentInitialSelectedText = selected
                showAddCommentDialog = true
            }
            FormatChip(label = "H1", icon = Icons.Default.Title) {
                insertSnippet("# ", placeholder = "Heading 1")
            }
            FormatChip(label = "H2", icon = Icons.Default.Title) {
                insertSnippet("## ", placeholder = "Heading 2")
            }
            FormatChip(label = "H3", icon = Icons.Default.FormatSize) {
                insertSnippet("### ", placeholder = "Heading 3")
            }
            FormatChip(label = "Bold", icon = Icons.Default.FormatBold) {
                insertSnippet("**", "**", placeholder = "bold text")
            }
            FormatChip(label = "Bullet", icon = Icons.AutoMirrored.Filled.FormatListBulleted) {
                insertSnippet("* ", placeholder = "Clinical bullet point")
            }
            FormatChip(label = "Dictate", icon = Icons.Default.Mic, color = Color(0xFFE11D48)) {
                showVoiceDictationDialog = true
            }
            FormatChip(label = "Citations", icon = Icons.Default.FormatQuote, color = MedicalBluePrimary) {
                showCitationDialog = true
            }
            FormatChip(label = "Templates", icon = Icons.Default.Description, color = MedicalTealPrimary) {
                showTemplateLibraryDialog = true
            }
            FormatChip(label = "Vocab Check", icon = Icons.Default.Spellcheck, color = Color(0xFF7C3AED)) {
                showVocabularyDialog = true
            }
            FormatChip(label = "Clinical Pearl", icon = Icons.Default.Lightbulb, color = MedicalBluePrimary) {
                insertSnippet("[KEY_POINT: ", "]", placeholder = "High-yield clinical pearl / takeaway")
            }
            FormatChip(label = "Red Flags", icon = Icons.Default.Warning, color = Color(0xFFDC2626)) {
                insertSnippet("[WARNING: ", "]", placeholder = "Critical contraindication or emergency red flag")
            }
            FormatChip(label = "Evidence Grade", icon = Icons.Default.CheckCircle, color = ClinicalGreen) {
                insertSnippet("[EVIDENCE_LEVEL: ", "]", placeholder = "Level A Evidence (ACC/AHA 2025)")
            }
            FormatChip(label = "AI Slides (PPT)", icon = Icons.Default.Slideshow, color = Color(0xFF0284C7)) {
                viewModel.generatePowerPointPresentation()
                showPresentationDialog = true
            }
            FormatChip(label = "Calculators", icon = Icons.Default.Calculate, color = MedicalBluePrimary) {
                showCalculatorsDialog = true
            }
            FormatChip(label = "Formulary", icon = Icons.Default.Medication, color = Color(0xFF0F766E)) {
                showDrugFormularyDialog = true
            }
            FormatChip(label = "Patient Leaflet", icon = Icons.Default.Language, color = Color(0xFF0284C7)) {
                showPatientLeafletDialog = true
            }
            FormatChip(label = "Annotate Scan", icon = Icons.Default.Brush, color = Color(0xFF1E293B)) {
                showImageAnnotationDialog = true
            }
            FormatChip(label = "Photo / Figure", icon = Icons.Default.AddPhotoAlternate, color = Color(0xFF0284C7)) {
                showInsertPhotoDialog = true
            }
            FormatChip(label = "Table", icon = Icons.Default.TableChart) {
                insertSnippet(
                    prefix = "\n| Classification | Diagnostic Criteria | Recommended Treatment |\n| :--- | :--- | :--- |\n| **Stage 1** | Early localized findings | Conservative / Medication |\n| **Stage 2** | Displaced / Progressive | Surgical Fixation / Interventional |\n"
                )
            }
            FormatChip(label = "Algorithm", icon = Icons.Default.Timeline) {
                insertSnippet(
                    prefix = "\n[STEP 1] Initial Clinical Triage -> Emergency Assessment\n[STEP 2] Diagnostic Radiographs & Lab Workup -> Risk Stratification\n[STEP 3A] Stable -> Medical Optimization & Observation\n[STEP 3B] Unstable -> Immediate Surgical Decompression\n[STEP 4] Postoperative Rehabilitation & DVT Prophylaxis\n"
                )
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
                        text = { Text("Clinical Calculators & Scores (Wells, CHA₂DS₂, GCS...)") },
                        onClick = {
                            aiTransformMenuExpanded = false
                            showCalculatorsDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Bedside Drug Formulary & Interaction Checker") },
                        onClick = {
                            aiTransformMenuExpanded = false
                            showDrugFormularyDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Multi-Language Patient Information Leaflet") },
                        onClick = {
                            aiTransformMenuExpanded = false
                            showPatientLeafletDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Radiology Image Annotation Canvas") },
                        onClick = {
                            aiTransformMenuExpanded = false
                            showImageAnnotationDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Generate AI PowerPoint Deck") },
                        onClick = {
                            aiTransformMenuExpanded = false
                            viewModel.generatePowerPointPresentation()
                            showPresentationDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Insert Clinical Radiograph / Photo") },
                        onClick = {
                            aiTransformMenuExpanded = false
                            showInsertPhotoDialog = true
                        }
                    )
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
                                            // Highlight or scroll to section
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
                    // Plain Text/Markdown Live Editor with Cursor Tracking
                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = {
                            if (canEditDoc) {
                                textFieldValue = it
                                viewModel.updateEditorContent(it.text)
                            }
                        },
                        readOnly = !canEditDoc,
                        placeholder = {
                            Text(
                                if (!canEditDoc) "Review & Feedback Mode: Select text above or tap '+ Feedback' to leave annotations..."
                                else "Start typing your medical notes or paste clinical findings..."
                            )
                        },
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

    if (showCommentsDialog) {
        DocumentCommentsDialog(
            comments = activeComments,
            collaborators = activeCollaborators,
            currentUser = currentUser,
            canEditDocument = canEditDoc,
            onDismiss = { showCommentsDialog = false },
            onAddNewComment = {
                val selMin = textFieldValue.selection.min
                val selMax = textFieldValue.selection.max
                val selected = if (selMin < selMax && selMax <= textFieldValue.text.length) {
                    textFieldValue.text.substring(selMin, selMax)
                } else ""
                commentInitialSelectedText = selected
                showAddCommentDialog = true
            },
            onAddReply = { commentId, replyText ->
                viewModel.addCommentReply(commentId, replyText)
            },
            onResolveComment = { commentId ->
                viewModel.resolveComment(commentId)
            },
            onReopenComment = { commentId ->
                viewModel.reopenComment(commentId)
            },
            onDeleteComment = { commentId ->
                viewModel.deleteComment(commentId)
            },
            onApplySuggestion = { comment ->
                viewModel.applyCommentSuggestion(comment)
                // Refresh local textfield value
                textFieldValue = TextFieldValue(viewModel.editorContent.value)
            }
        )
    }

    if (showAddCommentDialog) {
        AddCommentAnnotationDialog(
            currentUser = currentUser,
            initialSelectedText = commentInitialSelectedText,
            detectedSection = currentDoc.title,
            onDismiss = { showAddCommentDialog = false },
            onSubmitComment = { commentText, selectedText, sectionTitle, commentType, suggestedReplacement ->
                viewModel.addDocumentComment(
                    commentText = commentText,
                    selectedText = selectedText,
                    sectionTitle = sectionTitle,
                    commentType = commentType,
                    suggestedReplacement = suggestedReplacement
                )
            }
        )
    }

    if (showUserLoginDialog) {
        UserLoginSwitcherDialog(
            users = allUsers,
            currentUser = currentUser,
            onDismiss = { showUserLoginDialog = false },
            onSelectUser = { userId ->
                viewModel.switchUser(userId)
            },
            onOpenAdminPanel = {
                showAdminManagementDialog = true
            },
            onCreateUser = { name, email, role, specialty, title ->
                viewModel.createNewUser(name, email, role, specialty, title)
            }
        )
    }

    if (showAdminManagementDialog) {
        AdminUserManagementDialog(
            users = allUsers,
            documents = allDocs,
            currentUser = currentUser,
            onDismiss = { showAdminManagementDialog = false },
            onUpdateUserRole = { userId, newRole ->
                viewModel.updateUserRole(userId, newRole)
            },
            onDeleteUser = { userId ->
                viewModel.deleteUser(userId)
            },
            onSetDocumentPermission = { userId, docId, permission ->
                viewModel.setDocumentPermission(userId, docId, permission)
            },
            onSetBatchPermissions = { userId, docIds, permission ->
                viewModel.setBatchDocumentPermissions(userId, docIds, permission)
            },
            onCreateUser = { name, email, role, specialty, title ->
                viewModel.createNewUser(name, email, role, specialty, title)
            }
        )
    }

    if (showPresentationDialog) {
        PresentationViewDialog(
            presentation = generatedPresentation,
            isGenerating = isAiGenerating,
            onDismiss = { showPresentationDialog = false },
            onRegenerate = { viewModel.generatePowerPointPresentation() }
        )
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

    if (showInsertPhotoDialog) {
        InsertPhotoDialog(
            viewModel = viewModel,
            onDismiss = { showInsertPhotoDialog = false },
            onInsertPhoto = { uri, caption, notes ->
                viewModel.insertPhotoIntoEditor(uri, caption, notes)
            }
        )
    }

    if (showCitationDialog) {
        CitationManagerDialog(
            initialCitations = documentCitations,
            currentStyle = activeCitationStyle,
            onDismiss = { showCitationDialog = false },
            onInsertCitationKey = { key ->
                insertSnippet(key)
            },
            onUpdateBibliographyInDoc = { bibText, updatedCitations, style ->
                viewModel.updateDocumentBibliography(bibText, updatedCitations, style)
            }
        )
    }

    if (showVoiceDictationDialog) {
        VoiceDictationDialog(
            onDismiss = { showVoiceDictationDialog = false },
            onInsertText = { dictatedText ->
                insertSnippet(dictatedText)
            },
            onSynthesizeWithAi = { dictatedText ->
                viewModel.appendContentToEditor(dictatedText)
                viewModel.applyAiTransform("Synthesize and structure this dictated clinical observation into clean SOAP / academic format.")
            }
        )
    }

    if (showTemplateLibraryDialog) {
        TemplateLibraryDialog(
            onDismiss = { showTemplateLibraryDialog = false },
            onSelectTemplateForCurrentDoc = { template ->
                viewModel.insertTemplateIntoCurrentDoc(template)
            },
            onCreateNewDocFromTemplate = { template ->
                viewModel.createDocumentFromTemplate(template)
            }
        )
    }

    if (showVocabularyDialog) {
        VocabularyCheckerDialog(
            initialDocumentText = content,
            onDismiss = { showVocabularyDialog = false },
            onApplyCorrections = { correctedText ->
                viewModel.applyVocabularyCorrections(correctedText)
            }
        )
    }

    if (showCalculatorsDialog) {
        ClinicalCalculatorsDialog(
            onDismiss = { showCalculatorsDialog = false },
            onInsertCalculation = { result ->
                viewModel.insertCalculationIntoEditor(result)
                showCalculatorsDialog = false
            }
        )
    }

    if (showDrugFormularyDialog) {
        DrugFormularyDialog(
            repository = viewModel.drugFormularyRepository,
            onDismiss = { showDrugFormularyDialog = false },
            onInsertMonograph = { drug ->
                viewModel.insertDrugMonographIntoEditor(drug)
                showDrugFormularyDialog = false
            },
            onInsertInteractionReport = { interactions, names ->
                viewModel.insertInteractionReportIntoEditor(interactions, names)
                showDrugFormularyDialog = false
            }
        )
    }

    if (showPatientLeafletDialog) {
        val currentContent = viewModel.editorContent.collectAsState().value
        val generatedLeaflet = viewModel.generatedPatientLeaflet.collectAsState().value
        val isGenerating = viewModel.isAiGenerating.collectAsState().value
        PatientLeafletDialog(
            initialContent = currentContent,
            generatedLeaflet = generatedLeaflet,
            isGenerating = isGenerating,
            onDismiss = { showPatientLeafletDialog = false },
            onGenerate = { text, lang, lvl ->
                viewModel.generatePatientLeafletWithAi(text, lang, lvl)
            },
            onInsertLeaflet = { markdown ->
                viewModel.appendContentToEditor(markdown)
                showPatientLeafletDialog = false
            }
        )
    }

    if (showImageAnnotationDialog) {
        MedicalImageAnnotationDialog(
            onDismiss = { showImageAnnotationDialog = false },
            onInsertFigure = { figure ->
                viewModel.insertAnnotatedFigureIntoEditor(figure)
                showImageAnnotationDialog = false
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
    var zoomedImageUrl by remember { mutableStateOf<Pair<String, String>?>(null) }
    val blocks = content.lines()

    blocks.forEach { line ->
        val trimmed = line.trim()
        when {
            // Markdown Image Syntax: ![Caption](uri)
            trimmed.startsWith("![") && trimmed.contains("](") && trimmed.endsWith(")") -> {
                val captionEnd = trimmed.indexOf("](")
                val caption = trimmed.substring(2, captionEnd)
                val uri = trimmed.substring(captionEnd + 2, trimmed.length - 1)
                ClinicalFigureCard(
                    imageUri = uri,
                    caption = caption.ifBlank { "Clinical Figure" },
                    notes = "",
                    onZoom = { zoomedImageUrl = Pair(uri, caption) }
                )
            }
            // Custom Clinical Figure Block: [FIGURE: caption | notes | uri]
            trimmed.startsWith("[FIGURE:") || trimmed.startsWith("[IMAGE:") -> {
                val raw = if (trimmed.startsWith("[FIGURE:")) {
                    trimmed.removePrefix("[FIGURE:").removeSuffix("]").trim()
                } else {
                    trimmed.removePrefix("[IMAGE:").removeSuffix("]").trim()
                }
                val parts = raw.split("|").map { it.trim() }
                val caption = parts.getOrNull(0)?.ifBlank { "Clinical Figure / Diagnostic Scan" } ?: "Clinical Figure"
                val notes = parts.getOrNull(1) ?: ""
                val uri = parts.getOrNull(2) ?: ""

                ClinicalFigureCard(
                    imageUri = uri,
                    caption = caption,
                    notes = notes,
                    onZoom = { zoomedImageUrl = Pair(uri, caption) }
                )
            }
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

    zoomedImageUrl?.let { (url, caption) ->
        ClinicalImageZoomDialog(
            imageUrl = url,
            caption = caption,
            onDismiss = { zoomedImageUrl = null }
        )
    }
}

@Composable
fun ClinicalFigureCard(
    imageUri: String,
    caption: String,
    notes: String,
    onZoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MedicalBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CLINICAL FIGURE / DIAGNOSTIC SCAN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MedicalBluePrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MedicalBluePrimary.copy(alpha = 0.12f),
                    modifier = Modifier.clickable { onZoom() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Expand",
                            tint = MedicalBluePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tap to Zoom",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MedicalBluePrimary
                        )
                    }
                }
            }

            // Image Container with High Contrast & Rounded Shape
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .clickable { onZoom() },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri.isNotBlank()) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = caption,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("No Image Preview Available", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }

                // Overlay zoom button
                IconButton(
                    onClick = onZoom,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Figure Caption in Medical Bold Style
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Diagnostic Annotations / Findings
            if (notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Findings / Annotations: $notes",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ClinicalImageZoomDialog(
    imageUrl: String,
    caption: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Bar with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Centered High-Res Image View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = caption,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Footer instructions
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "High-Resolution Diagnostic Preview · Tap outside or close to return",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f)),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
