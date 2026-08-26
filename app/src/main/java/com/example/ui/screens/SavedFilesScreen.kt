package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.DocumentExportHelper
import com.example.data.model.MedicalPresentation
import com.example.data.model.PresentationSlide
import com.example.data.model.SavedFile
import com.example.ui.components.PresentationViewDialog
import com.example.ui.dialogs.SaveToFilesHubDialog
import com.example.ui.dialogs.SlideDeckEditorDialog
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalRed
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.DocuMedViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedFilesScreen(
    viewModel: DocuMedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedFiles by viewModel.savedFiles.collectAsState()
    val activeFilter by viewModel.savedFilesFilter.collectAsState()
    val searchQuery by viewModel.savedFilesSearchQuery.collectAsState()
    val googleDriveUrl by viewModel.googleDriveUrl.collectAsState()

    var showNewFileDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<SavedFile?>(null) }
    var presentationToEdit by remember { mutableStateOf<Pair<MedicalPresentation, SavedFile?>?>(null) }
    var presentationToView by remember { mutableStateOf<MedicalPresentation?>(null) }
    var textFileToEdit by remember { mutableStateOf<SavedFile?>(null) }
    var showDriveSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewFileDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_saved_file")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Save New File")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Saved Files Hub",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Manage, edit & sync PPT, PDF & Word files",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Google Drive Button
                        OutlinedButton(
                            onClick = { DocumentExportHelper.openGoogleDrive(context, googleDriveUrl) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF0F9D58)
                            ),
                            modifier = Modifier.testTag("open_google_drive_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Google Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Google Drive Link Sync Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDriveSettingsDialog = true }
                            .testTag("google_drive_banner"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F9D58).copy(alpha = 0.08f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(0xFF0F9D58).copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F9D58)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Google Drive Linked",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F9D58)
                                    )
                                    Text(
                                        text = googleDriveUrl,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }

                            Text(
                                text = "Configure",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F9D58)
                            )
                        }
                    }
                }
            }

            // Search and Filter Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.savedFilesSearchQuery.value = it },
                    placeholder = { Text("Search PPT, PDF, Word files...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("saved_files_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Format Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "All" to "All Files",
                        "PPT" to "PPT Decks",
                        "PDF" to "PDFs",
                        "DOCX" to "Word Docs"
                    ).forEach { (filterKey, label) ->
                        val isSelected = activeFilter == filterKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.savedFilesFilter.value = filterKey },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (filterKey) {
                                    "PPT" -> Color(0xFFEA580C)
                                    "PDF" -> Color(0xFFDC2626)
                                    "DOCX" -> Color(0xFF2563EB)
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_chip_$filterKey")
                        )
                    }
                }
            }

            Divider()

            // Files List
            if (savedFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No saved files found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Generate PPT presentations or save PDF/Word notes from AI Studio & Editor.",
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showNewFileDialog = true },
                            modifier = Modifier.testTag("create_first_file_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save a New File")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savedFiles, key = { it.id }) { file ->
                        SavedFileCard(
                            file = file,
                            onEdit = {
                                if (file.fileType == "PPT") {
                                    val slides = DocumentExportHelper.slidesFromJson(file.slidesJson)
                                    val presentation = MedicalPresentation(
                                        title = file.title,
                                        topic = file.title,
                                        totalSlides = slides.size,
                                        slides = slides
                                    )
                                    presentationToEdit = presentation to file
                                } else {
                                    textFileToEdit = file
                                }
                            },
                            onPreview = {
                                if (file.fileType == "PPT") {
                                    val slides = DocumentExportHelper.slidesFromJson(file.slidesJson)
                                    val presentation = MedicalPresentation(
                                        title = file.title,
                                        topic = file.title,
                                        totalSlides = slides.size,
                                        slides = slides
                                    )
                                    presentationToView = presentation
                                } else {
                                    val tempDoc = com.example.data.model.MedicalDocument(
                                        title = file.title,
                                        content = file.content,
                                        docType = file.fileType,
                                        specialty = "Clinical Medicine"
                                    )
                                    DocumentExportHelper.shareDocument(context, tempDoc, file.fileType)
                                }
                            },
                            onUploadToDrive = {
                                val tempFile = when (file.fileType) {
                                    "PPT" -> {
                                        val slides = DocumentExportHelper.slidesFromJson(file.slidesJson)
                                        val pres = MedicalPresentation(
                                            title = file.title,
                                            topic = file.title,
                                            totalSlides = slides.size,
                                            slides = slides
                                        )
                                        DocumentExportHelper.exportPresentationToHtmlFile(context, pres)
                                    }
                                    "DOCX" -> DocumentExportHelper.exportRawTextToDocxFile(context, file.title, file.content)
                                    else -> DocumentExportHelper.exportRawTextToPdfFile(context, file.title, file.content)
                                }
                                val mime = when (file.fileType) {
                                    "DOCX" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                    "PPT" -> "text/html"
                                    else -> "text/html"
                                }
                                DocumentExportHelper.shareFileToGoogleDrive(context, tempFile, mime, file.title)
                            },
                            onShare = {
                                if (file.fileType == "PPT") {
                                    val slides = DocumentExportHelper.slidesFromJson(file.slidesJson)
                                    val pres = MedicalPresentation(
                                        title = file.title,
                                        topic = file.title,
                                        totalSlides = slides.size,
                                        slides = slides
                                    )
                                    DocumentExportHelper.sharePresentation(context, pres)
                                } else {
                                    val tempDoc = com.example.data.model.MedicalDocument(
                                        title = file.title,
                                        content = file.content,
                                        docType = file.fileType,
                                        specialty = "Clinical Medicine"
                                    )
                                    DocumentExportHelper.shareDocument(context, tempDoc, file.fileType)
                                }
                            },
                            onDelete = { fileToDelete = file }
                        )
                    }
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        SaveToFilesHubDialog(
            initialTitle = viewModel.editorTitle.value.ifBlank { "Clinical Document" },
            initialFileType = "PDF",
            defaultDriveLink = googleDriveUrl,
            onDismiss = { showNewFileDialog = false },
            onSave = { title, fileType, description, driveLink ->
                viewModel.saveFileToHub(
                    title = title,
                    fileType = fileType,
                    description = description,
                    content = viewModel.editorContent.value,
                    driveLink = driveLink
                )
            }
        )
    }

    // Delete Confirmation Dialog
    if (fileToDelete != null) {
        val file = fileToDelete!!
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete Saved File?") },
            text = { Text("Are you sure you want to delete '${file.title}' (${file.fileType})? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSavedFile(file)
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClinicalRed),
                    modifier = Modifier.testTag("confirm_delete_saved_file_btn")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { fileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Slide Deck Editor Dialog (PPT)
    if (presentationToEdit != null) {
        val (presentation, savedFile) = presentationToEdit!!
        SlideDeckEditorDialog(
            initialPresentation = presentation,
            onDismiss = { presentationToEdit = null },
            onSaveDeck = { updatedPresentation ->
                val json = DocumentExportHelper.slidesToJson(updatedPresentation.slides)
                if (savedFile != null) {
                    val updatedFile = savedFile.copy(
                        title = updatedPresentation.title,
                        slidesJson = json,
                        updatedAt = System.currentTimeMillis()
                    )
                    viewModel.updateSavedFile(updatedFile)
                }
                viewModel.updateSlideDeck(updatedPresentation)
            },
            onSaveToHub = { updatedPresentation ->
                val json = DocumentExportHelper.slidesToJson(updatedPresentation.slides)
                viewModel.saveFileToHub(
                    title = updatedPresentation.title,
                    fileType = "PPT",
                    description = "${updatedPresentation.slides.size}-Slide Deck with Clinical Pearls & Speaker Notes",
                    slidesJson = json
                )
                presentationToEdit = null
            }
        )
    }

    // Slide Deck Viewer / Presentation Mode
    if (presentationToView != null) {
        PresentationViewDialog(
            presentation = presentationToView,
            isGenerating = false,
            onDismiss = { presentationToView = null },
            onRegenerate = {}
        )
    }

    // PDF / Word Text File Editor Dialog
    if (textFileToEdit != null) {
        val file = textFileToEdit!!
        var editTitle by remember { mutableStateOf(file.title) }
        var editContent by remember { mutableStateOf(file.content) }
        var editDesc by remember { mutableStateOf(file.description) }

        AlertDialog(
            onDismissRequest = { textFileToEdit = null },
            title = {
                Text("Edit ${file.fileType} Document: ${file.title}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editContent,
                        onValueChange = { editContent = it },
                        label = { Text("Document Body / Text") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = file.copy(
                            title = editTitle,
                            description = editDesc,
                            content = editContent,
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.updateSavedFile(updated)
                        textFileToEdit = null
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { textFileToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Google Drive URL Configuration Dialog
    if (showDriveSettingsDialog) {
        var tempUrl by remember { mutableStateOf(googleDriveUrl) }
        AlertDialog(
            onDismissRequest = { showDriveSettingsDialog = false },
            title = { Text("Google Drive Folder Link") },
            text = {
                Column {
                    Text(
                        text = "Enter your preferred Google Drive folder URL to quickly access and sync medical files:",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("Google Drive URL") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = Color(0xFF0F9D58))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setGoogleDriveUrl(tempUrl)
                        showDriveSettingsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58))
                ) {
                    Text("Save Link")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDriveSettingsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SavedFileCard(
    file: SavedFile,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onUploadToDrive: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val (typeColor, typeIcon, typeLabel) = when (file.fileType) {
        "PPT" -> Triple(Color(0xFFEA580C), Icons.Default.Slideshow, "PowerPoint Deck")
        "PDF" -> Triple(Color(0xFFDC2626), Icons.Default.PictureAsPdf, "PDF Document")
        "DOCX" -> Triple(Color(0xFF2563EB), Icons.Default.Description, "Word File")
        else -> Triple(MaterialTheme.colorScheme.primary, Icons.Default.Description, "Document")
    }

    val sdf = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
    val dateStr = remember(file.updatedAt) { sdf.format(Date(file.updatedAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_file_card_${file.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(typeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(typeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = file.fileType,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = typeColor
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = file.fileSize,
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = file.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("delete_file_btn_${file.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ClinicalRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (file.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = file.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata info & date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (file.driveLink.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = Color(0xFF0F9D58),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Drive Synced",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F9D58)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Edit Button
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("edit_file_btn_${file.id}"),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }

                // Preview / View Button
                OutlinedButton(
                    onClick = onPreview,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("preview_file_btn_${file.id}"),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (file.fileType == "PPT") "Present" else "View", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }

                // Google Drive Upload
                OutlinedButton(
                    onClick = onUploadToDrive,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F9D58)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("drive_upload_btn_${file.id}"),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Drive", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }

                // Share
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(36.dp).testTag("share_file_btn_${file.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
