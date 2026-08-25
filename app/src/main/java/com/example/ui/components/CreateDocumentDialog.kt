package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MedicalBluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDocumentDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, type: String, specialty: String, audience: String) -> Unit,
    onNavigateToAi: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("Orthopedics") }
    var docType by remember { mutableStateOf("Textbook Chapter") }
    var audience by remember { mutableStateOf("Postgraduate") }

    var expandedSpecialty by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    var expandedAudience by remember { mutableStateOf(false) }

    val specialties = listOf("Orthopedics", "Cardiology", "Neurology", "General Surgery", "Pediatrics", "Internal Medicine", "Oncology", "Radiology", "Pathology")
    val docTypes = listOf("Textbook Chapter", "Clinical Protocol", "Case Report", "Lecture Note", "Question Bank", "OSCE Guide")
    val audiences = listOf("Undergraduate Medical Student", "Postgraduate Resident", "Fellow / Specialist", "Paramedical / Nursing", "Patient Education")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.NoteAdd, contentDescription = null, tint = MedicalBluePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New Medical Document",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // AI Generator Shortcut Callout
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Prefer AI to draft the entire chapter?",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Use AI Studio with 17 structured clinical sections.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        FilledTonalButton(onClick = onNavigateToAi) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Draft")
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document / Chapter Title") },
                    placeholder = { Text("e.g. Supracondylar Humerus Fracture in Children") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Specialty Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedSpecialty,
                    onExpandedChange = { expandedSpecialty = !expandedSpecialty }
                ) {
                    OutlinedTextField(
                        value = specialty,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Specialty") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSpecialty) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSpecialty,
                        onDismissRequest = { expandedSpecialty = false }
                    ) {
                        specialties.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    specialty = item
                                    expandedSpecialty = false
                                }
                            )
                        }
                    }
                }

                // Document Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = !expandedType }
                ) {
                    OutlinedTextField(
                        value = docType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Document Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        docTypes.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    docType = item
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                // Target Audience Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedAudience,
                    onExpandedChange = { expandedAudience = !expandedAudience }
                ) {
                    OutlinedTextField(
                        value = audience,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Audience") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAudience) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAudience,
                        onDismissRequest = { expandedAudience = false }
                    ) {
                        audiences.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    audience = item
                                    expandedAudience = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onCreate(
                                title.ifBlank { "Untitled $docType" },
                                docType,
                                specialty,
                                audience
                            )
                        }
                    ) {
                        Text("Create Document")
                    }
                }
            }
        }
    }
}
