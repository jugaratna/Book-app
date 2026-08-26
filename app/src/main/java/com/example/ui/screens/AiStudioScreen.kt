package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timeline
import com.example.ui.dialogs.ClinicalCalculatorsDialog
import com.example.ui.dialogs.DrugFormularyDialog
import com.example.ui.dialogs.MedicalImageAnnotationDialog
import com.example.ui.dialogs.PatientLeafletDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.DocumentExportHelper
import com.example.data.model.MedicalPresentation
import com.example.ui.components.FlashcardDeckView
import com.example.ui.components.FlowchartVisualizer
import com.example.ui.components.ImageDiagnosticDialog
import com.example.ui.components.McqPracticeView
import com.example.ui.components.MedicalTableView
import com.example.ui.components.OsceStationView
import com.example.ui.components.PresentationViewDialog
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.DocuMedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudioScreen(
    viewModel: DocuMedViewModel,
    modifier: Modifier = Modifier
) {
    var selectedStudioTab by remember { mutableIntStateOf(0) }
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val aiStatusMessage by viewModel.aiStatusMessage.collectAsState()
    val selectedDoc by viewModel.selectedDocument.collectAsState()

    // Results state
    val generatedSummary by viewModel.generatedSummary.collectAsState()
    val generatedMCQs by viewModel.generatedMCQs.collectAsState()
    val generatedViva by viewModel.generatedViva.collectAsState()
    val generatedOSCE by viewModel.generatedOSCE.collectAsState()
    val generatedFlashcards by viewModel.generatedFlashcards.collectAsState()
    val generatedTable by viewModel.generatedTable.collectAsState()
    val generatedFlowchart by viewModel.generatedFlowchart.collectAsState()
    val generatedImageAnalysis by viewModel.generatedImageAnalysis.collectAsState()
    val generatedPresentation by viewModel.generatedPresentation.collectAsState()
    val generatedPatientLeaflet by viewModel.generatedPatientLeaflet.collectAsState()

    var showPresentationDialog by remember { mutableStateOf(false) }
    var showCalculatorsDialog by remember { mutableStateOf(false) }
    var showDrugFormularyDialog by remember { mutableStateOf(false) }
    var showPatientLeafletDialog by remember { mutableStateOf(false) }
    var showImageAnnotationDialog by remember { mutableStateOf(false) }

    val selectedAiEngine by viewModel.selectedAiEngine.collectAsState()
    val context = LocalContext.current

    val studioTabs = listOf(
        "Chapter Builder",
        "PowerPoint Deck",
        "Calculators & Scores",
        "Drug Formulary",
        "Patient Leaflets",
        "Radiology Canvas",
        "Summaries",
        "MCQ Bank",
        "Viva & Oral",
        "OSCE Station",
        "Flashcards (Anki)",
        "Flowcharts",
        "Tables",
        "Image & Scan"
    )

    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize()) {
        // AI Studio Header Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MedicalTealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "DocuMed AI Clinical Studio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Publication-Grade Medical Content Generator",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    if (selectedDoc != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MedicalBluePrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Active: ${selectedDoc?.title?.take(18)}...",
                                color = MedicalBluePrimary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AI Engine Selector Banner
                com.example.ui.components.AiEngineSelector(
                    selectedEngine = selectedAiEngine,
                    onEngineSelected = { viewModel.setSelectedAiEngine(it) },
                    label = "Active AI Generation Engine"
                )

                // AI Progress Bar
                if (isAiGenerating) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MedicalBluePrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MedicalBluePrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = aiStatusMessage,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MedicalBluePrimary
                        )
                    }
                }
            }
        }

        // Subcategory Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedStudioTab,
            edgePadding = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            studioTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedStudioTab == index,
                    onClick = { selectedStudioTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedStudioTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        // Tab Contents
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedStudioTab) {
                0 -> ChapterBuilderTab(viewModel)
                1 -> PowerPointDeckTab(viewModel, generatedPresentation, isAiGenerating) {
                    showPresentationDialog = true
                }
                2 -> CalculatorsTab(viewModel) { showCalculatorsDialog = true }
                3 -> DrugFormularyTab(viewModel) { showDrugFormularyDialog = true }
                4 -> PatientLeafletsTab(viewModel, generatedPatientLeaflet, isAiGenerating) { showPatientLeafletDialog = true }
                5 -> RadiologyCanvasTab(viewModel) { showImageAnnotationDialog = true }
                6 -> SummariesTab(viewModel, generatedSummary)
                7 -> McqBankTab(viewModel, generatedMCQs)
                8 -> VivaTab(viewModel, generatedViva)
                9 -> OsceTab(viewModel, generatedOSCE)
                10 -> FlashcardsTab(viewModel, generatedFlashcards)
                11 -> FlowchartsTab(viewModel, generatedFlowchart)
                12 -> TablesTab(viewModel, generatedTable)
                13 -> ImageScanTab(viewModel, isAiGenerating, generatedImageAnalysis)
            }
        }
    }

    if (showPresentationDialog) {
        PresentationViewDialog(
            presentation = generatedPresentation,
            isGenerating = isAiGenerating,
            onDismiss = { showPresentationDialog = false },
            onRegenerate = { viewModel.generatePowerPointPresentation() }
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
        PatientLeafletDialog(
            initialContent = currentContent,
            generatedLeaflet = generatedPatientLeaflet,
            isGenerating = isAiGenerating,
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

// ----------------------------------------------------
// Clinical Calculators Tab
// ----------------------------------------------------
@Composable
fun CalculatorsTab(viewModel: DocuMedViewModel, onOpenFullCalculatorSuite: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MedicalBluePrimary.copy(alpha = 0.08f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MedicalBluePrimary.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Interactive Clinical Scoring & Decision Engine", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MedicalBluePrimary)
                    Text("Wells PE, CHA₂DS₂-VASc, CURB-65, GCS, MELD-Na, eGFR CKD-EPI with one-tap insertion into clinical notes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onOpenFullCalculatorSuite,
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Launch Suite", fontSize = 12.sp)
                }
            }
        }

        Text("Available Evidence-Based Calculators:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

        val calcSummaries = listOf(
            Triple("Wells Criteria for PE", "Stratify pulmonary embolism probability into Low, Moderate, or High risk with D-dimer / CTPA guidance.", "Pulmonology / EM"),
            Triple("CHA₂DS₂-VASc Score", "Assess annual ischemic stroke risk in Non-Valvular AF and determine DOAC anticoagulation indication.", "Cardiology"),
            Triple("CURB-65 Pneumonia Severity", "Triage community-acquired pneumonia for outpatient vs inpatient vs ICU admission.", "Infectious Disease"),
            Triple("Glasgow Coma Scale (GCS)", "Standardized 3-15 consciousness assessment for trauma, stroke, and ICU neuro checks.", "Neurology / Trauma"),
            Triple("MELD-Na Liver Mortality", "Predict 90-day end-stage liver disease mortality and liver transplant prioritization.", "Gastroenterology"),
            Triple("eGFR (CKD-EPI 2021)", "Race-free 2021 CKD-EPI equation for renal staging and medication dosage adjustment.", "Nephrology")
        )

        calcSummaries.forEach { (name, desc, tag) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenFullCalculatorSuite),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MedicalBluePrimary)
                        Surface(shape = RoundedCornerShape(4.dp), color = MedicalBluePrimary.copy(alpha = 0.12f)) {
                            Text(tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MedicalBluePrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------
// Bedside Drug Formulary Tab
// ----------------------------------------------------
@Composable
fun DrugFormularyTab(viewModel: DocuMedViewModel, onOpenFormularyDialog: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F766E).copy(alpha = 0.08f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0F766E).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bedside Drug Formulary & Interaction Checker", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                    Text("Search clinical monographs, calculate pediatric weight doses, and check multi-drug interactions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onOpenFormularyDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Formulary", fontSize = 12.sp)
                }
            }
        }

        Text("Formulary Core Modules:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

        val features = listOf(
            Triple("Pharmacology Monographs", "Standard adult dosing, renal impairment clearance thresholds, hepatic safety, black box warnings, and contraindications.", "Monographs"),
            Triple("Multi-Drug Interaction Checker", "Analyze drug combinations for CYP3A4, bleeding risk, QT prolongation, and pharmacodynamic antagonism with clinical mitigation steps.", "Safety Analyzer"),
            Triple("Pediatric Weight-Based Dosing", "Weight-based mg/kg/dose calculator with max single-dose safety caps and age-appropriate frequency schedules.", "Pediatric Dosing")
        )

        features.forEach { (title, desc, tag) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenFormularyDialog),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF0F766E).copy(alpha = 0.12f)) {
                            Text(tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------
// Patient Information Leaflet Tab
// ----------------------------------------------------
@Composable
fun PatientLeafletsTab(
    viewModel: DocuMedViewModel,
    generatedLeaflet: com.example.data.model.PatientInformationLeaflet?,
    isGenerating: Boolean,
    onOpenLeafletDialog: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7).copy(alpha = 0.08f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Multi-Language Patient Leaflet Generator", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                    Text("Convert complex clinical jargon into patient discharge guides in English, Spanish, Hindi, French, Arabic, Chinese, German, and Portuguese", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onOpenLeafletDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Generator", fontSize = 12.sp)
                }
            }
        }

        if (generatedLeaflet != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Active Generated Leaflet: ${generatedLeaflet.title}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                    Text("${generatedLeaflet.language.flagEmoji} ${generatedLeaflet.language.displayName} · ${generatedLeaflet.readingLevel.label}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(generatedLeaflet.conditionSummary, fontSize = 12.sp, maxLines = 3)
                    Button(
                        onClick = onOpenLeafletDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("View Full Leaflet & Print/Share", fontSize = 11.sp)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Click 'Open Generator' to adapt any clinical summary or prescription note into clear, empathetic patient educational literature.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}

// ----------------------------------------------------
// Radiology & Image Annotation Tab
// ----------------------------------------------------
@Composable
fun RadiologyCanvasTab(viewModel: DocuMedViewModel, onOpenCanvas: () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.08f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1E293B).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Radiology & Image Annotation Canvas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text("Draw lesion contours, drop numbered anatomical callouts, take caliper measurements, and create split-screen normal vs pathology comparisons", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onOpenCanvas,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Launch Canvas", fontSize = 12.sp)
                }
            }
        }

        Text("Supported Modalities & Features:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

        val features = listOf(
            Triple("Anatomical Pin Drop", "Place numbered pins (1, 2, 3...) directly over organs or landmarks with customized clinical labels.", "Anatomy"),
            Triple("Lesion Contour & Highlighting", "Draw freehand boundaries around tumors, consolidations, or fractures in red, yellow, and blue color codes.", "Pathology"),
            Triple("Caliper Distance Measurement", "Measure lesion diameter, cardiothoracic ratio, or bone displacement in calibrated millimeters.", "Measurement"),
            Triple("Split-Screen Comparison Mode", "Render side-by-side normal baseline vs active pathology figures with synchronized captions.", "Diagnostic Triage")
        )

        features.forEach { (title, desc, tag) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenCanvas),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF1E293B).copy(alpha = 0.12f)) {
                            Text(tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------
// Tab 1: Chapter Builder (17 Structured Sections)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterBuilderTab(viewModel: DocuMedViewModel) {
    var topic by remember { mutableStateOf("Fracture Shaft of Femur in Adults") }
    var specialty by remember { mutableStateOf("Orthopedics") }
    var audience by remember { mutableStateOf("Postgraduate Resident") }

    val all17Sections = listOf(
        "1.0 Introduction & Clinical Significance",
        "2.0 Definition & Diagnostic Scope",
        "3.0 Relevant Surgical Anatomy & Blood Supply",
        "4.0 Mechanism of Injury & Biomechanics",
        "5.0 Classification Systems (AO/OTA & Eponymous)",
        "6.0 Clinical Presentation & Physical Signs",
        "7.0 Diagnostic Investigations & Lab Workup",
        "8.0 Radiographic & Cross-Sectional Imaging",
        "9.0 Differential Diagnoses & Mimics",
        "10.0 Evidence-Based Treatment Algorithm",
        "11.0 Surgical Indications & Operative Steps",
        "12.0 Intraoperative & Postoperative Complications",
        "13.0 Rehabilitation, Weight-Bearing & Follow-Up",
        "14.0 Prognosis & Long-Term Outcomes",
        "15.0 Recent Advances & Guidelines (2025-2026)",
        "16.0 High-Yield Key Points & Red Flags",
        "17.0 References & Evidence Hierarchy"
    )

    val selectedSections = remember { mutableStateListOf(*all17Sections.toTypedArray()) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "ACADEMIC CHAPTER GENERATOR",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MedicalBluePrimary)
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Medical Topic / Disease / Procedure") },
                    placeholder = { Text("e.g. ST-Elevation Myocardial Infarction, Acute Appendicitis") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = specialty,
                        onValueChange = { specialty = it },
                        label = { Text("Specialty") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = audience,
                        onValueChange = { audience = it },
                        label = { Text("Target Level") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section Selector (17 Structured Sections)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "17 STRUCTURED CHAPTER SECTIONS (${selectedSections.size}/17)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MedicalBluePrimary)
                    )
                    Row {
                        Text(
                            text = "Select All",
                            color = MedicalBluePrimary,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .clickable {
                                    selectedSections.clear()
                                    selectedSections.addAll(all17Sections)
                                }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                all17Sections.forEach { sec ->
                    val isChecked = selectedSections.contains(sec)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) selectedSections.remove(sec) else selectedSections.add(sec)
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { check ->
                                if (check) selectedSections.add(sec) else selectedSections.remove(sec)
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = sec, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Generate Action Button
        Button(
            onClick = {
                viewModel.generateChapterWithAi(topic, specialty, audience, selectedSections.toList())
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Complete Medical Chapter & Open in Editor")
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// ----------------------------------------------------
// Tab 2: Summaries Studio
// ----------------------------------------------------
@Composable
fun SummariesTab(viewModel: DocuMedViewModel, generatedSummary: String?) {
    var selectedSummaryType by remember { mutableStateOf("Quick 5-10 Bullets") }
    var selectedAudience by remember { mutableStateOf("Postgraduate Resident") }
    val summaryTypes = listOf("Quick 5-10 Bullets", "Detailed Structured", "Exam High-Yield", "Teaching Summary")
    val audiences = listOf("Undergraduate", "Postgraduate", "Specialist", "Patient Education")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "CLINICAL SUMMARY SUITE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MedicalBluePrimary)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(summaryTypes) { type ->
                        FilterChip(
                            selected = selectedSummaryType == type,
                            onClick = { selectedSummaryType = type },
                            label = { Text(type, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedicalBluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Text("Audience Level", style = MaterialTheme.typography.labelSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(audiences) { aud ->
                        FilterChip(
                            selected = selectedAudience == aud,
                            onClick = { selectedAudience = aud },
                            label = { Text(aud, fontSize = 12.sp) }
                        )
                    }
                }

                Button(
                    onClick = { viewModel.generateSummaryWithAi(selectedSummaryType, selectedAudience) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate $selectedSummaryType")
                }
            }
        }

        if (!generatedSummary.isNullOrBlank()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedicalTealPrimary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GENERATED SUMMARY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MedicalTealPrimary)
                        )
                        FilledTonalButton(
                            onClick = {
                                viewModel.appendContentToEditor("\n\n## Summary: $selectedSummaryType\n$generatedSummary")
                            }
                        ) {
                            Text("Insert into Document", fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = generatedSummary,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// Tab 3: MCQ Bank
// ----------------------------------------------------
@Composable
fun McqBankTab(viewModel: DocuMedViewModel, mcqs: List<com.example.data.model.McqItem>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Board Exam Clinical MCQs", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Button(onClick = { viewModel.generateMCQsWithAi(count = 4) }) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate MCQs", fontSize = 12.sp)
                }
            }
        }

        McqPracticeView(mcqs = mcqs, modifier = Modifier.weight(1f))
    }
}

// ----------------------------------------------------
// Tab 4: Viva Voce & Oral Prep
// ----------------------------------------------------
@Composable
fun VivaTab(viewModel: DocuMedViewModel, vivaList: List<com.example.data.model.VivaItem>) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "VIVA VOCE & ORAL BOARD QUESTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MedicalBluePrimary)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Extract tough viva questions with model answers and keyword checklists expected by examiners.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.generateVivaWithAi(count = 3) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Viva Voce Questions")
                }
            }
        }

        vivaList.forEachIndexed { index, viva ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Examiner Question ${index + 1}:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MedicalBluePrimary)
                    )
                    Text(
                        text = viva.question,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ClinicalGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("MODEL ANSWER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ClinicalGreen))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(viva.modelAnswer, style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp), color = Color(0xFF065F46))
                        }
                    }
                    if (viva.highYieldKeywords.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Essential Keywords:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                            items(viva.highYieldKeywords) { kw ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = kw,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// Tab 5: OSCE Station
// ----------------------------------------------------
@Composable
fun OsceTab(viewModel: DocuMedViewModel, osce: com.example.data.model.OsceStation?) {
    var stationTopic by remember { mutableStateOf("Geriatric Hip Fracture Evaluation & Management") }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = stationTopic,
                    onValueChange = { stationTopic = it },
                    label = { Text("OSCE Topic") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(onClick = { viewModel.generateOSCEWithAi(stationTopic) }) {
                    Icon(imageVector = Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Build OSCE", fontSize = 12.sp)
                }
            }
        }

        if (osce != null) {
            OsceStationView(
                osce = osce,
                modifier = Modifier.weight(1f),
                onAppendToDocument = { text ->
                    viewModel.appendContentToEditor(text)
                }
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Click 'Build OSCE' to create an 8-minute clinical exam station.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ----------------------------------------------------
// Tab 6: Flashcards
// ----------------------------------------------------
@Composable
fun FlashcardsTab(viewModel: DocuMedViewModel, flashcards: List<com.example.data.model.FlashcardItem>) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Spaced-Repetition Deck", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Button(onClick = { viewModel.generateFlashcardsWithAi() }) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate Cards", fontSize = 12.sp)
                }
            }
        }

        FlashcardDeckView(
            flashcards = flashcards,
            modifier = Modifier.weight(1f),
            onExportToAnki = {
                viewModel.exportFlashcardsToAnki(context, "DocuMed Study Deck")
            }
        )
    }
}

// ----------------------------------------------------
// Tab 7: Flowcharts & Algorithms
// ----------------------------------------------------
@Composable
fun FlowchartsTab(viewModel: DocuMedViewModel, flowchart: String?) {
    var promptText by remember { mutableStateOf("Acute Hip Fracture Triage & Surgical Timing Algorithm") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("CLINICAL DECISION ALGORITHM BUILDER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MedicalBluePrimary))
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    label = { Text("Flowchart / Algorithm Topic") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { viewModel.generateFlowchartWithAi(promptText) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.Default.Timeline, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Clinical Algorithm Flowchart")
                }
            }
        }

        if (!flowchart.isNullOrBlank()) {
            FlowchartVisualizer(flowchartText = flowchart)
            Button(
                onClick = { viewModel.appendContentToEditor("\n\n## Clinical Algorithm: $promptText\n$flowchart") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Insert Flowchart into Active Document")
            }
        }
    }
}

// ----------------------------------------------------
// Tab 8: Tables Studio
// ----------------------------------------------------
@Composable
fun TablesTab(viewModel: DocuMedViewModel, table: String?) {
    var promptText by remember { mutableStateOf("Garden vs Pauwels Classification Comparison Table") }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("MEDICAL COMPARISON TABLE BUILDER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MedicalBluePrimary))
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    label = { Text("Comparison / Classification Table Topic") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { viewModel.generateTableWithAi(promptText) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.Default.TableChart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Publication-Ready Table")
                }
            }
        }

        if (!table.isNullOrBlank()) {
            MedicalTableView(markdownTable = table, caption = "Table: $promptText")
            Button(
                onClick = { viewModel.appendContentToEditor("\n\n### Table: $promptText\n$table") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Insert Table into Active Document")
            }
        }
    }
}

// ----------------------------------------------------
// Tab 9: Image & Radiograph Case Studio
// ----------------------------------------------------
@Composable
fun ImageScanTab(viewModel: DocuMedViewModel, isGenerating: Boolean, analysisResult: String?) {
    var showImageModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = MedicalTealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RADIOLOGY & CLINICAL IMAGE ANALYSIS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MedicalTealPrimary)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Upload X-Rays, CT Scans, MRI, Histology or Clinical Photos to generate diagnostic descriptions, differential diagnosis, and board exam questions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(onClick = { showImageModal = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Image Diagnostic Studio")
                }
            }
        }

        if (!analysisResult.isNullOrBlank()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MedicalBluePrimary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("LATEST IMAGE ANALYSIS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MedicalBluePrimary))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(analysisResult, style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.appendContentToEditor("\n\n### Radiological / Image Case Analysis\n$analysisResult") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Insert Analysis into Active Document")
                    }
                }
            }
        }
    }

    if (showImageModal) {
        ImageDiagnosticDialog(
            isGenerating = isGenerating,
            analysisResult = analysisResult,
            onDismiss = { showImageModal = false },
            onAnalyzeImage = { category, findings ->
                viewModel.analyzeMedicalImageWithAi(category, findings)
            },
            onInsertIntoDocument = { text ->
                viewModel.appendContentToEditor(text)
            }
        )
    }
}

