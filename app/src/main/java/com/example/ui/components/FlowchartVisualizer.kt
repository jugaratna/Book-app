package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
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
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary

@Composable
fun FlowchartVisualizer(
    flowchartText: String,
    modifier: Modifier = Modifier
) {
    val steps = parseFlowchartSteps(flowchartText)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MedicalTealPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CLINICAL DECISION ALGORITHM / FLOWCHART",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        steps.forEachIndexed { index, step ->
            val isDecision = step.contains("->") || step.contains("If", ignoreCase = true)
            val isWarning = step.contains("Contraindication", ignoreCase = true) || step.contains("Red Flag", ignoreCase = true)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = when {
                    isWarning -> Color(0xFFFEF2F2)
                    isDecision -> Color(0xFFF0FDF4)
                    else -> MaterialTheme.colorScheme.surface
                },
                shadowElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                        isWarning -> Color(0xFFFCA5A5)
                        isDecision -> ClinicalGreen.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isWarning -> Color(0xFFDC2626)
                                    isDecision -> ClinicalGreen
                                    else -> MedicalBluePrimary
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isDecision) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isWarning) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    if (isDecision) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Decision Point",
                            tint = ClinicalGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    } else if (isWarning) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Next Step",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun parseFlowchartSteps(text: String): List<String> {
    return text.lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .map { line ->
            line.replace(Regex("^\\[STEP\\s*\\w*\\]\\s*"), "")
                .replace(Regex("^\\d+\\.\\s*"), "")
                .replace(Regex("^-\\s*"), "")
        }
}
