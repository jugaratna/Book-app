package com.example.ui.dialogs

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalRed
import com.example.ui.theme.MedicalBluePrimary
import com.example.util.MedicalVoiceDictationManager

@Composable
fun VoiceDictationDialog(
    initialText: String = "",
    onDismiss: () -> Unit,
    onInsertText: (dictatedText: String) -> Unit,
    onSynthesizeWithAi: ((dictatedText: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val dictationManager = remember { MedicalVoiceDictationManager(context) }

    val isListening by dictationManager.isListening.collectAsState()
    val recognizedText by dictationManager.spokenText.collectAsState()
    val rmsLevel by dictationManager.rmsDbLevel.collectAsState()
    val errorMessage by dictationManager.errorMessage.collectAsState()

    var manualText by remember { mutableStateOf(initialText) }
    var hasInitialized by remember { mutableStateOf(false) }
    var showCommandsHelp by remember { mutableStateOf(false) }

    // Sync speech manager text
    if (!hasInitialized && initialText.isNotBlank()) {
        dictationManager.setInitialText(initialText)
        hasInitialized = true
    }

    if (recognizedText.isNotBlank() && recognizedText != manualText) {
        manualText = recognizedText
    }

    // Microphone permission launcher
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            dictationManager.startListening()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice dictation", Toast.LENGTH_LONG).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            dictationManager.stopListening()
        }
    }

    // Pulsing animation for mic indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(
        onDismissRequest = {
            dictationManager.stopListening()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
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
                                .background(if (isListening) ClinicalRed.copy(alpha = 0.15f) else MedicalBluePrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = null,
                                tint = if (isListening) ClinicalRed else MedicalBluePrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Medical Voice Dictation",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isListening) "Listening... (Punctuation & clinical terms auto-formatted)" else "Hands-free clinical scribe & speech-to-text",
                                fontSize = 12.sp,
                                color = if (isListening) ClinicalRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = {
                        dictationManager.stopListening()
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Microphone Control & Audio Waveform Hero Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isListening) ClinicalRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Mic Button with Pulse Ring
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isListening) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .scale(pulseScale)
                                        .background(ClinicalRed.copy(alpha = 0.25f), CircleShape)
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = if (isListening) ClinicalRed else MedicalBluePrimary,
                                modifier = Modifier
                                    .size(62.dp)
                                    .clickable {
                                        if (isListening) {
                                            dictationManager.stopListening()
                                        } else {
                                            if (hasMicPermission) {
                                                dictationManager.startListening()
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    }
                                    .testTag("toggle_voice_dictation_btn"),
                                tonalElevation = 6.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = "Toggle Mic",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isListening) "Tap to Stop Recording" else "Tap Microphone to Start Speaking",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isListening) ClinicalRed else MaterialTheme.colorScheme.onSurface
                        )

                        // Real-time Audio Level Bars
                        if (isListening) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(9) { index ->
                                    val barHeight = ((rmsLevel * 3.5f) + ((index % 3) * 4f)).coerceIn(4f, 26f)
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(barHeight.dp)
                                            .background(ClinicalRed, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ClinicalRed.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            modifier = Modifier.padding(10.dp),
                            fontSize = 12.sp,
                            color = ClinicalRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Voice Commands Cheat Sheet Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCommandsHelp = !showCommandsHelp }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MedicalBluePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showCommandsHelp) "Hide Voice Commands Guide" else "View Medical Voice Commands Guide",
                            fontSize = 12.sp,
                            color = MedicalBluePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                AnimatedVisibility(visible = showCommandsHelp) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Try Speaking These Commands:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• 'Period' or 'Comma' -> Inserts . or ,", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("• 'New line' / 'New paragraph' -> Line breaks", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("• 'Bullet point' -> Starts bullet list (-)", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("• 'Clinical Pearl' -> Inserts clinical pearl blockquote", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("• 'Red Flag' -> Inserts warning alert blockquote", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("• 'Vital Signs' / 'Differential Diagnosis' -> Auto-headers", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Dictated / Transcribed Text Field
                OutlinedTextField(
                    value = manualText,
                    onValueChange = { manualText = it },
                    label = { Text("Transcribed Clinical Text (Editable)") },
                    placeholder = { Text("Speak clearly into microphone... Your dictated clinical observations, examination findings, and plans will appear here.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("dictated_text_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                dictationManager.clearBuffer()
                                manualText = ""
                            },
                            enabled = manualText.isNotBlank()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear", tint = ClinicalRed)
                        }

                        IconButton(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Dictated Clinical Note", manualText)
                                cm.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied dictated text to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            enabled = manualText.isNotBlank()
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onSynthesizeWithAi != null) {
                            OutlinedButton(
                                onClick = {
                                    dictationManager.stopListening()
                                    onSynthesizeWithAi(manualText)
                                    onDismiss()
                                },
                                enabled = manualText.isNotBlank()
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI Scribe", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                dictationManager.stopListening()
                                onInsertText(manualText)
                                onDismiss()
                            },
                            enabled = manualText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                            modifier = Modifier.testTag("insert_dictated_text_btn")
                        ) {
                            Text("Insert into Doc", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
