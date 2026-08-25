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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FlashcardDeckView
import com.example.ui.components.FlowchartVisualizer
import com.example.ui.components.ImageDiagnosticDialog
import com.example.ui.components.McqPracticeView
import com.example.ui.components.MedicalTableView
import com.example.ui.components.OsceStationView
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

    val studioTabs = listOf(
        "Chapter Builder",
        "Summaries",
        "MCQ Bank",
        "Viva & Oral",
        "OSCE Station",
        "Flashcards",
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
                1 -> SummariesTab(viewModel, generatedSummary)
                2 -> McqBankTab(viewModel, generatedMCQs)
                3 -> VivaTab(viewModel, generatedViva)
                4 -> OsceTab(viewModel, generatedOSCE)
                5 -> FlashcardsTab(viewModel, generatedFlashcards)
                6 -> FlowchartsTab(viewModel, generatedFlowchart)
                7 -> TablesTab(viewModel, generatedTable)
                8 -> ImageScanTab(viewModel, isAiGenerating, generatedImageAnalysis)
            }
        }
    }
}

@Composable
fun ScrollableTabRow(
    selectedTabIndex: Int,
    edgePadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit
) {
    androidx.compose.material3.ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        edgePadding = edgePadding,
        modifier = modifier
    ) {
        tabs()
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

        FlashcardDeckView(flashcards = flashcards, modifier = Modifier.weight(1f))
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
