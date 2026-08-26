package com.example.ui.dialogs

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MedicalPresentation
import com.example.data.model.PresentationSlide
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalRed
import com.example.ui.theme.MedicalBluePrimary

@Composable
fun SlideDeckEditorDialog(
    initialPresentation: MedicalPresentation,
    onDismiss: () -> Unit,
    onSaveDeck: (MedicalPresentation) -> Unit,
    onSaveToHub: ((MedicalPresentation) -> Unit)? = null
) {
    var deckTitle by remember { mutableStateOf(initialPresentation.title) }
    var selectedSlideIndex by remember { mutableIntStateOf(0) }

    // Mutable list of slides for in-dialog editing
    val editableSlides = remember {
        mutableStateListOf<PresentationSlide>().apply {
            addAll(initialPresentation.slides)
            if (isEmpty()) {
                add(
                    PresentationSlide(
                        slideNumber = 1,
                        title = "Slide 1: Clinical Overview",
                        subtitle = "Introduction",
                        bulletPoints = listOf("Key clinical presentation points"),
                        clinicalPearl = "High-yield takeaway message.",
                        redFlag = "Diagnostic pitfall to avoid.",
                        visualSuggestion = "Relevant anatomical/radiological diagram",
                        speakerNotes = "Presenter speaking points."
                    )
                )
            }
        }
    }

    val currentSlide = editableSlides.getOrNull(selectedSlideIndex) ?: editableSlides.first()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(16.dp))
                .testTag("slide_deck_editor_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEA580C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Editable PowerPoint Deck Editor",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${editableSlides.size} Slides • Live Edit & Save",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Deck Title Field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = deckTitle,
                        onValueChange = { deckTitle = it },
                        label = { Text("Presentation Deck Title") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("deck_title_input"),
                        singleLine = true
                    )
                }

                // Slide Thumbnails Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(editableSlides) { index, slide ->
                            val isSelected = index == selectedSlideIndex
                            Card(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(65.dp)
                                    .clickable { selectedSlideIndex = index }
                                    .testTag("slide_thumbnail_$index"),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFEA580C) else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Slide ${index + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFEA580C) else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = slide.title.ifBlank { "Untitled" },
                                        fontSize = 9.5.sp,
                                        maxLines = 2,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Add Slide Button
                    IconButton(
                        onClick = {
                            val nextNum = editableSlides.size + 1
                            editableSlides.add(
                                PresentationSlide(
                                    slideNumber = nextNum,
                                    title = "Slide $nextNum: New Clinical Section",
                                    subtitle = "Clinical Subtitle",
                                    bulletPoints = listOf("Point 1", "Point 2"),
                                    clinicalPearl = "",
                                    redFlag = "",
                                    visualSuggestion = "",
                                    speakerNotes = ""
                                )
                            )
                            selectedSlideIndex = editableSlides.size - 1
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEA580C))
                            .testTag("add_slide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Slide",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Divider()

                // Active Slide Editor Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Header of active slide with Delete Slide option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Editing Slide ${selectedSlideIndex + 1} of ${editableSlides.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (editableSlides.size > 1) {
                            OutlinedButton(
                                onClick = {
                                    val idx = selectedSlideIndex
                                    editableSlides.removeAt(idx)
                                    // Re-number slides
                                    editableSlides.forEachIndexed { i, s ->
                                        editableSlides[i] = s.copy(slideNumber = i + 1)
                                    }
                                    selectedSlideIndex = idx.coerceAtMost(editableSlides.size - 1)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ClinicalRed),
                                modifier = Modifier.testTag("delete_slide_button")
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete Slide", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Slide Title
                    OutlinedTextField(
                        value = currentSlide.title,
                        onValueChange = { newTitle ->
                            editableSlides[selectedSlideIndex] = currentSlide.copy(title = newTitle)
                        },
                        label = { Text("Slide Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slide_title_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle
                    OutlinedTextField(
                        value = currentSlide.subtitle,
                        onValueChange = { newSub ->
                            editableSlides[selectedSlideIndex] = currentSlide.copy(subtitle = newSub)
                        },
                        label = { Text("Subtitle / Category") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bullet Points Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.FormatListBulleted, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bullet Points (${currentSlide.bulletPoints.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                val updatedBullets = currentSlide.bulletPoints.toMutableList().apply { add("New bullet point") }
                                editableSlides[selectedSlideIndex] = currentSlide.copy(bulletPoints = updatedBullets)
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add bullet", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    currentSlide.bulletPoints.forEachIndexed { bIndex, bulletText ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = bulletText,
                                onValueChange = { newText ->
                                    val updatedBullets = currentSlide.bulletPoints.toMutableList().apply {
                                        this[bIndex] = newText
                                    }
                                    editableSlides[selectedSlideIndex] = currentSlide.copy(bulletPoints = updatedBullets)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bullet_input_${selectedSlideIndex}_$bIndex"),
                                singleLine = false,
                                maxLines = 3
                            )

                            if (currentSlide.bulletPoints.size > 1) {
                                IconButton(
                                    onClick = {
                                        val updatedBullets = currentSlide.bulletPoints.toMutableList().apply {
                                            removeAt(bIndex)
                                        }
                                        editableSlides[selectedSlideIndex] = currentSlide.copy(bulletPoints = updatedBullets)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Delete bullet", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clinical Pearl
                    OutlinedTextField(
                        value = currentSlide.clinicalPearl,
                        onValueChange = { newPearl ->
                            editableSlides[selectedSlideIndex] = currentSlide.copy(clinicalPearl = newPearl)
                        },
                        label = { Text("Clinical Pearl (High-Yield Takeaway)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = ClinicalAmber)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Red Flag Warning
                    OutlinedTextField(
                        value = currentSlide.redFlag,
                        onValueChange = { newWarning ->
                            editableSlides[selectedSlideIndex] = currentSlide.copy(redFlag = newWarning)
                        },
                        label = { Text("Red Flag / Contraindication") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ClinicalRed)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Visual / Diagram Suggestion
                    OutlinedTextField(
                        value = currentSlide.visualSuggestion,
                        onValueChange = { newVisual ->
                            editableSlides[selectedSlideIndex] = currentSlide.copy(visualSuggestion = newVisual)
                        },
                        label = { Text("Visual Diagram / Radiology Suggestion") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Speaker Notes
                    OutlinedTextField(
                        value = currentSlide.speakerNotes,
                        onValueChange = { newNotes ->
                            editableSlides[selectedSlideIndex] = currentSlide.copy(speakerNotes = newNotes)
                        },
                        label = { Text("Presenter Speaking Notes") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }

                // Bottom Action Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val updatedDeck = MedicalPresentation(
                                    title = deckTitle,
                                    topic = deckTitle,
                                    totalSlides = editableSlides.size,
                                    slides = editableSlides.toList()
                                )
                                onSaveToHub?.invoke(updatedDeck)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_to_hub_ppt_button")
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save to Files Hub", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val updatedDeck = MedicalPresentation(
                                    title = deckTitle,
                                    topic = deckTitle,
                                    totalSlides = editableSlides.size,
                                    slides = editableSlides.toList()
                                )
                                onSaveDeck(updatedDeck)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_deck_changes_button")
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Changes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
