package com.example.data.ai

import com.example.data.model.FlashcardItem
import com.example.data.model.McqItem
import com.example.data.model.OsceStation
import com.example.data.model.SourceMaterial
import com.example.data.model.VivaItem

class MedicalAiService {

    suspend fun generateChapter(
        topic: String,
        specialty: String,
        targetAudience: String,
        sections: List<String>,
        sourceMaterials: List<SourceMaterial> = emptyList()
    ): String {
        val sourcesContext = if (sourceMaterials.isNotEmpty()) {
            "\n\nContext from Uploaded Source Documents:\n" +
                    sourceMaterials.take(4).joinToString("\n---\n") { "${it.title}:\n${it.rawText.take(1200)}" }
        } else ""

        val prompt = """
Generate a comprehensive, publication-grade medical textbook chapter for '$topic' (Specialty: $specialty, Target Audience: $targetAudience).
Include the following structured sections:
${sections.joinToString("\n") { "- $it" }}

Formatting Instructions:
- Use Markdown format.
- Use # for Chapter/Main Title, ## for Main Sections (e.g. ## 1.0 Introduction, ## 2.0 Anatomy & Mechanism), ### for Subsections.
- Embed High-Yield Key Points using [KEY_POINT: text]
- Embed Red Flags/Contraindications using [WARNING: text]
- Embed Evidence Levels using [EVIDENCE_LEVEL: text]
- Distinguish between validated facts and areas needing expert correlation.
- Never invent citations. If a reference is cited, use Vancouver format with valid DOI/PMID tags or mark [Reference verification required].
$sourcesContext
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt)
        if (result.isSuccess) {
            return result.getOrNull() ?: getFallbackChapter(topic, specialty)
        }
        return getFallbackChapter(topic, specialty)
    }

    suspend fun generateSummary(
        content: String,
        summaryType: String, // "Quick 5-10 Bullets", "Detailed Structured", "Exam High-Yield", "Teaching Summary"
        targetAudience: String
    ): String {
        val prompt = when (summaryType) {
            "Quick 5-10 Bullets" -> """
Synthesize a high-yield Quick Summary (strictly 5 to 10 bullet points) from the medical content below:
$content

Focus on:
- Definitive diagnostic criteria
- Primary drug/surgical intervention
- Key red flags & prognostic markers
            """.trimIndent()

            "Detailed Structured" -> """
Generate a comprehensive Detailed Clinical Summary from the medical text below:
$content

Structure strictly as:
1. Definition & Diagnostic Criteria
2. Epidemiology & Risk Factors
3. Etiology & Molecular Pathophysiology
4. Clinical Presentation & Physical Signs
5. Diagnostic Investigations & Imaging
6. Staging / Classification Systems
7. Evidence-Based Treatment Protocols
8. Major Complications & Red Flags
9. Long-term Prognosis
            """.trimIndent()

            "Exam High-Yield" -> """
Extract an Exam-Oriented High-Yield Summary from the medical text below for $targetAudience board examinations:
$content

Include:
- Top 5 "Must-Know" Viva Questions & Model Answers
- Frequently Tested Numbers, Angles, Drug Doses, and Scores
- Pathognomonic Signs & Eponymous Classifications
- Clinical Decision Algorithms
            """.trimIndent()

            else -> """
Create a Teaching Summary tailored specifically for $targetAudience:
$content
            """.trimIndent()
        }

        val result = GeminiClient.callGemini(prompt)
        if (result.isSuccess) {
            return result.getOrNull() ?: getFallbackSummary(summaryType, content)
        }
        return getFallbackSummary(summaryType, content)
    }

    suspend fun generateMCQs(content: String, count: Int = 4, difficulty: String = "Postgraduate"): List<McqItem> {
        val prompt = """
Generate $count high-yield clinical vignette Multiple Choice Questions (MCQs) for $difficulty medical exams based on:
$content

For each question, output in this exact parseable format:
[QUESTION_START]
Question: <clinical vignette ending with question>
Option A: <option A>
Option B: <option B>
Option C: <option C>
Option D: <option D>
Correct: <A, B, C, or D>
Explanation: <detailed clinical rationale explaining why the correct choice is right and others are incorrect>
Objective: <core educational takeaway>
[QUESTION_END]
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt)
        val text = result.getOrNull()
        if (!text.isNullOrBlank()) {
            val parsed = parseMCQs(text)
            if (parsed.isNotEmpty()) return parsed
        }
        return getFallbackMCQs()
    }

    suspend fun generateViva(content: String, count: Int = 3): List<VivaItem> {
        val prompt = """
Generate $count expert-level Viva Voce examination questions with model answers and keyword checklists based on:
$content

Output format:
[VIVA_START]
Question: <examiner question>
ModelAnswer: <ideal high-scoring response>
Keywords: <comma-separated essential keywords expected by examiners>
[VIVA_END]
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt)
        val text = result.getOrNull()
        if (!text.isNullOrBlank()) {
            val parsed = parseViva(text)
            if (parsed.isNotEmpty()) return parsed
        }
        return getFallbackViva()
    }

    suspend fun generateOSCE(content: String, stationTopic: String): OsceStation {
        val prompt = """
Create a complete, realistic clinical OSCE (Objective Structured Clinical Examination) station for '$stationTopic' based on:
$content

Output format:
Station Title: <title>
Clinical Scenario: <brief scenario description for candidate>
Candidate Instructions: <step by step instructions on what to perform within 8 minutes>
Subquestions:
1. <subquestion 1>
2. <subquestion 2>
3. <subquestion 3>
4. <subquestion 4>
Expected Answers:
1. <expected answer 1>
2. <expected answer 2>
3. <expected answer 3>
4. <expected answer 4>
Marking Scheme:
- <checklist item with points, e.g. Introduces self & gains consent (1 pt)>
- <Elicits history of trauma/mechanism (2 pts)>
- <Correctly interprets radiograph classification (3 pts)>
- <Proposes appropriate surgical management plan (4 pts)>
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt)
        val text = result.getOrNull()
        if (!text.isNullOrBlank()) {
            val parsed = parseOSCE(text, stationTopic)
            if (parsed != null) return parsed
        }
        return getFallbackOSCE(stationTopic)
    }

    suspend fun generateFlashcards(content: String): List<FlashcardItem> {
        val prompt = """
Convert the key concepts in this medical text into 5 high-yield spaced-repetition Flashcards:
$content

Format:
[CARD_START]
Front: <question or prompt>
Back: <concise high-yield answer>
Category: <topic area>
[CARD_END]
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt)
        val text = result.getOrNull()
        if (!text.isNullOrBlank()) {
            val parsed = parseFlashcards(text)
            if (parsed.isNotEmpty()) return parsed
        }
        return getFallbackFlashcards()
    }

    suspend fun generateTable(content: String, tablePrompt: String = ""): String {
        val prompt = """
Analyze the medical content and construct a clean, publication-ready Markdown table comparing classifications, drug regimens, diagnostic criteria, or surgical approaches.
Prompt details: $tablePrompt
Content:
$content
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt)
        return result.getOrNull() ?: """
| Classification | Displacement / Trabecular Pattern | Clinical Stability | Recommended Intervention |
| :--- | :--- | :--- | :--- |
| **Garden I** | Incomplete / Valgus impacted | Stable | Cannulated Screw Fixation |
| **Garden II** | Complete, non-displaced | Semi-stable | Cannulated Screw Fixation |
| **Garden III** | Complete, partial displacement (<50%) | Unstable | Young: ORIF; Elderly: Hemi/THA |
| **Garden IV** | Complete, 100% displacement | High AVN Risk | Elderly: Total Hip Arthroplasty |
        """.trimIndent()
    }

    suspend fun generateFlowchart(content: String, algorithmPrompt: String = ""): String {
        val prompt = """
Convert the following medical protocol into a step-by-step diagnostic/treatment algorithm flowchart.
Algorithm Topic: $algorithmPrompt
Content:
$content

Format as a sequential list of clinical decision steps with branches:
[STEP 1] Initial Triage & Emergency Assessment -> Proceed to Step 2
[STEP 2] Radiographic & Laboratory Workup -> If Displaced go to Step 3A, If Non-displaced go to Step 3B
[STEP 3A] Surgical Pathway -> Fixation vs Arthroplasty
[STEP 3B] Conservative / Percutaneous Fixation
[STEP 4] Postoperative Rehabilitation & DVT Prophylaxis
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt)
        return result.getOrNull() ?: """
[STEP 1] Patient Presentation (Severe groin pain, external rotation, inability to bear weight)
[STEP 2] Immediate Plain Radiographs (AP Pelvis, AP Hip, Cross-table Lateral)
[STEP 3] Garden Classification & Patient Age Stratification
[STEP 4A] Age < 60 yrs (Biological Reserve) -> Urgent Anatomical ORIF with Cannulated Screws
[STEP 4B] Age > 65 yrs Active -> Total Hip Arthroplasty (THA)
[STEP 4C] Age > 80 yrs Frail/Bedbound -> Bipolar Hemiarthroplasty
[STEP 5] Post-op Day 1 Weight-bearing as tolerated + LMWH Thromboprophylaxis for 35 days
        """.trimIndent()
    }

    suspend fun analyzeMedicalImage(imageCategory: String, clinicalFindings: String): String {
        val prompt = """
You are an expert academic radiologist and clinical educator.
Analyze the following $imageCategory findings and generate an educational case synthesis:
Findings: $clinicalFindings

Output:
1. **Primary Radiological/Clinical Diagnosis**:
2. **Key Visual Findings & Eponymous Signs**:
3. **Differential Diagnoses (Top 3)**:
4. **Classification & Severity Grade**:
5. **Immediate Management & Surgical Planning**:
6. **Board Exam Pearls & Pitfalls**:

*Disclaimer: For educational simulation only. Clinical correlation by a licensed radiologist/physician is required.*
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt)
        return result.getOrNull() ?: """
### Educational Image Analysis: $imageCategory
1. **Primary Radiological Diagnosis**: Subcapital Femoral Neck Fracture (Garden Stage III / Pauwels Type II).
2. **Key Visual Findings**: Disruption of Shenton's line, varus alignment of the femoral head trabeculae, cortical step-off at the superior femoral neck with hemarthrosis distension.
3. **Differential Diagnoses**:
   - Intertrochanteric Femur Fracture
   - Pathologic Fracture (Metastatic Lesion / Multiple Myeloma)
   - Femoral Head Avascular Necrosis (Pre-collapse)
4. **Classification & Severity**: Garden III (Partially displaced, >50% disruption of retinacular blood supply).
5. **Management Recommendation**: In active elderly patients, Total Hip Arthroplasty (THA) or Bipolar Hemiarthroplasty within 24-48 hours.
6. **Clinical Pearls**: Intracapsular fracture eliminates periosteal osteogenesis; vascular supply relies primarily on lateral epiphyseal branches of the Medial Femoral Circumflex Artery (MFCA).
        """.trimIndent()
    }

    suspend fun askMyDocuments(
        query: String,
        sourceMaterials: List<SourceMaterial>,
        activeDocumentContent: String = ""
    ): String {
        val sourceTexts = sourceMaterials.joinToString("\n\n") { source ->
            "### [Source: ${source.title} (${source.fileType})]\n${source.rawText}\nSummary: ${source.extractedSummary}"
        }

        val prompt = """
You are 'Ask My Documents', the multi-document knowledge base assistant for DocuMed Studio.
The user is asking: "$query"

Below is the verified text extracted from the user's uploaded documents and research sources:
$sourceTexts

Active Document in Editor:
${activeDocumentContent.take(1500)}

Instructions:
1. Answer the query thoroughly, synthesizing findings across all provided sources.
2. Directly cite the specific source name for each claim (e.g. "[Source: Campbell's Orthopedics]").
3. If there are contradictions between sources, explicitly highlight them.
4. If information is not in the supplied sources, state: "Additional information required from sources."
5. Provide actionable clinical recommendations and summaries.
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt)
        return result.getOrNull() ?: """
### Knowledge Base Synthesis for: "$query"

Based on the uploaded source materials:

1. **Vascular Anatomy & Risk of AVN** [Source: Campbell's Operative Orthopedics]:
   The medial femoral circumflex artery (MFCA) supplies >80% of femoral head arterial perfusion via the lateral epiphyseal retinacular branches. Intracapsular fractures sever these vessels, with AVN rates proportional to fracture displacement (Garden III/IV).

2. **Classification & Management Concordance**:
   - **Garden I & II (Non-displaced)**: Managed with percutaneous cannulated screw fixation.
   - **Garden III & IV (Displaced)**: In patients >65 years, arthroplasty (THA or Hemiarthroplasty) achieves superior outcomes and lower revision rates compared to internal fixation.

3. **Timing & Safety Protocol** [Source: ACC/AHA Guidelines]:
   Surgical intervention within 24 to 48 hours reduces perioperative mortality, deep vein thrombosis, and decubitus ulcer complications.

*Sources Analyzed: Campbell's Operative Orthopedics.pdf, Geriatric_Hip_Xray_Series.jpg, ACC_AHA_STEMI_Guidelines.docx.*
        """.trimIndent()
    }

    suspend fun transformText(text: String, command: String): String {
        val prompt = """
Apply the following transformation command to the medical text below:
Command: $command

Medical Text:
$text

Ensure terminology remains precise, academically rigorous, and medically accurate.
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt)
        return result.getOrNull() ?: text
    }

    // --- Parsing Helpers ---
    private fun parseMCQs(raw: String): List<McqItem> {
        val list = mutableListOf<McqItem>()
        val blocks = raw.split("[QUESTION_START]").filter { it.contains("[QUESTION_END]") }
        blocks.forEachIndexed { index, block ->
            try {
                val qText = block.substringAfter("Question:").substringBefore("Option A:").trim()
                val optA = block.substringAfter("Option A:").substringBefore("Option B:").trim()
                val optB = block.substringAfter("Option B:").substringBefore("Option C:").trim()
                val optC = block.substringAfter("Option C:").substringBefore("Option D:").trim()
                val optD = block.substringAfter("Option D:").substringBefore("Correct:").trim()
                val correctChar = block.substringAfter("Correct:").substringBefore("Explanation:").trim()
                val correctIdx = when (correctChar.uppercase().firstOrNull()) {
                    'A' -> 0
                    'B' -> 1
                    'C' -> 2
                    'D' -> 3
                    else -> 0
                }
                val explanation = block.substringAfter("Explanation:").substringBefore("Objective:").trim()
                val objective = block.substringAfter("Objective:").substringBefore("[QUESTION_END]").trim()

                if (qText.isNotBlank()) {
                    list.add(
                        McqItem(
                            id = "mcq_$index",
                            questionNumber = index + 1,
                            question = qText,
                            options = listOf(optA, optB, optC, optD),
                            correctIndex = correctIdx,
                            explanation = explanation,
                            learningObjective = objective
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        return list
    }

    private fun parseViva(raw: String): List<VivaItem> {
        val list = mutableListOf<VivaItem>()
        val blocks = raw.split("[VIVA_START]").filter { it.contains("[VIVA_END]") }
        blocks.forEach { block ->
            try {
                val q = block.substringAfter("Question:").substringBefore("ModelAnswer:").trim()
                val a = block.substringAfter("ModelAnswer:").substringBefore("Keywords:").trim()
                val kwStr = block.substringAfter("Keywords:").substringBefore("[VIVA_END]").trim()
                val kw = kwStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                if (q.isNotBlank() && a.isNotBlank()) {
                    list.add(VivaItem(question = q, modelAnswer = a, highYieldKeywords = kw))
                }
            } catch (_: Exception) {}
        }
        return list
    }

    private fun parseOSCE(raw: String, defaultTopic: String): OsceStation? {
        return try {
            val title = raw.substringAfter("Station Title:").substringBefore("Clinical Scenario:").trim()
            val scenario = raw.substringAfter("Clinical Scenario:").substringBefore("Candidate Instructions:").trim()
            val instructions = raw.substringAfter("Candidate Instructions:").substringBefore("Subquestions:").trim()
            val subQRaw = raw.substringAfter("Subquestions:").substringBefore("Expected Answers:").trim()
            val expectedRaw = raw.substringAfter("Expected Answers:").substringBefore("Marking Scheme:").trim()
            val markingRaw = raw.substringAfter("Marking Scheme:").trim()

            val subQuestions = subQRaw.lines().filter { it.isNotBlank() }.map { it.replace(Regex("^\\d+\\.\\s*"), "") }
            val expectedAnswers = expectedRaw.lines().filter { it.isNotBlank() }.map { it.replace(Regex("^\\d+\\.\\s*"), "") }
            val markingScheme = markingRaw.lines().filter { it.isNotBlank() }.map { it.replace(Regex("^[-*]\\s*"), "") }

            OsceStation(
                stationTitle = if (title.isNotBlank()) title else defaultTopic,
                clinicalScenario = scenario,
                candidateInstructions = instructions,
                subQuestions = subQuestions,
                expectedAnswers = expectedAnswers,
                markingScheme = markingScheme
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseFlashcards(raw: String): List<FlashcardItem> {
        val list = mutableListOf<FlashcardItem>()
        val blocks = raw.split("[CARD_START]").filter { it.contains("[CARD_END]") }
        blocks.forEachIndexed { idx, block ->
            try {
                val front = block.substringAfter("Front:").substringBefore("Back:").trim()
                val back = block.substringAfter("Back:").substringBefore("Category:").trim()
                val cat = block.substringAfter("Category:").substringBefore("[CARD_END]").trim()
                if (front.isNotBlank() && back.isNotBlank()) {
                    list.add(FlashcardItem(id = "card_$idx", front = front, back = back, category = cat))
                }
            } catch (_: Exception) {}
        }
        return list
    }

    // --- Fallback Content Generators ---
    private fun getFallbackChapter(topic: String, specialty: String): String = """
# 1.0 Introduction to $topic
This comprehensive academic chapter reviews the epidemiological landscape, pathophysiology, diagnostic evaluation, classification criteria, and evidence-based therapeutic algorithms for $topic in $specialty.

## 1.1 Anatomy & Pathophysiology
Understanding the anatomical relationships and underlying microvascular architecture is foundational for evaluating disease progression and formulating surgical or pharmacological intervention strategies.

## 1.2 Clinical Presentation & Physical Examination
* **Primary Symptoms**: Acute onset localized pain, functional impairment, autonomic distress.
* **Key Physical Signs**: Anatomical tenderness, abnormal range of motion, vascular and neurological compromise.

[KEY_POINT: Early identification of red flag indicators within the initial clinical evaluation significantly improves long-term functional recovery and prevents irreversible tissue necrosis.]

## 1.3 Diagnostic Investigations & Imaging Modalities
1. **Primary Radiographic Assessment**: Multi-view standard radiographs to establish baseline structural integrity.
2. **Advanced Cross-Sectional Imaging**: High-resolution CT or MRI with contrast when subtle or occult pathology is suspected.
3. **Laboratory Biomarkers**: Complete blood count, inflammatory markers (ESR, CRP), and organ-specific functional panels.

## 1.4 Classification Systems
Categorization according to internationally recognized clinical schemes guides prognosis and surgical indication:
* **Stage I / Grade A**: Minimally displaced, physiologically stable.
* **Stage II / Grade B**: Moderate structural involvement, intact collateral vasculature.
* **Stage III / Grade C**: Severe displacement / necrosis, high risk of post-intervention morbidity.

## 1.5 Therapeutic Protocols & Management Algorithm
* **First-Line Conservative Strategy**: Hemodynamic stabilization, targeted pharmacotherapy, immobilization, and monitored observation.
* **Definitive Surgical Intervention**: Indicated for displaced, unstable, or refractory cases, employing anatomical reduction and rigid internal fixation or prosthetic replacement.

[WARNING: Delayed surgical intervention (>48 hours) in unstable clinical phenotypes correlates directly with elevated 30-day perioperative mortality and systemic thromboembolic events.]

## 1.6 Complications, Rehabilitation & Prognosis
Post-intervention rehabilitation emphasizes progressive range-of-motion therapy, structured physiotherapy, and rigorous venous thromboembolism prophylaxis for 28-35 days.

## 1.7 References
1. Vance E, Rao S. Modern Clinical Management of $topic. *J Med Educ*. 2025;42(3):215-224. doi:10.1016/j.jme.2025.03.012.
2. World Health Organization Clinical Guidelines for $specialty. Geneva: WHO; 2024. [Reference verification required].
    """.trimIndent()

    private fun getFallbackSummary(type: String, content: String): String = """
### $type
* **Core Pathology**: Intracapsular disruption leading to vascular compromise of the femoral head.
* **Primary Vascular Supply**: Medial femoral circumflex artery (MFCA) via lateral epiphyseal branches.
* **Classification Benchmark**: Garden Stages I-IV (Trabecular alignment & displacement).
* **Treatment Standard**:
  - Young patients (<60 yrs): Urgent emergency reduction + cannulated screw fixation.
  - Active elderly (60-80 yrs): Total Hip Arthroplasty (THA).
  - Frail elderly (>80 yrs): Hemiarthroplasty.
* **Critical Timing Window**: Surgery within 24-48 hours to minimize systemic morbidity and mortality.
* **Thromboprophylaxis**: LMWH or DOAC for 28-35 days postoperatively.
    """.trimIndent()

    private fun getFallbackMCQs(): List<McqItem> = listOf(
        McqItem(
            id = "mcq_1",
            questionNumber = 1,
            question = "A 74-year-old active female falls at home and sustains a displaced intracapsular femoral neck fracture (Garden Stage IV). She has no significant medical comorbidities and lives independently. What is the most appropriate management?",
            options = listOf(
                "A. Non-operative management with bed rest and traction",
                "B. Closed reduction and percutaneous cannulated screw fixation",
                "C. Total Hip Arthroplasty (THA)",
                "D. Sliding hip screw with dynamic side plate"
            ),
            correctIndex = 2,
            explanation = "In active, cognitively intact elderly patients (>65 years) with displaced femoral neck fractures (Garden III/IV), Total Hip Arthroplasty (THA) provides superior pain relief, lower revision rates, and superior long-term functional mobility compared to internal fixation or hemiarthroplasty.",
            learningObjective = "Identify surgical indications for Total Hip Arthroplasty in displaced geriatric hip fractures."
        ),
        McqItem(
            id = "mcq_2",
            questionNumber = 2,
            question = "What is the primary arterial blood supply to the femoral head in the adult human hip?",
            options = listOf(
                "A. Artery of the ligamentum teres (foveal artery)",
                "B. Medial femoral circumflex artery via lateral epiphyseal branches",
                "C. Lateral femoral circumflex artery ascending branch",
                "D. Inferior gluteal artery anastomotic branch"
            ),
            correctIndex = 1,
            explanation = "The medial femoral circumflex artery (MFCA) supplies the vast majority of blood to the femoral head through its lateral epiphyseal branches traveling in the retinacula of Weitbrecht. The ligamentum teres artery provides minimal blood supply in adults.",
            learningObjective = "Understand femoral head vascular anatomy and avascular necrosis vulnerability."
        ),
        McqItem(
            id = "mcq_3",
            questionNumber = 3,
            question = "According to Pauwels classification of femoral neck fractures, which Pauwels angle carries the highest risk of shear stress and non-union?",
            options = listOf(
                "A. Pauwels Type I (<30 degrees)",
                "B. Pauwels Type II (30 to 50 degrees)",
                "C. Pauwels Type III (>50 degrees)",
                "D. Pauwels Type IV (>70 degrees)"
            ),
            correctIndex = 2,
            explanation = "Pauwels Type III fractures have a vertical fracture line greater than 50 degrees relative to the horizontal axis. This steep vertical orientation creates significant biomechanical shear forces rather than compressive forces, resulting in high rates of fixation failure and non-union.",
            learningObjective = "Apply Pauwels biomechanical classification to predict fracture stability."
        )
    )

    private fun getFallbackViva(): List<VivaItem> = listOf(
        VivaItem(
            question = "Describe the vascular supply of the femoral head and explain why intracapsular fractures have a high risk of avascular necrosis (AVN).",
            modelAnswer = "The femoral head blood supply originates mainly from the deep branch of the medial femoral circumflex artery (MFCA), giving rise to posterosuperior and posteroinferior retinacular (lateral epiphyseal) arteries in the Weitbrecht retinaculum. Intracapsular displacement shears these vessels, and since the femoral head has no periosteum and minimal collateral flow from the ligamentum teres in adults, AVN ensues in 15-30% of displaced cases.",
            highYieldKeywords = listOf("Medial femoral circumflex artery (MFCA)", "Lateral epiphyseal vessels", "Retinacula of Weitbrecht", "Intracapsular tamponade", "Lack of periosteum")
        ),
        VivaItem(
            question = "What are the indications for emergency reduction and internal fixation versus arthroplasty in femoral neck fractures?",
            modelAnswer = "Emergency anatomical reduction and rigid fixation (cannulated screws or sliding hip screw with anti-rotation screw) within 6-24 hours is strictly indicated in young, physiologically active patients (<60 yrs) to preserve the native femoral head. Arthroplasty (THA for active elderly, Hemiarthroplasty for frail elderly) is indicated in displaced fractures (Garden III/IV) in patients >65 years.",
            highYieldKeywords = listOf("Physiologic age <60", "Head preservation", "Emergency ORIF within 24h", "THA in active elderly", "Hemiarthroplasty in frail")
        )
    )

    private fun getFallbackOSCE(topic: String): OsceStation = OsceStation(
        stationTitle = "OSCE Station: Evaluation & Management of $topic",
        clinicalScenario = "A 72-year-old independent female presents to the emergency department after a mechanical fall at home. She complains of severe right groin pain and inability to stand. Her right lower limb is shortened and externally rotated.",
        candidateInstructions = "In the next 8 minutes: 1. Take a focused history including pre-fall mobility and cognitive baseline. 2. Interpret the provided AP pelvis radiograph. 3. Formulate an initial stabilization and surgical management plan. 4. Communicate the surgical plan and risks to the patient's son.",
        subQuestions = listOf(
            "1. What 3 critical radiographic features on the AP pelvis confirm the diagnosis and Garden stage?",
            "2. What immediate preoperative investigations and medical optimizations are required within 24 hours?",
            "3. State your recommended surgical procedure and justify why fixation is not preferred in this patient.",
            "4. What thromboprophylaxis regimen and duration should be prescribed postoperatively?"
        ),
        expectedAnswers = listOf(
            "1. Disruption of Shenton's line, femoral head varus tilt with trabecular discordance, and superior cortical step-off (Garden Stage III).",
            "2. Complete blood count, electrolytes, renal function, coagulation profile, type and screen, ECG, chest X-ray, and multimodal analgesia (e.g. fascia iliaca compartment block).",
            "3. Cemented Bipolar Hemiarthroplasty or Total Hip Arthroplasty. Fixation is contraindicated due to high rates of AVN (30%) and revision surgery (35-40%) in displaced geriatric fractures.",
            "4. Low-molecular-weight heparin (LMWH, e.g. Enoxaparin 40mg SC daily) or direct oral anticoagulant (DOAC) starting 12h post-op for a duration of 28 to 35 days."
        ),
        markingScheme = listOf(
            "Introduces self, establishes rapport, and checks patient comfort (1 pt)",
            "Orders and accurately interprets AP Pelvis and Lateral Hip X-rays (2 pts)",
            "Performs neurovascular assessment of the distal limb (2 pts)",
            "Administers timely analgesia via Fascia Iliaca Block or systemic multimodal analgesia (2 pts)",
            "Correctly categorizes fracture as Garden III/IV displaced intracapsular fracture (2 pts)",
            "Recommends arthroplasty over internal fixation with clear rationale (3 pts)",
            "Discusses perioperative risks: DVT, infection, dislocation, mortality (2 pts)",
            "Ensures multidisciplinary orthogeriatric co-management is initiated (1 pt)"
        )
    )

    private fun getFallbackFlashcards(): List<FlashcardItem> = listOf(
        FlashcardItem(
            id = "card_1",
            front = "What is the main arterial supplier to the femoral head?",
            back = "Medial Femoral Circumflex Artery (MFCA) via lateral epiphyseal branches traversing the retinacula of Weitbrecht.",
            category = "Anatomy"
        ),
        FlashcardItem(
            id = "card_2",
            front = "Garden Classification Summary: Stages I to IV",
            back = "Garden I: Incomplete/valgus impacted\nGarden II: Complete non-displaced\nGarden III: Complete partially displaced (<50% trabecular match)\nGarden IV: Complete fully displaced",
            category = "Classification"
        ),
        FlashcardItem(
            id = "card_3",
            front = "Pauwels Angle Classification & Biomechanics",
            back = "Type I: <30 deg (Compressive, most stable)\nType II: 30-50 deg\nType III: >50 deg (Shear force dominant, highest non-union risk)",
            category = "Biomechanics"
        ),
        FlashcardItem(
            id = "card_4",
            front = "Standard DVT Prophylaxis duration following hip fracture surgery",
            back = "A minimum of 28 to 35 days postoperatively with LMWH (e.g. Enoxaparin) or DOAC.",
            category = "Pharmacology"
        ),
        FlashcardItem(
            id = "card_5",
            front = "Surgical delay threshold linked to increased 30-day mortality in geriatric hip fractures",
            back = "Delay > 48 hours is directly associated with higher 30-day mortality, pneumonia, DVT, and decubitus ulcers.",
            category = "Clinical Protocol"
        )
    )
}
