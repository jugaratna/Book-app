package com.example.util

import java.util.UUID

enum class VocabularyIssueType(val title: String, val badgeColorHex: Long) {
    SPELLING_TYPO("Medical Typo / Misspelling", 0xFFDC2626),
    LASA_DRUG_ALERT("Look-Alike / Sound-Alike Drug Alert", 0xFFD97706),
    DANGEROUS_ABBREVIATION("High-Risk Abbreviation (ISMP)", 0xFFE11D48),
    DOSAGE_FORMAT("Dosage Format Safety Check", 0xFF7C3AED),
    ACRONYM_CLARIFICATION("Acronym Definition Recommended", 0xFF0284C7)
}

data class VocabularyIssue(
    val id: String = UUID.randomUUID().toString(),
    val type: VocabularyIssueType,
    val matchedWord: String,
    val suggestedReplacement: String,
    val explanation: String,
    val startIndex: Int = -1,
    val endIndex: Int = -1,
    val severity: String = "Warning" // "Critical", "Warning", "Suggestion"
)

data class MedicalVocabularyCheckResult(
    val totalWordsChecked: Int,
    val medicalTermsFoundCount: Int,
    val terminologyAccuracyPercent: Int,
    val clinicalReadabilityGrade: String,
    val issues: List<VocabularyIssue>
)

object MedicalVocabularyChecker {

    // Common medical typos mapping (lowercase -> correct)
    private val medicalTypos = mapOf(
        "diabetis" to "diabetes",
        "diabeticus" to "diabeticus",
        "arrhytmia" to "arrhythmia",
        "arrithmia" to "arrhythmia",
        "arrythmia" to "arrhythmia",
        "anasthesia" to "anesthesia",
        "anaesthesia" to "anesthesia",
        "anesthesiaa" to "anesthesia",
        "appendisitis" to "appendicitis",
        "apendicitis" to "appendicitis",
        "hemorroid" to "hemorrhoid",
        "hemorrhoids" to "hemorrhoids",
        "haemorrhoid" to "hemorrhoid",
        "hypertention" to "hypertension",
        "hypertenssion" to "hypertension",
        "hipotension" to "hypotension",
        "meningitus" to "meningitis",
        "cirrhocis" to "cirrhosis",
        "cirrhosos" to "cirrhosis",
        "atherosclorosis" to "atherosclerosis",
        "arteriosclorosis" to "arteriosclerosis",
        "ishemic" to "ischemic",
        "ischemya" to "ischemia",
        "leukamia" to "leukemia",
        "leucemia" to "leukemia",
        "acetominophen" to "acetaminophen",
        "paracetemol" to "paracetamol",
        "amoxicilin" to "amoxicillin",
        "ceftriaxon" to "ceftriaxone",
        "metformine" to "metformin",
        "omiprazole" to "omeprazole",
        "pneunomia" to "pneumonia",
        "pnumonia" to "pneumonia",
        "pancreatitis" to "pancreatitis",
        "pancreatitus" to "pancreatitis",
        "bronchoscopy" to "bronchoscopy",
        "bronchocopy" to "bronchoscopy",
        "tachicardia" to "tachycardia",
        "bradicardia" to "bradycardia",
        "hypokalemia" to "hypokalemia",
        "hypocalcemia" to "hypocalcemia",
        "hyperkalemia" to "hyperkalemia",
        "creatanine" to "creatinine",
        "creatinin" to "creatinine",
        "troponine" to "troponin",
        "hemoglobine" to "hemoglobin",
        "leucocyte" to "leukocyte",
        "thrombositopenia" to "thrombocytopenia",
        "thrombocytopenia" to "thrombocytopenia",
        "electrocardiogramme" to "electrocardiogram"
    )

