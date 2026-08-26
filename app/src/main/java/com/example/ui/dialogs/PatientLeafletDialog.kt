package com.example.ui.dialogs

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.export.DocumentExportHelper
import com.example.data.model.LeafletLanguage
import com.example.data.model.PatientInformationLeaflet
import com.example.data.model.ReadingLevel
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalRed
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary

@Composable
fun PatientLeafletDialog(
    initialContent: String,
    generatedLeaflet: PatientInformationLeaflet?,
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (String, LeafletLanguage, ReadingLevel) -> Unit,
    onInsertLeaflet: (String) -> Unit
) {
    val context = LocalContext.current
    var inputMedicalText by remember { mutableStateOf(initialContent) }
    var selectedLanguage by remember { mutableStateOf<LeafletLanguage>(LeafletLanguage.ENGLISH) }
    var selectedLevel by remember { mutableStateOf<ReadingLevel>(ReadingLevel.SIMPLIFIED) }

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
                    color = Color(0xFF0284C7),
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
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Multi-Language Patient Information Leaflet Generator",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Translate complex clinical notes into 8th-grade patient discharge leaflets in 8 languages",
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

                // Body
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Controls Strip: Language & Reading Level
                    Text("Target Patient Language:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LeafletLanguage.values()) { lang ->
                            FilterChip(
                                selected = selectedLanguage == lang,
                                onClick = { selectedLanguage = lang },
                                label = { Text("${lang.flagEmoji} ${lang.displayName}", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0284C7),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Text("Reading Comprehension Level:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReadingLevel.values().forEach { level ->
                            FilterChip(
                                selected = selectedLevel == level,
                                onClick = { selectedLevel = level },
                                label = { Text(level.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MedicalTealPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Input Text
                    OutlinedTextField(
                        value = inputMedicalText,
                        onValueChange = { inputMedicalText = it },
                        label = { Text("Clinical Note / Discharge Summary / Diagnosis") },
                        placeholder = { Text("Enter physician notes, pathology, or treatment regimen to adapt for patient...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )

                    // Generate Button
                    Button(
                        onClick = { onGenerate(inputMedicalText, selectedLanguage, selectedLevel) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isGenerating && inputMedicalText.isNotBlank()
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synthesizing Patient Guide...", fontSize = 13.sp)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Patient Leaflet (${selectedLanguage.displayName})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Leaflet Display Card
                    if (generatedLeaflet != null) {
                        val leaflet = generatedLeaflet

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Header row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = leaflet.title,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0284C7)
                                        )
                                        Text(
                                            text = "${leaflet.language.flagEmoji} ${leaflet.language.displayName} · ${leaflet.readingLevel.label}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row {
                                        // Share HTML / Print
                                        IconButton(
                                            onClick = {
                                                val html = DocumentExportHelper.buildPatientLeafletHtml(leaflet)
                                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/html"
                                                    putExtra(Intent.EXTRA_SUBJECT, leaflet.title)
                                                    putExtra(Intent.EXTRA_TEXT, html)
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "Share Leaflet"))
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = MedicalBluePrimary)
                                        }

                                        // Insert into document
                                        Button(
                                            onClick = { onInsertLeaflet(leaflet.toMarkdownBlock()) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Insert into Doc", fontSize = 11.sp)
                                        }
                                    }
                                }

                                // Condition
                                Text("ℹ️ About Your Condition:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(leaflet.conditionSummary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 16.sp)

                                // Red flags in alert box
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = ClinicalRed.copy(alpha = 0.08f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ClinicalRed.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("🚨 When to Seek Immediate Emergency Care:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ClinicalRed)
                                        leaflet.emergencyRedFlags.forEach { flag ->
                                            Text("• $flag", fontSize = 11.sp, color = ClinicalRed, modifier = Modifier.padding(top = 2.dp))
                                        }
                                    }
                                }

                                // Treatment & Meds
                                Text("💊 Medication Instructions:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                leaflet.medicationInstructions.forEach { med ->
                                    Text("• $med", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
                                }

                                // Questions for Doctor
                                Text("❓ Questions to Ask Your Doctor at Follow-Up:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                leaflet.questionsForDoctor.forEach { q ->
                                    Text("• $q", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
