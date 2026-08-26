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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.parser.SourceParserHelper
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.viewmodel.DocuMedViewModel
import kotlinx.coroutines.launch

enum class SourceUploadType(val label: String, val icon: ImageVector, val color: Color, val badge: String) {
    PDF("PDF Document", Icons.Default.PictureAsPdf, Color(0xFFDC2626), ".pdf"),
    PHOTO("Photo / Image", Icons.Default.Image, Color(0xFF0284C7), ".jpg/.png"),
    WEB("Web Link / URL", Icons.Default.Language, Color(0xFF2563EB), "URL"),
    WORD("Word Document", Icons.Default.Description, Color(0xFF1D4ED8), ".docx"),
    EXCEL("Excel / Sheet", Icons.Default.TableChart, Color(0xFF059669), ".xlsx/.csv"),
    TEXT("Direct Text", Icons.Default.EditNote, Color(0xFFD97706), "Notes")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddSourceDialog(
    viewModel: DocuMedViewModel,
    initialTab: SourceUploadType = SourceUploadType.PDF,
    onDismiss: () -> Unit,
    onSourceAdded: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isProcessing by viewModel.isSourceProcessing.collectAsState()
    val processingStatus by viewModel.sourceProcessingStatus.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) }

    // State for Raw Text
    var textTitle by remember { mutableStateOf("") }
    var textContent by remember { mutableStateOf("") }
    var textCategory by remember { mutableStateOf("Clinical Note") }

    // State for Web Link
    var webUrl by remember { mutableStateOf("") }
    var webTitleCustom by remember { mutableStateOf("") }

    // State for Picked / Extracted Result
    var parsedResult by remember { mutableStateOf<SourceParserHelper.ParsedDocumentResult?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    // File pickers
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            localError = null
            viewModel.processAndUploadUri(
                context = context,
                uri = uri,
                onDone = {
                    Toast.makeText(context, "PDF Source added successfully!", Toast.LENGTH_SHORT).show()
                    onSourceAdded()
                    onDismiss()
                },
                onError = { localError = it }
            )
        }
    }

    val wordPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            localError = null
            viewModel.processAndUploadUri(
                context = context,
                uri = uri,
                onDone = {
                    Toast.makeText(context, "Word Document added to Knowledge Base!", Toast.LENGTH_SHORT).show()
                    onSourceAdded()
                    onDismiss()
                },
                onError = { localError = it }
            )
        }
    }

    val excelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            localError = null
            viewModel.processAndUploadUri(
                context = context,
                uri = uri,
                onDone = {
                    Toast.makeText(context, "Excel Spreadsheet indexed into Knowledge Base!", Toast.LENGTH_SHORT).show()
                    onSourceAdded()
                    onDismiss()
                },
                onError = { localError = it }
            )
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            localError = null
            viewModel.processAndUploadUri(
                context = context,
                uri = uri,
                onDone = {
                    Toast.makeText(context, "Clinical Photo added to Knowledge Base!", Toast.LENGTH_SHORT).show()
                    onSourceAdded()
                    onDismiss()
                },
                onError = { localError = it }
            )
        }
    }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
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
                // Modal Header
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MedicalBluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Add Clinical Source",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Upload PDF, Word, Excel, Web Links, or Direct Clinical Text",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            enabled = !isProcessing
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Type Navigation Bar (5 Modalities)
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SourceUploadType.values().forEach { tab ->
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
                                        tint = if (isSelected) tab.color else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = tab.label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) tab.color else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        )
                    }
                }

                // Status Banner if processing
                if (isProcessing) {
                    Surface(
                        color = MedicalTealPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MedicalTealPrimary
                            )
                            Text(
                                text = processingStatus.ifBlank { "Processing and extracting source material..." },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Error Banner
                if (localError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = localError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            IconButton(onClick = { localError = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Content Panel based on Selected Mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        SourceUploadType.PDF -> {
                            PdfUploadPanel(
                                onLaunchPicker = { pdfPickerLauncher.launch("application/pdf") },
                                onPickPreset = { presetTitle, presetText, summary ->
                                    viewModel.uploadSourceFile(
                                        title = presetTitle,
                                        fileType = "PDF",
                                        rawText = presetText,
                                        summary = summary,
                                        fileSize = "2.8 MB"
                                    )
                                    Toast.makeText(context, "Clinical PDF guideline loaded!", Toast.LENGTH_SHORT).show()
                                    onSourceAdded()
                                    onDismiss()
                                }
                            )
                        }
                        SourceUploadType.PHOTO -> {
                            PhotoUploadPanel(
                                onLaunchPicker = { photoPickerLauncher.launch("image/*") },
                                onSavePhotoSource = { title, uriOrUrl, modality, summary, keyPoints ->
                                    viewModel.uploadSourceFile(
                                        title = title,
                                        fileType = "IMAGE",
                                        rawText = "![$title]($uriOrUrl)\n\n$summary\n\nModality: $modality",
                                        summary = summary,
                                        keyPoints = keyPoints,
                                        fileSize = "2.4 MB"
                                    )
                                    Toast.makeText(context, "Photo indexed into Knowledge Base!", Toast.LENGTH_SHORT).show()
                                    onSourceAdded()
                                    onDismiss()
                                }
                            )
                        }
                        SourceUploadType.WEB -> {
                            WebLinkUploadPanel(
                                url = webUrl,
                                onUrlChange = { webUrl = it },
                                onFetchAndUpload = { urlToFetch ->
                                    viewModel.fetchAndUploadWebLink(
                                        url = urlToFetch,
                                        onDone = {
                                            Toast.makeText(context, "Web reference imported successfully!", Toast.LENGTH_SHORT).show()
                                            onSourceAdded()
                                            onDismiss()
                                        },
                                        onError = { localError = it }
                                    )
                                }
                            )
                        }
                        SourceUploadType.WORD -> {
                            WordUploadPanel(
                                onLaunchPicker = { wordPickerLauncher.launch("*/*") },
                                onPickPreset = { presetTitle, presetText, summary ->
                                    viewModel.uploadSourceFile(
                                        title = presetTitle,
                                        fileType = "DOCX",
                                        rawText = presetText,
                                        summary = summary,
                                        fileSize = "1.5 MB"
                                    )
                                    Toast.makeText(context, "Clinical Word document imported!", Toast.LENGTH_SHORT).show()
                                    onSourceAdded()
                                    onDismiss()
                                }
                            )
                        }
                        SourceUploadType.EXCEL -> {
                            ExcelUploadPanel(
                                onLaunchPicker = { excelPickerLauncher.launch("*/*") },
                                onPickPreset = { presetTitle, presetTable, summary ->
                                    viewModel.uploadSourceFile(
                                        title = presetTitle,
                                        fileType = "EXCEL",
                                        rawText = presetTable,
                                        summary = summary,
                                        tables = presetTable,
                                        fileSize = "780 KB"
                                    )
                                    Toast.makeText(context, "Excel spreadsheet table imported!", Toast.LENGTH_SHORT).show()
                                    onSourceAdded()
                                    onDismiss()
                                }
                            )
                        }
                        SourceUploadType.TEXT -> {
                            DirectTextUploadPanel(
                                title = textTitle,
                                onTitleChange = { textTitle = it },
                                content = textContent,
                                onContentChange = { textContent = it },
                                category = textCategory,
                                onCategoryChange = { textCategory = it },
                                onSave = {
                                    if (textTitle.isNotBlank() && textContent.isNotBlank()) {
                                        viewModel.uploadSourceFile(
                                            title = textTitle,
                                            fileType = "TXT",
                                            rawText = textContent,
                                            summary = "Direct clinical notes ($textCategory) containing ${textContent.split(Regex("\\s+")).size} words.",
                                            fileSize = "${(textContent.length * 2 / 1024).coerceAtLeast(1)} KB"
                                        )
                                        Toast.makeText(context, "Direct text source saved!", Toast.LENGTH_SHORT).show()
                                        onSourceAdded()
                                        onDismiss()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 1: PDF Upload Panel
// ---------------------------------------------------------------------------
@Composable
fun PdfUploadPanel(
    onLaunchPicker: () -> Unit,
    onPickPreset: (title: String, text: String, summary: String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upload Action Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLaunchPicker() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFDC2626).copy(alpha = 0.06f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                Color(0xFFDC2626).copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Upload PDF",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Select PDF Document from Device",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Extracts clinical text, diagnostic criteria, and guidelines automatically",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onLaunchPicker,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse PDF Files")
                }
            }
        }

        // Quick Clinical PDF Presets
        Text(
            text = "Or Load Clinical Practice Guideline Presets (One-Tap)",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PdfPresetCard(
            title = "WHO_Sepsis_3_Consensus_Guidelines_2025.pdf",
            subtitle = "World Health Organization · Sepsis & Septic Shock Diagnostic Definitions",
            keyHighlights = "SOFA score thresholds, qSOFA emergency triage, early fluid resuscitation (30 mL/kg), target MAP >= 65 mmHg, and Norepinephrine first-line vasopressor protocol.",
            onClick = {
                onPickPreset(
                    "WHO_Sepsis_3_Consensus_Guidelines_2025.pdf",
                    """
# WHO Sepsis-3 International Consensus Management Guidelines
Source Document: WHO Technical Report Series 2025.

## 1.0 Operational Clinical Definitions
* **Sepsis**: Life-threatening organ dysfunction caused by a dysregulated host response to infection.
* **Organ Dysfunction Metric**: An acute change in total Sequential Organ Failure Assessment (SOFA) score >= 2 points consequent to the infection.
* **Septic Shock**: Subset of sepsis with profound circulatory, cellular, and metabolic abnormalities. Clinically identified by:
  1. Persistent hypotension requiring vasopressors to maintain MAP >= 65 mmHg.
  2. Serum lactate level > 2 mmol/L (18 mg/dL) despite adequate volume resuscitation.

## 2.0 Emergency Resuscitation Pathway (Hour-1 Bundle)
1. Measure serum blood lactate level immediately.
2. Obtain blood cultures prior to administration of antimicrobial therapy.
3. Administer broad-spectrum intravenous antimicrobials within 1 hour of recognition.
4. Rapidly initiate 30 mL/kg crystalloid fluid for hypotension or lactate >= 4 mmol/L.
5. Apply vasopressors (Norepinephrine first-line; add Vasopressin up to 0.03 U/min if refractory) to maintain MAP >= 65 mmHg.

[WARNING: Every hour delay in targeted antimicrobial administration in septic shock increases in-hospital mortality by 7.6%.]
                    """.trimIndent(),
                    "WHO international consensus definitions for Sepsis-3 and Hour-1 bundle resuscitation targets."
                )
            }
        )

        PdfPresetCard(
            title = "KDIGO_Acute_Kidney_Injury_Clinical_Practice_Guideline.pdf",
            subtitle = "Kidney Disease: Improving Global Outcomes · AKI Staging & Nephroprotection",
            keyHighlights = "Serum creatinine doubling criteria, urine output staging (<0.5 mL/kg/h for >6h), renal replacement therapy indications, and nephrotoxic drug avoidance.",
            onClick = {
                onPickPreset(
                    "KDIGO_Acute_Kidney_Injury_Clinical_Practice_Guideline.pdf",
                    """
# KDIGO Clinical Practice Guideline for Acute Kidney Injury (AKI)
Source: Kidney International Supplements.

## 1.0 KDIGO Staging Criteria
* **Stage 1**: Serum Creatinine (SCr) 1.5–1.9 times baseline OR >= 0.3 mg/dL increase; Urine Output (UO) < 0.5 mL/kg/h for 6–12 hours.
* **Stage 2**: SCr 2.0–2.9 times baseline; UO < 0.5 mL/kg/h for >= 12 hours.
* **Stage 3**: SCr 3.0 times baseline OR increase to >= 4.0 mg/dL OR initiation of Renal Replacement Therapy (RRT); UO < 0.3 mL/kg/h for >= 24 hours OR Anuria for >= 12 hours.

## 2.0 Management Principles
1. Discontinue all potential nephrotoxic agents (NSAIDs, aminoglycosides, ACEi/ARBs, radiocontrast).
2. Ensure volume status and perfusion pressure optimization.
3. Consider functional hemodynamic monitoring in Stage 2/3.
4. Monitor serum creatinine, electrolytes, and acid-base status daily.
5. Absolute indications for emergent RRT (AEIOU): Acidosis (pH < 7.1), Electrolytes (K > 6.5 mEq/L refractory), Ingestions, Overload (pulmonary edema refractory to diuretics), Uremia (pericarditis, encephalopathy).
                    """.trimIndent(),
                    "KDIGO consensus criteria for AKI diagnosis, staging (1-3), fluid management, and emergency dialytic indications."
                )
            }
        )

        PdfPresetCard(
            title = "GOLD_2026_COPD_Global_Diagnostic_Strategy.pdf",
            subtitle = "Global Initiative for Chronic Obstructive Lung Disease · Exacerbation Protocols",
            keyHighlights = "Post-bronchodilator FEV1/FVC < 0.70 ratio, refined ABE assessment tool, LAMA/LABA dual therapy recommendations, and blood eosinophil-directed ICS use.",
            onClick = {
                onPickPreset(
                    "GOLD_2026_COPD_Global_Diagnostic_Strategy.pdf",
                    """
# Global Strategy for the Diagnosis, Management, and Prevention of COPD (GOLD 2026)
Source: Global Initiative for Chronic Obstructive Lung Disease.

## 1.0 Diagnostic Criteria & Spirometric Grading
* A post-bronchodilator **FEV1/FVC < 0.70** is the prerequisite threshold for confirming persistent airflow limitation.
* **GOLD 1 (Mild)**: FEV1 >= 80% predicted.
* **GOLD 2 (Moderate)**: 50% <= FEV1 < 80% predicted.
* **GOLD 3 (Severe)**: 30% <= FEV1 < 50% predicted.
* **GOLD 4 (Very Severe)**: FEV1 < 30% predicted.

## 2.0 The Refined 'ABE' Assessment Model
* **Group A**: 0 or 1 moderate exacerbation not leading to hospital admission; mMRC 0-1, CAT < 10. Treatment: Single Bronchodilator.
* **Group B**: 0 or 1 moderate exacerbation not leading to hospital admission; mMRC >= 2, CAT >= 10. Treatment: LABA + LAMA combination.
* **Group E**: >= 2 moderate exacerbations OR >= 1 exacerbation leading to hospital admission. Treatment: LABA + LAMA (Add Inhaled Corticosteroid if blood eosinophils >= 300 cells/uL).
                    """.trimIndent(),
                    "GOLD 2026 guidelines for COPD spirometric classification, ABE staging, and tailored pharmacological therapies."
                )
            }
        )
    }
}

@Composable
fun PdfPresetCard(
    title: String,
    subtitle: String,
    keyHighlights: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDC2626).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = keyHighlights, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), maxLines = 2)
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(onClick = onClick) {
                Text("Load", fontSize = 12.sp)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 2: Web Link / URL Ingestion Panel
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WebLinkUploadPanel(
    url: String,
    onUrlChange: (String) -> Unit,
    onFetchAndUpload: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB).copy(alpha = 0.05f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Import Clinical Guidelines from Web URL",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Fetches online journal articles, PubMed publications, or government health portals",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    label = { Text("Web Link (URL)") },
                    placeholder = { Text("https://www.who.int/... or https://pubmed.ncbi.nlm.nih.gov/...") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF2563EB)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = { if (url.isNotBlank()) onFetchAndUpload(url) },
                    enabled = url.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fetch & Extract Web Source")
                }
            }
        }

        Text(
            text = "Popular Medical Portals & Verified Guidelines Bookmarks",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        WebBookmarkItem(
            name = "WHO Antimicrobial Resistance Surveillance",
            domain = "who.int/news-room/fact-sheets/detail/antimicrobial-resistance",
            description = "Global AMR threat data, high-priority fungal and bacterial pathogen catalogue, antibiotic stewardship pillars.",
            onClick = { onFetchAndUpload("https://www.who.int/news-room/fact-sheets/detail/antimicrobial-resistance") }
        )

        WebBookmarkItem(
            name = "PubMed Central: SGLT2 Inhibitors in Cardiorenal Syndrome",
            domain = "pubmed.ncbi.nlm.nih.gov/38312345",
            description = "Mechanisms of renal hemodynamic protection, reduction in heart failure hospitalizations, and metabolic outcomes.",
            onClick = { onFetchAndUpload("https://pubmed.ncbi.nlm.nih.gov/38312345") }
        )

        WebBookmarkItem(
            name = "NICE Guideline: Hypertension Management in Adults (NG136)",
            domain = "nice.org.uk/guidance/ng136",
            description = "Automated blood pressure threshold staging (Stage 1-3), ambulatory BP monitoring, step-wise pharmacotherapy ABCD protocol.",
            onClick = { onFetchAndUpload("https://www.nice.org.uk/guidance/ng136") }
        )

        WebBookmarkItem(
            name = "CDC Guidelines: Hospital Infection Prevention & Control",
            domain = "cdc.gov/infection-control/guidelines",
            description = "Standard, contact, droplet, and airborne precautions; catheter-associated UTI and central line infection prevention.",
            onClick = { onFetchAndUpload("https://www.cdc.gov/infection-control/guidelines") }
        )
    }
}

@Composable
fun WebBookmarkItem(
    name: String,
    domain: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(text = domain, style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = Color(0xFF2563EB)))
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), maxLines = 2)
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(onClick = onClick) {
                Text("Import", fontSize = 12.sp)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 3: Word Document (.docx / .doc) Panel
// ---------------------------------------------------------------------------
@Composable
fun WordUploadPanel(
    onLaunchPicker: () -> Unit,
    onPickPreset: (title: String, text: String, summary: String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLaunchPicker() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1D4ED8).copy(alpha = 0.06f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1D4ED8).copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1D4ED8).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Upload Word",
                        tint = Color(0xFF1D4ED8),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Upload Word Document (.docx / .doc)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Parses case conferences, research manuscripts, and department guidelines directly",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onLaunchPicker,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Word File")
                }
            }
        }

        Text(
            text = "Or Load Clinical Word Document Templates",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        WordPresetCard(
            title = "Internal_Medicine_Grand_Rounds_Case_Report.docx",
            subtitle = "Department of Medicine · Complex Diagnostic Case",
            summary = "A 64-year-old male with multisystem inflammatory features, bicytopenia, fever of unknown origin, and systemic necrotizing vasculitis differential.",
            onClick = {
                onPickPreset(
                    "Internal_Medicine_Grand_Rounds_Case_Report.docx",
                    """
# Clinical Case Report: Grand Rounds Presentation
**Author**: Dr. Sarah Jenkins, MD, Senior Internal Medicine Resident
**Date**: February 2026

## 1.0 Patient Demographics & Chief Complaint
* **Patient**: 64-year-old male retired carpenter.
* **Chief Complaint**: Progressive fatigue, involuntary 8 kg weight loss over 3 months, low-grade fevers, and tender purpuric lesions over lower extremities.

## 2.0 Past Medical & Social History
* Hypertension (on Amlodipine 5 mg daily).
* No prior tobacco, alcohol, or illicit substance use.
* No recent foreign travel or tick bites.

## 3.0 Physical Examination Findings
* Temp: 38.1°C, BP: 146/88 mmHg, HR: 88 bpm regular, SpO2: 98% on room air.
* HEENT: Scleral icterus absent; no oral aphthous ulcers.
* Cardiovascular: Dual heart sounds; grade II/VI early systolic murmur at apex.
* Abdomen: Soft, non-tender; mild palpable splenomegaly (2 cm below costal margin).
* Extremities: Palpable non-blanching purpura on bilateral shins and ankles; splinter hemorrhages on 2nd and 4th digits of left hand.

## 4.0 Laboratory & Diagnostic Workup
* Hemoglobin: 9.4 g/dL (Normocytic normochromic anemia).
* WBC: 12,800/uL with neutrophilia (82%).
* Platelets: 98,000/uL (Thrombocytopenia).
* ESR: 92 mm/hr; CRP: 114 mg/L.
* Serum Creatinine: 1.8 mg/dL (baseline 1.0 mg/dL).
* Urinalysis: 3+ Proteinuria, 20-30 RBCs/HPF with red blood cell casts.
* Serologies: ANA Negative; c-ANCA/PR3 Positive (1:160); Blood cultures x3: No growth.

## 5.0 Clinical Assessment & Differential Diagnosis
* **Primary Diagnosis**: Granulomatosis with Polyangiitis (GPA) with pulmonary-renal involvement.
* **Differentials**: Microscopic Polyangiitis (MPA), Subacute Bacterial Endocarditis (SBE), Cryoglobulinemic Vasculitis.

## 6.0 Management & Outcome
* High-dose pulse IV Methylprednisolone (1 g daily x 3 days) followed by oral Prednisone.
* Induction therapy with Rituximab (375 mg/m2 weekly x 4 doses).
* Prophylaxis with Trimethoprim-Sulfamethoxazole for Pneumocystis jirovecii.
                    """.trimIndent(),
                    "Comprehensive internal medicine grand rounds case report on systemic necrotizing vasculitis (GPA)."
                )
            }
        )

        WordPresetCard(
            title = "OSCE_Station_Cardiology_Auscultation_Rubric.docx",
            subtitle = "Postgraduate Medical Board · Objective Structured Exam Rubric",
            summary = "8-minute standardized candidate checklist covering precordial inspection, palpation of heaves/thrills, and dynamic murmur maneuvers.",
            onClick = {
                onPickPreset(
                    "OSCE_Station_Cardiology_Auscultation_Rubric.docx",
                    """
# Postgraduate OSCE Station: Cardiovascular Examination & Murmur Localization
**Station Duration**: 8 Minutes (6 min candidate interaction + 2 min examiner viva)

## 1.0 Candidate Instructions
You are the medical registrar in the acute assessment unit. A 55-year-old patient presents with exertional dyspnea and lightheadedness. Perform a focused cardiovascular examination, localize any heart murmurs, and deliver your synthesized differential diagnosis to the examiner.

## 2.0 Examiner Marking Checklist (Total: 20 Marks)
1. **Introduction & Infection Control**: Introduces self, confirms patient identity, washes hands, positions patient at 45° with adequate chest exposure. [2 Marks]
2. **General Inspection**: Checks for peripheral stigmata (splinter hemorrhages, Osler nodes, Janeway lesions, xanthomata, marfanoid habitus). [2 Marks]
3. **Pulse & Blood Pressure**: Palpates radial pulse (rate, rhythm, collapsing quality/water-hammer pulse); checks radio-radial delay and blood pressure. [2 Marks]
4. **Jugular Venous Pulse (JVP)**: Evaluates JVP waveform (a wave, v wave, y descent) with hepatojugular reflux. [2 Marks]
5. **Precordial Palpation**: Locates apex beat (displaced/heaving/tapping); palpates for parasternal heave and thrill over aortic/mitral areas. [3 Marks]
6. **Auscultation of Four Valve Areas**: Bell & diaphragm over Mitral, Tricuspid, Pulmonary, Aortic areas with carotid pulse correlation. [4 Marks]
7. **Dynamic Maneuvers**: Left lateral position with bell in expiration (mitral stenosis); sitting forward in end-expiration (aortic regurgitation); Valsalva / handgrip response. [3 Marks]
8. **Synthesis & Clear Presentation**: Succinctly summarizes findings and proposes top differential (e.g. Severe Aortic Stenosis vs HOCM) and next investigations (Echocardiogram). [2 Marks]
                    """.trimIndent(),
                    "Postgraduate cardiology OSCE marking station rubric with dynamic maneuvers and scoring points."
                )
            }
        )
    }
}