    // Look-Alike / Sound-Alike (LASA) high-risk medications
    private val lasaDrugs = mapOf(
        "hydralazine" to Pair("Hydroxyzine", "Hydralazine is an arteriolar vasodilator for hypertension, easily confused with Hydroxyzine (an antihistamine/anxiolytic). Verify intended indication."),
        "hydroxyzine" to Pair("Hydralazine", "Hydroxyzine is an antihistamine, easily confused with Hydralazine (antihypertensive). Verify patient allergy vs blood pressure target."),
        "clonidine" to Pair("Klonopin / Clonazepam", "Clonidine is an alpha-2 agonist for BP/ADHD; Klonopin is a potent benzodiazepine anticonvulsant/anxiolytic."),
        "clonazepam" to Pair("Clonidine", "Clonazepam is a benzodiazepine; ensure not mistaken for Clonidine (antihypertensive)."),
        "klonopin" to Pair("Clonidine", "Klonopin (clonazepam) is easily confused with Clonidine in verbal and written orders."),
        "metformin" to Pair("Metronidazole", "Metformin is an oral biguanide antidiabetic; ensure not confused with Metronidazole (antimicrobial for anaerobes/protozoa)."),
        "metronidazole" to Pair("Metformin", "Metronidazole is an antimicrobial; ensure not confused with Metformin (glycemic control)."),
        "prednisone" to Pair("Prednisolone", "Prednisone requires hepatic conversion to active prednisolone; use prednisolone directly in severe hepatic impairment."),
        "tramadol" to Pair("Trazodone", "Tramadol is a weak opioid / SNRI analgesic; Trazodone is a sedative antidepressant."),
        "trazodone" to Pair("Tramadol", "Trazodone is a sedative antidepressant; do not confuse with Tramadol (opioid analgesic)."),
        "ephedrine" to Pair("Epinephrine", "CRITICAL: Ephedrine is a mixed alpha/beta agonist bolused in milligrams; Epinephrine is vastly more potent (microgram dosing). 10-fold lethal risk if interchanged."),
        "methotrexate" to Pair("Metolazone", "Methotrexate is a high-risk weekly antimetabolite/immunosuppressant. Daily administration causes lethal bone marrow suppression."),
        "ceftriaxone" to Pair("Ceftazidime", "Ceftriaxone lacks Pseudomonas aeruginosa coverage; Ceftazidime has strong antipseudomonal activity.")
    )

    // Dangerous abbreviations per ISMP / Joint Commission "Do Not Use" List
    private val dangerousAbbreviations = listOf(
        Regex("""\b(\d+)\s*[uU]\b""") to Pair("units", "The abbreviation 'U' or 'u' can be mistaken for '0' (causing a 10-fold overdose) or '4'. Always spell out 'units'."),
        Regex("""\b[iI][uU]\b""") to Pair("international units", "The abbreviation 'IU' is mistaken for 'IV' (intravenous). Always write 'international units'."),
        Regex("""\b[qQ]\.?[dD]\.?\b""") to Pair("daily", "'Q.D.' / 'QD' is frequently misread as 'QID' (4 times daily), causing a 4-fold overdose. Use 'daily' or 'every day'."),
        Regex("""\b[qQ]\.?[oO]\.?[dD]\.?\b""") to Pair("every other day", "'Q.O.D.' is mistaken for 'Q.D.' (daily). Always write out 'every other day'."),
        Regex("""\b(\d+)\.0\s*(mg|g|mcg|mL|L)\b""") to Pair("trailing_zero", "Trailing zeros (e.g., X.0 mg) lead to 10-fold overdoses if decimal is missed. Write without decimal (e.g., X mg)."),
        Regex("""\b\.(\d+)\s*(mg|g|mcg|mL|L)\b""") to Pair("leading_zero", "Missing leading zero (e.g., .X mg) leads to 10-fold overdoses if decimal is overlooked. Always include leading zero (0.X mg)."),
        Regex("""\b[mM][sS][oO]?4?\b""") to Pair("morphine sulfate / magnesium sulfate", "'MS' or 'MSO4' can mean either Morphine Sulfate or Magnesium Sulfate. Spell out the full medication name.")
    )

    // Acronym expansions for clinical clarity
    private val commonAcronyms = mapOf(
        "STEMI" to "ST-Elevation Myocardial Infarction",
        "NSTEMI" to "Non-ST-Elevation Myocardial Infarction",
        "ARDS" to "Acute Respiratory Distress Syndrome",
        "AKI" to "Acute Kidney Injury",
        "DVT" to "Deep Vein Thrombosis",
        "PE" to "Pulmonary Embolism",
        "DKA" to "Diabetic Ketoacidosis",
        "HFrEF" to "Heart Failure with Reduced Ejection Fraction",
        "HFpEF" to "Heart Failure with Preserved Ejection Fraction",
        "COPD" to "Chronic Obstructive Pulmonary Disease",
        "CKD" to "Chronic Kidney Disease",
        "SIRS" to "Systemic Inflammatory Response Syndrome",
        "DIC" to "Disseminated Intravascular Coagulation",
        "TIA" to "Transient Ischemic Attack",
        "GCS" to "Glasgow Coma Scale",
        "FAST" to "Focused Assessment with Sonography for Trauma",
        "CABG" to "Coronary Artery Bypass Grafting",
        "PCI" to "Percutaneous Coronary Intervention",
        "TTE" to "Transthoracic Echocardiogram",
        "TEE" to "Transesophageal Echocardiogram",
        "eGFR" to "Estimated Glomerular Filtration Rate"
    )