// ----------------------------------------------------
// Tab: AI PowerPoint & Slide Deck Generator
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerPointDeckTab(
    viewModel: DocuMedViewModel,
    presentation: MedicalPresentation?,
    isGenerating: Boolean,
    onOpenDeckViewer: () -> Unit
) {
    val context = LocalContext.current
    var audience by remember { mutableStateOf("Postgraduate Medical Residents & Fellows") }
    var slideCount by remember { mutableIntStateOf(6) }
    val scrollState = rememberScrollState()
    var showSlideEditor by remember { mutableStateOf(false) }
    var showSaveToHub by remember { mutableStateOf(false) }

    val audienceOptions = listOf(
        "Postgraduate Medical Residents & Fellows",
        "Medical Students (Undergraduate)",
        "Interdisciplinary Grand Rounds / Consultants",
        "Patient Education & Nursing Staff"
    )
    var audienceExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Slideshow,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI POWERPOINT & SLIDE DECK BUILDER",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                        )
                        Text(
                            text = "Transforms active medical document into presentation slides",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Target Audience
                ExposedDropdownMenuBox(
                    expanded = audienceExpanded,
                    onExpandedChange = { audienceExpanded = it }
                ) {
                    OutlinedTextField(
                        value = audience,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Audience & Tone") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = audienceExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = audienceExpanded,
                        onDismissRequest = { audienceExpanded = false }
                    ) {
                        audienceOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt) },
                                onClick = {
                                    audience = opt
                                    audienceExpanded = false
                                }
                            )
                        }
                    }
                }

                // Slide Count Selector
                Column {
                    Text(
                        text = "Number of Slides: $slideCount slides",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(4, 6, 8, 10).forEach { count ->
                            val isSelected = slideCount == count
                            FilterChip(
                                selected = isSelected,
                                onClick = { slideCount = count },
                                label = { Text("$count Slides") },
                                leadingIcon = if (isSelected) {
                                    { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        viewModel.generatePowerPointPresentation(
                            audience = audience,
                            slideCount = slideCount,
                            onComplete = { onOpenDeckViewer() }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isGenerating) "Synthesizing Slides..." else "Generate AI Presentation Deck")
                }
            }
        }

        // Generated Presentation Preview Card
        if (presentation != null && presentation.slides.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "READY: ${presentation.title}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                            )
                            Text(
                                text = "${presentation.slides.size} Slides • Presenter: ${presentation.presenter}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Slide 1 summary preview
                    val firstSlide = presentation.slides.first()
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Slide 1: ${firstSlide.title}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            firstSlide.bulletPoints.take(2).forEach { bp ->
                                Text(
                                    text = "• $bp",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenDeckViewer,
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Icon(imageVector = Icons.Default.Slideshow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Launch Deck", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showSlideEditor = true },
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
                        ) {
                            Icon(imageVector = Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Slides", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.OutlinedButton(
                            onClick = {
                                viewModel.saveCurrentPresentationToHub(presentation)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save to Files Hub", fontSize = 11.5.sp)
                        }

                        androidx.compose.material3.OutlinedButton(
                            onClick = {
                                val htmlFile = DocumentExportHelper.exportPresentationToHtmlFile(context, presentation)
                                DocumentExportHelper.shareFileToGoogleDrive(context, htmlFile, "text/html", presentation.title)
                            },
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F9D58)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Google Drive", fontSize = 11.5.sp)
                        }

                        FilledTonalButton(
                            onClick = {
                                DocumentExportHelper.sharePresentation(context, presentation)
                            },
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }

    if (showSlideEditor && presentation != null) {
        com.example.ui.dialogs.SlideDeckEditorDialog(
            initialPresentation = presentation,
            onDismiss = { showSlideEditor = false },
            onSaveDeck = { updated ->
                viewModel.updateSlideDeck(updated)
            },
            onSaveToHub = { updated ->
                viewModel.saveCurrentPresentationToHub(updated)
                showSlideEditor = false
            }
        )
    }
}

