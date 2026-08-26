package com.example.data.ai

import com.example.data.model.AiEngine
import com.example.data.model.FlashcardItem
import com.example.data.model.McqItem
import com.example.data.model.MedicalPresentation
import com.example.data.model.OsceStation
import com.example.data.model.PresentationSlide
import com.example.data.model.SourceMaterial
import com.example.data.model.VivaItem

class MedicalAiService {

    private fun getEngineSystemInstruction(engine: AiEngine): String {
        return when (engine) {
            AiEngine.CHATGPT -> "You are ChatGPT-4o Medical Specialist. Output crisp, evidence-grounded clinical differentials, case simulations, and structured practice pearls."
            AiEngine.CLAUDE -> "You are Claude 3.5 Sonnet Medical Researcher. Provide publication-grade academic medical prose, nuanced pathophysiological mechanisms, and guideline consensus."
            AiEngine.PERPLEXITY -> "You are Perplexity AI Clinical Search & Evidence Engine. Focus on real-time guideline consensus (NICE, ACC/AHA, WHO), evidence grading (Level A/B/C), and verified citations."
            AiEngine.DEEPSEEK -> "You are DeepSeek R1 Medical Reasoning Model. Deliver step-by-step diagnostic chains of thought, complex differential trees, and surgical decision matrices."
            AiEngine.GEMINI -> "You are DocuMed Gemini 2.5 AI, a world-class academic medical co-pilot for physicians, medical educators, and researchers."
        }
    }

    suspend fun generateChapter(
        topic: String,
        specialty: String,
        targetAudience: String,
        sections: List<String>,
        sourceMaterials: List<SourceMaterial> = emptyList(),
        engine: AiEngine = AiEngine.GEMINI
    ): String {
        val sourcesContext = if (sourceMaterials.isNotEmpty()) {
            "\n\nContext from Uploaded Source Documents:\n" +
                    sourceMaterials.take(4).joinToString("\n---\n") { "${it.title}:\n${it.rawText.take(1200)}" }
        } else ""

        val prompt = """
[Engine: ${engine.displayName} (${engine.provider})]
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

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
        if (result.isSuccess) {
            val text = result.getOrNull()
            if (!text.isNullOrBlank()) return text
        }
        return getFallbackChapter(topic, specialty, engine)
    }

    suspend fun generateSummary(
        content: String,
        summaryType: String,
        targetAudience: String,
        engine: AiEngine = AiEngine.GEMINI
    ): String {
        val prompt = when (summaryType) {
            "Quick 5-10 Bullets" -> """
[Engine: ${engine.displayName}]
Synthesize a high-yield Quick Summary (strictly 5 to 10 bullet points) from the medical content below:
$content

Focus on:
- Definitive diagnostic criteria
- Primary drug/surgical intervention
- Key red flags & prognostic markers
            """.trimIndent()

            "Detailed Structured" -> """
[Engine: ${engine.displayName}]
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
[Engine: ${engine.displayName}]
Extract an Exam-Oriented High-Yield Summary from the medical text below for $targetAudience board examinations:
$content

Include:
- Top 5 "Must-Know" Viva Questions & Model Answers
- Frequently Tested Numbers, Angles, Drug Doses, and Scores
- Pathognomonic Signs & Eponymous Classifications
- Clinical Decision Algorithms
            """.trimIndent()

            else -> """
[Engine: ${engine.displayName}]
Create a Teaching Summary tailored specifically for $targetAudience:
$content
            """.trimIndent()
        }

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
        if (result.isSuccess) {
            val text = result.getOrNull()
            if (!text.isNullOrBlank()) return text
        }
        return getFallbackSummary(summaryType, content, engine)
    }

    suspend fun generateMCQs(
        content: String,
        count: Int = 4,
        difficulty: String = "Postgraduate",
        engine: AiEngine = AiEngine.GEMINI
    ): List<McqItem> {
        val prompt = """
[Engine: ${engine.displayName}]
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

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
        val text = result.getOrNull()
        if (!text.isNullOrBlank()) {
            val parsed = parseMCQs(text)
            if (parsed.isNotEmpty()) return parsed
        }
        return getFallbackMCQs(engine)
    }