@Composable
fun WordPresetCard(
    title: String,
    subtitle: String,
    summary: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1D4ED8).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Article, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), maxLines = 2)
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(onClick = onClick) {
                Text("Load", fontSize = 12.sp)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 4: Excel / Spreadsheet (.xlsx / .csv) Panel
// ---------------------------------------------------------------------------
@Composable
fun ExcelUploadPanel(
    onLaunchPicker: () -> Unit,
    onPickPreset: (title: String, tableData: String, summary: String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLaunchPicker() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF059669).copy(alpha = 0.06f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF059669).copy(alpha = 0.35f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF059669).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = "Upload Excel",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Upload Spreadsheet (.xlsx / .xls / .csv)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Converts drug dosing matrices, lab reference ranges, and research cohort tables into structured formats",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onLaunchPicker,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Excel / CSV File")
                }
            }
        }

        Text(
            text = "Or Load Clinical Reference Table Datasets",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ExcelPresetCard(
            title = "ICU_Emergency_Vasopressor_Inotrope_Matrix.xlsx",
            subtitle = "Critical Care Pharmacotherapy · Dosing, Receptors & Titrations",
            tablePreview = "| Drug | Receptor Activity | Standard Dosing Range | Primary Indications |\n| Norepinephrine | Alpha-1 >> Beta-1 | 0.02 - 1.0 mcg/kg/min | Septic Shock, Cardiogenic Shock |\n| Epinephrine | Alpha-1, Beta-1, Beta-2 | 0.01 - 0.5 mcg/kg/min | Anaphylaxis, Refractory Shock |\n| Vasopressin | V1, V2 | 0.03 - 0.04 units/min | Second-line in Septic Shock |\n| Dobutamine | Beta-1 > Beta-2 | 2.5 - 20.0 mcg/kg/min | Inotropic Support, Acute Heart Failure |",
            onClick = {
                onPickPreset(
                    "ICU_Emergency_Vasopressor_Inotrope_Matrix.xlsx",
                    """
# ICU Emergency Vasopressor & Inotropic Infusion Matrix

| Medication | Receptor Profile | Starting Dose | Max Titration Dose | Hemodynamic Effects | Key Adverse Effects |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Norepinephrine** | Alpha-1 (strong), Beta-1 (mod) | 0.05 mcg/kg/min | 1.0 - 2.0 mcg/kg/min | Increases SVR, MAP, Cardiac Output | Peripheral ischemia, tachyarrhythmias |
| **Epinephrine** | Alpha-1, Beta-1, Beta-2 | 0.02 mcg/kg/min | 0.5 - 1.0 mcg/kg/min | Potent inotrope, chronotrope, vasoconstrictor | Hyperglycemia, lactic acidosis, myocardial ischemia |
| **Vasopressin** | V1a (vascular), V2 (renal) | 0.03 units/min (fixed) | 0.04 units/min | SVR elevation without Beta stimulation | Coronary and mesenteric vasoconstriction |
| **Dobutamine** | Beta-1 >> Beta-2 | 2.5 mcg/kg/min | 20 mcg/kg/min | Enhances contractility, decreases SVR/PVR | Hypotension (Beta-2 vasodilation), ventricular ectopy |
| **Milrinone** | PDE-3 Inhibitor | 0.25 mcg/kg/min | 0.75 mcg/kg/min | Inodilator: Increases CO, reduces afterload | Renal clearance dependent, profound hypotension |
                    """.trimIndent(),
                    "Complete ICU pharmacological matrix for vasopressors, inotropes, starting doses, and hemodynamic monitoring."
                )
            }
        )

        ExcelPresetCard(
            title = "Biochemistry_Hematology_Reference_Ranges_2026.csv",
            subtitle = "Laboratory Medicine Reference Database · Adult Standard Ranges",
            tablePreview = "| Parameter | Conventional Units | SI Units | Critical/Panic Low | Critical/Panic High |\n| Sodium (Na+) | 135 - 145 mEq/L | 135 - 145 mmol/L | < 120 mEq/L | > 160 mEq/L |\n| Potassium (K+) | 3.5 - 5.0 mEq/L | 3.5 - 5.0 mmol/L | < 2.8 mEq/L | > 6.2 mEq/L |\n| Serum Creatinine | 0.7 - 1.3 mg/dL | 62 - 115 umol/L | - | > 4.0 mg/dL |\n| High-Sensitivity Troponin I | < 14 ng/L | < 14 ng/L | - | > 52 ng/L (ACS) |",
            onClick = {
                onPickPreset(
                    "Biochemistry_Hematology_Reference_Ranges_2026.csv",
                    """
# Clinical Biochemistry & Hematology Reference Ranges (Adult)

| Test Analyte | Reference Range (US) | SI Reference Range | Panic Low Threshold | Panic High Threshold | Clinical Pearl |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Serum Sodium (Na)** | 135 - 145 mEq/L | 135 - 145 mmol/L | < 120 mEq/L | > 160 mEq/L | Correct <8-10 mEq/24h to avoid ODS |
| **Serum Potassium (K)** | 3.5 - 5.0 mEq/L | 3.5 - 5.0 mmol/L | < 2.8 mEq/L | > 6.2 mEq/L | Urgent Calcium gluconate if peaked T waves |
| **Serum Bicarbonate (HCO3)** | 22 - 29 mEq/L | 22 - 29 mmol/L | < 10 mEq/L | > 40 mEq/L | Calculate anion gap: Na - (Cl + HCO3) |
| **Serum Creatinine** | 0.7 - 1.3 mg/dL | 62 - 115 umol/L | - | > 4.0 mg/dL | Muscle mass affects baseline |
| **Blood Urea Nitrogen (BUN)** | 7 - 20 mg/dL | 2.5 - 7.1 mmol/L | - | > 80 mg/dL | BUN:Cr ratio > 20 indicates prerenal etiology |
| **High Sensitivity Troponin I** | < 14 ng/L | < 14 ng/L | - | > 52 ng/L | Rule-in delta > 5 ng/L at 1-2 hours |
| **Arterial Blood pH** | 7.35 - 7.45 | 7.35 - 7.45 | < 7.20 | > 7.60 | Evaluate PaCO2 vs HCO3 compensation |
| **Hemoglobin (Male/Female)** | 13.5-17.5 / 12.0-15.5 g/dL | 135-175 / 120-155 g/L | < 7.0 g/dL | > 20.0 g/dL | Transfuse threshold: 7.0 g/dL (8.0 in CAD) |
                    """.trimIndent(),
                    "Complete reference dataset for emergency laboratory critical values, units, and panic thresholds."
                )
            }
        )
    }
}

