package com.example.data.model

import java.util.UUID

enum class CitationStyle(val displayName: String, val description: String) {
    VANCOUVER("Vancouver (ICMJE)", "Standard numbered style used in biomedical journals (e.g., Lancet, NEJM)."),
    APA_7("APA 7th Edition", "Author-date format used in clinical psychology and behavioral health."),
    AMA_11("AMA 11th Edition", "American Medical Association numbered superscript standard (JAMA Network)."),
    HARVARD("Harvard Medical", "Parenthetical author-date style for academic medical dissertations."),
    CHICAGO_AUTHOR_DATE("Chicago (Author-Date)", "Author-date format favored by academic health science publishers."),
    NLM("NLM / PubMed", "National Library of Medicine standard indexing format with PMIDs."),
    IEEE("IEEE Biomedical", "Numbered bracket format for biomedical engineering and health informatics.")
}

enum class CitationSourceType(val displayName: String) {
    JOURNAL_ARTICLE("Peer-Reviewed Journal Article"),
    CLINICAL_GUIDELINE("Clinical Practice Guideline"),
    SYSTEMATIC_REVIEW("Systematic Review & Meta-Analysis"),
    BOOK_CHAPTER("Medical Textbook / Monograph"),
    RANDOMIZED_TRIAL("Randomized Controlled Trial (RCT)"),
    WEB_RESOURCE("Clinical Database (UpToDate / WHO / CDC)")
}

data class CitationEntry(
    val id: String = UUID.randomUUID().toString(),
    val citationNumber: Int = 1,
    val title: String,
    val authors: String, // e.g. "Smith JA, Davis RB, Patel MK"
    val journalOrPublisher: String, // e.g. "N Engl J Med", "Lancet", "Elsevier"
    val year: String, // e.g. "2023"
    val volume: String = "",
    val issue: String = "",
    val pages: String = "",
    val doi: String = "",
    val pmid: String = "",
    val url: String = "",
    val sourceType: CitationSourceType = CitationSourceType.JOURNAL_ARTICLE,
    val evidenceLevel: String = "Level 1A (High-Quality RCT/Review)",
    val notes: String = ""
) {
    fun getInTextKey(style: CitationStyle): String {
        return when (style) {
            CitationStyle.VANCOUVER -> "[$citationNumber]"
            CitationStyle.AMA_11 -> "[$citationNumber]"
            CitationStyle.IEEE -> "[$citationNumber]"
            CitationStyle.APA_7, CitationStyle.HARVARD, CitationStyle.CHICAGO_AUTHOR_DATE -> {
                val primaryAuthor = authors.split(",").firstOrNull()?.trim() ?: "Author"
                val lastName = primaryAuthor.split(" ").firstOrNull() ?: primaryAuthor
                if (authors.contains(",") || authors.contains(";")) {
                    "($lastName et al., $year)"
                } else {
                    "($lastName, $year)"
                }
            }
            CitationStyle.NLM -> "[$citationNumber]"
        }
    }

    fun format(style: CitationStyle): String {
        return when (style) {
            CitationStyle.VANCOUVER -> {
                val volIssue = buildString {
                    if (volume.isNotBlank()) append(volume)
                    if (issue.isNotBlank()) append("($issue)")
                    if (pages.isNotBlank()) append(":$pages")
                }
                val doiPart = if (doi.isNotBlank()) " doi: $doi." else ""
                "$citationNumber. $authors. $title. $journalOrPublisher. $year;$volIssue.$doiPart"
            }
            CitationStyle.APA_7 -> {
                val volIssue = buildString {
                    if (volume.isNotBlank()) append(volume)
                    if (issue.isNotBlank()) append("($issue)")
                    if (pages.isNotBlank()) append(", $pages")
                }
                val doiPart = if (doi.isNotBlank()) " https://doi.org/$doi" else if (url.isNotBlank()) " $url" else ""
                "$authors ($year). $title. _${journalOrPublisher}_, $volIssue.$doiPart"
            }
            CitationStyle.AMA_11 -> {
                val volIssue = buildString {
                    if (volume.isNotBlank()) append(volume)
                    if (issue.isNotBlank()) append("($issue)")
                    if (pages.isNotBlank()) append(":$pages")
                }
                val doiPart = if (doi.isNotBlank()) " doi:$doi" else ""
                "$citationNumber. $authors. $title. _${journalOrPublisher}_. $year;$volIssue.$doiPart"
            }
            CitationStyle.HARVARD -> {
                val volIssue = buildString {
                    if (volume.isNotBlank()) append(", $volume")
                    if (issue.isNotBlank()) append("($issue)")
                    if (pages.isNotBlank()) append(", pp. $pages")
                }
                val doiPart = if (doi.isNotBlank()) " Available at: https://doi.org/$doi" else ""
                "$authors ($year) '$title', _${journalOrPublisher}_$volIssue.$doiPart"
            }
            CitationStyle.CHICAGO_AUTHOR_DATE -> {
                val volIssue = buildString {
                    if (volume.isNotBlank()) append(" $volume")
                    if (issue.isNotBlank()) append(", no. $issue")
                    if (pages.isNotBlank()) append(": $pages")
                }
                val doiPart = if (doi.isNotBlank()) " https://doi.org/$doi." else ""
                "$authors. $year. \"$title.\" _${journalOrPublisher}_$volIssue.$doiPart"
            }
            CitationStyle.NLM -> {
                val pmidPart = if (pmid.isNotBlank()) " PMID: $pmid." else ""
                val doiPart = if (doi.isNotBlank()) " DOI: $doi." else ""
                "$citationNumber. $authors. $title. $journalOrPublisher. $year;$volume($issue):$pages.$pmidPart$doiPart"
            }
            CitationStyle.IEEE -> {
                val doiPart = if (doi.isNotBlank()) ", doi: $doi." else "."
                "[$citationNumber] $authors, \"$title,\" _${journalOrPublisher}_, vol. $volume, no. $issue, pp. $pages, $year$doiPart"
            }
        }
    }
}

