package com.example.data.calculator

import com.example.data.model.CalculationResult
import com.example.data.model.CalculatorCategory
import com.example.data.model.CalculatorQuestion
import com.example.data.model.RiskSeverity
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

object ClinicalCalculatorEngine {

    // 1. Wells PE Score
    fun calculateWellsPE(
        clinicalSignsDVT: Boolean,
        peLikelyAlternative: Boolean,
        heartRateOver100: Boolean,
        immobilizationOrSurgery: Boolean,
        priorDVTorPE: Boolean,
        hemoptysis: Boolean,
        activeMalignancy: Boolean
    ): CalculationResult {
        var score = 0.0
        if (clinicalSignsDVT) score += 3.0
        if (peLikelyAlternative) score += 3.0
        if (heartRateOver100) score += 1.5
        if (immobilizationOrSurgery) score += 1.5
        if (priorDVTorPE) score += 1.5
        if (hemoptysis) score += 1.0
        if (activeMalignancy) score += 1.0

        val (risk, category, interp, recs) = when {
            score > 6.0 -> Quadruple(
                RiskSeverity.HIGH,
                "High Probability (PE Likely >60%)",
                "High clinical probability of pulmonary embolism. CT Pulmonary Angiography (CTPA) indicated immediately. Consider empirical therapeutic anticoagulation while awaiting imaging if no contraindications.",
                listOf(
                    "Order urgent CT Pulmonary Angiogram (CTPA).",
                    "If CTPA contraindicated (e.g. severe renal failure, contrast allergy), order V/Q scan or Lower Extremity Duplex Ultrasound.",
                    "Initiate weight-adjusted therapeutic anticoagulation (LMWH, UFH, or DOAC) immediately if bleeding risk is acceptable."
                )
            )
            score >= 2.0 -> Quadruple(
                RiskSeverity.MODERATE,
                "Moderate Probability (PE Likely 28-35%)",
                "Moderate pre-test probability. Diagnostic strategy depends on institutional protocol; age-adjusted High-Sensitivity D-Dimer vs. direct CTPA.",
                listOf(
                    "Obtain High-Sensitivity D-Dimer test (use age-adjusted cutoff for age >50: Age x 10 ug/L).",
                    "If D-Dimer positive: Proceed to CTPA.",
                    "If D-Dimer negative: PE safely excluded without imaging."
                )
            )
            else -> Quadruple(
                RiskSeverity.LOW,
                "Low Probability (PE Unlikely <10%)",
                "Low pre-test probability of pulmonary embolism. Apply Pulmonary Embolism Rule-out Criteria (PERC). If PERC negative, no further testing required.",
                listOf(
                    "Evaluate PERC criteria (Age <50, HR <100, SaO2 >=95%, no hemoptysis, no estrogen, no prior VTE, no surgery, no unilateral leg swelling).",
                    "If PERC negative (all 8 met): 0% further workup needed.",
                    "If any PERC criterion positive: Order High-Sensitivity D-Dimer."
                )
            )
        }

        return CalculationResult(
            calculatorId = "wells_pe",
            calculatorName = "Wells Criteria for Pulmonary Embolism (PE)",
            scoreValue = "$score points",
            riskCategory = risk,
            interpretation = interp,
            recommendations = recs,
            evidenceReference = "Wells PS et al. Thromb Haemost 2000; 83: 416-420. ESC 2019 PE Guidelines."
        )
    }