@Composable
fun ExcelPresetCard(
    title: String,
    subtitle: String,
    tablePreview: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF059669).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                FilledTonalButton(onClick = onClick) {
                    Text("Load", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = tablePreview,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// TAB 5: Direct Clinical Text / Notes Panel
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DirectTextUploadPanel(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()
    val categories = listOf("Clinical Note", "Lecture Excerpt", "Guideline Summary", "Radiology Report", "Surgical Protocol")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Document / Note Title") },
            placeholder = { Text("e.g. Harrison_Cardiology_Heart_Failure_Pearls.txt") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Column {
            Text("Select Clinical Category:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { onCategoryChange(cat) },
                        label = { Text(cat, fontSize = 12.sp) },
                        leadingIcon = if (category == cat) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }
        }

        // Quick Clinical Snippet Inserts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Insert Clinical Template:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        onTitleChange("Cardiology_Heart_Failure_HFrEF_Vignette.txt")
                        onContentChange("""
# Clinical Vignette: Acute Decompensated Heart Failure (HFrEF)
* **Patient**: 68-year-old male with ischemic cardiomyopathy (LVEF 25%).
* **Presentation**: Orthopnea, PND, elevated JVP to angle of jaw, bilateral basilar crackles, 3+ pitting pedal edema.
* **Biomarkers**: NT-proBNP 4,800 pg/mL; Serum Troponin I 0.04 ng/L.
* **Management**: IV Furosemide bolus (2.5x oral dose), continuation of GDMT (ARNI Sacubitril/Valsartan, SGLT2i Empagliflozin, Beta-blocker Metoprolol Succinate, MRA Spironolactone).
                        """.trimIndent())
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("HFrEF Note", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = {
                        onTitleChange("Emergency_Stroke_tPA_Checklist.txt")
                        onContentChange("""
# Emergency Ischemic Stroke Thrombolysis Protocol
* **Time Window**: Within 4.5 hours of last known normal.
* **Inclusion Criteria**: Measurable neurological deficit (NIHSS >= 4), CT head excludes intracranial hemorrhage.
* **Absolute Contraindications**: Active internal bleeding, previous ICH anytime, head trauma or stroke in past 3 months, platelet count < 100,000/uL, INR > 1.7, Blood pressure > 185/110 mmHg refractory to antihypertensives.
* **Dosage**: IV Alteplase 0.9 mg/kg (max 90 mg); 10% bolus over 1 min, remainder infused over 60 mins.
                        """.trimIndent())
                    },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Stroke Checklist", fontSize = 11.sp)
                }
            }
        }

        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            label = { Text("Clinical Text / Notes Content") },
            placeholder = { Text("Paste extracted text, textbook excerpt, clinical case notes, or laboratory findings...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 8,
            maxLines = 14,
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = onSave,
            enabled = title.isNotBlank() && content.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save to Knowledge Base")
        }
    }
}

// ---------------------------------------------------------------------------
// TAB: Medical Photo & Imaging Upload Panel
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhotoUploadPanel(
    onLaunchPicker: () -> Unit,
    onSavePhotoSource: (title: String, uriOrUrl: String, modality: String, summary: String, keyPoints: String) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var photoTitle by remember { mutableStateOf("") }
    var photoUriOrUrl by remember { mutableStateOf("") }
    var selectedModality by remember { mutableStateOf("Plain Radiography (X-Ray)") }
    var photoSummary by remember { mutableStateOf("") }
    var keyPoints by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Upload & Browse Action Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0284C7).copy(alpha = 0.3f))
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
                        .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Upload Medical Photo / Imaging from Device",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "Supports JPEG, PNG, WEBP radiographs, clinical photos, microscopy, ECG tracings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onLaunchPicker,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Photo from Gallery", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Image Preview (if present)
        if (photoUriOrUrl.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = photoUriOrUrl,
                        contentDescription = photoTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color.Black.copy(alpha = 0.65f),
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
            }
        }

        // Modality Chooser
        Column {
            Text(
                text = "Clinical Modality:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                modalities.forEach { mod ->
                    FilterChip(
                        selected = selectedModality == mod,
                        onClick = { selectedModality = mod },
                        label = { Text(mod, fontSize = 11.sp) },
                        leadingIcon = if (selectedModality == mod) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        } else null
                    )
                }
            }
        }

        // Diagnostic Photo Presets
        Text(
            text = "Or Load Clinical Preset Imaging:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CLINICAL_PHOTO_PRESETS.take(3).forEach { preset ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        photoTitle = preset.title
                        photoUriOrUrl = preset.sampleUriOrUrl
                        selectedModality = preset.modality
                        photoSummary = preset.clinicalNotes
                        keyPoints = "• Modality: ${preset.modality}\n• Finding: ${preset.caption}"
                        Toast.makeText(context, "Loaded preset: ${preset.title}", Toast.LENGTH_SHORT).show()
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(preset.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(preset.modality, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    FilledTonalButton(
                        onClick = {
                            photoTitle = preset.title
                            photoUriOrUrl = preset.sampleUriOrUrl
                            selectedModality = preset.modality
                            photoSummary = preset.clinicalNotes
                            keyPoints = "• Modality: ${preset.modality}\n• Finding: ${preset.caption}"
                        }
                    ) {
                        Text("Use", fontSize = 11.sp)
                    }
                }
            }
        }

        // Fields for custom entries
        OutlinedTextField(
            value = photoTitle,
            onValueChange = { photoTitle = it },
            label = { Text("Photo / Scan Title") },
            placeholder = { Text("e.g. Left_Hip_Pelvis_Radiograph_AP.jpg") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = photoUriOrUrl,
            onValueChange = { photoUriOrUrl = it },
            label = { Text("Image File URI or Web URL") },
            placeholder = { Text("content://... or https://...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = photoSummary,
            onValueChange = { photoSummary = it },
            label = { Text("Radiological / Clinical Findings & Diagnostic Pearls") },
            placeholder = { Text("Describe anatomical disruption, opacity, borders, clinical correlation...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(10.dp)
        )

        Button(
            onClick = {
                if (photoTitle.isNotBlank() && photoUriOrUrl.isNotBlank()) {
                    onSavePhotoSource(
                        photoTitle,
                        photoUriOrUrl,
                        selectedModality,
                        photoSummary.ifBlank { "Clinical diagnostic photo ($selectedModality)." },
                        keyPoints.ifBlank { "• Modality: $selectedModality\n• Source: $photoTitle" }
                    )
                }
            },
            enabled = photoTitle.isNotBlank() && photoUriOrUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Photo to Knowledge Base", fontWeight = FontWeight.Bold)
        }
    }
}