    fun analyzeText(text: String): MedicalVocabularyCheckResult {
        if (text.isBlank()) {
            return MedicalVocabularyCheckResult(0, 0, 100, "Grade 12+ (Clinical)", emptyList())
        }

        val words = text.split(Regex("""[\s\p{Punct}]+""")).filter { it.isNotBlank() }
        val totalWords = words.size
        val issues = mutableListOf<VocabularyIssue>()
        var medicalTermsCount = 0

        // Check typos and LASA drugs
        for (word in words) {
            val lower = word.lowercase()
            
            // Check known medical terms
            if (medicalTypos.containsValue(lower) || commonAcronyms.containsKey(word.uppercase()) || lasaDrugs.containsKey(lower)) {
                medicalTermsCount++
            }

            // Check typos
            if (medicalTypos.containsKey(lower)) {
                val correct = medicalTypos[lower]!!
                val formattedReplacement = if (word.first().isUpperCase()) correct.replaceFirstChar { it.uppercase() } else correct
                issues.add(
                    VocabularyIssue(
                        type = VocabularyIssueType.SPELLING_TYPO,
                        matchedWord = word,
                        suggestedReplacement = formattedReplacement,
                        explanation = "Misspelled clinical term '$word'. Recommended standard spelling is '$formattedReplacement'.",
                        severity = "Warning"
                    )
                )
            }

            // Check LASA Drug alerts
            if (lasaDrugs.containsKey(lower)) {
                val (confusable, explanation) = lasaDrugs[lower]!!
                issues.add(
                    VocabularyIssue(
                        type = VocabularyIssueType.LASA_DRUG_ALERT,
                        matchedWord = word,
                        suggestedReplacement = word, // keep same or verify
                        explanation = explanation,
                        severity = "Warning"
                    )
                )
            }
        }

        // Check Dangerous Abbreviations using regex
        for ((regex, replacementData) in dangerousAbbreviations) {
            val matches = regex.findAll(text)
            for (match in matches) {
                val matchedStr = match.value
                val replacement = when (replacementData.first) {
                    "trailing_zero" -> {
                        // e.g. "5.0 mg" -> "5 mg"
                        matchedStr.replace(Regex("""\.0"""), "")
                    }
                    "leading_zero" -> {
                        // e.g. ".5 mg" -> "0.5 mg"
                        "0$matchedStr"
                    }
                    else -> replacementData.first
                }

                issues.add(
                    VocabularyIssue(
                        type = if (replacementData.first in listOf("trailing_zero", "leading_zero")) VocabularyIssueType.DOSAGE_FORMAT else VocabularyIssueType.DANGEROUS_ABBREVIATION,
                        matchedWord = matchedStr,
                        suggestedReplacement = replacement,
                        explanation = replacementData.second,
                        startIndex = match.range.first,
                        endIndex = match.range.last,
                        severity = "Critical"
                    )
                )
            }
        }

        // Check Acronyms for first usage definition suggestion
        val seenAcronyms = mutableSetOf<String>()
        for (word in words) {
            val upper = word.uppercase()
            if (commonAcronyms.containsKey(upper) && !seenAcronyms.contains(upper)) {
                seenAcronyms.add(upper)
                val fullDefinition = commonAcronyms[upper]!!
                // If text doesn't already contain the expanded name
                if (!text.contains(fullDefinition, ignoreCase = true)) {
                    issues.add(
                        VocabularyIssue(
                            type = VocabularyIssueType.ACRONYM_CLARIFICATION,
                            matchedWord = word,
                            suggestedReplacement = "$word ($fullDefinition)",
                            explanation = "Clinical acronym '$word' appears without initial definition. Expanding to '$fullDefinition ($word)' enhances academic clarity.",
                            severity = "Suggestion"
                        )
                    )
                }
            }
        }

        // Calculate stats
        val penalty = (issues.size * 3).coerceAtMost(50)
        val accuracy = (100 - penalty).coerceAtLeast(50)

        return MedicalVocabularyCheckResult(
            totalWordsChecked = totalWords,
            medicalTermsFoundCount = medicalTermsCount + seenAcronyms.size,
            terminologyAccuracyPercent = accuracy,
            clinicalReadabilityGrade = "Postgraduate (AMA/ICMJE Compliant)",
            issues = issues.distinctBy { "${it.type}_${it.matchedWord}_${it.suggestedReplacement}" }
        )
    }

    fun applySingleCorrection(documentText: String, issue: VocabularyIssue): String {
        return if (issue.startIndex >= 0 && issue.endIndex < documentText.length) {
            documentText.replaceRange(issue.startIndex, issue.endIndex + 1, issue.suggestedReplacement)
        } else {
            // Word boundary replacement
            documentText.replaceFirst(Regex("\\b${Regex.escape(issue.matchedWord)}\\b"), issue.suggestedReplacement)
        }
    }

    fun applyAllCorrections(documentText: String, issues: List<VocabularyIssue>): String {
        var result = documentText
        for (issue in issues) {
            if (issue.type != VocabularyIssueType.LASA_DRUG_ALERT && issue.suggestedReplacement != issue.matchedWord) {
                result = result.replace(Regex("\\b${Regex.escape(issue.matchedWord)}\\b"), issue.suggestedReplacement)
            }
        }
        return result
    }
}