    // 2. CHA2DS2-VASc Score
    fun calculateCHA2DS2VASc(
        chf: Boolean,
        hypertension: Boolean,
        ageCategory: Int, // 0: <65 (0pts), 1: 65-74 (1pt), 2: >=75 (2pts)
        diabetes: Boolean,
        strokeOrTia: Boolean, // 2pts
        vascularDisease: Boolean, // MI, PAD, aortic plaque (1pt)
        isFemale: Boolean // 1pt
    ): CalculationResult {
        var score = 0
        if (chf) score += 1
        if (hypertension) score += 1
        if (ageCategory == 2) score += 2
        else if (ageCategory == 1) score += 1
        if (diabetes) score += 1
        if (strokeOrTia) score += 2
        if (vascularDisease) score += 1
        if (isFemale) score += 1

        val adjustedScoreForMen = if (isFemale) score - 1 else score
        val annualStrokeRisk = when (score) {
            0 -> "0.2%"
            1 -> "0.6%"
            2 -> "2.2%"
            3 -> "3.2%"
            4 -> "4.8%"
            5 -> "7.2%"
            6 -> "9.7%"
            7 -> "11.2%"
            8 -> "12.5%"
            else -> "15.2%"
        }

        val (risk, interp, recs) = when {
            (isFemale && score >= 3) || (!isFemale && score >= 2) -> Triple(
                RiskSeverity.HIGH,
                "High thromboembolic stroke risk (Annual stroke risk ~$annualStrokeRisk). Oral anticoagulation strongly recommended.",
                listOf(
                    "Oral Anticoagulation (OAC) strongly indicated (Class I recommendation).",
                    "Direct Oral Anticoagulants (DOACs: Apixaban, Rivaroxaban, Dabigatran, Edoxaban) preferred over Warfarin in non-valvular AF.",
                    "Assess bleeding risk using HAS-BLED score and address modifiable bleeding factors."
                )
            )
            (isFemale && score == 2) || (!isFemale && score == 1) -> Triple(
                RiskSeverity.MODERATE,
                "Intermediate thromboembolic risk (Annual stroke risk ~$annualStrokeRisk). Oral anticoagulation should be considered.",
                listOf(
                    "Oral Anticoagulation should be considered (Class IIa recommendation).",
                    "Engage in shared decision-making discussing stroke reduction vs. bleeding risk.",
                    "Aspirin monotherapy is NOT recommended for stroke prevention in AF."
                )
            )
            else -> Triple(
                RiskSeverity.LOW,
                "Low thromboembolic risk (Annual stroke risk ~$annualStrokeRisk). No antithrombotic therapy indicated.",
                listOf(
                    "No antithrombotic therapy recommended (Class III - harm).",
                    "Re-evaluate stroke risk factors annually or if patient develops new comorbidities (hypertension, diabetes, age transition)."
                )
            )
        }

        return CalculationResult(
            calculatorId = "cha2ds2_vasc",
            calculatorName = "CHA₂DS₂-VASc Score for Atrial Fibrillation Stroke Risk",
            scoreValue = "$score points (Annual Stroke Risk ~$annualStrokeRisk)",
            riskCategory = risk,
            interpretation = interp,
            recommendations = recs,
            evidenceReference = "Lip GY et al. Chest 2010; 137(2): 263-272. 2023 ACC/AHA/ACCP/HRS Guideline for Atrial Fibrillation."
        )
    }

