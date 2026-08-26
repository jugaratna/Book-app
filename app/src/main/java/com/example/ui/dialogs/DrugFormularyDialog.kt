package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DrugCategory
import com.example.data.model.DrugInteractionPair
import com.example.data.model.DrugMonograph
import com.example.data.model.InteractionSeverity
import com.example.data.repository.DrugFormularyRepository
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalRed
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary

@Composable
fun DrugFormularyDialog(
    repository: DrugFormularyRepository,
    onDismiss: () -> Unit,
    onInsertMonograph: (DrugMonograph) -> Unit,
    onInsertInteractionReport: (List<DrugInteractionPair>, List<String>) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Formulary Monographs, 1: Interaction Checker, 2: Pediatric Dosing
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<DrugCategory?>(null) }
    var activeDrug by remember { mutableStateOf<DrugMonograph?>(repository.medications.firstOrNull()) }

    // Multi-drug interaction selection
    val selectedRegimen = remember { mutableStateListOf<String>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF0F766E), // Deep Teal
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Bedside Drug Formulary & Interaction Checker",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Pharmacology monographs, renal dosing & multi-drug safety analyzer",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Drug Monographs", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Interaction Checker", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                                if (selectedRegimen.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(ClinicalAmber, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${selectedRegimen.size}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Pediatric Calculator", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> DrugMonographsView(
                            repository = repository,
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            selectedCategory = selectedCategory,
                            onCategoryChange = { selectedCategory = it },
                            activeDrug = activeDrug,
                            onSelectDrug = { activeDrug = it },
                            onInsert = onInsertMonograph,
                            onAddToInteractionRegimen = { drugName ->
                                if (!selectedRegimen.contains(drugName)) {
                                    selectedRegimen.add(drugName)
                                }
                            }
                        )
                        1 -> InteractionCheckerView(
                            repository = repository,
                            selectedRegimen = selectedRegimen,
                            onToggleDrug = { name ->
                                if (selectedRegimen.contains(name)) selectedRegimen.remove(name)
                                else selectedRegimen.add(name)
                            },
                            onClearRegimen = { selectedRegimen.clear() },
                            onInsertReport = onInsertInteractionReport
                        )
                        2 -> PediatricDosingCalculatorView(
                            repository = repository,
                            onInsertMonograph = onInsertMonograph
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrugMonographsView(
    repository: DrugFormularyRepository,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: DrugCategory?,
    onCategoryChange: (DrugCategory?) -> Unit,
    activeDrug: DrugMonograph?,
    onSelectDrug: (DrugMonograph) -> Unit,
    onInsert: (DrugMonograph) -> Unit,
    onAddToInteractionRegimen: (String) -> Unit
) {
    val filtered = repository.medications.filter { drug ->
        (searchQuery.isBlank() || drug.genericName.contains(searchQuery, ignoreCase = true) || drug.brandNames.contains(searchQuery, ignoreCase = true)) &&
                (selectedCategory == null || drug.category == selectedCategory)
    }

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Left Column: Drug list
        Column(modifier = Modifier.weight(0.42f).fillMaxHeight()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search medication...", fontSize = 12.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered) { drug ->
                    val isSelected = activeDrug?.id == drug.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectDrug(drug) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MedicalTealPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MedicalTealPrimary) else null
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = drug.genericName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MedicalTealPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = drug.brandNames,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = drug.category.displayName,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Right Column: Monograph Details
        Column(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            if (activeDrug != null) {
                val drug = activeDrug

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = drug.genericName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                        Text(text = "Brand: ${drug.brandNames} · ${drug.category.displayName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row {
                        OutlinedButton(
                            onClick = { onAddToInteractionRegimen(drug.genericName) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("+ Regimen", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { onInsert(drug) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Insert", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Black Box Warning if present
                if (!drug.blackBoxWarning.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = ClinicalRed.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, ClinicalRed)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ClinicalRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("FDA BLACK BOX WARNING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ClinicalRed)
                                Text(drug.blackBoxWarning, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 15.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Dosage & Renal
                MonographSection(title = "Standard Adult Dosing", content = drug.standardAdultDose)
                MonographSection(title = "Renal Impairment Dosing", content = drug.renalAdjustment)
                MonographSection(title = "Hepatic Impairment", content = drug.hepaticAdjustment)

                // Contraindications
                Text("Contraindications:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                drug.contraindications.forEach { c ->
                    Text("• $c", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 6.dp, top = 2.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Monitoring Parameters:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                drug.monitoringParameters.forEach { m ->
                    Text("• $m", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 6.dp, top = 2.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                MonographSection(title = "Pregnancy & Lactation", content = drug.pregnancyCategory)
            }
        }
    }
}

@Composable
fun MonographSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(content, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
    }
}

@Composable
fun InteractionCheckerView(
    repository: DrugFormularyRepository,
    selectedRegimen: List<String>,
    onToggleDrug: (String) -> Unit,
    onClearRegimen: () -> Unit,
    onInsertReport: (List<DrugInteractionPair>, List<String>) -> Unit
) {
    val detectedInteractions = repository.checkInteractions(selectedRegimen)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Patient Medication Regimen", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Select 2 or more drugs to analyze drug-drug interactions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (selectedRegimen.isNotEmpty()) {
                OutlinedButton(
                    onClick = onClearRegimen,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Clear", fontSize = 10.sp)
                }
            }
        }

        // Available medication pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(repository.medications) { drug ->
                val isSelected = selectedRegimen.contains(drug.genericName)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleDrug(drug.genericName) },
                    label = { Text(drug.genericName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0F766E),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Interaction Results
        if (selectedRegimen.size < 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Select at least 2 medications from the bar above to run the interaction safety checker.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (detectedInteractions.isEmpty()) "✅ No Major Interactions Detected" else "⚠️ ${detectedInteractions.size} Interaction(s) Flagged",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (detectedInteractions.isEmpty()) ClinicalGreen else ClinicalRed
                )

                Button(
                    onClick = { onInsertReport(detectedInteractions, selectedRegimen) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Insert Report into Doc", fontSize = 11.sp)
                }
            }

            detectedInteractions.forEach { interaction ->
                val sevColor = Color(interaction.severity.colorHex)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = sevColor.copy(alpha = 0.08f)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, sevColor.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "${interaction.drug1Name} ⚡ ${interaction.drug2Name}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = sevColor
                            )
                            Text(
                                text = interaction.severity.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = sevColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Mechanism:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(interaction.mechanism, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Clinical Effect:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(interaction.clinicalEffect, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Management Action:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = sevColor)
                        Text(interaction.managementAction, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PediatricDosingCalculatorView(
    repository: DrugFormularyRepository,
    onInsertMonograph: (DrugMonograph) -> Unit
) {
    var weightText by remember { mutableStateOf("15") }
    var selectedDrug by remember { mutableStateOf(repository.medications.find { it.pediatricDoseMgPerKg != null } ?: repository.medications.first()) }

    val weightKg = weightText.toDoubleOrNull() ?: 10.0
    val dosePerKg = selectedDrug.pediatricDoseMgPerKg ?: 0.0
    val calculatedDose = weightKg * dosePerKg
    val cappedDose = if (selectedDrug.pediatricMaxSingleDoseMg != null) minOf(calculatedDose, selectedDrug.pediatricMaxSingleDoseMg!!) else calculatedDose

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Pediatric Weight-Based Dosing Calculator", fontSize = 15.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Child Weight (kg)") },
                modifier = Modifier.weight(1f)
            )
        }

        Text("Select Pediatric Medication:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(repository.medications.filter { it.pediatricDoseMgPerKg != null }) { drug ->
                FilterChip(
                    selected = selectedDrug.id == drug.id,
                    onClick = { selectedDrug = drug },
                    label = { Text(drug.genericName, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0F766E),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MedicalTealPrimary.copy(alpha = 0.1f)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, MedicalTealPrimary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${selectedDrug.genericName} (${selectedDrug.brandNames})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F766E)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Recommended Dose: ${String.format("%.1f", cappedDose)} mg ${selectedDrug.pediatricFrequency ?: "per dose"}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Base Rate: $dosePerKg mg/kg/dose (Weight: $weightKg kg)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (selectedDrug.pediatricMaxSingleDoseMg != null) {
                    Text(
                        text = "Maximum Single Dose: ${selectedDrug.pediatricMaxSingleDoseMg} mg",
                        fontSize = 11.sp,
                        color = ClinicalAmber
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onInsertMonograph(selectedDrug) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Insert Full Monograph into Document", fontSize = 12.sp)
                }
            }
        }
    }
}
