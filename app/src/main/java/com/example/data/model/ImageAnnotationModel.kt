package com.example.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class AnnotationToolType(val label: String) {
    PEN("Freehand Pen"),
    HIGHLIGHTER("Highlighter"),
    ARROW("Arrow Pointer"),
    CIRCLE_LESION("Lesion Circle"),
    RECT_ROI("ROI Box"),
    CALLOUT_PIN("Numbered Pin"),
    MEASUREMENT("Measurement Line"),
    TEXT("Text Label")
}

data class CanvasStroke(
    val points: List<Offset>,
    val colorHex: Long = 0xFFDC2626,
    val strokeWidth: Float = 6f,
    val isHighlighter: Boolean = false
)

data class AnatomicalCalloutPin(
    val id: String = UUID.randomUUID().toString(),
    val pinNumber: Int,
    val position: Offset,
    val label: String,
    val colorHex: Long = 0xFFDC2626
)

data class LesionMeasurement(
    val id: String = UUID.randomUUID().toString(),
    val start: Offset,
    val end: Offset,
    val measuredValueMm: Double,
    val label: String = "Lesion Diameter",
    val colorHex: Long = 0xFF2563EB
)

data class RegionOfInterest(
    val id: String = UUID.randomUUID().toString(),
    val topLeft: Offset,
    val size: androidx.compose.ui.geometry.Size,
    val label: String,
    val isCircle: Boolean = false,
    val colorHex: Long = 0xFFEAB308
)

data class AnnotatedFigureData(
    val id: String = UUID.randomUUID().toString(),
    val baseImageUri: String,
    val title: String,
    val modality: String, // X-Ray, CT, MRI, ECG, Histology, Pathology, Derm
    val strokes: List<CanvasStroke> = emptyList(),
    val pins: List<AnatomicalCalloutPin> = emptyList(),
    val measurements: List<LesionMeasurement> = emptyList(),
    val rois: List<RegionOfInterest> = emptyList(),
    val clinicalDescription: String = "",
    val comparisonImageUri: String? = null,
    val comparisonTitle: String? = null
)