    // 3. CURB-65 Pneumonia Severity Score
    fun calculateCURB65(
        confusion: Boolean,
        ureaHigh: Boolean, // BUN > 19 mg/dL or Urea > 7 mmol/L
        respiratoryRateHigh: Boolean, // RR >= 30 /min
        bloodPressureLow: Boolean, // SBP < 90 or DBP <= 60 mmHg
        age65OrOlder: Boolean
    ): CalculationResult {
        var score = 0
        if (confusion) score++
        if (ureaHigh) score++
        if (respiratoryRateHigh) score++
        if (bloodPressureLow) score++
        if (age65OrOlder) score++

        val (risk, interp, recs) = when (score) {
            0, 1 -> Triple(
                RiskSeverity.LOW,
                "Low risk of 30-day mortality (0.6% - 2.7%). Suitable for outpatient treatment.",
                listOf(
                    "Patient can typically be treated in the outpatient setting.",
                    "First-line outpatient empiric antibiotics: Amoxicillin or Macrolide (Azithromycin/Clarithromycin) or Doxycycline.",
                    "Ensure safety netting: Return to ER if worsening dyspnea, confusion, or persistent fever >48h."
                )
            )
            2 -> Triple(
                RiskSeverity.MODERATE,
                "Moderate risk of 30-day mortality (~6.8%). Hospital inpatient admission or close supervised outpatient setting.",
                listOf(
                    "Short inpatient hospital admission or supervised ambulatory care.",
                    "Inpatient empiric antibiotics: Beta-lactam (Ceftriaxone / Ampicillin-Sulbactam) PLUS Macrolide (Azithromycin) OR Respiratory Fluoroquinolone (Levofloxacin/Moxifloxacin).",
                    "Monitor pulse oximetry, renal function, and vital signs."
                )
            )
            else -> Triple(
                RiskSeverity.HIGH,
                "High risk of 30-day mortality (14% - 27.8%). Urgent hospital admission; evaluate for ICU.",
                listOf(
                    "Urgent inpatient hospitalization.",
                    "For score 4-5: Assess immediate criteria for Intensive Care Unit (ICU) admission.",
                    "Initiate IV broad-spectrum antibiotics within 1 hour of hospital arrival.",
                    "Obtain blood cultures and sputum Gram stain prior to antibiotic initiation if feasible."
                )
            )
        }

        return CalculationResult(
            calculatorId = "curb_65",
            calculatorName = "CURB-65 Pneumonia Severity Score",
            scoreValue = "$score / 5 points",
            riskCategory = risk,
            interpretation = interp,
            recommendations = recs,
            evidenceReference = "Lim WS et al. Thorax 2003; 58: 377-382. British Thoracic Society (BTS) & ATS/IDSA Guidelines."
        )
    }

    // 4. Glasgow Coma Scale (GCS)
    fun calculateGCS(
        eyeOpening: Int, // 1 to 4
        verbalResponse: Int, // 1 to 5
        motorResponse: Int // 1 to 6
    ): CalculationResult {
        val total = eyeOpening + verbalResponse + motorResponse
        val (risk, interp, recs) = when {
            total <= 8 -> Triple(
                RiskSeverity.CRITICAL,
                "Severe Brain Injury / Coma (GCS <= 8). High risk of airway compromise.",
                listOf(
                    "Intubation and mechanical ventilation strongly indicated for airway protection ('GCS 8, Intubate').",
                    "Immediate Emergency CT Brain without contrast to evaluate for acute hemorrhage, herniation, or midline shift.",
                    "Urgent Neurosurgical consultation.",
                    "Maintain cerebral perfusion pressure (CPP > 60 mmHg) and avoid hypoxia/hypotension."
                )
            )
            total in 9..12 -> Triple(
                RiskSeverity.MODERATE,
                "Moderate Head Injury (GCS 9-12).",
                listOf(
                    "Urgent non-contrast Head CT indicated (Canadian CT Head Rule / New Orleans Criteria).",
                    "Admit to step-down or ICU for serial neurological monitoring every 1-2 hours.",
                    "Frequent pupillary checks and repeat GCS assessments."
                )
            )
            else -> Triple(
                RiskSeverity.LOW,
                "Mild / Minimal Neurological Impairment (GCS 13-15).",
                listOf(
                    "Evaluate need for Head CT based on high-risk features (vomiting >=2, age >=65, retrograde amnesia >30 min, dangerous mechanism).",
                    "Serial neuro observation for at least 4-6 hours.",
                    "Discharge with clear written head injury return precautions if CT normal and GCS 15."
                )
            )
        }

        return CalculationResult(
            calculatorId = "gcs_scale",
            calculatorName = "Glasgow Coma Scale (GCS)",
            scoreValue = "$total / 15 (E$eyeOpening V$verbalResponse M$motorResponse)",
            riskCategory = risk,
            interpretation = interp,
            recommendations = recs,
            evidenceReference = "Teasdale G, Jennett B. Lancet 1974; 2(7872): 81-84. Advanced Trauma Life Support (ATLS) 10th Ed."
        )
    }

