package com.example.data.repository

import com.example.data.model.DrugCategory
import com.example.data.model.DrugInteractionPair
import com.example.data.model.DrugMonograph
import com.example.data.model.InteractionSeverity

class DrugFormularyRepository {

    val medications: List<DrugMonograph> = listOf(
        DrugMonograph(
            id = "drug_warfarin",
            genericName = "Warfarin",
            brandNames = "Coumadin, Jantoven",
            category = DrugCategory.ANTICOAGULANTS,
            standardAdultDose = "Initial 2 - 5 mg PO daily, titrated to target INR (typically 2.0 - 3.0; 2.5 - 3.5 for mechanical mitral valves).",
            renalAdjustment = "No dose adjustment required. Monitor INR closely.",
            hepaticAdjustment = "Caution; decreased clotting factor synthesis increases sensitivity. Dose reduction usually required.",
            blackBoxWarning = "Major or fatal bleeding. Perform regular monitoring of INR. Numerous drug-drug, dietary (Vitamin K), and herbal interactions.",
            contraindications = listOf("Active severe bleeding", "Pregnancy (teratogenic / fetal warfarin syndrome)", "Severe uncontrolled malignant hypertension", "Recent lumbar puncture or epidural anesthesia"),
            commonAdverseEffects = listOf("Bleeding / Hematoma", "Skin necrosis (protein C/S deficiency)", "Purple toe syndrome", "Alopecia"),
            monitoringParameters = listOf("Prothrombin Time (PT) / INR", "Complete Blood Count (Hemoglobin/Hematocrit)", "Signs of occult bleeding (stool guaiac, hematuria)"),
            pregnancyCategory = "Category X (Contraindicated except with mechanical heart valves in selected trimesters)"
        ),
        DrugMonograph(
            id = "drug_apixaban",
            genericName = "Apixaban",
            brandNames = "Eliquis",
            category = DrugCategory.ANTICOAGULANTS,
            standardAdultDose = "Nonvalvular AF: 5 mg PO BID. Reduce to 2.5 mg BID if >=2 of: Age >=80, Body weight <=60 kg, Serum Cr >=1.5 mg/dL. DVT/PE Treatment: 10 mg BID x 7 days, then 5 mg BID.",
            pediatricDoseMgPerKg = null,
            renalAdjustment = "Standard criteria: If Serum Cr >=1.5 mg/dL and (Age >=80 or Weight <=60kg), reduce to 2.5 mg BID. In ESRD on hemodialysis: 5 mg BID (or 2.5 mg BID if age >=80 or weight <=60kg).",
            hepaticAdjustment = "Child-Pugh A: No adjustment. Child-Pugh B: Use with caution. Child-Pugh C: Not recommended / contraindicated.",
            blackBoxWarning = "Premature discontinuation increases risk of thrombotic events. Epidural/spinal hematoma risk with neuraxial anesthesia.",
            contraindications = listOf("Active pathological bleeding", "Severe hypersensitivity", "Mechanical prosthetic heart valves", "Triple-positive Antiphospholipid Syndrome"),
            commonAdverseEffects = listOf("Epistaxis", "Gastrointestinal bleeding", "Hematuria", "Bruising"),
            monitoringParameters = listOf("Renal function (eGFR/CrCl)", "CBC", "Liver function tests", "Anti-Factor Xa activity (if specialized reversal needed with Andexanet Alfa)"),
            pregnancyCategory = "Category B (Avoid unless clinical benefit clearly outweighs unknown fetal risk)"
        ),
        DrugMonograph(
            id = "drug_amox_clav",
            genericName = "Amoxicillin-Clavulanate",
            brandNames = "Augmentin",
            category = DrugCategory.ANTIBIOTICS,
            standardAdultDose = "875/125 mg PO q12h or 500/125 mg PO q8h for 7 - 14 days.",
            pediatricDoseMgPerKg = 45.0, // 45-90 mg/kg/day divided q12h
            pediatricMaxSingleDoseMg = 875.0,
            pediatricFrequency = "Divided every 12 hours (High-dose otitis media: 90 mg/kg/day)",
            renalAdjustment = "CrCl 10-30 mL/min: 500/125 mg or 250/125 mg q12h. CrCl <10 mL/min: 500/125 mg q24h. Hemodialysis: Additional dose post-dialysis.",
            hepaticAdjustment = "Use caution; monitor LFTs. Contraindicated in patients with prior Augmentin-associated cholestatic jaundice.",
            blackBoxWarning = null,
            contraindications = listOf("History of severe penicillin allergy / anaphylaxis", "History of Augmentin-associated hepatic cholestasis"),
            commonAdverseEffects = listOf("Diarrhea / Nausea (clavulanate-induced)", "Vulvovaginal candidiasis", "Maculopapular rash", "C. difficile colitis"),
            monitoringParameters = listOf("Resolution of infection signs", "Renal & hepatic function in prolonged therapy", "Stool frequency for C. diff"),
            pregnancyCategory = "Category B (Considered safe in pregnancy)"
        ),
        DrugMonograph(
            id = "drug_vancomycin",
            genericName = "Vancomycin (IV)",
            brandNames = "Vancocin",
            category = DrugCategory.ANTIBIOTICS,
            standardAdultDose = "15 - 20 mg/kg IV q8-12h (Target AUC/MIC 400 - 600 or trough 15-20 mcg/mL for severe MRSA).",
            pediatricDoseMgPerKg = 15.0,
            pediatricMaxSingleDoseMg = 1000.0,
            pediatricFrequency = "q6h - q8h based on age and renal function",
            renalAdjustment = "Strict pharmacokinetic dosing based on CrCl / AUC. CrCl 30-50: q24h. CrCl <30 or HD: Dose per serum trough levels.",
            hepaticAdjustment = "No routine dose adjustment required.",
            blackBoxWarning = null,
            contraindications = listOf("Hypersensitivity to vancomycin"),
            commonAdverseEffects = listOf("Red Man Syndrome (infusion-rate histamine release)", "Nephrotoxicity (especially with Piperacillin-Tazobactam or Aminoglycosides)", "Ototoxicity", "Phlebitis"),
            monitoringParameters = listOf("Serum Vancomycin trough or 24h AUC", "Serum Creatinine & BUN daily", "Auditory function in prolonged courses"),
            pregnancyCategory = "Category C (Use IV when indicated for serious MRSA infections)"
        ),
        DrugMonograph(
            id = "drug_lisinopril",
            genericName = "Lisinopril",
            brandNames = "Zestril, Prinivil",
            category = DrugCategory.CARDIOVASCULAR,
            standardAdultDose = "Hypertension: 10 - 40 mg PO daily. Heart Failure with reduced EF: Initial 2.5 - 5 mg daily, titrate to target 20 - 40 mg daily.",
            renalAdjustment = "CrCl 10-30 mL/min: Initial 5 mg daily. CrCl <10 mL/min: Initial 2.5 mg daily. Hemodialysis: 2.5 mg daily post-dialysis.",
            hepaticAdjustment = "Lisinopril is not metabolized by the liver (not a prodrug); safe in hepatic dysfunction.",
            blackBoxWarning = "Fetal toxicity: Discontinue as soon as pregnancy is detected (causes oligohydramnios, fetal renal failure, craniofacial deformities).",
            contraindications = listOf("History of ACE-inhibitor induced angioedema", "Concomitant use with Sacubitril/Valsartan (allow 36h washout)", "Bilateral renal artery stenosis", "Pregnancy"),
            commonAdverseEffects = listOf("Dry hacking cough (bradykinin accumulation)", "Hyperkalemia", "Hypotension / Dizziness", "Acute increase in Serum Creatinine", "Angioedema (life-threatening airway swelling)"),
            monitoringParameters = listOf("Blood pressure", "Serum Potassium", "Serum Creatinine / BUN (expect <=30% rise, hold if >30% rise)"),
            pregnancyCategory = "Category D (Contraindicated in 2nd and 3rd trimesters)"
        ),
        DrugMonograph(
            id = "drug_amiodarone",
            genericName = "Amiodarone",
            brandNames = "Cordarone, Pacerone, Nexterone",
            category = DrugCategory.CARDIOVASCULAR,
            standardAdultDose = "Ventricular Arrhythmias / AF Loading: 400 mg TID or 400 mg BID x 1-2 weeks, then 200 mg PO daily maintenance. Cardiac Arrest (VF/pVT): 300 mg IV push, second dose 150 mg IV.",
            renalAdjustment = "No dose adjustment required in renal impairment or hemodialysis.",
            hepaticAdjustment = "Extensive hepatic metabolism. Dose reduction and close monitoring required in moderate-to-severe hepatic impairment.",
            blackBoxWarning = "Pulmonary toxicity (fatal interstitial pneumonitis/fibrosis), hepatotoxicity, and proarrhythmic exacerbation (QT prolongation / Torsades de Pointes). Only initiate in hospitalized patients with cardiac monitoring.",
            contraindications = listOf("Severe sinus node dysfunction / Sick Sinus Syndrome without pacemaker", "2nd or 3rd degree AV block", "Cardiogenic shock", "Known iodine hypersensitivity"),
            commonAdverseEffects = listOf("Pulmonary fibrosis", "Thyroid dysfunction (both hypothyroidism 15% and hyperthyroidism 3%)", "Corneal microdeposits (haloes)", "Blue-gray skin discoloration (slate skin)", "Hepatotoxicity", "Peripheral neuropathy"),
            monitoringParameters = listOf("Baseline & periodic Chest X-Ray / Pulmonary Function Tests (DLCO)", "TSH, Free T4 every 3-6 months", "AST, ALT, Bilirubin every 6 months", "ECG for QTc interval prolongation (keep QTc < 500 ms)", "Annual ophthalmology exam"),
            pregnancyCategory = "Category D (Fetal harm; passes into breast milk with high iodine content)"
        ),
        DrugMonograph(
            id = "drug_metformin",
            genericName = "Metformin",
            brandNames = "Glucophage, Fortamet",
            category = DrugCategory.ENDOCRINE_METABOLIC,
            standardAdultDose = "Initial 500 mg PO BID or 850 mg daily with meals; titrate to maximum 2000 - 2550 mg daily in divided doses.",
            renalAdjustment = "eGFR >=45: No adjustment. eGFR 30-44: Max 1000 mg daily; do not initiate. eGFR <30: Contraindicated (risk of lactic acidosis).",
            hepaticAdjustment = "Avoid in severe liver disease or alcohol abuse due to impaired lactate clearance.",
            blackBoxWarning = "Lactic acidosis: Rare but fatal metabolic acidosis. Risk increases in renal impairment, sepsis, hypoxemia, congestive heart failure, and iodinated contrast administration.",
            contraindications = listOf("Severe renal failure (eGFR < 30 mL/min/1.73m²)", "Acute metabolic or diabetic ketoacidosis", "Severe tissue hypoperfusion / sepsis / shock"),
            commonAdverseEffects = listOf("Gastrointestinal distress (diarrhea, abdominal cramps, nausea)", "Metallic taste", "Vitamin B12 deficiency (with long-term use)"),
            monitoringParameters = listOf("eGFR / Serum Creatinine baseline and at least annually", "HbA1c every 3 months", "Serum Vitamin B12 levels every 2-3 years"),
            pregnancyCategory = "Category B (Widely used in gestational diabetes)"
        ),
        DrugMonograph(
            id = "drug_fluconazole",
            genericName = "Fluconazole",
            brandNames = "Diflucan",
            category = DrugCategory.ANTIBIOTICS,
            standardAdultDose = "Candidiasis: 200 - 400 mg PO/IV loading, then 100 - 200 mg daily. Vaginal candidiasis: 150 mg PO single dose. Cryptococcal Meningitis: 400 - 800 mg daily.",
            renalAdjustment = "CrCl 11-50 mL/min: Administer 50% of standard dose. Hemodialysis: 100% of standard dose after each dialysis session.",
            hepaticAdjustment = "Potent CYP2C9 and CYP3A4 inhibitor; monitor LFTs.",
            blackBoxWarning = null,
            contraindications = listOf("Co-administration with CYP3A4 substrates that prolong QT interval (Pimozide, Quinidine, Cisapride)"),
            commonAdverseEffects = listOf("Headache", "Nausea", "Elevated transaminases (ALT/AST)", "QTc prolongation"),
            monitoringParameters = listOf("Liver enzymes (AST, ALT)", "Renal function", "ECG for QTc if combined with other QT-prolonging drugs", "Interacting drug levels (Warfarin INR, Tacrolimus, Phenytoin)"),
            pregnancyCategory = "Category D (High-dose chronic use associated with congenital anomalies)"
        ),
        DrugMonograph(
            id = "drug_spironolactone",
            genericName = "Spironolactone",
            brandNames = "Aldactone",
            category = DrugCategory.CARDIOVASCULAR,
            standardAdultDose = "HFrEF: 12.5 - 25 mg PO daily (titrate to 50 mg). Ascites/Cirrhosis: 100 mg daily (titrate with Furosemide in 100:40 ratio). Resistant HTN: 25 - 50 mg daily.",
            renalAdjustment = "CrCl 30-50 mL/min: Initial 12.5 mg daily or every other day. CrCl <30 mL/min: Contraindicated (severe hyperkalemia risk).",
            hepaticAdjustment = "Drug of choice for cirrhotic ascites; titrate slowly to avoid precipitating encephalopathy.",
            blackBoxWarning = "Tumorigenic in chronic animal toxicity studies at high doses. Avoid unnecessary use.",
            contraindications = listOf("Hyperkalemia (Serum K+ > 5.0 mEq/L at baseline)", "Severe renal impairment (eGFR < 30 mL/min)", "Addison's disease", "Concomitant Eplerenone"),
            commonAdverseEffects = listOf("Hyperkalemia", "Gynecomastia & breast tenderness (anti-androgen effect)", "Menstrual irregularities", "Dehydration / Hyponatremia"),
            monitoringParameters = listOf("Serum Potassium and Creatinine at 1 week, 4 weeks, and every 3-6 months", "Blood pressure"),
            pregnancyCategory = "Category C"
        )
    )