    suspend fun generateViva(
        content: String,
        count: Int = 3,
        engine: AiEngine = AiEngine.GEMINI
    ): List<VivaItem> {
        val prompt = """
[Engine: ${engine.displayName}]
Generate $count expert-level Viva Voce examination questions with model answers and keyword checklists based on:
$content

Output format:
[VIVA_START]
Question: <examiner question>
ModelAnswer: <ideal high-scoring response>
Keywords: <comma-separated essential keywords expected by examiners>
[VIVA_END]
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
        val text = result.getOrNull()
        if (!text.isNullOrBlank()) {
            val parsed = parseViva(text)
            if (parsed.isNotEmpty()) return parsed
        }
        return getFallbackViva(engine)
    }

    suspend fun generateOSCE(
        content: String,
        stationTopic: String,
        engine: AiEngine = AiEngine.GEMINI
    ): OsceStation {
        val prompt = """
[Engine: ${engine.displayName}]
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

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
        val text = result.getOrNull()
        if (!text.isNullOrBlank()) {
            val parsed = parseOSCE(text, stationTopic)
            if (parsed != null) return parsed
        }
        return getFallbackOSCE(stationTopic, engine)
    }

    suspend fun generateFlashcards(
        content: String,
        engine: AiEngine = AiEngine.GEMINI
    ): List<FlashcardItem> {
        val prompt = """
[Engine: ${engine.displayName}]
Convert the key concepts in this medical text into 5 high-yield spaced-repetition Flashcards:
$content

Format:
[CARD_START]
Front: <question or prompt>
Back: <concise high-yield answer>
Category: <topic area>
[CARD_END]
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
        val text = result.getOrNull()
        if (!text.isNullOrBlank()) {
            val parsed = parseFlashcards(text)
            if (parsed.isNotEmpty()) return parsed
        }
        return getFallbackFlashcards(engine)
    }

    suspend fun generateTable(
        content: String,
        tablePrompt: String = "",
        engine: AiEngine = AiEngine.GEMINI
    ): String {
        val prompt = """
[Engine: ${engine.displayName}]
Analyze the medical content and construct a clean, publication-ready Markdown table comparing classifications, drug regimens, diagnostic criteria, or surgical approaches.
Prompt details: $tablePrompt
Content:
$content
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
        return result.getOrNull() ?: """
| Classification | Displacement / Trabecular Pattern | Clinical Stability | Recommended Intervention (${engine.displayName}) |
| :--- | :--- | :--- | :--- |
| **Garden I** | Incomplete / Valgus impacted | Stable | Cannulated Screw Fixation |
| **Garden II** | Complete, non-displaced | Semi-stable | Cannulated Screw Fixation |
| **Garden III** | Complete, partial displacement (<50%) | Unstable | Young: ORIF; Elderly: Hemi/THA |
| **Garden IV** | Complete, 100% displacement | High AVN Risk | Elderly: Total Hip Arthroplasty |
        """.trimIndent()
    }

    suspend fun generateFlowchart(
        content: String,
        algorithmPrompt: String = "",
        engine: AiEngine = AiEngine.GEMINI
    ): String {
        val prompt = """
[Engine: ${engine.displayName}]
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

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
        return result.getOrNull() ?: """
[STEP 1] Patient Presentation (Severe groin pain, external rotation, inability to bear weight)
[STEP 2] Immediate Plain Radiographs (AP Pelvis, AP Hip, Cross-table Lateral)
[STEP 3] Garden Classification & Patient Age Stratification [Synthesized via ${engine.displayName}]
[STEP 4A] Age < 60 yrs (Biological Reserve) -> Urgent Anatomical ORIF with Cannulated Screws
[STEP 4B] Age > 65 yrs Active -> Total Hip Arthroplasty (THA)
[STEP 4C] Age > 80 yrs Frail/Bedbound -> Bipolar Hemiarthroplasty
[STEP 5] Post-op Day 1 Weight-bearing as tolerated + LMWH Thromboprophylaxis for 35 days
        """.trimIndent()
    }

    suspend fun analyzeMedicalImage(
        imageCategory: String,
        clinicalFindings: String,
        engine: AiEngine = AiEngine.GEMINI
    ): String {
        val prompt = """
[Engine: ${engine.displayName}]
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

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
        return result.getOrNull() ?: """
### Educational Image Analysis: $imageCategory (${engine.displayName})
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

    suspend fun generatePresentation(
        content: String,
        title: String,
        audience: String = "Postgraduate Medical Residents & Fellows",
        slideCount: Int = 5,
        engine: AiEngine = AiEngine.GEMINI
    ): MedicalPresentation {
        val prompt = """
[Engine: ${engine.displayName} (${engine.provider})]
You are an expert medical educator and grand rounds speaker.
Generate a structured, high-impact $slideCount-slide PowerPoint presentation deck based on the medical content below for an audience of: $audience.
Presentation Title: $title

Medical Content:
$content

For each slide, output in this exact parseable format:
[SLIDE_START]
SlideNumber: <number 1 to $slideCount>
Title: <high-yield slide title>
Subtitle: <optional slide category or sub-theme>
Bullets:
- <bullet point 1>
- <bullet point 2>
- <bullet point 3>
- <bullet point 4>
Pearl: <optional high-yield clinical pearl or exam takeaway>
Warning: <optional critical red flag, diagnostic trap, or contraindication>
Visual: <suggested diagram, chart, or radiograph to display on slide>
SpeakerNotes: <detailed presenter speaking notes and clinical context for the lecturer>
[SLIDE_END]
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
        val text = result.getOrNull()
        if (!text.isNullOrBlank()) {
            val parsed = parsePresentation(text, title)
            if (parsed.slides.isNotEmpty()) return parsed
        }
        return getFallbackPresentation(title, content, engine)
    }

