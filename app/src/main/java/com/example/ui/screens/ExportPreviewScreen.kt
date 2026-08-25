package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.DocumentExportHelper
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.DocuMedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportPreviewScreen(
    viewModel: DocuMedViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val selectedDoc by viewModel.selectedDocument.collectAsState()
    val content by viewModel.editorContent.collectAsState()

    var citationStyle by remember { mutableStateOf("Vancouver (NLM / PubMed)") }
    var expandedCitation by remember { mutableStateOf(false) }

    val citationStyles = listOf(
        "Vancouver (NLM / PubMed)",
        "American Medical Association (AMA)",
        "APA 7th Edition",
        "Harvard Medical Reference"
    )

    if (selectedDoc == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Select a document to preview and export", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.navigateTo(AppNavTab.LIBRARY) }) {
                    Text("Go to Document Library")
                }
            }
        }
        return
    }

    val currentDoc = selectedDoc!!.copy(content = content)
    val toc = remember(content) { DocumentExportHelper.generateTableOfContents(content) }
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize()) {
        // Top Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.navigateTo(AppNavTab.EDITOR) }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Editor")
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Publication Preview & Export",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${currentDoc.title.take(30)}...",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Export Format Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export to Word
                    Button(
                        onClick = {
                            try {
                                DocumentExportHelper.shareDocument(context, currentDoc, "DOCX")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Exporting Word doc: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Word (.docx)", fontSize = 12.sp)
                    }

                    // Export to PDF / HTML
                    Button(
                        onClick = {
                            try {
                                DocumentExportHelper.shareDocument(context, currentDoc, "HTML")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Sharing HTML: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PDF / Print", fontSize = 12.sp)
                    }

                    // Copy Text
                    FilledTonalButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(currentDoc.content))
                            Toast.makeText(context, "Copied document to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Citation Style Selector
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Citation Style:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Box(modifier = Modifier.width(220.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedCitation,
                        onExpandedChange = { expandedCitation = !expandedCitation }
                    ) {
                        OutlinedTextField(
                            value = citationStyle,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCitation) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            textStyle = TextStyle(fontSize = 11.sp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCitation,
                            onDismissRequest = { expandedCitation = false }
                        ) {
                            citationStyles.forEach { style ->
                                DropdownMenuItem(
                                    text = { Text(style, fontSize = 12.sp) },
                                    onClick = {
                                        citationStyle = style
                                        expandedCitation = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Printable / Publication Render Canvas (A4 Page Mockup)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Header Block
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFE0F2FE)
                        ) {
                            Text(
                                text = "${currentDoc.docType} · ${currentDoc.specialty} · ${currentDoc.targetAudience}",
                                color = Color(0xFF0369A1),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "DocuMed Academic Series",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentDoc.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0C4A6E),
                            lineHeight = 30.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Authors: ${currentDoc.authors} | Institution: ${currentDoc.institution}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "Version: v${currentDoc.version} · Word Count: ${currentDoc.wordCount} words · Date: ${currentDoc.getFormattedDate()}",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFF0284C7)))
                    Spacer(modifier = Modifier.height(18.dp))

                    // Table of Contents Box
                    if (toc.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "TABLE OF CONTENTS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        letterSpacing = 0.5.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                toc.forEach { item ->
                                    val startPadding = when (item.level) {
                                        1 -> 0.dp
                                        2 -> 16.dp
                                        else -> 32.dp
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .padding(start = startPadding),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${item.number} ${item.title}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (item.level == 1) FontWeight.Bold else FontWeight.Normal,
                                                color = if (item.level == 1) Color(0xFF0F172A) else Color(0xFF334155)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Main Content Render
                    RenderRichDocumentContent(currentDoc.content)

                    Spacer(modifier = Modifier.height(30.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0)))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Prepared and verified using DocuMed Studio · Academic Medical Co-Pilot · Citation Style: $citationStyle",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8), textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
