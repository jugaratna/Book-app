package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MedicalBluePrimary

@Composable
fun MedicalTableView(
    markdownTable: String,
    caption: String = "Table: Clinical & Diagnostic Characteristics",
    modifier: Modifier = Modifier
) {
    val (headers, rows) = parseMarkdownTable(markdownTable)
    val horizontalScroll = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TableChart,
                    contentDescription = null,
                    tint = MedicalBluePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = caption,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (headers.isNotEmpty()) {
                Box(modifier = Modifier.horizontalScroll(horizontalScroll)) {
                    Column {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            headers.forEach { header ->
                                Box(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = header,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }

                        // Data Rows
                        rows.forEachIndexed { rowIndex, rowCells ->
                            val isEven = rowIndex % 2 == 0
                            Row(
                                modifier = Modifier
                                    .background(
                                        if (isEven) MaterialTheme.colorScheme.surface
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                headers.indices.forEach { colIndex ->
                                    val cellText = rowCells.getOrNull(colIndex) ?: ""
                                    Box(
                                        modifier = Modifier
                                            .width(180.dp)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = cellText,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = markdownTable,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun parseMarkdownTable(raw: String): Pair<List<String>, List<List<String>>> {
    val lines = raw.lines().map { it.trim() }.filter { it.startsWith("|") && it.endsWith("|") }
    if (lines.size < 2) return Pair(emptyList(), emptyList())

    val headerLine = lines[0]
    val headers = headerLine.split("|")
        .filterIndexed { index, _ -> index > 0 && index < headerLine.split("|").size - 1 }
        .map { it.trim().replace("**", "") }

    val dataLines = lines.drop(1).filter { !it.contains("---") }
    val rows = dataLines.map { line ->
        line.split("|")
            .filterIndexed { index, _ -> index > 0 && index < line.split("|").size - 1 }
            .map { it.trim().replace("**", "") }
    }

    return Pair(headers, rows)
}