object PredefinedMedicalCitations {
    val sampleCitations = listOf(
        CitationEntry(
            citationNumber = 1,
            title = "Pathophysiology and management of acute coronary syndromes: Clinical practice guidelines",
            authors = "Collet JP, Thiele H, Barbato E, Barthélémy O",
            journalOrPublisher = "Eur Heart J",
            year = "2021",
            volume = "42",
            issue = "14",
            pages = "1289-1367",
            doi = "10.1093/eurheartj/ehaa575",
            pmid = "32860005",
            sourceType = CitationSourceType.CLINICAL_GUIDELINE,
            evidenceLevel = "Level 1A (Clinical Guideline)",
            notes = "Definitive European Society of Cardiology guideline on NSTE-ACS."
        ),
        CitationEntry(
            citationNumber = 2,
            title = "Surviving Sepsis Campaign: International Guidelines for Management of Sepsis and Septic Shock 2021",
            authors = "Evans L, Rhodes A, Alhazzani W, Antonelli M",
            journalOrPublisher = "Crit Care Med",
            year = "2021",
            volume = "49",
            issue = "11",
            pages = "e1063-e1143",
            doi = "10.1097/CCM.0000000000005337",
            pmid = "34605781",
            sourceType = CitationSourceType.CLINICAL_GUIDELINE,
            evidenceLevel = "Level 1A (International Consensus)",
            notes = "Standard sepsis resuscitation bundle (1-hour and 3-hour resuscitation metrics)."
        ),
        CitationEntry(
            citationNumber = 3,
            title = "Harrison's Principles of Internal Medicine, 21st Edition",
            authors = "Loscalzo J, Fauci A, Kasper D, Hauser S, Longo D, Jameson JL",
            journalOrPublisher = "McGraw-Hill Education",
            year = "2022",
            volume = "1 & 2",
            pages = "1-4100",
            doi = "10.1036/0071802150",
            sourceType = CitationSourceType.BOOK_CHAPTER,
            evidenceLevel = "Standard Academic Reference",
            notes = "Foundational reference for postgraduate internal medicine and diagnostic workups."
        ),
        CitationEntry(
            citationNumber = 4,
            title = "Advanced Trauma Life Support (ATLS®): The Ninth and Tenth Editions",
            authors = "American College of Surgeons Committee on Trauma",
            journalOrPublisher = "J Trauma Acute Care Surg",
            year = "2023",
            volume = "74",
            issue = "5",
            pages = "1363-1366",
            doi = "10.1097/TA.0b013e31828b82f5",
            sourceType = CitationSourceType.CLINICAL_GUIDELINE,
            evidenceLevel = "Level 1A (Trauma Protocol)",
            notes = "Standard ABCDE primary and secondary trauma resuscitation."
        ),
        CitationEntry(
            citationNumber = 5,
            title = "Global Initiative for Chronic Obstructive Lung Disease 2024 Report: GOLD Executive Summary",
            authors = "Agustí A, Celli BR, Criner GJ, Halpin D",
            journalOrPublisher = "Am J Respir Crit Care Med",
            year = "2024",
            volume = "209",
            issue = "7",
            pages = "805-827",
            doi = "10.1164/rccm.202312-2188PP",
            pmid = "38416972",
            sourceType = CitationSourceType.CLINICAL_GUIDELINE,
            evidenceLevel = "Level 1A (GOLD Standard)",
            notes = "ABCD assessment tool and triple-therapy inhalation escalation pathways."
        ),
        CitationEntry(
            citationNumber = 6,
            title = "Efficacy and Safety of SGLT2 Inhibitors in Patients with Heart Failure: A Systematic Review and Meta-Analysis",
            authors = "Zannad F, Ferreira JP, Pocock SJ, Anker SD",
            journalOrPublisher = "Lancet",
            year = "2023",
            volume = "396",
            issue = "10254",
            pages = "819-829",
            doi = "10.1016/S0140-6736(20)31824-9",
            pmid = "32877652",
            sourceType = CitationSourceType.SYSTEMATIC_REVIEW,
            evidenceLevel = "Level 1A (Meta-Analysis)",
            notes = "Seminal meta-analysis establishing SGLT2i as the 4th pillar in HFrEF management."
        )
    )
}
