package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalRed
import com.example.ui.theme.MedicalBluePrimary
import com.example.util.MedicalVocabularyCheckResult
import com.example.util.MedicalVocabularyChecker
import com.example.util.VocabularyIssue
import com.example.util.VocabularyIssueType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VocabularyCheckerDialog(
    initialDocumentText: String,
    onDismiss: () -> Unit,
    onApplyCorrections: (updatedText: String) -> Unit
) {
    val context = LocalContext.current
    var currentText by remember { mutableStateOf(initialDocumentText) }
    var analysisResult by remember { mutableStateOf(MedicalVocabularyChecker.analyzeText(currentText)) }
    var selectedFilter by remember { mutableStateOf<VocabularyIssueType?>(null) }

    val issuesList = remember {
        mutableStateListOf<VocabularyIssue>().apply {
            addAll(analysisResult.issues)
        }
    }

    fun reAnalyze() {
        val newResult = MedicalVocabularyChecker.analyzeText(currentText)
        analysisResult = newResult
        issuesList.clear()
        issuesList.addAll(newResult.issues)
    }

    val displayedIssues = remember(issuesList.toList(), selectedFilter) {
        if (selectedFilter == null) {
            issuesList
        } else {
            issuesList.filter { it.type == selectedFilter }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF7C3AED).copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spellcheck,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Smart Medical Vocabulary Check",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Clinical spelling, LASA drug alerts & dangerous abbreviation safety",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Dashboard Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${analysisResult.terminologyAccuracyPercent}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (analysisResult.terminologyAccuracyPercent > 90) ClinicalGreen else ClinicalAmber
                            )
                            Text("Accuracy Score", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${analysisResult.medicalTermsFoundCount}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MedicalBluePrimary
                            )
                            Text("Clinical Terms", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${issuesList.size}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (issuesList.isEmpty()) ClinicalGreen else ClinicalRed
                            )
                            Text("Issues Flagged", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All Issues (${issuesList.size})", fontSize = 11.sp) }
                    )

                    VocabularyIssueType.values().forEach { type ->
                        val count = issuesList.count { it.type == type }
                        if (count > 0) {
                            FilterChip(
                                selected = selectedFilter == type,
                                onClick = { selectedFilter = if (selectedFilter == type) null else type },
                                label = { Text("${type.title.split(" ").first()} ($count)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(type.badgeColorHex).copy(alpha = 0.15f),
                                    selectedLabelColor = Color(type.badgeColorHex)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Issues List
                if (displayedIssues.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(54.dp),
                                tint = ClinicalGreen
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No Clinical Terminology Issues Found!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Your document complies with AMA / ICMJE terminology guidelines.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayedIssues, key = { it.id }) { issue ->
                            VocabularyIssueCard(
                                issue = issue,
                                onApply = {
                                    val updated = MedicalVocabularyChecker.applySingleCorrection(currentText, issue)
                                    currentText = updated
                                    reAnalyze()
                                    Toast.makeText(context, "Applied correction: '${issue.suggestedReplacement}'", Toast.LENGTH_SHORT).show()
                                },
                                onIgnore = {
                                    issuesList.remove(issue)
                                }
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Action Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (issuesList.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    val updated = MedicalVocabularyChecker.applyAllCorrections(currentText, issuesList)
                                    currentText = updated
                                    reAnalyze()
                                    Toast.makeText(context, "Applied all safe terminology corrections", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto-Fix All", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                onApplyCorrections(currentText)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                            modifier = Modifier.testTag("apply_vocab_changes_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save to Editor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VocabularyIssueCard(
    issue: VocabularyIssue,
    onApply: () -> Unit,
    onIgnore: () -> Unit
) {
    val badgeColor = Color(issue.type.badgeColorHex)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (issue.type) {
                                VocabularyIssueType.SPELLING_TYPO -> Icons.Default.Spellcheck
                                VocabularyIssueType.LASA_DRUG_ALERT -> Icons.Default.Warning
                                VocabularyIssueType.DANGEROUS_ABBREVIATION -> Icons.Default.Dangerous
                                VocabularyIssueType.DOSAGE_FORMAT -> Icons.Default.MedicalServices
                                VocabularyIssueType.ACRONYM_CLARIFICATION -> Icons.Default.AutoFixHigh
                            },
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = issue.type.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }

                Text(
                    text = issue.severity,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (issue.severity == "Critical") ClinicalRed else ClinicalAmber
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Matched word -> Suggestion
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ClinicalRed.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = issue.matchedWord,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ClinicalRed
                    )
                }

                Text("➔", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = ClinicalGreen.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = issue.suggestedReplacement,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ClinicalGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Explanation
            Text(
                text = issue.explanation,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onIgnore, modifier = Modifier.height(32.dp)) {
                    Text("Ignore", fontSize = 11.5.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply Correction", fontSize = 11.5.sp)
                }
            }
        }
    }
}