    private val interactionsDatabase: List<DrugInteractionPair> = listOf(
        DrugInteractionPair(
            drug1Name = "Warfarin",
            drug2Name = "Fluconazole",
            severity = InteractionSeverity.MAJOR_AVOID,
            mechanism = "Fluconazole is a potent inhibitor of CYP2C9, the primary enzyme responsible for metabolizing the more active S-warfarin enantiomer.",
            clinicalEffect = "Dramatic elevation of plasma Warfarin concentrations, severe prolongation of INR (often > 10.0), and life-threatening gastrointestinal or intracranial bleeding.",
            managementAction = "Avoid combination if possible. If unavoidable, empirically reduce Warfarin maintenance dose by 50% upon initiating Fluconazole. Check INR on day 3, 5, and every 48h until stable."
        ),
        DrugInteractionPair(
            drug1Name = "Lisinopril",
            drug2Name = "Spironolactone",
            severity = InteractionSeverity.MODERATE_MONITOR,
            mechanism = "Additive potassium retention: ACE inhibitors decrease Aldosterone secretion while Spironolactone directly antagonizes the mineralocorticoid receptor in the distal tubule.",
            clinicalEffect = "Synergistic risk of severe, life-threatening hyperkalemia (K+ > 6.0 mEq/L) leading to cardiac conduction blocks, peaked T waves, and fatal arrhythmias.",
            managementAction = "Frequently used together in HFrEF, but requires strict biochemical surveillance. Check serum K+ and Creatinine within 3-7 days of initiation, at 4 weeks, and quarterly thereafter. Advise patient to avoid salt substitutes containing potassium."
        ),
        DrugInteractionPair(
            drug1Name = "Amiodarone",
            drug2Name = "Warfarin",
            severity = InteractionSeverity.MAJOR_AVOID,
            mechanism = "Amiodarone inhibits CYP2C9 and CYP3A4, reducing Warfarin clearance with an onset of effect that may continue for months due to Amiodarone's long half-life (40-60 days).",
            clinicalEffect = "Profound increase in INR and severe hemorrhage risk.",
            managementAction = "Empirically reduce Warfarin maintenance dose by 33% to 50% when starting Amiodarone. Monitor INR weekly for the first 6 weeks."
        ),
        DrugInteractionPair(
            drug1Name = "Vancomycin",
            drug2Name = "Lisinopril",
            severity = InteractionSeverity.MODERATE_MONITOR,
            mechanism = "Combined hemodynamic glomerular efferent arteriolar vasodilation (ACEi) with direct proximal tubular toxicity (Vancomycin).",
            clinicalEffect = "Accelerated onset of Acute Kidney Injury (AKI) and diminished Vancomycin renal clearance.",
            managementAction = "Monitor Serum Creatinine daily. Track Vancomycin AUC24 / trough levels. Maintain adequate hydration and avoid concomitant NSAIDs."
        ),
        DrugInteractionPair(
            drug1Name = "Apixaban",
            drug2Name = "Fluconazole",
            severity = InteractionSeverity.MODERATE_MONITOR,
            mechanism = "Fluconazole inhibits CYP3A4 and P-glycoprotein (P-gp), leading to increased systemic exposure to Apixaban.",
            clinicalEffect = "Increased plasma concentrations of Apixaban and elevated risk of bleeding.",
            managementAction = "If patient is receiving 5 mg or 10 mg twice daily of Apixaban, reduce dose by 50% when coadministered with combined strong inhibitors of CYP3A4 and P-gp. If already receiving 2.5 mg BID, avoid co-administration."
        )
    )

    fun checkInteractions(selectedDrugNames: List<String>): List<DrugInteractionPair> {
        if (selectedDrugNames.size < 2) return emptyList()
        val results = mutableListOf<DrugInteractionPair>()

        for (i in 0 until selectedDrugNames.size) {
            for (j in i + 1 until selectedDrugNames.size) {
                val d1 = selectedDrugNames[i].trim()
                val d2 = selectedDrugNames[j].trim()

                val found = interactionsDatabase.find {
                    (it.drug1Name.equals(d1, ignoreCase = true) && it.drug2Name.equals(d2, ignoreCase = true)) ||
                            (it.drug1Name.equals(d2, ignoreCase = true) && it.drug2Name.equals(d1, ignoreCase = true))
                }
                if (found != null) {
                    results.add(found)
                }
            }
        }
        return results
    }
}
