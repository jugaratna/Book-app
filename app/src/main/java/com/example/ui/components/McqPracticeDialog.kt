package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.McqItem
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalRed
import com.example.ui.theme.MedicalBluePrimary

@Composable
fun McqPracticeView(
    mcqs: List<McqItem>,
    modifier: Modifier = Modifier
) {
    if (mcqs.isEmpty()) {
        Box(modifier = modifier.padding(24.dp), contentAlignment = Alignment.Center) {
            Text("No MCQs generated. Use 'Generate MCQs' in the AI panel.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BOARD EXAMINATION QUESTION BANK",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${mcqs.size} Questions",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        mcqs.forEachIndexed { qIndex, mcq ->
            val userSelected = selectedAnswers[qIndex]
            val isAnswered = userSelected != null

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Question Header
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(MedicalBluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Q${qIndex + 1}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = mcq.question,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Options A, B, C, D
                    mcq.options.forEachIndexed { optIndex, optionText ->
                        val isThisOptionSelected = userSelected == optIndex
                        val isCorrectOption = optIndex == mcq.correctIndex

                        val (bgColor, borderColor, textColor) = when {
                            !isAnswered -> Triple(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.onSurface
                            )
                            isCorrectOption -> Triple(
                                ClinicalGreen.copy(alpha = 0.15f),
                                ClinicalGreen,
                                Color(0xFF065F46)
                            )
                            isThisOptionSelected && !isCorrectOption -> Triple(
                                ClinicalRed.copy(alpha = 0.15f),
                                ClinicalRed,
                                Color(0xFF991B1B)
                            )
                            else -> Triple(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isAnswered) {
                                    selectedAnswers[qIndex] = optIndex
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = bgColor,
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val optionLetter = listOf("A", "B", "C", "D").getOrElse(optIndex) { "?" }
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isThisOptionSelected || (isAnswered && isCorrectOption)) borderColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = optionLetter,
                                        color = if (isThisOptionSelected || (isAnswered && isCorrectOption)) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isThisOptionSelected || (isAnswered && isCorrectOption)) FontWeight.Bold else FontWeight.Normal,
                                        color = textColor
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                if (isAnswered) {
                                    if (isCorrectOption) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Correct",
                                            tint = ClinicalGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else if (isThisOptionSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Incorrect",
                                            tint = ClinicalRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Explanation Box
                    AnimatedVisibility(visible = isAnswered) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MedicalBluePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CLINICAL RATIONALE & LEARNING OBJECTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = mcq.explanation,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (mcq.learningObjective.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Objective: ${mcq.learningObjective}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
