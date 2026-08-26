package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.SourceMaterial
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.viewmodel.DocuMedViewModel

data class MedicalPhotoPreset(
    val title: String,
    val modality: String,
    val caption: String,
    val clinicalNotes: String,
    val sampleUriOrUrl: String,
    val diagnosticBadge: String
)

val CLINICAL_PHOTO_PRESETS = listOf(
    MedicalPhotoPreset(
        title = "Femoral Neck Fracture AP Radiograph",
        modality = "Plain Radiography (X-Ray)",
        caption = "Figure 1: AP Pelvis Radiograph demonstrating displaced subcapital left femoral neck fracture.",
        clinicalNotes = "Cortical disruption with Garden Stage III varus tilt. High risk of avascular necrosis (AVN) of the femoral head due to medial circumflex femoral artery disruption.",
        sampleUriOrUrl = "https://images.unsplash.com/photo-1516549655169-df83a0774514?auto=format&fit=crop&w=800&q=80",
        diagnosticBadge = "Ortho / Trauma"
    ),
    MedicalPhotoPreset(
        title = "Acute Anterior Wall STEMI 12-Lead ECG",
        modality = "12-Lead Electrocardiogram (ECG)",
        caption = "Figure 2: 12-Lead ECG showing hyperacute 'tombstone' ST-segment elevations in V1-V4.",
        clinicalNotes = "ST-elevation in precordial leads V1-V4 with reciprocal ST-depression in inferior leads (II, III, aVF). Diagnostic of acute Left Anterior Descending (LAD) coronary artery occlusion. Immediate primary PCI indicated.",
        sampleUriOrUrl = "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=800&q=80",
        diagnosticBadge = "Cardiology"
    ),
    MedicalPhotoPreset(
        title = "Right Middle Lobe Lobar Pneumonia CXR",
        modality = "Chest Radiograph (PA View)",
        caption = "Figure 3: PA Chest Radiograph demonstrating dense consolidation in the right middle lobe.",
        clinicalNotes = "Prominent air bronchograms with silhouetting of the right heart border, confirming right middle lobe anatomical localization (Streptococcus pneumoniae etiology).",
        sampleUriOrUrl = "https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?auto=format&fit=crop&w=800&q=80",
        diagnosticBadge = "Pulmonology"
    ),
    MedicalPhotoPreset(
        title = "Malignant Melanoma Clinical Photography",
        modality = "Clinical Dermatology Photography",
        caption = "Figure 4: Macroscopic dermoscopy of 8mm pigmented skin lesion on left scapula.",
        clinicalNotes = "Positive for ABCD criteria: Asymmetry, Border irregularity, Color variegation (dark brown to slate black), Diameter > 6mm. Urgent full-thickness excisional biopsy required with 2mm margins.",
        sampleUriOrUrl = "https://images.unsplash.com/photo-1579684385127-1ef15d508118?auto=format&fit=crop&w=800&q=80",
        diagnosticBadge = "Dermatology"
    ),
    MedicalPhotoPreset(
        title = "Epidural Hematoma Head Non-Contrast CT",
        modality = "Computed Tomography (CT Brain)",
        caption = "Figure 5: Non-contrast axial head CT demonstrating biconvex hyperdense extra-axial collection.",
        clinicalNotes = "Classic lens-shaped (lenticular) hematoma limited by cranial sutures, caused by middle meningeal artery laceration associated with temporal bone fracture. Midline shift present.",
        sampleUriOrUrl = "https://images.unsplash.com/photo-1559757175-5700dde675bc?auto=format&fit=crop&w=800&q=80",
        diagnosticBadge = "Neurosurgery"
    ),
    MedicalPhotoPreset(
        title = "Hodgkin Lymphoma Reed-Sternberg Histopathology",
        modality = "Histopathology (H&E Stain 400x)",
        caption = "Figure 6: High-power photomicrograph of lymph node biopsy demonstrating Reed-Sternberg cell.",
        clinicalNotes = "Characteristic binucleated 'owl-eyes' giant cell with prominent eosinophilic nucleoli on a background of mixed reactive lymphocytes, eosinophils, and plasma cells.",
        sampleUriOrUrl = "https://images.unsplash.com/photo-1530026405186-ed1f139313f8?auto=format&fit=crop&w=800&q=80",
        diagnosticBadge = "Pathology"
    )
)

