package com.example.data.model

enum class TemplateCategory(val displayName: String, val iconName: String) {
    ALL("All Templates", "Category"),
    WARD_CLINICAL("Ward & Bedside Notes", "LocalHospital"),
    SURGERY_PROCEDURES("Surgical & Operative", "Biotech"),
    ACADEMIC_RESEARCH("Academic & Research", "School"),
    EXAM_OSCE("Exam Prep & OSCE", "FactCheck"),
    EMERGENCY_CRITICAL("Emergency & ICU", "Emergency")
}

data class MedicalTemplate(
    val id: String,
    val title: String,
    val description: String,
    val category: TemplateCategory,
    val docType: String,
    val specialty: String,
    val estimatedWordCount: String,
    val tags: List<String>,
    val defaultTitle: String,
    val defaultAuthors: String,
    val defaultAudience: String,
    val templateContent: String
)

object PredefinedMedicalTemplates {
    val templates = listOf(
        MedicalTemplate(
            id = "template_soap_note",
            title = "SOAP Clinical Ward Encounter Note",
            description = "Standardized 4-part bedside clinical progress note for inpatient and outpatient rounds.",
            category = TemplateCategory.WARD_CLINICAL,
            docType = "Clinical Protocol",
            specialty = "Internal Medicine",
            estimatedWordCount = "600-900 words",
            tags = listOf("SOAP", "Ward Rounds", "Progress Note", "Bedside", "Inpatient"),
            defaultTitle = "Clinical Progress Note: Post-Admission Day 2",
            defaultAuthors = "Attending Physician / Clinical Fellow",
            defaultAudience = "Multidisciplinary Clinical Care Team",
            templateContent = """
# CLINICAL PROGRESS NOTE (SOAP)

**Patient Identifier:** [Patient Name / MRN: ______]  
**Age/Sex:** [____ yr old Male/Female]  
**Hospital Day:** Day [____] | **ICU/Ward Bed:** [____]  
**Attending Consultant:** Dr. [______] | **Resident/Fellow:** Dr. [______]  
**Primary Admitting Diagnosis:** [e.g., Acute Exacerbation of COPD / Community-Acquired Pneumonia]

---

## 1. SUBJECTIVE (S)
- **Overnight Events:** [e.g., Patient remained afebrile overnight; reports improved dyspnea on 2L NC; no chest pain or hemoptysis.]
- **Patient Symptoms & Complaints:** [e.g., Mild productive cough with whitish sputum; appetite improving.]
- **Pain Score:** [____/10 on Numeric Rating Scale]
- **Review of Systems (ROS):**
  - *Constitutional:* No fever, chills, or night sweats.
  - *Cardiovascular:* No orthopnea, PND, or palpitation.
  - *Respiratory:* Decreased wheezing, mild exertional shortness of breath.
  - *Gastrointestinal:* Normal bowel movements, tolerating regular diabetic diet.

---

## 2. OBJECTIVE (O)
### Vital Signs:
| Parameter | Value | Target / Reference |
| :--- | :--- | :--- |
| **Blood Pressure** | 128/78 mmHg | MAP > 65 mmHg |
| **Heart Rate** | 76 bpm | Regular sinus rhythm |
| **Respiratory Rate**| 18 breaths/min | 12-20 bpm |
| **SpO₂** | 96% on 2L NC | Target 94-98% (or 88-92% if chronic hypercapnia) |
| **Temperature** | 36.8°C (98.2°F) | Afebrile |
| **24-Hour Urine Output** | 1,850 mL (0.9 mL/kg/hr) | > 0.5 mL/kg/hr |

### Physical Examination:
- **General Appearance:** Alert, oriented x 3, in no acute respiratory distress.
- **Cardiovascular:** S1 and S2 present, regular rhythm, no murmurs, JVP not elevated.
- **Respiratory:** Bilateral air entry symmetric; minimal bilateral end-expiratory polyphonic wheezes at bases, significantly improved from admission.
- **Abdomen:** Soft, non-tender, non-distended, active bowel sounds.
- **Extremities:** Trace bilateral pedal edema, warm and well-perfused, distal pulses 2+ palpable.

### Diagnostic & Laboratory Highlights:
- **CBC:** WBC 8.4 x 10⁹/L (down from 14.2 on admission), Hb 13.2 g/dL, Platelets 240 x 10⁹/L.
- **Basic Metabolic Panel:** Na 139, K 4.1, Cl 102, HCO₃ 26, BUN 16, Cr 0.92 (eGFR > 90).
- **Serial Biomarkers:** hs-Troponin I < 6 ng/L (negative), CRP 14 mg/L (decreasing).
- **Imaging:** Chest X-ray (AP Portable): Resolving right lower lobe infiltrate, no pneumothorax or pleural effusion.

---

## 3. ASSESSMENT (A)
**Summary Formulation:**  
[____]-year-old [Male/Female] with history of [COPD GOLD Stage II / Type 2 Diabetes / HTN] admitted for [Severe Community-Acquired Pneumonia], now demonstrating marked clinical and biochemical improvement on Day 3 of IV Ceftriaxone and Azithromycin.

### Active Problem List & Stratification:
1. **#1. Community-Acquired Pneumonia (CURB-65 = 1):** Resolving clinically, afebrile for 48 hours, inflammatory markers trending downward.
2. **#2. Chronic Obstructive Pulmonary Disease (COPD):** Stable on bronchodilator nebulizers; ready for step-down to metered-dose inhalers.
3. **#3. Type 2 Diabetes Mellitus:** Glycemic control stable on sliding-scale insulin; blood glucose ranged 120-160 mg/dL.
4. **#4. VTE Prophylaxis:** Low risk, receiving Enoxaparin 40 mg SC daily.

---

## 4. PLAN (P)
### 4.1 Diagnostic & Monitoring:
- Continue continuous pulse oximetry; spot-check vitals Q4H.
- Wean supplemental oxygen to ambient room air as tolerated today.
- Repeat CBC and Basic Metabolic Panel tomorrow morning.

### 4.2 Therapeutics:
- **Antibiotic Step-Down:** Switch IV Ceftriaxone 1g IV daily to Oral Cefpodoxime 200 mg PO BID to complete a 5-day total course.
- **Bronchodilators:** Transition from Q4H Duoneb (Ipratropium/Albuterol) nebulizers to maintenance inhalers (Tiotropium 18mcg daily + Formoterol/Budesonide 160/4.5mcg BID).
- **Fluids & Nutrition:** Discontinue IV maintenance fluids; encourage oral hydration; continue diabetic cardiac diet.
- **Prophylaxis:** Continue Enoxaparin 40 mg SC daily and bowel regimen.

### 4.3 Disposition & Discharge Planning:
- Physical Therapy (PT) evaluation today for home safety assessment.
- Anticipate discharge home in 24-48 hours if room air trial successful.
- Schedule outpatient pulmonary follow-up clinic appointment in 3 weeks.

---

> [!NOTE]
> **CLINICAL PEARL:** In stable CAP patients showing clinical improvement (afebrile > 48h, stable vitals, tolerating oral intake), early switch from IV to oral antibiotics reduces length of hospital stay without increasing treatment failure rates (ATS/IDSA Guidelines).
            """.trimIndent()
        ),
        MedicalTemplate(
            id = "template_textbook_chapter",
            title = "Academic Medical Textbook Chapter",
            description = "Comprehensive multi-tier pedagogical chapter with learning objectives, mechanisms, workup, tables, and pearls.",
            category = TemplateCategory.ACADEMIC_RESEARCH,
            docType = "Textbook Chapter",
            specialty = "Cardiology",
            estimatedWordCount = "2,500-4,000 words",
            tags = listOf("Textbook", "Pedagogy", "Harrison's Style", "Comprehensive", "Medical School"),
            defaultTitle = "Acute Coronary Syndromes: Molecular Pathogenesis, Risk Stratification, and Contemporary Revascularization",
            defaultAuthors = "Prof. Cardiovascular Medicine / AI Academic Editor",
            defaultAudience = "Cardiology Fellows, Residents, and Senior Clinicians",
            templateContent = """
# CHAPTER: ACUTE CORONARY SYNDROMES (ACS)
### Pathophysiology, Rapid Triage, Biomarkers, and Evidence-Based Pharmacotherapy

**Author:** [Faculty Name, MD, FACC, FSCAI]  
**Department:** [Division of Cardiology, University Medical Center]  
**Academic Target:** [Postgraduate Cardiology Fellowship & Residency Curriculum]

---

## 1.0 LEARNING OBJECTIVES
Upon completion of this chapter, the reader should be able to:
1. Differentiate the biomolecular mechanisms of plaque rupture versus plaque erosion in the genesis of acute coronary thrombosis.
2. Formulate an evidence-based risk stratification strategy utilizing high-sensitivity Cardiac Troponin (hs-cTn 0/1-hour algorithm) and TIMI/GRACE scores.
3. Contrast indications for immediate invasive coronary angiography (< 2 hours) versus early invasive strategies (< 24 hours) in NSTE-ACS.
4. Prescribe tailored dual antiplatelet therapy (DAPT) and secondary prevention regimens according to bleeding vs. ischemic risk scores (PRECISE-DAPT / ARC-HBR).

---

## 2.0 EPIDEMIOLOGY AND GLOBAL BURDEN
Ischemic heart disease remains the singular leading cause of mortality globally, accounting for approximately 9 million deaths annually. In high-income and transitioning economies, prompt reperfusion networks have reduced 30-day in-hospital STEMI mortality from >30% in the pre-thrombolytic era to < 5% with primary percutaneous coronary intervention (PPCI).

---

## 3.0 PATHOPHYSIOLOGY & MOLECULAR MECHANISMS
Acute coronary syndromes encompass a clinical spectrum spanning:
- **ST-Elevation Myocardial Infarction (STEMI):** Transmural myocardial ischemia caused by complete occlusive red (fibrin-rich) thrombus.
- **Non-ST-Elevation Myocardial Infarction (NSTEMI):** Subendocardial necrosis caused by non-occlusive white (platelet-rich) thrombus with downstream micro-embolization.
- **Unstable Angina (UA):** Severe ischemia without biochemical evidence of myocyte necrosis.

```
Plaque Disruption (Rupture / Erosion)
         │
         ▼
Subendothelial Matrix Exposure (Collagen + Tissue Factor)
         │
         ▼
Platelet Adhesion (GP Ib-IX-V) & Activation (TxA2 / ADP release)
         │
         ▼
Platelet Aggregation (GP IIb/IIIa Cross-linking via Fibrinogen)
         │
         ▼
Thrombus Propagation ──► Coronary Occlusion ──► Ischemia / Necrosis
```

---

## 4.0 CLINICAL PRESENTATION & DIAGNOSTIC WORKUP
### 4.1 Presenting Symptoms
The hallmark presentation is acute retrosternal chest pain (crushing, heavy pressure, or squeezing sensation) lasting > 20 minutes, radiating to the left arm, neck, jaw, or epigastrium, often associated with diaphoresis, dyspnea, and nausea.

### 4.2 12-Lead Electrocardiography (ECG)
- Standard 12-lead ECG must be obtained and interpreted within **10 minutes** of first medical contact (FMC).
- If standard leads are non-diagnostic and posterior/inferior ischemia is suspected, record leads **V7-V9** (posterior) and **V3R-V4R** (right ventricular).

---

## 5.0 DIAGNOSTIC ALGORITHMS & BIOMARKERS
### High-Sensitivity Cardiac Troponin (hs-cTn 0/1-Hour ESC Protocol):
| Baseline (0 hr) | 1-Hour Delta (Δ) | Triage Decision | Clinical Action |
| :--- | :--- | :--- | :--- |
| **Very Low** (e.g. < 5 ng/L) | **No Δ** (< 3 ng/L) | **Rule-Out** | Safe for early discharge with outpatient provocative testing |
| **Intermediate** | **Moderate Δ** (3-5 ng/L) | **Observe** | Repeat at 3 hours; echocardiography; clinical risk score |
| **High** (e.g. > 52 ng/L) | **Significant Δ** (≥ 5 ng/L) | **Rule-In** | Admit to CCU; initiate DAPT + anticoagulation; plan coronary angiography |

---

## 6.0 CONTEMPORARY PHARMACOTHERAPY & MANAGEMENT
### 6.1 Acute Antiplatelet & Anticoagulant Regimen:
1. **Aspirin:** 300 mg loading dose chewable, followed by 75-100 mg daily indefinitely.
2. **Potent P2Y₁₂ Inhibitor:**
   - **Ticagrelor:** 180 mg loading dose, then 90 mg PO BID, OR
   - **Prasugrel:** 60 mg loading dose, then 10 mg daily (indicated once coronary anatomy is defined prior to PCI).
3. **Parenteral Anticoagulation:**
   - **Unfractionated Heparin (UFH):** 70-100 units/kg IV bolus during PCI.
   - **Enoxaparin:** 1 mg/kg SC Q12H (preferred in conservative or delayed invasive NSTE-ACS).

---

## 7.0 CLINICAL PEARLS & RED FLAGS
> [!NOTE]
> **CLINICAL PEARL:** In patients presenting with acute inferior STEMI (ST-elevation in II, III, aVF), always obtain right-sided leads (V3R, V4R) before administering nitrates or morphine. RV infarction causes profound preload dependence; nitrates can precipitate catastrophic hemodynamic collapse.

> [!WARNING]
> **RED FLAG:** "De Winter's T-waves" (upsloping ST depression > 1mm at the J-point in precordial leads with tall, symmetric T-waves) and "Wellens' Syndrome" (deeply inverted or biphasic T-waves in V2-V3) represent critical proximal LAD occlusion equivalent to STEMI and require immediate emergency cardiac catheterization!

---

## 8.0 REFERENCES & EVIDENCE BASE
1. Collet JP, Thiele H, Barbato E, et al. 2020 ESC Guidelines for the management of acute coronary syndromes in patients presenting without persistent ST-segment elevation. *Eur Heart J*. 2021;42(14):1289-1367.
2. Gulati M, Levy PD, Mukherjee D, et al. 2021 AHA/ACC Guideline for the Evaluation and Diagnosis of Chest Pain. *Circulation*. 2021;144(22):e368-e454.
            """.trimIndent()
        ),
        MedicalTemplate(
            id = "template_surgical_operative_report",
            title = "Surgical Operative Report (OR Note)",
            description = "Detailed operative record covering pre/post-op diagnoses, anatomical findings, procedural steps, and post-op instructions.",
            category = TemplateCategory.SURGERY_PROCEDURES,
            docType = "Clinical Protocol",
            specialty = "General Surgery",
            estimatedWordCount = "700-1,200 words",
            tags = listOf("Surgery", "Operative Note", "OR", "Surgical Technique", "Perioperative"),
            defaultTitle = "Operative Report: Laparoscopic Cholecystectomy with Intraoperative Cholangiogram",
            defaultAuthors = "Attending Surgeon / Surgical Fellow",
            defaultAudience = "Surgical Team, PACU, and Inpatient Ward",
            templateContent = """
# OPERATIVE REPORT

**Date of Operation:** [DD/MM/YYYY]  
**Patient Identifier:** [Patient Name / MRN: ______]  
**Age / Gender:** [____ yr old Male/Female]  
**Preoperative Diagnosis:** Symptomatic Cholelithiasis with Chronic Calculous Cholecystitis  
**Postoperative Diagnosis:** Severe Acute Calculous Cholecystitis with Extensive Pericholecystic Adhesions  
**Procedure Performed:** Laparoscopic Cholecystectomy with Critical View of Safety (CVS) Dissection  
**Primary Surgeon:** Dr. [____________________], MD, FACS  
**Assistant Surgeon / First Assist:** Dr. [____________________], MD  
**Anesthesia:** General Endotracheal Anesthesia (GETA)  
**Estimated Blood Loss (EBL):** < 30 mL  
**Specimens Removed:** Gallbladder intact sent to Surgical Pathology  
**Drains Placed:** None (or: 19 Fr Blake drain in subhepatic Morrison's pouch)  
**Intraoperative Complications:** None  
**Counts:** Sponge and needle counts were correct x 2 at conclusion of procedure.

---

## 1. INDICATION FOR PROCEDURE
The patient is a [____]-year-old [male/female] with recurrent right upper quadrant biliary colic refractory to conservative management. Preoperative ultrasound confirmed multiple gallstones with gallbladder wall thickening (4.5 mm) and a positive sonographic Murphy's sign. Risks, benefits, and alternatives of laparoscopic versus open surgery were discussed in detail, and informed written consent was obtained.

---

## 2. SURGICAL FINDINGS
1. Distended, thick-walled, chronically inflamed gallbladder with dense omental and duodenal adhesions to the gallbladder infundibulum.
2. Clear cystic duct and single cystic artery entering the gallbladder neck.
3. No aberrant duct of Luschka or anomalous right hepatic artery identified.
4. Liver parenchyma appeared grossly normal with sharp borders and no cirrhosis or focal metastases.

---

## 3. DESCRIPTION OF TECHNIQUE & PROCEDURAL STEPS
### 3.1 Patient Positioning & Port Placement:
- Patient placed in supine position with split-leg configuration under GETA.
- Preoperative prophylactic IV Cefazolin (2g) administered within 30 minutes of incision.
- Sequential compression devices applied to bilateral lower extremities.
- Abdomen prepped with ChloraPrep and draped in standard sterile surgical fashion.
- A 10-mm infraumbilical incision made; pneumoperitoneum established using a Veress needle with opening pressure < 10 mmHg. Abdomen insufflated to 14 mmHg with CO₂.
- A 10-mm 30-degree laparoscope introduced. Three additional trocars placed under direct vision:
  - 10-mm subxiphoid epigastric port
  - 5-mm right subcostal midclavicular port
  - 5-mm right anterior axillary port

### 3.2 Mobilization & Critical View of Safety (CVS):
- Atraumatic graspers used to retract gallbladder fundus cephalad over liver edge, and infundibulum inferolaterally to expose Calot's triangle.
- Omental adhesions dissected gently using blunt laparoscopic dissecting peanuts and hook electrocautery.
- The hepatocystic triangle cleared of all fat and fibrous tissue.
- The lower third of the gallbladder dissected off the cystic plate (liver bed).
- **Critical View of Safety (Strasberg Criteria) confirmed:**
  - 1. Hepatocystic triangle cleared of fat and fibrous tissue.
  - 2. Lower third of gallbladder dissected off the cystic plate.
  - 3. Only two structures (cystic duct and cystic artery) seen entering the gallbladder.

### 3.3 Ligation & Transection:
- Cystic artery doubly clipped proximally with titanium clips and singly clipped distally, then transected with laparoscopic shears.
- Cystic duct doubly clipped at its junction with the infundibulum (leaving common bile duct unimpeded) and divided.

### 3.4 Gallbladder Excision & Extraction:
- Gallbladder dissected from the hepatic fossa in a retrograde dome-down fashion using hook cautery with meticulous hemostasis.
- Gallbladder placed in an EndoCatch specimen retrieval bag and extracted through the umbilical port site under direct vision.
- Hemostasis in liver bed verified with low-pressure irrigation (8 mmHg); no active bleeding or bile leak identified.

### 3.5 Closure:
- All trocars removed under direct visualization; pneumoperitoneum desufflated.
- Umbilical fascia closed with 0-Vicryl figure-of-eight suture.
- Skin incisions closed with subcuticular 4-0 Monocryl and dressed with Dermabond sterile skin adhesive.

---

## 4. POSTOPERATIVE INSTRUCTIONS & DISPOSITION
1. Transfer to PACU in stable condition; monitor vitals Q15 min until awake.
2. Advance diet from clear liquids to regular low-fat diet as tolerated.
3. Analgesia: IV Acetaminophen 1g Q6H + Ketorolac 15mg IV Q6H PRN (Multimodal opioid-sparing protocol).
4. Early ambulation encouraged within 4 hours.
5. Anticipate same-day outpatient discharge if pain controlled and voiding spontaneously.
            """.trimIndent()
        ),
        MedicalTemplate(
            id = "template_clinical_case_report",
            title = "Clinical Case Report (CARE Guidelines)",
            description = "Peer-review ready academic case report following international CARE reporting standards.",
            category = TemplateCategory.ACADEMIC_RESEARCH,
            docType = "Case Report",
            specialty = "Neurology",
            estimatedWordCount = "1,500-2,200 words",
            tags = listOf("Case Report", "CARE Guidelines", "Rare Disease", "Academic Publishing", "Neurology"),
            defaultTitle = "Atypical Presentation of Anti-NMDAR Encephalitis Mimicking Acute Psychosis: A Case Report and Diagnostic Pathway",
            defaultAuthors = "Lead Resident / Neuro-Immunology Attending",
            defaultAudience = "Clinical Neurologists, Psychiatrists, and Medical Researchers",
            templateContent = """
# CLINICAL CASE REPORT
### Following CARE (CAse REport) Consensus Guidelines

**Title:** [Atypical Presentation of ____________: Diagnostic Dilemma and Therapeutic Resolution]  
**Corresponding Author:** [Author Name, MD, Department of Neurology]  
**Institutions:** [Academic Teaching Hospital / Medical University]  
**Keywords:** [Encephalitis; Autoimmune; Psychosis; Teratoma; Immunotherapy]

---

## 1. ABSTRACT
- **Background:** [Briefly describe the clinical rarity or diagnostic challenge of the condition.]
- **Case Presentation:** [Summarize age, gender, cardinal symptoms, key exam findings, and primary diagnostic tests.]
- **Interventions & Outcomes:** [Summarize therapeutic regimen, surgical resection if applicable, and degree of functional recovery on mRS score.]
- **Conclusion:** [Key clinical takeaway message for practicing clinicians.]

---

## 2. INTRODUCTION
Autoimmune encephalitides represent a rapidly expanding spectrum of antibody-mediated neuro-inflammatory disorders. Among these, anti-N-methyl-D-aspartate receptor (NMDAR) encephalitis classically presents with progressive behavioral changes, psychosis, autonomic instability, and seizures. We report a unique case of...

---

## 3. PATIENT INFORMATION & TIMELINE
### 3.1 Demographic Information:
- **Age / Sex:** 24-year-old female
- **Occupation:** University Graduate Student
- **Past Medical History:** Unremarkable; no prior psychiatric or neurological illness.
- **Family History:** Negative for autoimmune diseases or psychiatric disorders.

### 3.2 Chronological Timeline of Illness:
```
Day -14: Prodromal viral-like headache, low-grade fever (37.9°C), and fatigue.
Day -7: Acute onset insomnia, paranoia, visual hallucinations, and agitation.
Day 0: Hospital admission to Acute Psychiatry unit with presumptive first-episode psychosis.
Day +3: Development of oro-facial dyskinesias, speech reduction, and autonomic tachycardia (HR 140 bpm).
Day +5: Neurological consult; Lumbar puncture & Brain MRI performed.
Day +8: Confirmed CSF Anti-GluN1 (NMDAR) antibody positivity (1:64).
Day +9: First-line immunotherapy initiated (High-dose IV Methylprednisolone + IVIG).
Day +12: Pelvic MRI revealed 2.4 cm mature cystic ovarian teratoma; Laparoscopic cystectomy performed.
Day +30: Marked cognitive recovery; discharged to neuro-rehabilitation facility.
Day +180: Full functional independence (mRS = 0); normal MMSE (30/30).
```

---

## 4. CLINICAL FINDINGS & DIAGNOSTIC ASSESSMENT
### 4.1 Neurological Examination:
- **Mental Status:** Fluctuating catatonia alternating with agitated delirium, mutism, echolalia.
- **Cranial Nerves:** Pupils equal and reactive, spontaneous involuntary lip-smacking and chewing movements.
- **Motor / Reflexes:** Hyperreflexia 3+ throughout, bilateral Babinski signs positive.

### 4.2 Diagnostic Investigations:
- **Serum Labs:** Normal CBC, ESR, ANA, HIV, Syphilis RPR, Vitamin B12, and Thyroid Panel.
- **CSF Analysis:**
  - Opening pressure: 18 cm H₂O
  - WBC: 32 /μL (94% lymphocytes - lymphocytic pleocytosis)
  - Protein: 58 mg/dL (mildly elevated) | Glucose: 64 mg/dL (CSF/Serum ratio 0.65)
  - CSF Oligoclonal Bands: Positive (6 unique bands)
  - **CSF Autoimmune Encephalopathy Panel:** Positive for Anti-NMDAR (GluN1 subunit) IgG antibodies.
- **Electroencephalogram (EEG):** Generalized background slowing with "Extreme Delta Brush" pattern.
- **Neuroimaging (Brain MRI 3T with Contrast):** Subtle hyperintensity in bilateral medial temporal lobes on T2/FLAIR sequences without pathological gadolinium enhancement.

---

## 5. THERAPEUTIC INTERVENTION
1. **Pulse Corticosteroids:** Methylprednisolone 1,000 mg IV daily for 5 consecutive days.
2. **Intravenous Immunoglobulin (IVIG):** 0.4 g/kg/day for 5 days (total dose 2 g/kg).
3. **Surgical Source Control:** Laparoscopic right ovarian cystectomy with pathology confirming mature cystic teratoma containing neural tissue elements.
4. **Second-Line Immunotherapy:** Rituximab (375 mg/m² weekly for 4 doses) initiated on Day 16 due to persistent dyskinesias.

---

## 6. DISCUSSION & CLINICAL IMPLICATIONS
This case illustrates the paramount importance of entertaining an autoimmune etiology in young patients presenting with explosive, new-onset psychiatric symptoms accompanied by subtle neurological features (e.g., autonomic dysfunction, movement disorders). 

### Key Lessons for Clinical Practice:
- **Red Flags for Autoimmune Psychosis:** Rapid progression (< 3 months), treatment resistance to antipsychotics, unexplained dyskinesias, autonomic instability, and CSF pleocytosis.
- **Prompt Tumor Screening:** All female patients with confirmed anti-NMDAR encephalitis require urgent pelvic imaging (ultrasound/MRI) to evaluate for ovarian teratoma, as tumor removal accelerates clinical remission.

---

## 7. REFERENCES (Vancouver Style)
1. Dalmau J, Armangué T, Planagumà J, et al. An update on anti-NMDA receptor encephalitis for neurologists and psychiatrists. *Lancet Neurol*. 2019;18(11):1045-1057.
2. Graus F, Titulaer MJ, Balu R, et al. A clinical approach to diagnosis of autoimmune encephalitis. *Lancet Neurol*. 2016;15(4):391-404.
            """.trimIndent()
        ),
        MedicalTemplate(
            id = "template_osce_station",
            title = "OSCE Clinical Station & Examiner Marking Scheme",
            description = "High-yield 8-minute objective structured clinical exam station with candidate brief, patient actor script, and marking rubric.",
            category = TemplateCategory.EXAM_OSCE,
            docType = "OSCE Station",
            specialty = "Emergency Medicine",
            estimatedWordCount = "800-1,200 words",
            tags = listOf("OSCE", "Exam", "Marking Scheme", "Medical School", "Clinical Skills"),
            defaultTitle = "OSCE Station: Acute Severe Asthma Exacerbation in a 22-Year-Old Patient",
            defaultAuthors = "Clinical Skills Committee / OSCE Lead",
            defaultAudience = "Medical Students and Clinical Exam Examiners",
            templateContent = """
# OSCE CLINICAL STATION GUIDE

**Station Number:** Station [____]  
**Station Duration:** 8 Minutes (7 mins clinical interaction + 1 min examiner viva)  
**Specialty Domain:** Acute Emergency Medicine / Respiratory  
**Assessed Competencies:** Acute Triage (ABCDE), Inhaler Technique, Severity Stratification, Emergency Pharmacotherapy, and Communication.

---

## 1. CANDIDATE INSTRUCTIONS (Displayed Outside Station Door)
### Clinical Scenario:
You are the Foundation Doctor / Resident in the Emergency Department. A 22-year-old female known asthmatic has been brought in by triage due to worsening breathlessness over the past 4 hours.

### Tasks to Perform:
1. Conduct a focused acute assessment and severity stratification of this patient.
2. Formulate and verbally explain your immediate emergency management plan.
3. Answer 1-2 viva questions from the examiner in the final minute.

---

## 2. SIMULATED PATIENT ACTOR BRIEF
- **Name:** Chloe Taylor, 22-year-old female.
- **Demeanor:** Breathless, speaking in short 2-3 word fragmented phrases, sitting upright leaning forward (tripod position).
- **History:** Has had a mild cold for 2 days; asthma started worsening last night; used Salbutamol inhaler 8 puffs at home with minimal relief.
- **Key Trigger Question:** If the candidate asks about previous ICU admissions: *"Yes, I was intubated in ICU two years ago for a severe asthma attack."* (High-risk feature!).

---

## 3. EXAMINER MARKING RUBRIC (Checklist Breakdown)
| Domain / Action Step | Performed Competently (2 pts) | Partially Performed (1 pt) | Not Performed (0 pts) |
| :--- | :---: | :---: | :---: |
| **1. Immediate Airway & Oxygenation:** Assessed airway patency; prescribed high-flow oxygen via reservoir bag targeting SpO₂ 94-98%. | [ ] | [ ] | [ ] |
| **2. Severity Assessment:** Checked PEFR (Peak Flow), noted inability to complete sentences, assessed respiratory rate (> 25/min) and heart rate (> 110 bpm). | [ ] | [ ] | [ ] |
| **3. Red Flag Identification:** Identified previous ICU/intubation as a marker of life-threatening risk; checked for "Silent Chest" or exhaustion. | [ ] | [ ] | [ ] |
| **4. First-Line Inhaled Bronchodilator:** Administered nebulized Salbutamol (5 mg) + Ipratropium Bromide (0.5 mg) driven by oxygen. | [ ] | [ ] | [ ] |
| **5. Systemic Corticosteroid:** Prescribed Oral Prednisolone (40-50 mg) or IV Hydrocortisone (100 mg STAT). | [ ] | [ ] | [ ] |
| **6. Escalation Plan:** Recognized criteria for IV Magnesium Sulfate (2g IV over 20 min) if severe features persist. | [ ] | [ ] | [ ] |
| **7. Communication & Empathy:** Reassured the anxious patient in a calm, professional manner without causing further respiratory distress. | [ ] | [ ] | [ ] |

**Total Score:** [____ / 14 Points]  
**Global Performance Rating:** [ ] Clear Pass | [ ] Borderline | [ ] Fail

---

## 4. EXAMINER VIVA QUESTIONS (Final 1 Minute)
1. **Examiner:** *"What are three clinical features that define a Life-Threatening Asthma attack?"*
   - **Expected Model Answer:** (Candidate must name at least 3)
     - 1. Peak Expiratory Flow Rate (PEFR) < 33% of predicted or best.
     - 2. Silent chest (absent wheeze due to severe bronchoconstriction).
     - 3. Cyanosis, poor respiratory effort, or exhaustion.
     - 4. Bradycardia, hypotension, or arrhythmia.
     - 5. Altered mental status / confusion / coma.
     - 6. Normal or elevated PaCO₂ on ABG (indicates impending respiratory muscle fatigue).

2. **Examiner:** *"Why is a normal PaCO₂ (40 mmHg / 5.3 kPa) concerning in a breathless asthmatic?"*
   - **Expected Model Answer:** Asthmatic patients should hyperventilate and exhibit hypocapnia (low PaCO₂). A normal or rising PaCO₂ signals respiratory muscle exhaustion and imminent arrest, mandating urgent ICU consultation for possible mechanical ventilation.
            """.trimIndent()
        ),
        MedicalTemplate(
            id = "template_emergency_trauma_protocol",
            title = "Emergency Trauma Resuscitation Protocol (ATLS)",
            description = "Systematic primary and secondary survey protocol for acute polytrauma resuscitation in the trauma bay.",
            category = TemplateCategory.EMERGENCY_CRITICAL,
            docType = "Clinical Protocol",
            specialty = "Emergency Medicine",
            estimatedWordCount = "900-1,400 words",
            tags = listOf("Trauma", "ATLS", "Emergency", "Resuscitation", "Critical Care"),
            defaultTitle = "Emergency Trauma Protocol: Level 1 Polytrauma Primary Survey",
            defaultAuthors = "Trauma Team Leader / Emergency Medicine Specialist",
            defaultAudience = "Trauma Resuscitation Team & Emergency Department Staff",
            templateContent = """
# EMERGENCY TRAUMA PROTOCOL
### ATLS® 10th Edition Primary & Secondary Survey Workflow

**Trauma Activation Level:** [Level 1 Major Trauma Activation]  
**Arrival Time:** [HH:MM] | **Trauma Bay Bed:** [Resus Bay 1]  
**Trauma Team Leader:** Dr. [____________________] | **Scribe:** [____________________]  
**Mechanism of Injury:** [e.g., High-speed motor vehicle collision with rollover and prolonged extrication]

---

## 1. PRIMARY SURVEY (ABCDE) WITH IMMEDIATE RESUSCITATIVE INTERVENTIONS

### 1.1 A - Airway Maintenance with Cervical Spine Protection:
- **C-Spine:** Hard cervical collar and manual in-line stabilization maintained.
- **Airway Patency:** Clear / blood / debris / facial trauma.
- **Intervention:** [ ] High-flow O₂ via non-rebreather | [ ] Rapid Sequence Intubation (RSI) with in-line stabilization.
- *RSI Medications:* Etomidate 0.3 mg/kg IV (or Ketamine 1.5-2 mg/kg IV) + Rocuronium 1.2 mg/kg IV.

### 1.2 B - Breathing and Ventilation:
- **Tracheal Alignment:** Midline / Deviated to [Left / Right].
- **Chest Wall Motion:** Symmetric / Paradoxical flail segment / Crepitus.
- **Breath Sounds:** Bilateral clear / Diminished on [Left / Right].
- **Red Flag Diagnoses:**
  - *Tension Pneumothorax:* Immediate needle decompression (5th intercostal space, anterior axillary line) followed by 28-32 Fr thoracostomy tube.
  - *Open Pneumothorax:* 3-sided occlusive flutter-valve dressing.
  - *Massive Hemothorax:* Chest tube placement with autotransfusion circuit.

### 1.3 C - Circulation and Hemorrhage Control:
- **External Hemorrhage:** Direct manual pressure / Hemostatic gauze / Combat Application Tourniquet (CAT) applied.
- **Hemodynamic Status:** Pulse: [____] bpm | BP: [____/____] mmHg | Capillary Refill: [____] sec.
- **Vascular Access:** Two large-bore (16G or 14G) peripheral IV lines (or 8.5 Fr Rapid Infusion Catheter in femoral vein).
- **Massive Transfusion Protocol (MTP):** Activated if Shock Index (HR/SBP) > 1.0 or ABC Score ≥ 2.
  - *Balanced 1:1:1 Resuscitation:* Uncrossed O-negative Packed RBCs : FFP : Platelets.
  - *Tranexamic Acid (TXA):* 1 gram IV bolus over 10 min (within 3h of injury) + 1 gram IV infusion over 8 hours (CRASH-2 trial).
  - *Pelvic Binder:* Placed centered over greater trochanters for suspected open-book pelvic fracture.

### 1.4 D - Disability (Neurological Evaluation):
- **Glasgow Coma Scale (GCS):** Eye [____/4] + Verbal [____/5] + Motor [____/6] = Total [____/15].
- **Pupils:** Right: [____] mm (reactive/fixed) | Left: [____] mm (reactive/fixed).
- **Bedside Blood Glucose:** [____] mg/dL.

### 1.5 E - Exposure and Environmental Hypothermia Control:
- Full trauma shears strip of all clothing.
- Log-roll examination of spine and rectal tone (with team leader stabilizing head).
- Warm blanket coverage and fluid warmers activated to prevent the **Lethal Triad (Hypothermia, Acidosis, Coagulopathy)**.

---

## 2. ADJUNCTS TO PRIMARY SURVEY
- **eFAST Exam (Extended Focused Assessment with Sonography for Trauma):**
  - Right Upper Quadrant (Morrison's Pouch): [Negative / Free Fluid Present]
  - Left Upper Quadrant (Splenorenal Recess): [Negative / Free Fluid Present]
  - Pelvis / Retrovesical Pouch: [Negative / Free Fluid Present]
  - Pericardial Window: [No Tamponade / Pericardial Effusion]
  - Bilateral Lung Apices (Pneumothorax): [Lung sliding present / Absent sliding with barcode sign]
- **Trauma Series Radiographs:** Portable Chest X-ray, AP Pelvis, Lateral C-Spine.

---

## 3. SECONDARY SURVEY & DEFINITIVE DISPOSITION
- Comprehensive head-to-toe physical examination performed once primary survey stabilized.
- Transfer directly to:
  - [ ] Emergency Operating Room (Exploratory Laparotomy / Damage Control Surgery)
  - [ ] Interventional Radiology Suite (Pelvic / Splenic Angioembolization)
  - [ ] Trauma CT Scanner (Pan-Scan: Head, C-Spine, Chest/Abdomen/Pelvis with IV Contrast)
  - [ ] Surgical Intensive Care Unit (SICU)
            """.trimIndent()
        ),
        MedicalTemplate(
            id = "template_discharge_summary",
            title = "Hospital Discharge Summary & Handover",
            description = "Complete inpatient hospital discharge summary with hospital course, reconciled discharge medications, and follow-up plan.",
            category = TemplateCategory.WARD_CLINICAL,
            docType = "Clinical Protocol",
            specialty = "Internal Medicine",
            estimatedWordCount = "700-1,100 words",
            tags = listOf("Discharge Summary", "Handover", "Medication Reconciliation", "Inpatient", "Continuity of Care"),
            defaultTitle = "Inpatient Hospital Discharge Summary: Acute Decompensated Heart Failure",
            defaultAuthors = "Discharge Resident / Attending Physician",
            defaultAudience = "Primary Care Physician, Outpatient Cardiologist, and Patient",
            templateContent = """
# INPATIENT HOSPITAL DISCHARGE SUMMARY

**Patient Name:** [____________________]  
**MRN:** [__________] | **Date of Birth:** [DD/MM/YYYY]  
**Admission Date:** [DD/MM/YYYY] | **Discharge Date:** [DD/MM/YYYY]  
**Length of Stay:** [____] Days  
**Primary Attending Physician:** Dr. [____________________], MD  
**Primary Discharge Diagnosis:** Acute Decompensated Heart Failure with Reduced Ejection Fraction (HFrEF, NYHA Class III)  
**Secondary Diagnoses:**  
1. Hypertensive Heart Disease  
2. Chronic Kidney Disease (CKD Stage 3a)  
3. Atrial Fibrillation (Rate-controlled)  
4. Type 2 Diabetes Mellitus  

---

## 1. REASON FOR ADMISSION & CLINICAL SUMMARY
The patient is a [____]-year-old [male/female] with known ischemic cardiomyopathy who presented to the Emergency Department with a 5-day history of progressive dyspnea on exertion, orthopnea (3 pillows), paroxysmal nocturnal dyspnea, and bilateral lower extremity pitting edema (+3 up to mid-calf).

---

## 2. HOSPITAL COURSE & KEY INTERVENTIONS
### 2.1 Cardiovascular & Decongestion:
- Admitted to the cardiac telemetry unit. Initial BNP was 1,840 pg/mL. Transthoracic echocardiogram (TTE) demonstrated LVEF of 30-35% with global hypokinesis and moderate mitral regurgitation.
- Managed with aggressive IV Furosemide (40 mg IV BID) with gradual transition to oral loop diuretic.
- Net negative fluid balance achieved during hospitalization: **- 5.4 Liters**, resulting in complete resolution of peripheral edema, orthopnea, and lung crackles.
- Guideline-Directed Medical Therapy (GDMT) optimized prior to discharge: Initiated Sacubitril/Valsartan and Empagliflozin in accordance with current heart failure guidelines.

### 2.2 Renal & Electrolyte Monitoring:
- Baseline creatinine on admission was 1.4 mg/dL; peaked at 1.6 mg/dL during diuresis and stabilized at 1.35 mg/dL on day of discharge. Potassium remained 4.0 - 4.4 mmol/L.

---

## 3. RECONCILED DISCHARGE MEDICATIONS
| Medication Name | Dose & Route | Frequency | Special Instructions |
| :--- | :--- | :--- | :--- |
| **Sacubitril / Valsartan (Entresto)** | 24/26 mg PO | BID | GDMT 1st Pillar - monitor BP; titrate in 2-4 weeks |
| **Bisoprolol** | 5 mg PO | Daily in morning | GDMT 2nd Pillar - hold if HR < 55 bpm |
| **Spironolactone** | 25 mg PO | Daily | GDMT 3rd Pillar - check potassium in 7-10 days |
| **Empagliflozin (Jardiance)** | 10 mg PO | Daily | GDMT 4th Pillar (SGLT2i) |
| **Furosemide (Lasix)** | 40 mg PO | Daily in morning | Take in morning; weigh self daily |
| **Apixaban (Eliquis)** | 5 mg PO | BID | Anticoagulation for stroke prevention in AF |
| **Atorvastatin** | 40 mg PO | At bedtime | Lipid-lowering therapy |

---

## 4. DISCHARGE INSTRUCTIONS & WARNING SIGNS
- **Fluid & Sodium Restriction:** Maintain fluid intake < 2.0 Liters/day and dietary sodium < 2,000 mg/day.
- **Daily Weight Protocol:** Weigh yourself every morning after first void and before breakfast.
- **RED FLAG WARNING:** Call the cardiology clinic or present to the nearest ED if:
  - Weight gain of **> 3 lbs in 1 day** or **> 5 lbs in 1 week**.
  - Worsening shortness of breath when lying flat or waking up gasping.
  - Increasing swelling in feet, ankles, or abdomen.

---

## 5. FOLLOW-UP APPOINTMENTS & ORDERS
1. **Outpatient Cardiology Clinic:** Dr. [______] in 2 weeks on [DD/MM/YYYY] at 10:00 AM.
2. **Primary Care Provider (PCP):** Follow-up in 4 weeks.
3. **Laboratory Orders:** Repeat Serum Creatinine, eGFR, and Electrolytes in **7 to 10 days** at local outpatient lab.
            """.trimIndent()
        )
    )
}