    // 5. MELD Score (Model for End-Stage Liver Disease)
    fun calculateMELD(
        serumBilirubinMgDl: Double,
        serumInr: Double,
        serumCreatinineMgDl: Double,
        serumSodiumMeqL: Double,
        onDialysisLastWeek: Boolean
    ): CalculationResult {
        val cr = if (onDialysisLastWeek) 4.0 else max(1.0, min(serumCreatinineMgDl, 4.0))
        val bili = max(1.0, serumBilirubinMgDl)
        val inr = max(1.0, serumInr)

        val rawMeld = 9.57 * ln(cr) + 3.78 * ln(bili) + 11.2 * ln(inr) + 6.43
        var meldScore = (rawMeld).roundToInt()

        // MELD-Na adjustment if MELD > 11
        if (meldScore > 11) {
            val na = max(125.0, min(serumSodiumMeqL, 137.0))
            val meldNa = meldScore + 1.32 * (137 - na) - (0.033 * meldScore * (137 - na))
            meldScore = meldNa.roundToInt()
        }

        val estimated90DayMortality = when {
            meldScore >= 40 -> "71.3% - 100%"
            meldScore >= 30 -> "52.6%"
            meldScore >= 20 -> "19.6%"
            meldScore >= 10 -> "6.0%"
            else -> "< 1.9%"
        }

        val (risk, interp, recs) = when {
            meldScore >= 25 -> Triple(
                RiskSeverity.CRITICAL,
                "Severe end-stage liver disease (MELD-Na: $meldScore, Estimated 90-day mortality: $estimated90DayMortality). High priority for liver transplantation.",
                listOf(
                    "Immediate referral to Liver Transplant Center for priority listing.",
                    "Screen and manage complications: Spontaneous Bacterial Peritonitis (SBP), Hepatorenal Syndrome (HRS), and Esophageal Varices.",
                    "Avoid nephrotoxic agents (NSAIDs, aminoglycosides) and excessive paracentesis without albumin."
                )
            )
            meldScore >= 15 -> Triple(
                RiskSeverity.HIGH,
                "Moderate-to-Severe hepatic dysfunction (MELD-Na: $meldScore, Estimated 90-day mortality: $estimated90DayMortality).",
                listOf(
                    "Referral for Liver Transplantation evaluation is indicated (threshold MELD >= 15).",
                    "Surveillance for Hepatocellular Carcinoma (HCC) with abdominal ultrasound + AFP every 6 months.",
                    "Screen for gastroesophageal varices via EGD."
                )
            )
            else -> Triple(
                RiskSeverity.LOW,
                "Compensated / Mild hepatic dysfunction (MELD-Na: $meldScore, Estimated 90-day mortality: $estimated90DayMortality).",
                listOf(
                    "Conservative medical management of underlying etiology (Alcohol cessation, Antivirals for HBV/HCV, MASLD lifestyle).",
                    "Routine laboratory and ultrasound monitoring every 6 months."
                )
            )
        }

        return CalculationResult(
            calculatorId = "meld_na",
            calculatorName = "MELD-Na Score (Model for End-Stage Liver Disease)",
            scoreValue = "$meldScore points (90-day mortality: $estimated90DayMortality)",
            riskCategory = risk,
            interpretation = interp,
            recommendations = recs,
            evidenceReference = "Kim WR et al. N Engl J Med 2008; 359: 1018-1026. UNOS / OPTN Policy."
        )
    }

