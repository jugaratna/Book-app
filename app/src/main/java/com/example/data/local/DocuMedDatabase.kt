package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DocumentVersion
import com.example.data.model.MedicalDocument
import com.example.data.model.SourceMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [MedicalDocument::class, SourceMaterial::class, DocumentVersion::class],
    version = 1,
    exportSchema = false
)
abstract class DocuMedDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun sourceMaterialDao(): SourceMaterialDao
    abstract fun versionDao(): VersionDao

    companion object {
        @Volatile
        private var INSTANCE: DocuMedDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DocuMedDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DocuMedDatabase::class.java,
                    "documed_studio_database"
                )
                    .addCallback(DocuMedDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DocuMedDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.documentDao(), database.sourceMaterialDao(), database.versionDao())
                }
            }
        }
    }
}

suspend fun populateInitialData(
    documentDao: DocumentDao,
    sourceMaterialDao: SourceMaterialDao,
    versionDao: VersionDao
) {
    // 1. Preloaded Medical Textbook Chapter: Fracture Neck of Femur
    val doc1Content = """
# 1.0 Introduction
Fractures of the femoral neck represent one of the most common and devastating orthopedic injuries in the elderly population and a high-energy surgical challenge in young patients. They carry significant morbidity, high mortality rates (up to 20-30% at 1 year in geriatric cohorts), and profound socioeconomic implications.

## 1.1 Anatomy & Vascular Vulnerability
The blood supply to the femoral head is predominantly derived from the medial femoral circumflex artery (MFCA) via lateral epiphyseal arteries running in the retinacula of Weitbrecht. Intracapsular displacement readily shears these vessels, predisposing to avascular necrosis (AVN) and non-union.

## 1.2 Garden Classification System
* **Garden Stage I**: Incomplete/impacted fracture with valgus alignment of trabeculae.
* **Garden Stage II**: Complete fracture without displacement; trabecular alignment preserved.
* **Garden Stage III**: Complete fracture with partial displacement; femoral head tilted into varus.
* **Garden Stage IV**: Complete fracture with total displacement; femoral head trabeculae realigned with acetabular roof.

[KEY_POINT: In patients >65 years with displaced fractures (Garden III/IV), arthroplasty yields superior functional outcomes and lower revision rates compared to internal fixation.]

## 1.3 Clinical Evaluation & Imaging
Patients typically present with severe groin pain, inability to bear weight, and a shortened, externally rotated lower extremity. Standard radiographic assessment includes AP Pelvis, AP Hip, and Cross-table Lateral views. MRI or CT is indicated if radiographs are equivocal.

## 1.4 Treatment Protocol & Surgical Decision Algorithm
Management is guided by patient age, pre-injury functional status, and fracture displacement:
* **Young Patients (<60 years)**: Emergency anatomical reduction and rigid internal fixation (multiple cannulated cancellous screws or sliding hip screw with anti-rotation screw) within 6-24 hours to preserve the native femoral head.
* **Active Elderly Patients (60-80 years, independent)**: Total Hip Arthroplasty (THA).
* **Frail / Low-demand Elderly (>80 years)**: Unipolar or Bipolar Hemiarthroplasty.

[WARNING: Prolonged surgical delay (>48 hours) in geriatric hip fractures is directly correlated with increased 30-day mortality, pressure ulcers, deep vein thrombosis, and hospital-acquired pneumonia.]

## 1.5 Complications & Postoperative Rehabilitation
Major complications include avascular necrosis (10-30% in ORIF), non-union (15-33%), prosthetic dislocation, periprosthetic fracture, and venous thromboembolism (VTE). Chemical thromboprophylaxis is recommended for a minimum of 28-35 days postoperatively.

## 1.6 Key Points & Clinical Pearls
* Intracapsular fractures do not form external callus due to lack of periosteum and presence of synovial fluid.
* Early surgery (<24-48 hours) optimizes medical stabilization and ambulation.
* Multidisciplinary orthogeriatric co-management reduces in-hospital mortality.
    """.trimIndent()

    val doc1Id = documentDao.insertDocument(
        MedicalDocument(
            title = "Fracture Neck of Femur: Comprehensive Textbook Chapter",
            docType = "Textbook Chapter",
            specialty = "Orthopedics",
            targetAudience = "Postgraduate",
            content = doc1Content,
            authors = "Prof. E. Vance, MD, FRCS & Dr. S. Rao, MS Ortho",
            institution = "DocuMed Orthopedic Institute",
            wordCount = 420,
            version = 1,
            isFavorite = true
        )
    )

    versionDao.insertVersion(
        DocumentVersion(
            documentId = doc1Id,
            versionNumber = 1,
            changeDescription = "Initial AI generated chapter with Garden classification & surgical algorithm",
            contentSnapshot = doc1Content
        )
    )

    // 2. Preloaded Clinical Protocol: ST-Elevation Myocardial Infarction (STEMI)
    val doc2Content = """
# 1.0 Acute STEMI Clinical Pathway & Reperfusion Protocol
This clinical protocol delineates emergency triage, pharmacotherapy, and reperfusion strategy for patients presenting with suspected acute ST-elevation myocardial infarction.

## 1.1 Immediate Emergency Diagnostic Criteria
* ST-segment elevation at the J-point in at least 2 contiguous leads:
  - Leads V2-V3: >=2.5 mm in men <40 yrs, >=2.0 mm in men >=40 yrs, or >=1.5 mm in women.
  - All other leads: >=1.0 mm.
* New or presumed new Left Bundle Branch Block (LBBB) in clinical context of ischemic symptoms.

## 1.2 Emergency Pharmacotherapy (First 10 Minutes)
* Aspirin 300 mg chewed/soluble immediately.
* P2Y12 Inhibitor loading dose: Ticagrelor 180 mg (or Prasugrel 60 mg if proceeding to PCI; Clopidogrel 600 mg if fibrinolysis).
* Unfractionated Heparin (UFH) 70-100 U/kg IV bolus (max 4000 U) or Enoxaparin.
* High-intensity Statin (Atorvastatin 80 mg PO).
* Supplemental O2 ONLY if SaO2 < 90%.

## 1.3 Reperfusion Strategy Timing Algorithm
* **Primary PCI Door-to-Balloon Time**: <=90 minutes from first medical contact (<=60 minutes for direct presentation to PCI center).
* **Fibrinolysis Door-to-Needle Time**: <=30 minutes (or <=10 min FMC-to-needle) if anticipated transfer delay to PCI is >120 minutes.
* Agent: Weight-adjusted IV Tenecteplase (TNK-tPA) over 5-10 seconds.

[EVIDENCE_LEVEL: Level A evidence confirms Primary Percutaneous Coronary Intervention (PPCI) achieves lower re-infarction, stroke, and overall mortality compared to fibrinolysis when delivered within standard timeframes.]
    """.trimIndent()

    val doc2Id = documentDao.insertDocument(
        MedicalDocument(
            title = "STEMI Emergency Reperfusion & Pharmacotherapy Protocol",
            docType = "Clinical Protocol",
            specialty = "Cardiology",
            targetAudience = "Specialist",
            content = doc2Content,
            authors = "Dr. M. Chen, MD, FACC",
            institution = "DocuMed Heart & Vascular Center",
            wordCount = 280,
            version = 1,
            isFavorite = true
        )
    )

    versionDao.insertVersion(
        DocumentVersion(
            documentId = doc2Id,
            versionNumber = 1,
            changeDescription = "Approved acute coronary syndrome protocol",
            contentSnapshot = doc2Content
        )
    )

    // Preload Source Materials in Knowledge Base
    sourceMaterialDao.insertSource(
        SourceMaterial(
            documentId = doc1Id,
            title = "Campbell's Operative Orthopedics - Hip Fractures Excerpt.pdf",
            fileType = "PDF",
            rawText = "Vascular supply to femoral head relies on lateral epiphyseal vessels from medial femoral circumflex artery. Garden classification categorizes degree of trabecular displacement. Arthroplasty versus fixation decision matrix depends on physiologic age and cognitive baseline.",
            extractedSummary = "Classic reference on femoral head blood supply and Garden classification indications.",
            extractedKeyPoints = "1. MFCA is main artery. 2. Garden III & IV are displaced. 3. THA preferred in active elderly.",
            extractedClassifications = "Garden I-IV; Pauwels I-III angle classification.",
            fileSize = "2.4 MB"
        )
    )

    sourceMaterialDao.insertSource(
        SourceMaterial(
            documentId = doc1Id,
            title = "Geriatric_Hip_Xray_Series_AP_Lateral.jpg",
            fileType = "XRAY",
            rawText = "Plain radiograph AP pelvis and lateral hip demonstrating subcapital fracture of left femoral neck with varus tilt and superior cortex displacement, consistent with Garden Stage III.",
            extractedSummary = "Left Garden III femoral neck fracture identified with intact acetabular socket.",
            extractedKeyPoints = "Varus tilt, cortical step-off at superior femoral neck, trabecular disruption.",
            fileSize = "3.8 MB"
        )
    )

    sourceMaterialDao.insertSource(
        SourceMaterial(
            documentId = doc2Id,
            title = "ACC_AHA_STEMI_Guidelines_2025_Update.docx",
            fileType = "DOCX",
            rawText = "Primary PCI remains gold standard reperfusion modality with FMC-to-balloon target under 90 minutes. Routine thrombus aspiration is not recommended. Routine oxygen therapy in non-hypoxemic patients may induce coronary vasoconstriction.",
            extractedSummary = "Clinical guidelines emphasizing rapid PCI triage, Ticagrelor/Prasugrel over Clopidogrel, and restrictive oxygen use.",
            extractedKeyPoints = "FMC to PCI < 90 min; Fibrinolysis within 120 min window if PCI unavailable; Aspirin + Ticagrelor 180mg.",
            fileSize = "1.1 MB"
        )
    )
}