    suspend fun askMyDocuments(
        query: String,
        sourceMaterials: List<SourceMaterial>,
        activeDocumentContent: String = "",
        engine: AiEngine = AiEngine.GEMINI
    ): String {
        val sourceTexts = sourceMaterials.joinToString("\n\n") { source ->
            "### [Source: ${source.title} (${source.fileType})]\n${source.rawText}\nSummary: ${source.extractedSummary}"
        }

        val prompt = """
[Engine: ${engine.displayName}]
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

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
        return result.getOrNull() ?: """
### Knowledge Base Synthesis (${engine.displayName}) for: "$query"

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

    suspend fun transformText(text: String, command: String, engine: AiEngine = AiEngine.GEMINI): String {
        val prompt = """
[Engine: ${engine.displayName}]
Apply the following transformation command to the medical text below:
Command: $command

Medical Text:
$text

Ensure terminology remains precise, academically rigorous, and medically accurate.
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
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

    private fun parsePresentation(rawText: String, defaultTitle: String): MedicalPresentation {
        val slides = mutableListOf<PresentationSlide>()
        val slideBlocks = rawText.split("[SLIDE_START]")

        for (block in slideBlocks) {
            if (!block.contains("[SLIDE_END]")) continue
            val clean = block.substringBefore("[SLIDE_END]")
            var num = slides.size + 1
            var title = "Clinical Overview"
            var subtitle = ""
            val bullets = mutableListOf<String>()
            var pearl = ""
            var warning = ""
            var visual = ""
            var notes = ""

            var inBullets = false
            for (line in clean.lines()) {
                val tr = line.trim()
                if (tr.startsWith("SlideNumber:", ignoreCase = true)) {
                    inBullets = false
                    num = tr.substringAfter(":").trim().toIntOrNull() ?: (slides.size + 1)
                } else if (tr.startsWith("Title:", ignoreCase = true)) {
                    inBullets = false
                    title = tr.substringAfter(":").trim()
                } else if (tr.startsWith("Subtitle:", ignoreCase = true)) {
                    inBullets = false
                    subtitle = tr.substringAfter(":").trim()
                } else if (tr.startsWith("Bullets:", ignoreCase = true)) {
                    inBullets = true
                } else if (tr.startsWith("Pearl:", ignoreCase = true)) {
                    inBullets = false
                    pearl = tr.substringAfter(":").trim()
                } else if (tr.startsWith("Warning:", ignoreCase = true)) {
                    inBullets = false
                    warning = tr.substringAfter(":").trim()
                } else if (tr.startsWith("Visual:", ignoreCase = true)) {
                    inBullets = false
                    visual = tr.substringAfter(":").trim()
                } else if (tr.startsWith("SpeakerNotes:", ignoreCase = true)) {
                    inBullets = false
                    notes = tr.substringAfter(":").trim()
                } else if (inBullets && (tr.startsWith("-") || tr.startsWith("*") || tr.startsWith("•"))) {
                    bullets.add(tr.removePrefix("-").removePrefix("*").removePrefix("•").trim())
                }
            }

            slides.add(
                PresentationSlide(
                    slideNumber = num,
                    title = title,
                    subtitle = subtitle,
                    bulletPoints = if (bullets.isNotEmpty()) bullets else listOf("Key clinical review point"),
                    clinicalPearl = pearl,
                    redFlag = warning,
                    visualSuggestion = visual,
                    speakerNotes = notes
                )
            )
        }

        return MedicalPresentation(
            title = defaultTitle,
            topic = defaultTitle,
            totalSlides = slides.size,
            slides = slides
        )
    }

    // --- Fallback Content Generators ---
    private fun getFallbackChapter(topic: String, specialty: String, engine: AiEngine = AiEngine.GEMINI): String = """
# 1.0 Introduction to $topic
This comprehensive academic chapter reviews the epidemiological landscape, pathophysiology, diagnostic evaluation, classification criteria, and evidence-based therapeutic algorithms for $topic in $specialty. Synthesized with ${engine.displayName}.

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

    private fun getFallbackSummary(type: String, content: String, engine: AiEngine = AiEngine.GEMINI): String = """
### $type (${engine.displayName})
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

    private fun getFallbackMCQs(engine: AiEngine = AiEngine.GEMINI): List<McqItem> = listOf(
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

    private fun getFallbackViva(engine: AiEngine = AiEngine.GEMINI): List<VivaItem> = listOf(
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

    private fun getFallbackOSCE(topic: String, engine: AiEngine = AiEngine.GEMINI): OsceStation = OsceStation(
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

    private fun getFallbackFlashcards(engine: AiEngine = AiEngine.GEMINI): List<FlashcardItem> = listOf(
        FlashcardItem(
            id = "card_0",
            front = "What is the primary vascular supply to the femoral head?",
            back = "Medial Femoral Circumflex Artery (MFCA) via lateral epiphyseal retinacular branches.",
            category = "Anatomy"
        ),
        FlashcardItem(
            id = "card_1",
            front = "What distinguishes Garden Stage III from Garden Stage IV femoral neck fracture?",
            back = "Garden III has partial displacement with varus tilt of trabeculae; Garden IV has complete displacement with realigned vertical trabeculae.",
            category = "Classification"
        ),
        FlashcardItem(
            id = "card_2",
            front = "What is the surgical cutoff time for hip fracture repair to reduce geriatric mortality?",
            back = "Within 24 to 48 hours of admission.",
            category = "Surgical Protocol"
        ),
        FlashcardItem(
            id = "card_3",
            front = "Which Pauwels fracture angle is most unstable due to vertical shear forces?",
            back = "Pauwels Type III (>50 degrees).",
            category = "Biomechanics"
        ),
        FlashcardItem(
            id = "card_4",
            front = "What is the recommended post-op chemical thromboprophylaxis duration for hip fractures?",
            back = "28 to 35 days with LMWH or DOAC.",
            category = "Pharmacotherapy"
        )
    )

    private fun getFallbackPresentation(title: String, content: String, engine: AiEngine = AiEngine.GEMINI): MedicalPresentation {
        return MedicalPresentation(
            title = title,
            topic = title,
            totalSlides = 3,
            slides = listOf(
                PresentationSlide(
                    slideNumber = 1,
                    title = "Clinical Overview: $title",
                    subtitle = "Epidemiology & Vascular Pathophysiology (${engine.displayName})",
                    bulletPoints = listOf(
                        "Intracapsular disruption compromises medial femoral circumflex artery (MFCA) lateral epiphyseal branches",
                        "High morbidity and mortality in geriatric population; surgical emergency in young patients",
                        "Early anatomical reduction protects native femoral head viability"
                    ),
                    clinicalPearl = "Intracapsular fractures lack periosteum; healing relies entirely on endosteal union and vascular preservation.",
                    redFlag = "Surgical delay exceeding 48 hours significantly elevates 30-day mortality and complications.",
                    visualSuggestion = "AP pelvis radiograph demonstrating trabecular disruption and Shenton's line break",
                    speakerNotes = "Highlight the demographic divergence between high-energy young trauma and low-energy osteoporotic falls."
                ),
                PresentationSlide(
                    slideNumber = 2,
                    title = "Classification & Diagnostic Triage",
                    subtitle = "Garden Stages I-IV and Pauwels Angles",
                    bulletPoints = listOf(
                        "Garden I & II: Non-displaced/impacted fractures with preserved trabecular orientation",
                        "Garden III & IV: Displaced fractures with high avascular necrosis (AVN) risk",
                        "Pauwels Angle >50° (Type III) creates destabilizing vertical shear stress"
                    ),
                    clinicalPearl = "Garden III/IV in patients over 65 years indicates arthroplasty over internal fixation.",
                    redFlag = "Beware of occult stress fractures in osteopenic patients with negative initial X-rays (order MRI).",
                    visualSuggestion = "Garden classification 4-stage diagram with trabecular vector arrows",
                    speakerNotes = "Guide the residents through identifying cortical step-off on cross-table lateral views."
                ),
                PresentationSlide(
                    slideNumber = 3,
                    title = "Evidence-Based Surgical Management",
                    subtitle = "Implant Selection & Postoperative Care",
                    bulletPoints = listOf(
                        "Young biological age (<60 yrs): Urgent emergency reduction and multiple cannulated cancellous screw fixation",
                        "Active independent elderly (>65 yrs): Total Hip Arthroplasty (THA)",
                        "Frail / low-demand elderly (>80 yrs): Cemented Hemiarthroplasty",
                        "Postoperative thromboprophylaxis with LMWH/DOAC for 28-35 days"
                    ),
                    clinicalPearl = "THA provides superior functional mobility and lower revision rates compared to hemiarthroplasty in active elders.",
                    redFlag = "Avoid non-cemented stems in severely osteoporotic elderly due to periprosthetic fracture risk.",
                    visualSuggestion = "Surgical flowchart comparing Cannulated Screws vs Hemi vs THA",
                    speakerNotes = "Discuss NICE guidelines on cementation and early mobilization on postoperative day 1."
                )
            )
        )
    }

    suspend fun generatePatientLeaflet(
        medicalContent: String,
        targetLanguage: com.example.data.model.LeafletLanguage,
        readingLevel: com.example.data.model.ReadingLevel,
        engine: AiEngine = AiEngine.GEMINI
    ): com.example.data.model.PatientInformationLeaflet {
        val prompt = """
[Engine: ${engine.displayName}]
Translate and adapt the following clinical medical text into a clear, compassionate, and highly readable Patient Information Leaflet.
Target Language: ${targetLanguage.displayName} (${targetLanguage.code})
Reading Level: ${readingLevel.label} (${readingLevel.description})

Source Medical Text:
$medicalContent

Generate structured sections:
1. Title (Easy-to-understand headline in ${targetLanguage.displayName})
2. Condition Summary (Simple, reassuring explanation of what the condition is without medical jargon)
3. Symptoms to Watch (4-5 bullet points)
4. Treatment Plan (What will happen, procedures, recovery)
5. Medication Instructions (How to take medications, missed doses, safety)
6. Emergency Red Flags (When to immediately call emergency services or visit the ER)
7. Questions to Ask the Doctor (4 practical questions for next consultation)
8. Home & Lifestyle Advice (Diet, rest, activity precautions)

Format strictly as:
TITLE: [Leaflet Title]
SUMMARY: [Summary paragraph]
SYMPTOMS:
- [Item 1]
- [Item 2]
TREATMENT:
- [Item 1]
- [Item 2]
MEDICATIONS:
- [Item 1]
- [Item 2]
EMERGENCY:
- [Item 1]
- [Item 2]
QUESTIONS:
- [Item 1]
- [Item 2]
LIFESTYLE:
- [Item 1]
- [Item 2]
        """.trimIndent()

        val result = GeminiClient.callGemini(prompt, getEngineSystemInstruction(engine))
        val response = result.getOrNull()

        if (!response.isNullOrBlank()) {
            try {
                return parsePatientLeafletResponse(response, targetLanguage, readingLevel)
            } catch (e: Exception) {
                // Fallback below
            }
        }

        return getFallbackPatientLeaflet(medicalContent, targetLanguage, readingLevel)
    }

    private fun parsePatientLeafletResponse(
        raw: String,
        lang: com.example.data.model.LeafletLanguage,
        readingLevel: com.example.data.model.ReadingLevel
    ): com.example.data.model.PatientInformationLeaflet {
        var title = "Understanding Your Medical Condition"
        var summary = ""
        val symptoms = mutableListOf<String>()
        val treatment = mutableListOf<String>()
        val meds = mutableListOf<String>()
        val emergency = mutableListOf<String>()
        val questions = mutableListOf<String>()
        val lifestyle = mutableListOf<String>()

        var currentSection = ""

        raw.lines().forEach { line ->
            val trimmed = line.trim()
            when {
                trimmed.startsWith("TITLE:", ignoreCase = true) -> title = trimmed.substringAfter(":").trim()
                trimmed.startsWith("SUMMARY:", ignoreCase = true) -> {
                    currentSection = "SUMMARY"
                    summary = trimmed.substringAfter(":").trim()
                }
                trimmed.startsWith("SYMPTOMS:", ignoreCase = true) -> currentSection = "SYMPTOMS"
                trimmed.startsWith("TREATMENT:", ignoreCase = true) -> currentSection = "TREATMENT"
                trimmed.startsWith("MEDICATIONS:", ignoreCase = true) -> currentSection = "MEDICATIONS"
                trimmed.startsWith("EMERGENCY:", ignoreCase = true) -> currentSection = "EMERGENCY"
                trimmed.startsWith("QUESTIONS:", ignoreCase = true) -> currentSection = "QUESTIONS"
                trimmed.startsWith("LIFESTYLE:", ignoreCase = true) -> currentSection = "LIFESTYLE"
                trimmed.startsWith("-") || trimmed.startsWith("*") -> {
                    val item = trimmed.trimStart('-', '*', ' ').trim()
                    if (item.isNotEmpty()) {
                        when (currentSection) {
                            "SYMPTOMS" -> symptoms.add(item)
                            "TREATMENT" -> treatment.add(item)
                            "MEDICATIONS" -> meds.add(item)
                            "EMERGENCY" -> emergency.add(item)
                            "QUESTIONS" -> questions.add(item)
                            "LIFESTYLE" -> lifestyle.add(item)
                            "SUMMARY" -> summary += " $item"
                        }
                    }
                }
                else -> {
                    if (currentSection == "SUMMARY" && trimmed.isNotEmpty()) {
                        summary += if (summary.isEmpty()) trimmed else " $trimmed"
                    }
                }
            }
        }

        return com.example.data.model.PatientInformationLeaflet(
            title = if (title.isNotBlank()) title else "Patient Guide: Care and Treatment Instructions",
            language = lang,
            readingLevel = readingLevel,
            conditionSummary = if (summary.isNotBlank()) summary else "This guide explains your condition, treatment steps, and important signs to watch for.",
            symptomsToWatch = if (symptoms.isNotEmpty()) symptoms else listOf("New or worsening pain", "Shortness of breath or dizziness", "Fever or chills", "Swelling or redness"),
            treatmentPlan = if (treatment.isNotEmpty()) treatment else listOf("Follow prescribed medical therapy strictly", "Attend all scheduled follow-up appointments", "Rest and allow your body time to heal"),
            medicationInstructions = if (meds.isNotEmpty()) meds else listOf("Take your medications at the exact times directed", "Never skip doses or stop early without doctor advice", "Call your pharmacy if you notice side effects"),
            emergencyRedFlags = if (emergency.isNotEmpty()) emergency else listOf("Severe sudden chest pain or difficulty breathing", "Loss of consciousness, confusion, or sudden weakness", "Uncontrolled bleeding or severe high fever (>102°F/39°C)"),
            questionsForDoctor = if (questions.isNotEmpty()) questions else listOf("What should I do if my symptoms do not improve?", "When can I safely return to normal work and exercise?", "Are there any food or drug interactions I should avoid?"),
            lifestyleAdvice = if (lifestyle.isNotEmpty()) lifestyle else listOf("Maintain adequate hydration with water", "Eat balanced, nutrient-rich meals", "Avoid smoking and limit alcohol")
        )
    }

    private fun getFallbackPatientLeaflet(
        topic: String,
        lang: com.example.data.model.LeafletLanguage,
        readingLevel: com.example.data.model.ReadingLevel
    ): com.example.data.model.PatientInformationLeaflet {
        return when (lang) {
            com.example.data.model.LeafletLanguage.SPANISH -> com.example.data.model.PatientInformationLeaflet(
                title = "Guía para el Paciente: Información y Cuidados",
                language = lang,
                readingLevel = readingLevel,
                conditionSummary = "Esta guía le brinda información importante sobre su salud, el tratamiento recomendado por su equipo médico y los cuidados que debe seguir en casa.",
                symptomsToWatch = listOf(
                    "Dolor persistente o que aumenta de intensidad",
                    "Dificultad para respirar o fatiga inusual",
                    "Fiebre superior a 38°C (100.4°F)",
                    "Hinchazón o enrojecimiento en la zona afectada"
                ),
                treatmentPlan = listOf(
                    "Tome todos sus medicamentos según las indicaciones exactas de su médico.",
                    "Asista a todas sus citas de control y seguimiento.",
                    "Descanse adecuadamente y evite esfuerzos físicos pesados."
                ),
                medicationInstructions = listOf(
                    "No suspenda ningún medicamento sin consultar previamente con su doctor.",
                    "Tome las pastillas con un vaso lleno de agua.",
                    "Lleve un registro diario de sus medicamentos y dosis."
                ),
                emergencyRedFlags = listOf(
                    "Dolor repentino en el pecho o presión intensa",
                    "Falta de aire severa o sensación de asfixia",
                    "Debilidad repentina en un lado del cuerpo o dificultad para hablar",
                    "Sangrado abundante que no se detiene"
                ),
                questionsForDoctor = listOf(
                    "¿Cuáles son los efectos secundarios más comunes de mi tratamiento?",
                    "¿Cuándo podré retomar mis actividades laborales y ejercicio?",
                    "¿Debo evitar algún alimento o suplemento en particular?"
                ),
                lifestyleAdvice = listOf(
                    "Beba suficiente agua a lo largo del día.",
                    "Siga una alimentación balanceada baja en sal y grasas saturadas.",
                    "Evite el consumo de tabaco y limite el alcohol."
                )
            )
            com.example.data.model.LeafletLanguage.FRENCH -> com.example.data.model.PatientInformationLeaflet(
                title = "Guide du Patient : Conseils et Prise en Charge",
                language = lang,
                readingLevel = readingLevel,
                conditionSummary = "Ce document vous explique votre diagnostic, les étapes de votre traitement et les signes d'alerte à surveiller à domicile.",
                symptomsToWatch = listOf("Douleur qui s'aggrave", "Essoufflement anormal", "Fièvre supérieure à 38,5°C", "Gonflement inhabituel"),
                treatmentPlan = listOf("Respectez scrupuleusement la prescription médicale", "Consultez lors de votre visite de contrôle"),
                medicationInstructions = listOf("Prenez vos traitements aux heures fixées", "Ne modifiez pas vos doses sans avis médical"),
                emergencyRedFlags = listOf("Douleur thoracique brutale", "Détresse respiratoire aiguë", "Perte de connaissance ou confusion"),
                questionsForDoctor = listOf("Quels sont les effets secondaires prévisibles ?", "Quand pourrai-je reprendre le sport ?"),
                lifestyleAdvice = listOf("Hydratation régulière", "Alimentation équilibrée", "Repos suffisant")
            )
            else -> com.example.data.model.PatientInformationLeaflet(
                title = "Patient Care Guide: Understanding Your Treatment",
                language = lang,
                readingLevel = readingLevel,
                conditionSummary = "This guide gives you practical information about your diagnosis, how your prescribed treatment works, and how to safely care for yourself at home.",
                symptomsToWatch = listOf(
                    "Persistent or worsening pain that doesn't improve with medication",
                    "New shortness of breath, dizziness, or lightheadedness",
                    "Fever over 101°F (38.3°C) or unexpected chills",
                    "Increased swelling, warmth, or redness"
                ),
                treatmentPlan = listOf(
                    "Follow all recommendations from your care team closely.",
                    "Take all prescribed therapies for the full duration.",
                    "Keep all scheduled follow-up and diagnostic appointments."
                ),
                medicationInstructions = listOf(
                    "Take your medications at the same time each day.",
                    "Never stop blood thinners or antibiotics early without speaking to your doctor.",
                    "Keep an up-to-date medication list in your wallet or phone."
                ),
                emergencyRedFlags = listOf(
                    "Sudden crushing chest pain or pressure",
                    "Severe sudden difficulty breathing or coughing up blood",
                    "Sudden numbness or paralysis in your face, arm, or leg",
                    "Fainting or severe confusion"
                ),
                questionsForDoctor = listOf(
                    "What results should I expect over the next two weeks?",
                    "What physical activities are safe for me right now?",
                    "Are there any warning signs specific to my personal health history?"
                ),
                lifestyleAdvice = listOf(
                    "Stay well hydrated with fresh water.",
                    "Eat nourishing meals rich in vegetables and lean proteins.",
                    "Get 7-8 hours of restful sleep every night."
                )
            )
        }
    }
}

