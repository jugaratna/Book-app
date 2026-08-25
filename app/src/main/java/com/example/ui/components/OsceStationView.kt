package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.OsceStation
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary

@Composable
fun OsceStationView(
    osce: OsceStation,
    modifier: Modifier = Modifier,
    onAppendToDocument: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var showAnswers by remember { mutableStateOf(false) }
    var showMarkingScheme by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Station Title Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "OSCE CLINICAL STATION",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = ClinicalAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "8 Minutes Station",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ClinicalAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = osce.stationTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Clinical Scenario Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "CLINICAL SCENARIO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MedicalBluePrimary,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = osce.clinicalScenario,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Candidate Instructions Box
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFEFF6FF),
            border = androidx.compose.foundation.BorderStroke(1.dp, MedicalBluePrimary.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "CANDIDATE INSTRUCTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MedicalBluePrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = osce.candidateInstructions,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = Color(0xFF1E3A8A)
                )
            }
        }

        // Subquestions
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "STATION QUESTIONS (ORAL/WRITTEN PROMPTS)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                osce.subQuestions.forEachIndexed { index, q ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MedicalTealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = q,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Toggle Expected Answers
        FilledTonalButton(
            onClick = { showAnswers = !showAnswers },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (showAnswers) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (showAnswers) "Hide Model Answers" else "Show Examiner Model Answers")
        }

        AnimatedVisibility(visible = showAnswers) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = androidx.compose.foundation.BorderStroke(1.dp, ClinicalGreen.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXPECTED CLINICAL RESPONSES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ClinicalGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    osce.expectedAnswers.forEachIndexed { index, ans ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "Answer to Q${index + 1}:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ClinicalGreen)
                            )
                            Text(
                                text = ans,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }
            }
        }

        // Toggle Marking Scheme
        FilledTonalButton(
            onClick = { showMarkingScheme = !showMarkingScheme },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (showMarkingScheme) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (showMarkingScheme) "Hide Checklist & Marking Scheme" else "Show 20-Point Checklist & Marking Scheme")
        }

        AnimatedVisibility(visible = showMarkingScheme) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXAMINER MARKING SCHEME (TOTAL 20 PTS)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    osce.markingScheme.forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ClinicalGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Insert into Document Action
        Button(
            onClick = {
                val fullOsceText = """
## OSCE Station: ${osce.stationTitle}
**Clinical Scenario**: ${osce.clinicalScenario}
**Candidate Instructions**: ${osce.candidateInstructions}

### Subquestions:
${osce.subQuestions.joinToString("\n") { "- $it" }}

### Expected Answers:
${osce.expectedAnswers.joinToString("\n") { "- $it" }}

### Marking Checklist:
${osce.markingScheme.joinToString("\n") { "- [ ] $it" }}
                """.trimIndent()
                onAppendToDocument(fullOsceText)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Insert OSCE Station into Active Document")
        }
    }
}
