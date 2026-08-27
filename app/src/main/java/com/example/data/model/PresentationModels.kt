package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PresentationSlide(
    @Json(name = "slideNumber") val slideNumber: Int = 1,
    @Json(name = "title") val title: String = "",
    @Json(name = "subtitle") val subtitle: String = "",
    @Json(name = "bulletPoints") val bulletPoints: List<String> = emptyList(),
    @Json(name = "clinicalPearl") val clinicalPearl: String = "",
    @Json(name = "redFlag") val redFlag: String = "",
    @Json(name = "visualSuggestion") val visualSuggestion: String = "",
    @Json(name = "speakerNotes") val speakerNotes: String = ""
)

@JsonClass(generateAdapter = true)
data class MedicalPresentation(
    @Json(name = "title") val title: String = "",
    @Json(name = "presenter") val presenter: String = "",
    @Json(name = "audience") val audience: String = "",
    @Json(name = "slides") val slides: List<PresentationSlide> = emptyList(),
    @Json(name = "generatedAt") val generatedAt: Long = System.currentTimeMillis()
)
