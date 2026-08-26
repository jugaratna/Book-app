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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.calculator.ClinicalCalculatorEngine
import com.example.data.model.CalculationResult
import com.example.data.model.CalculatorCategory
import com.example.data.model.RiskSeverity
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalRed
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary

@Composable
fun ClinicalCalculatorsDialog(
    onDismiss: () -> Unit,
    onInsertCalculation: (CalculationResult) -> Unit
) {
    var selectedCalculator by remember { mutableStateOf("wells_pe") }

    val calculatorsList = listOf(
        Triple("wells_pe", "Wells PE Score", "Pulmonary Embolism Pre-test Probability"),
        Triple("cha2ds2_vasc", "CHA₂DS₂-VASc", "AF Stroke Risk & Anticoagulation"),
        Triple("curb_65", "CURB-65", "Pneumonia Severity & Triage"),
        Triple("gcs_scale", "Glasgow Coma Scale", "Neurological Consciousness & Coma"),
        Triple("meld_na", "MELD-Na Score", "End-Stage Liver Disease & Mortality"),
        Triple("egfr_ckdepi", "eGFR & CrCl", "Renal Function & CKD-EPI 2021")
    )

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
                    color = MedicalBluePrimary,
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
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Clinical Calculators & Scoring Suite",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Evidence-based risk stratification & guideline recommendations",
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

                // Calculator Selector Strip
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(calculatorsList) { (id, name, desc) ->
                        FilterChip(
                            selected = selectedCalculator == id,
                            onClick = { selectedCalculator = id },
                            label = {
                                Text(name, fontSize = 12.sp, fontWeight = if (selectedCalculator == id) FontWeight.Bold else FontWeight.Normal)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedicalBluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Calculator Content Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (selectedCalculator) {
                        "wells_pe" -> WellsPECalculatorView(onInsertCalculation)
                        "cha2ds2_vasc" -> CHA2DS2VAScCalculatorView(onInsertCalculation)
                        "curb_65" -> CURB65CalculatorView(onInsertCalculation)
                        "gcs_scale" -> GCSCalculatorView(onInsertCalculation)
                        "meld_na" -> MELDCalculatorView(onInsertCalculation)
                        "egfr_ckdepi" -> EGFRCalculatorView(onInsertCalculation)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    result: CalculationResult,
    onInsert: (CalculationResult) -> Unit
) {
    val badgeColor = Color(result.riskCategory.colorHex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, badgeColor.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.scoreValue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                    Text(
                        text = result.riskCategory.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeColor
                    )
                }

                Button(
                    onClick = { onInsert(result) },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Insert in Doc", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = result.interpretation,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text("Recommended Clinical Actions:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            result.recommendations.forEach { rec ->
                Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.Top) {
                    Text("• ", color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(rec, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ref: ${result.evidenceReference}",
                fontSize = 10.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun WellsPECalculatorView(onInsert: (CalculationResult) -> Unit) {
    var c1 by remember { mutableStateOf(false) }
    var c2 by remember { mutableStateOf(false) }
    var c3 by remember { mutableStateOf(false) }
    var c4 by remember { mutableStateOf(false) }
    var c5 by remember { mutableStateOf(false) }
    var c6 by remember { mutableStateOf(false) }
    var c7 by remember { mutableStateOf(false) }

    val result = ClinicalCalculatorEngine.calculateWellsPE(c1, c2, c3, c4, c5, c6, c7)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Wells Criteria for Pulmonary Embolism (PE)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text("Select all criteria present in patient history & examination:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        CriteriaCheckboxItem(title = "Clinical signs and symptoms of DVT (leg swelling, tenderness)", points = "+3.0 pts", checked = c1, onCheckedChange = { c1 = it })
        CriteriaCheckboxItem(title = "PE is #1 diagnosis OR equally likely to other diagnoses", points = "+3.0 pts", checked = c2, onCheckedChange = { c2 = it })
        CriteriaCheckboxItem(title = "Heart rate > 100 bpm (Tachycardia)", points = "+1.5 pts", checked = c3, onCheckedChange = { c3 = it })
        CriteriaCheckboxItem(title = "Immobilization (>=3 days) OR surgery in previous 4 weeks", points = "+1.5 pts", checked = c4, onCheckedChange = { c4 = it })
        CriteriaCheckboxItem(title = "Previous objectively diagnosed PE or DVT", points = "+1.5 pts", checked = c5, onCheckedChange = { c5 = it })
        CriteriaCheckboxItem(title = "Hemoptysis (coughing blood)", points = "+1.0 pt", checked = c6, onCheckedChange = { c6 = it })
        CriteriaCheckboxItem(title = "Malignancy (treatment within 6 months or palliative)", points = "+1.0 pt", checked = c7, onCheckedChange = { c7 = it })

        Spacer(modifier = Modifier.height(8.dp))
        ResultCard(result = result, onInsert = onInsert)
    }
}

@Composable
fun CHA2DS2VAScCalculatorView(onInsert: (CalculationResult) -> Unit) {
    var chf by remember { mutableStateOf(false) }
    var htn by remember { mutableStateOf(false) }
    var ageCat by remember { mutableIntStateOf(0) } // 0: <65, 1: 65-74, 2: >=75
    var dm by remember { mutableStateOf(false) }
    var stroke by remember { mutableStateOf(false) }
    var vasc by remember { mutableStateOf(false) }
    var isFemale by remember { mutableStateOf(false) }

    val result = ClinicalCalculatorEngine.calculateCHA2DS2VASc(chf, htn, ageCat, dm, stroke, vasc, isFemale)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("CHA₂DS₂-VASc Score for Atrial Fibrillation Stroke Risk", fontSize = 15.sp, fontWeight = FontWeight.Bold)

        CriteriaCheckboxItem(title = "C - Congestive Heart Failure / LV dysfunction", points = "+1 pt", checked = chf, onCheckedChange = { chf = it })
        CriteriaCheckboxItem(title = "H - Hypertension (resting BP >140/90 or on meds)", points = "+1 pt", checked = htn, onCheckedChange = { htn = it })

        // Age Radio Group
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("A - Age Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = ageCat == 0, onClick = { ageCat = 0 })
                        Text("< 65 (0 pt)", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = ageCat == 1, onClick = { ageCat = 1 })
                        Text("65-74 (+1 pt)", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = ageCat == 2, onClick = { ageCat = 2 })
                        Text("≥ 75 (+2 pts)", fontSize = 12.sp)
                    }
                }
            }
        }

        CriteriaCheckboxItem(title = "D - Diabetes Mellitus (oral meds or insulin)", points = "+1 pt", checked = dm, onCheckedChange = { dm = it })
        CriteriaCheckboxItem(title = "S₂ - Prior Stroke / TIA / Thromboembolism", points = "+2 pts", checked = stroke, onCheckedChange = { stroke = it })
        CriteriaCheckboxItem(title = "V - Vascular Disease (Prior MI, PAD, Aortic plaque)", points = "+1 pt", checked = vasc, onCheckedChange = { vasc = it })
        CriteriaCheckboxItem(title = "Sc - Sex Category (Female)", points = "+1 pt", checked = isFemale, onCheckedChange = { isFemale = it })

        Spacer(modifier = Modifier.height(8.dp))
        ResultCard(result = result, onInsert = onInsert)
    }
}

@Composable
fun CURB65CalculatorView(onInsert: (CalculationResult) -> Unit) {
    var confusion by remember { mutableStateOf(false) }
    var urea by remember { mutableStateOf(false) }
    var rr by remember { mutableStateOf(false) }
    var bp by remember { mutableStateOf(false) }
    var age by remember { mutableStateOf(false) }

    val result = ClinicalCalculatorEngine.calculateCURB65(confusion, urea, rr, bp, age)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("CURB-65 Pneumonia Severity Score", fontSize = 15.sp, fontWeight = FontWeight.Bold)

        CriteriaCheckboxItem(title = "C - Confusion (Abbreviated Mental Test <= 8 or new disorientation)", points = "+1 pt", checked = confusion, onCheckedChange = { confusion = it })
        CriteriaCheckboxItem(title = "U - Urea > 7 mmol/L (BUN > 19 mg/dL)", points = "+1 pt", checked = urea, onCheckedChange = { urea = it })
        CriteriaCheckboxItem(title = "R - Respiratory rate ≥ 30 breaths/min", points = "+1 pt", checked = rr, onCheckedChange = { rr = it })
        CriteriaCheckboxItem(title = "B - Blood pressure (Systolic < 90 or Diastolic ≤ 60 mmHg)", points = "+1 pt", checked = bp, onCheckedChange = { bp = it })
        CriteriaCheckboxItem(title = "65 - Age ≥ 65 years", points = "+1 pt", checked = age, onCheckedChange = { age = it })

        Spacer(modifier = Modifier.height(8.dp))
        ResultCard(result = result, onInsert = onInsert)
    }
}

@Composable
fun GCSCalculatorView(onInsert: (CalculationResult) -> Unit) {
    var eye by remember { mutableIntStateOf(4) }
    var verbal by remember { mutableIntStateOf(5) }
    var motor by remember { mutableIntStateOf(6) }

    val result = ClinicalCalculatorEngine.calculateGCS(eye, verbal, motor)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Glasgow Coma Scale (GCS)", fontSize = 15.sp, fontWeight = FontWeight.Bold)

        // Eye opening
        GcsCategorySelector(
            category = "Eye Opening (E)",
            options = listOf(4 to "4 - Spontaneous", 3 to "3 - To Speech", 2 to "2 - To Pain", 1 to "1 - None"),
            selected = eye,
            onSelect = { eye = it }
        )

        // Verbal
        GcsCategorySelector(
            category = "Verbal Response (V)",
            options = listOf(5 to "5 - Oriented", 4 to "4 - Confused", 3 to "3 - Inappropriate Words", 2 to "2 - Incomprehensible Sounds", 1 to "1 - None"),
            selected = verbal,
            onSelect = { verbal = it }
        )

        // Motor
        GcsCategorySelector(
            category = "Motor Response (M)",
            options = listOf(6 to "6 - Obeys Commands", 5 to "5 - Localizes Pain", 4 to "4 - Withdraws from Pain", 3 to "3 - Abnormal Flexion (Decorticate)", 2 to "2 - Extension (Decerebrate)", 1 to "1 - None"),
            selected = motor,
            onSelect = { motor = it }
        )

        Spacer(modifier = Modifier.height(8.dp))
        ResultCard(result = result, onInsert = onInsert)
    }
}

@Composable
fun MELDCalculatorView(onInsert: (CalculationResult) -> Unit) {
    var biliText by remember { mutableStateOf("2.0") }
    var inrText by remember { mutableStateOf("1.6") }
    var crText by remember { mutableStateOf("1.4") }
    var naText by remember { mutableStateOf("135") }
    var dialysis by remember { mutableStateOf(false) }

    val bili = biliText.toDoubleOrNull() ?: 1.0
    val inr = inrText.toDoubleOrNull() ?: 1.0
    val cr = crText.toDoubleOrNull() ?: 1.0
    val na = naText.toDoubleOrNull() ?: 135.0

    val result = ClinicalCalculatorEngine.calculateMELD(bili, inr, cr, na, dialysis)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("MELD-Na Score (Model for End-Stage Liver Disease)", fontSize = 15.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = biliText,
                onValueChange = { biliText = it },
                label = { Text("Bilirubin (mg/dL)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = inrText,
                onValueChange = { inrText = it },
                label = { Text("INR") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = crText,
                onValueChange = { crText = it },
                label = { Text("Creatinine (mg/dL)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = naText,
                onValueChange = { naText = it },
                label = { Text("Sodium (mEq/L)") },
                modifier = Modifier.weight(1f)
            )
        }

        CriteriaCheckboxItem(title = "Hemodialysis ≥2 times in the past 7 days", points = "Cr set to 4.0", checked = dialysis, onCheckedChange = { dialysis = it })

        Spacer(modifier = Modifier.height(8.dp))
        ResultCard(result = result, onInsert = onInsert)
    }
}

@Composable
fun EGFRCalculatorView(onInsert: (CalculationResult) -> Unit) {
    var ageText by remember { mutableStateOf("65") }
    var isFemale by remember { mutableStateOf(false) }
    var crText by remember { mutableStateOf("1.2") }
    var weightText by remember { mutableStateOf("70") }

    val age = ageText.toIntOrNull() ?: 60
    val cr = crText.toDoubleOrNull() ?: 1.0
    val weight = weightText.toDoubleOrNull() ?: 70.0

    val result = ClinicalCalculatorEngine.calculateeGFR(age, isFemale, cr, weight)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("eGFR (CKD-EPI 2021) & Creatinine Clearance", fontSize = 15.sp, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it },
                label = { Text("Age (years)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = crText,
                onValueChange = { crText = it },
                label = { Text("Creatinine (mg/dL)") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Weight (kg)") },
                modifier = Modifier.weight(1f)
            )
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isFemale, onCheckedChange = { isFemale = it })
                Text("Female Sex", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        ResultCard(result = result, onInsert = onInsert)
    }
}

@Composable
fun CriteriaCheckboxItem(
    title: String,
    points: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) MedicalBluePrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = if (checked) androidx.compose.foundation.BorderStroke(1.dp, MedicalBluePrimary.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(checkedColor = MedicalBluePrimary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = points,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (checked) MedicalBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GcsCategorySelector(
    category: String,
    options: List<Pair<Int, String>>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(category, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            options.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(value) }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == value, onClick = { onSelect(value) })
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(label, fontSize = 12.sp, fontWeight = if (selected == value) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}