enum class PhotoInsertTab(val label: String, val icon: ImageVector) {
    UPLOAD("Device Gallery / Pick", Icons.Default.AddPhotoAlternate),
    KNOWLEDGE_BASE("Knowledge Base", Icons.Default.FolderOpen),
    CLINICAL_PRESETS("Clinical Presets", Icons.Default.MedicalServices),
    WEB_URL("Image URL", Icons.Default.Language)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InsertPhotoDialog(
    viewModel: DocuMedViewModel,
    onDismiss: () -> Unit,
    onInsertPhoto: (photoUriOrUrl: String, caption: String, clinicalNotes: String) -> Unit
) {
    val context = LocalContext.current
    val allSources: List<SourceMaterial> by viewModel.sourceMaterials.collectAsState()
    val imageSources: List<SourceMaterial> = remember(allSources) {
        allSources.filter {
            it.fileType.equals("IMAGE", ignoreCase = true) ||
            it.fileType.equals("XRAY", ignoreCase = true) ||
            it.fileType.equals("CT_SCAN", ignoreCase = true) ||
            it.fileType.equals("PHOTO", ignoreCase = true) ||
            it.rawText.contains("![") || it.rawText.contains("http") || it.rawText.contains("content://")
        }
    }

    var selectedTab by remember { mutableStateOf(PhotoInsertTab.UPLOAD) }

    // Photo details state
    var photoUriOrUrl by remember { mutableStateOf("") }
    var figureCaption by remember { mutableStateOf("Figure 1: Clinical Diagnostic Finding") }
    var clinicalNotes by remember { mutableStateOf("Visual findings demonstrate key anatomical and pathological hallmarks.") }
    var selectedModality by remember { mutableStateOf("Plain Radiography (X-Ray)") }
    var saveToKnowledgeBaseAlso by remember { mutableStateOf(true) }

    val modalities = listOf(
        "Plain Radiography (X-Ray)",
        "Computed Tomography (CT)",
        "Magnetic Resonance (MRI)",
        "12-Lead ECG",
        "Clinical Photography",
        "Histopathology",
        "Ultrasound",
        "Intraoperative Photo"
    )

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val (name, size) = com.example.data.parser.SourceParserHelper.getFileInfoFromUri(context, uri)
            photoUriOrUrl = uri.toString()
            val cleanName = name.substringBeforeLast(".")
            figureCaption = "Figure: $cleanName"
            clinicalNotes = "High-resolution clinical photograph / diagnostic scan uploaded from device ($size)."
            Toast.makeText(context, "Photo loaded from device!", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .height(720.dp)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp
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
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MedicalTealPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Add / Insert Photo to Editor",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Embed clinical photographs, radiographs, ECGs & scans into your notes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Tabs Navigation
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PhotoInsertTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MedicalTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = tab.label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MedicalTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        )
                    }
                }

                // Main Content Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedTab) {
                        PhotoInsertTab.UPLOAD -> {
                            // Device Gallery / Storage Picker
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, MedicalTealPrimary.copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(MedicalTealPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Collections,
                                            contentDescription = null,
                                            tint = MedicalTealPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Text(
                                        text = "Select Medical Photo or Scan from Device",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Supports JPEG, PNG, WEBP, BMP radiographs, ECG photos & clinical images",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Button(
                                        onClick = { imagePickerLauncher.launch("image/*") },
                                        colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Browse Photos / Gallery")
                                    }
                                }
                            }
                        }

                        PhotoInsertTab.KNOWLEDGE_BASE -> {
                            Text(
                                text = "Choose from Uploaded Knowledge Base Photos (${imageSources.size}):",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            if (imageSources.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("No photos found in Knowledge Base yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(onClick = { selectedTab = PhotoInsertTab.UPLOAD }) {
                                            Text("Upload from Device")
                                        }
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    imageSources.forEach { source ->
                                        val uriMatch = Regex("!\\[.*?\\]\\((.*?)\\)").find(source.rawText)?.groupValues?.get(1)
                                            ?: if (source.rawText.startsWith("http") || source.rawText.startsWith("content://")) source.rawText else ""

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    photoUriOrUrl = uriMatch.ifBlank { "https://images.unsplash.com/photo-1516549655169-df83a0774514?auto=format&fit=crop&w=800&q=80" }
                                                    figureCaption = "Figure: ${source.title}"
                                                    clinicalNotes = source.extractedSummary.ifBlank { "Clinical findings from ${source.title}." }
                                                    Toast.makeText(context, "Selected: ${source.title}", Toast.LENGTH_SHORT).show()
                                                },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(text = source.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                                        Text(text = "${source.fileType} · ${source.fileSize}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    }
                                                }
                                                FilledTonalButton(
                                                    onClick = {
                                                        photoUriOrUrl = uriMatch.ifBlank { "https://images.unsplash.com/photo-1516549655169-df83a0774514?auto=format&fit=crop&w=800&q=80" }
                                                        figureCaption = "Figure: ${source.title}"
                                                        clinicalNotes = source.extractedSummary.ifBlank { "Clinical findings from ${source.title}." }
                                                    }
                                                ) {
                                                    Text("Select", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        PhotoInsertTab.CLINICAL_PRESETS -> {
                            Text(
                                text = "Select High-Yield Medical Preset Image:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                CLINICAL_PHOTO_PRESETS.forEach { preset ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                photoUriOrUrl = preset.sampleUriOrUrl
                                                figureCaption = preset.caption
                                                clinicalNotes = preset.clinicalNotes
                                                selectedModality = preset.modality
                                                Toast.makeText(context, "Selected preset: ${preset.title}", Toast.LENGTH_SHORT).show()
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MedicalBluePrimary.copy(alpha = 0.12f),
                                                    modifier = Modifier.padding(end = 8.dp)
                                                ) {
                                                    Text(
                                                        text = preset.diagnosticBadge,
                                                        color = MedicalBluePrimary,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(text = preset.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                                    Text(text = preset.modality, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            FilledTonalButton(
                                                onClick = {
                                                    photoUriOrUrl = preset.sampleUriOrUrl
                                                    figureCaption = preset.caption
                                                    clinicalNotes = preset.clinicalNotes
                                                    selectedModality = preset.modality
                                                }
                                            ) {
                                                Text("Use", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        PhotoInsertTab.WEB_URL -> {
                            OutlinedTextField(
                                value = photoUriOrUrl,
                                onValueChange = { photoUriOrUrl = it },
                                label = { Text("Image Web URL") },
                                placeholder = { Text("https://example.org/medical_xray_figure1.jpg") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // --- Live Image Preview & Configuration Box ---
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "PHOTO PREVIEW & FIGURE CONFIGURATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalTealPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            )

                            // Image Thumbnail Preview
                            if (photoUriOrUrl.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0F172A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = photoUriOrUrl,
                                        contentDescription = figureCaption,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color.Black.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = selectedModality,
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Select or pick a photo above to preview", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            // Modality Chips
                            Column {
                                Text("Imaging Modality:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    modalities.forEach { modality ->
                                        FilterChip(
                                            selected = selectedModality == modality,
                                            onClick = { selectedModality = modality },
                                            label = { Text(modality, fontSize = 11.sp) },
                                            leadingIcon = if (selectedModality == modality) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                            } else null
                                        )
                                    }
                                }
                            }

                            // Figure Caption Input
                            OutlinedTextField(
                                value = figureCaption,
                                onValueChange = { figureCaption = it },
                                label = { Text("Figure Caption / Title") },
                                placeholder = { Text("e.g. Figure 1: AP Pelvis Radiograph of femoral neck fracture") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Clinical Notes / Annotation Input
                            OutlinedTextField(
                                value = clinicalNotes,
                                onValueChange = { clinicalNotes = it },
                                label = { Text("Clinical Findings & Diagnostic Annotations") },
                                placeholder = { Text("Describe anatomical landmarks, pathological displacement, contraindications...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (photoUriOrUrl.isNotBlank()) {
                                    if (saveToKnowledgeBaseAlso) {
                                        viewModel.uploadSourceFile(
                                            title = figureCaption.take(50),
                                            fileType = "IMAGE",
                                            rawText = "![$figureCaption]($photoUriOrUrl)\n\n$clinicalNotes\n\nModality: $selectedModality",
                                            summary = clinicalNotes,
                                            keyPoints = "• Modality: $selectedModality\n• Caption: $figureCaption",
                                            fileSize = "2.4 MB"
                                        )
                                    }
                                    onInsertPhoto(photoUriOrUrl, figureCaption, clinicalNotes)
                                    Toast.makeText(context, "Photo inserted into editor!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Please select or pick a photo first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = photoUriOrUrl.isNotBlank(),
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Insert Photo into Note", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