    // 6. eGFR CKD-EPI (2021 Race-Free) & Cockcroft-Gault
    fun calculateeGFR(
        age: Int,
        isFemale: Boolean,
        serumCreatinineMgDl: Double,
        weightKg: Double = 70.0
    ): CalculationResult {
        val scr = max(0.2, serumCreatinineMgDl)
        val k = if (isFemale) 0.7 else 0.9
        val alpha = if (isFemale) -0.241 else -0.302
        val minRatio = min(scr / k, 1.0)
        val maxRatio = max(scr / k, 1.0)
        val genderFactor = if (isFemale) 1.012 else 1.0

        val egfr = (142 * minRatio.pow(alpha) * maxRatio.pow(-1.200) * 0.9938.pow(age.toDouble()) * genderFactor).roundToInt()

        // Cockcroft-Gault CrCl
        val crCl = (((140 - age) * weightKg) / (72 * scr) * (if (isFemale) 0.85 else 1.0)).roundToInt()

        val (stage, risk, interp, recs) = when {
            egfr >= 90 -> Quadruple(
                "Stage G1 (Normal or High)",
                RiskSeverity.LOW,
                "Normal renal function (eGFR: $egfr mL/min/1.73m², CrCl: $crCl mL/min).",
                listOf(
                    "Standard drug dosing for normal renal function.",
                    "Annual screening for microalbuminuria in diabetic and hypertensive patients."
                )
            )
            egfr >= 60 -> Quadruple(
                "Stage G2 (Mildly Decreased)",
                RiskSeverity.LOW,
                "Mildly decreased renal function (eGFR: $egfr mL/min/1.73m², CrCl: $crCl mL/min).",
                listOf(
                    "Monitor eGFR and urine albumin-to-creatinine ratio (uACR) annually.",
                    "Manage cardiovascular risk factors and maintain blood pressure < 120/80 (KDIGO)."
                )
            )
            egfr >= 45 -> Quadruple(
                "Stage G3a (Mild-to-Moderate CKD)",
                RiskSeverity.MODERATE,
                "Mild-to-moderate CKD (eGFR: $egfr mL/min/1.73m², CrCl: $crCl mL/min).",
                listOf(
                    "Check and adjust doses for renally cleared medications (DOACs, Metformin, Gabapentin, Antibiotics).",
                    "Initiate SGLT2 inhibitor (Empagliflozin/Dapagliflozin) for kidney and cardiovascular protection.",
                    "Recheck renal panel every 6 months."
                )
            )
            egfr >= 30 -> Quadruple(
                "Stage G3b (Moderate-to-Severe CKD)",
                RiskSeverity.HIGH,
                "Moderate-to-severe CKD (eGFR: $egfr mL/min/1.73m², CrCl: $crCl mL/min). High risk of drug accumulation.",
                listOf(
                    "Strict renal dosage adjustment mandatory.",
                    "Avoid NSAIDs and iodinated IV radiocontrast if possible.",
                    "Screen for anemia of CKD (iron, ferritin, Hb) and mineral bone disease (calcium, phosphate, PTH)."
                )
            )
            egfr >= 15 -> Quadruple(
                "Stage G4 (Severe CKD)",
                RiskSeverity.HIGH,
                "Severe CKD (eGFR: $egfr mL/min/1.73m², CrCl: $crCl mL/min). Preparation for renal replacement therapy.",
                listOf(
                    "Nephrology referral mandatory.",
                    "Plan for vascular access (AV fistula) or peritoneal dialysis catheter.",
                    "Vaccinate against Hepatitis B prior to dialysis initiation."
                )
            )
            else -> Quadruple(
                "Stage G5 (Kidney Failure / ESRD)",
                RiskSeverity.CRITICAL,
                "End-Stage Kidney Disease / Renal Failure (eGFR: $egfr mL/min/1.73m², CrCl: $crCl mL/min). Dialysis or transplant required.",
                listOf(
                    "Initiate renal replacement therapy (Hemodialysis / Peritoneal Dialysis) or Kidney Transplantation.",
                    "Strict management of hyperkalemia, metabolic acidosis, and fluid overload."
                )
            )
        }

        return CalculationResult(
            calculatorId = "egfr_ckdepi",
            calculatorName = "eGFR (CKD-EPI 2021) & Creatinine Clearance (Cockcroft-Gault)",
            scoreValue = "eGFR: $egfr mL/min/1.73m² ($stage) | CrCl: $crCl mL/min",
            riskCategory = risk,
            interpretation = interp,
            recommendations = recs,
            evidenceReference = "Inker LA et al. N Engl J Med 2021; 385: 1737-1749. KDIGO 2024 Clinical Practice Guideline for CKD."
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
