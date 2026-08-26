package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ControlPoint
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.AnatomicalCalloutPin
import com.example.data.model.AnnotatedFigureData
import com.example.data.model.AnnotationToolType
import com.example.data.model.CanvasStroke
import com.example.data.model.LesionMeasurement
import com.example.ui.theme.ClinicalAmber
import com.example.ui.theme.ClinicalGreen
import com.example.ui.theme.ClinicalRed
import com.example.ui.theme.MedicalBluePrimary
import com.example.ui.theme.MedicalTealPrimary
import java.util.UUID

@Composable
fun MedicalImageAnnotationDialog(
    initialImageUri: String = "",
    initialTitle: String = "Chest X-Ray Diagnostic Annotation",
    initialModality: String = "Radiography / X-Ray",
    onDismiss: () -> Unit,
    onInsertFigure: (AnnotatedFigureData) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var modality by remember { mutableStateOf(initialModality) }
    var activeImageUri by remember { mutableStateOf(if (initialImageUri.isNotBlank()) initialImageUri else "https://images.unsplash.com/photo-1516549655169-df83a0774514?w=900") }
    var clinicalDescription by remember { mutableStateOf("Well-demarcated lesion noted in right middle lobe margin. No pleural effusion or pneumothorax.") }

    var selectedTool by remember { mutableStateOf(AnnotationToolType.PEN) }
    var selectedColor by remember { mutableStateOf(0xFFDC2626) } // Red for lesion
    var strokeWidth by remember { mutableStateOf(6f) }

    // Canvas Draw state
    val strokes = remember { mutableStateListOf<CanvasStroke>() }
    val currentPoints = remember { mutableStateListOf<Offset>() }
    val pins = remember { mutableStateListOf<AnatomicalCalloutPin>() }
    val measurements = remember { mutableStateListOf<LesionMeasurement>() }

    var showPinLabelDialog by remember { mutableStateOf(false) }
    var pendingPinPosition by remember { mutableStateOf(Offset.Zero) }
    var pinLabelText by remember { mutableStateOf("") }

    var isComparisonMode by remember { mutableStateOf(false) }
    var comparisonImageUri by remember { mutableStateOf("https://images.unsplash.com/photo-1530497610245-94d3c16cda28?w=900") }
    var comparisonTitle by remember { mutableStateOf("Normal Baseline Reference") }

    val presetImages = listOf(
        Triple("Chest X-Ray (AP)", "Radiography / X-Ray", "https://images.unsplash.com/photo-1516549655169-df83a0774514?w=900"),
        Triple("Brain MRI (Axial T2)", "MRI Scan", "https://images.unsplash.com/photo-1530497610245-94d3c16cda28?w=900"),
        Triple("12-Lead ECG Strip", "Electrophysiology", "https://images.unsplash.com/photo-1576091160550-2173dba999ef?w=900"),
        Triple("Histopathology Slide", "Microscopy / Biopsy", "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=900")
    )

    val clinicalColors = listOf(
        0xFFDC2626 to "Lesion / Vascular (Red)",
        0xFFEAB308 to "Warning / Nerve (Yellow)",
        0xFF2563EB to "Fluid / Edema (Blue)",
        0xFF16A34A to "Normal Margin (Green)",
        0xFFFFFFFF to "Measurement (White)"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Brush, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Radiology & Clinical Image Canvas", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Interactive lesion marker, numbered anatomical pins & side-by-side comparison", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Main Studio Body
                Row(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Left Tool Palette
                    Column(
                        modifier = Modifier
                            .width(220.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Annotation Tools", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        // Tools Selector
                        AnnotationToolChip(label = "Freehand Pen", icon = Icons.Default.Edit, isSelected = selectedTool == AnnotationToolType.PEN) { selectedTool = AnnotationToolType.PEN }
                        AnnotationToolChip(label = "Highlighter", icon = Icons.Default.Highlight, isSelected = selectedTool == AnnotationToolType.HIGHLIGHTER) { selectedTool = AnnotationToolType.HIGHLIGHTER }
                        AnnotationToolChip(label = "Numbered Pin Callout", icon = Icons.Default.ControlPoint, isSelected = selectedTool == AnnotationToolType.CALLOUT_PIN) { selectedTool = AnnotationToolType.CALLOUT_PIN }
                        AnnotationToolChip(label = "Measurement Line", icon = Icons.Default.Straighten, isSelected = selectedTool == AnnotationToolType.MEASUREMENT) { selectedTool = AnnotationToolType.MEASUREMENT }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Clinical Palette", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        // Color selection
                        clinicalColors.forEach { (colorHex, name) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedColor = colorHex }
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(Color(colorHex), CircleShape)
                                        .border(if (selectedColor == colorHex) 2.dp else 0.5.dp, if (selectedColor == colorHex) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name, fontSize = 10.sp, fontWeight = if (selectedColor == colorHex) FontWeight.Bold else FontWeight.Normal)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            OutlinedButton(
                                onClick = {
                                    if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                                    else if (pins.isNotEmpty()) pins.removeAt(pins.lastIndex)
                                    else if (measurements.isNotEmpty()) measurements.removeAt(measurements.lastIndex)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Undo", fontSize = 10.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    strokes.clear()
                                    pins.clear()
                                    measurements.clear()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Clear", fontSize = 10.sp)
                            }
                        }

                        // Presets
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Sample Clinical Scans", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        presetImages.forEach { (name, mod, uri) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        activeImageUri = uri
                                        title = "$name Diagnostic Annotation"
                                        modality = mod
                                    },
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(mod, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Toggle Split Comparison Mode
                        OutlinedButton(
                            onClick = { isComparisonMode = !isComparisonMode },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isComparisonMode) Color(0xFF1E293B) else Color.Transparent,
                                contentColor = if (isComparisonMode) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Compare, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isComparisonMode) "Exit Split View" else "Side-by-Side View", fontSize = 10.sp)
                        }
                    }

                    // Center & Right: Canvas + Metadata
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        // Title and Insert Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Figure Title & Modality") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    val figData = AnnotatedFigureData(
                                        baseImageUri = activeImageUri,
                                        title = title,
                                        modality = modality,
                                        strokes = strokes.toList(),
                                        pins = pins.toList(),
                                        measurements = measurements.toList(),
                                        clinicalDescription = clinicalDescription,
                                        comparisonImageUri = if (isComparisonMode) comparisonImageUri else null,
                                        comparisonTitle = if (isComparisonMode) comparisonTitle else null
                                    )
                                    onInsertFigure(figData)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Insert into Doc", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Canvas Area (Split screen if isComparisonMode)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color.Black, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            if (!isComparisonMode) {
                                // Single Canvas Mode
                                InteractiveAnnotationCanvas(
                                    imageUri = activeImageUri,
                                    selectedTool = selectedTool,
                                    selectedColor = selectedColor,
                                    strokeWidth = strokeWidth,
                                    strokes = strokes,
                                    currentPoints = currentPoints,
                                    pins = pins,
                                    measurements = measurements,
                                    onAddPin = { pos ->
                                        pendingPinPosition = pos
                                        pinLabelText = "Finding #${pins.size + 1}"
                                        showPinLabelDialog = true
                                    }
                                )
                            } else {
                                // Side-by-Side Split Mode
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                        InteractiveAnnotationCanvas(
                                            imageUri = activeImageUri,
                                            selectedTool = selectedTool,
                                            selectedColor = selectedColor,
                                            strokeWidth = strokeWidth,
                                            strokes = strokes,
                                            currentPoints = currentPoints,
                                            pins = pins,
                                            measurements = measurements,
                                            onAddPin = { pos ->
                                                pendingPinPosition = pos
                                                pinLabelText = "Finding #${pins.size + 1}"
                                                showPinLabelDialog = true
                                            }
                                        )
                                        Text(
                                            text = "Active Pathology / Case",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(Color.White))

                                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                        AsyncImage(
                                            model = comparisonImageUri,
                                            contentDescription = "Comparison Normal Scan",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                        Text(
                                            text = comparisonTitle,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Clinical Findings Caption
                        OutlinedTextField(
                            value = clinicalDescription,
                            onValueChange = { clinicalDescription = it },
                            label = { Text("Clinical Findings & Radiologist Impression Note") },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                        )
                    }
                }
            }
        }
    }

    // Pin Label Dialog
    if (showPinLabelDialog) {
        Dialog(onDismissRequest = { showPinLabelDialog = false }) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Label Anatomical Pin #${pins.size + 1}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = pinLabelText,
                        onValueChange = { pinLabelText = it },
                        label = { Text("Structure or Lesion Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showPinLabelDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            pins.add(
                                AnatomicalCalloutPin(
                                    pinNumber = pins.size + 1,
                                    position = pendingPinPosition,
                                    label = pinLabelText.ifBlank { "Pin #${pins.size + 1}" },
                                    colorHex = selectedColor
                                )
                            )
                            showPinLabelDialog = false
                        }) { Text("Place Pin") }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveAnnotationCanvas(
    imageUri: String,
    selectedTool: AnnotationToolType,
    selectedColor: Long,
    strokeWidth: Float,
    strokes: MutableList<CanvasStroke>,
    currentPoints: MutableList<Offset>,
    pins: MutableList<AnatomicalCalloutPin>,
    measurements: MutableList<LesionMeasurement>,
    onAddPin: (Offset) -> Unit
) {
    var measureStart by remember { mutableStateOf<Offset?>(null) }
    var measureEnd by remember { mutableStateOf<Offset?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Medical Scan Base",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedTool, selectedColor) {
                    when (selectedTool) {
                        AnnotationToolType.PEN, AnnotationToolType.HIGHLIGHTER -> {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPoints.clear()
                                    currentPoints.add(offset)
                                },
                                onDrag = { change, _ ->
                                    currentPoints.add(change.position)
                                },
                                onDragEnd = {
                                    if (currentPoints.size > 1) {
                                        strokes.add(
                                            CanvasStroke(
                                                points = currentPoints.toList(),
                                                colorHex = selectedColor,
                                                strokeWidth = if (selectedTool == AnnotationToolType.HIGHLIGHTER) 18f else strokeWidth,
                                                isHighlighter = selectedTool == AnnotationToolType.HIGHLIGHTER
                                            )
                                        )
                                    }
                                    currentPoints.clear()
                                }
                            )
                        }
                        AnnotationToolType.CALLOUT_PIN -> {
                            detectTapGestures { offset ->
                                onAddPin(offset)
                            }
                        }
                        AnnotationToolType.MEASUREMENT -> {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    measureStart = offset
                                    measureEnd = offset
                                },
                                onDrag = { change, _ ->
                                    measureEnd = change.position
                                },
                                onDragEnd = {
                                    if (measureStart != null && measureEnd != null) {
                                        val dx = measureEnd!!.x - measureStart!!.x
                                        val dy = measureEnd!!.y - measureStart!!.y
                                        val pixelDist = kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
                                        val estimatedMm = pixelDist * 0.15 // calibration factor
                                        measurements.add(
                                            LesionMeasurement(
                                                start = measureStart!!,
                                                end = measureEnd!!,
                                                measuredValueMm = estimatedMm,
                                                colorHex = selectedColor
                                            )
                                        )
                                    }
                                    measureStart = null
                                    measureEnd = null
                                }
                            )
                        }
                        else -> {}
                    }
                }
        ) {
            // Draw completed strokes
            strokes.forEach { stroke ->
                if (stroke.points.size > 1) {
                    val path = Path().apply {
                        moveTo(stroke.points.first().x, stroke.points.first().y)
                        for (i in 1 until stroke.points.size) {
                            lineTo(stroke.points[i].x, stroke.points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = Color(stroke.colorHex).copy(alpha = if (stroke.isHighlighter) 0.45f else 0.9f),
                        style = Stroke(
                            width = stroke.strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Draw active stroke
            if (currentPoints.size > 1) {
                val path = Path().apply {
                    moveTo(currentPoints.first().x, currentPoints.first().y)
                    for (i in 1 until currentPoints.size) {
                        lineTo(currentPoints[i].x, currentPoints[i].y)
                    }
                }
                drawPath(
                    path = path,
                    color = Color(selectedColor).copy(alpha = if (selectedTool == AnnotationToolType.HIGHLIGHTER) 0.45f else 0.9f),
                    style = Stroke(
                        width = if (selectedTool == AnnotationToolType.HIGHLIGHTER) 18f else strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Draw measurements
            measurements.forEach { m ->
                drawLine(
                    color = Color(m.colorHex),
                    start = m.start,
                    end = m.end,
                    strokeWidth = 3f
                )
                drawCircle(color = Color(m.colorHex), radius = 5f, center = m.start)
                drawCircle(color = Color(m.colorHex), radius = 5f, center = m.end)
            }

            // Active measurement in progress
            if (measureStart != null && measureEnd != null) {
                drawLine(
                    color = Color(selectedColor),
                    start = measureStart!!,
                    end = measureEnd!!,
                    strokeWidth = 3f
                )
            }

            // Draw pins
            pins.forEach { pin ->
                drawCircle(
                    color = Color(pin.colorHex),
                    radius = 12f,
                    center = pin.position
                )
                drawCircle(
                    color = Color.White,
                    radius = 10f,
                    center = pin.position
                )
            }
        }

        // Overlay Pin Badges with Text
        pins.forEach { pin ->
            Box(
                modifier = Modifier
                    .padding(start = (pin.position.x - 12).coerceAtLeast(0f).dp, top = (pin.position.y - 12).coerceAtLeast(0f).dp)
                    .size(24.dp)
                    .background(Color(pin.colorHex), CircleShape)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${pin.pinNumber}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AnnotationToolChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MedicalBluePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MedicalBluePrimary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MedicalBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MedicalBluePrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
