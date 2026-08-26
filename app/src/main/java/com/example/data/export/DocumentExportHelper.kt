package com.example.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.DocumentVersion
import com.example.data.model.MedicalDocument
import com.example.data.model.MedicalPresentation
import com.example.data.model.SourceMaterial
import com.example.data.model.TocItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DocumentExportHelper {

    fun generateTableOfContents(content: String): List<TocItem> {
        val list = mutableListOf<TocItem>()
        var h1Count = 0
        var h2Count = 0
        var h3Count = 0

        content.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("# ") && !trimmed.startsWith("##")) {
                h1Count++
                h2Count = 0
                h3Count = 0
                val title = trimmed.removePrefix("#").trim().replace(Regex("^\\d+(\\.\\d+)*\\s*"), "")
                list.add(TocItem(level = 1, number = "$h1Count.0", title = title, rawLine = trimmed))
            } else if (trimmed.startsWith("## ") && !trimmed.startsWith("###")) {
                h2Count++
                h3Count = 0
                val title = trimmed.removePrefix("##").trim().replace(Regex("^\\d+(\\.\\d+)*\\s*"), "")
                list.add(TocItem(level = 2, number = "$h1Count.$h2Count", title = title, rawLine = trimmed))
            } else if (trimmed.startsWith("### ")) {
                h3Count++
                val title = trimmed.removePrefix("###").trim().replace(Regex("^\\d+(\\.\\d+)*\\s*"), "")
                list.add(TocItem(level = 3, number = "$h1Count.$h2Count.$h3Count", title = title, rawLine = trimmed))
            }
        }
        return list
    }

    fun exportToDocx(context: Context, document: MedicalDocument): File {
        val sanitizedTitle = document.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(40)
        val file = File(context.cacheDir, "${sanitizedTitle}_DocuMed.docx")
        
        // Build valid rich text HTML/DOCX formatted document
        val docContent = buildHtmlDocument(document)
        FileOutputStream(file).use { out ->
            out.write(docContent.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun exportToPdfHtml(document: MedicalDocument): String {
        return buildHtmlDocument(document)
    }

    fun buildHtmlDocument(doc: MedicalDocument): String {
        val toc = generateTableOfContents(doc.content)
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val dateStr = sdf.format(Date(doc.updatedAt))

        val tocHtml = if (toc.isNotEmpty()) {
            """
            <div class="toc-container">
                <div class="toc-title">TABLE OF CONTENTS</div>
                ${toc.joinToString("") { item ->
                    val indent = when (item.level) {
                        1 -> "margin-left: 0px; font-weight: bold; color: #0f172a;"
                        2 -> "margin-left: 20px; font-weight: 500; color: #334155;"
                        else -> "margin-left: 40px; font-style: italic; color: #64748b;"
                    }
                    """<div class="toc-row" style="$indent">
                        <span>${item.number} ${item.title}</span>
                    </div>"""
                }}
            </div>
            """.trimIndent()
        } else ""

        // Convert custom tags in content into styled boxes
        val formattedBody = doc.content
            .replace(Regex("\\[KEY_POINT:\\s*(.*?)\\]")) {
                """<div class="callout keypoint"><div class="callout-label">CLINICAL PEARL / KEY POINT</div>${it.groupValues[1]}</div>"""
            }
            .replace(Regex("\\[WARNING:\\s*(.*?)\\]")) {
                """<div class="callout warning"><div class="callout-label">CRITICAL WARNING / RED FLAG</div>${it.groupValues[1]}</div>"""
            }
            .replace(Regex("\\[EVIDENCE_LEVEL:\\s*(.*?)\\]")) {
                """<div class="callout evidence"><div class="callout-label">EVIDENCE GRADE & CITATION</div>${it.groupValues[1]}</div>"""
            }
            .lines().joinToString("\n") { line ->
                val tr = line.trim()
                when {
                    tr.startsWith("# ") -> "<h1>${tr.removePrefix("#").trim()}</h1>"
                    tr.startsWith("## ") -> "<h2>${tr.removePrefix("##").trim()}</h2>"
                    tr.startsWith("### ") -> "<h3>${tr.removePrefix("###").trim()}</h3>"
                    tr.startsWith("* ") || tr.startsWith("- ") -> "<li>${tr.substring(2)}</li>"
                    tr.startsWith("|") && tr.endsWith("|") -> parseMarkdownTableRow(tr)
                    tr.isBlank() -> "<br/>"
                    else -> "<p>$tr</p>"
                }
            }

        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>${doc.title}</title>
<style>
    @page {
        size: A4;
        margin: 25mm 20mm 25mm 20mm;
        @top-center { content: "DocuMed Studio Academic Medical Series"; font-size: 8pt; color: #94a3b8; }
        @bottom-right { content: "Page " counter(page); font-size: 8pt; color: #64748b; }
    }
    body {
        font-family: 'Helvetica Neue', Arial, sans-serif;
        color: #0f172a;
        line-height: 1.65;
        font-size: 11pt;
        background: #ffffff;
        padding: 24px;
    }
    .header-block {
        border-bottom: 2px solid #0284c7;
        padding-bottom: 16px;
        margin-bottom: 24px;
    }
    .doc-type-badge {
        display: inline-block;
        background: #e0f2fe;
        color: #0369a1;
        font-size: 9pt;
        font-weight: bold;
        padding: 4px 10px;
        border-radius: 4px;
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }
    h1.main-title {
        font-size: 22pt;
        color: #0c4a6e;
        margin: 12px 0 8px 0;
        line-height: 1.25;
    }
    .meta-line {
        font-size: 9.5pt;
        color: #64748b;
        margin-bottom: 4px;
    }
    .toc-container {
        background: #f8fafc;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        padding: 16px 20px;
        margin: 24px 0;
    }
    .toc-title {
        font-weight: bold;
        font-size: 11pt;
        color: #0f172a;
        margin-bottom: 12px;
        letter-spacing: 0.5px;
        border-bottom: 1px solid #cbd5e1;
        padding-bottom: 6px;
    }
    .toc-row {
        margin-bottom: 6px;
        font-size: 10pt;
    }
    h1 { font-size: 16pt; color: #0369a1; margin-top: 24px; border-bottom: 1px solid #e2e8f0; padding-bottom: 4px; }
    h2 { font-size: 13.5pt; color: #0f2b48; margin-top: 18px; }
    h3 { font-size: 11.5pt; color: #1e3a5f; margin-top: 14px; }
    p { margin: 8px 0; }
    li { margin: 4px 0 4px 20px; }
    .callout {
        border-radius: 6px;
        padding: 12px 16px;
        margin: 14px 0;
        font-size: 10.5pt;
    }
    .callout-label {
        font-size: 8pt;
        font-weight: bold;
        letter-spacing: 0.5px;
        margin-bottom: 4px;
    }
    .callout.keypoint {
        background: #eff6ff;
        border-left: 4px solid #2563eb;
        color: #1e3a8a;
    }
    .callout.keypoint .callout-label { color: #1d4ed8; }
    .callout.warning {
        background: #fef2f2;
        border-left: 4px solid #dc2626;
        color: #991b1b;
    }
    .callout.warning .callout-label { color: #b91c1c; }
    .callout.evidence {
        background: #f0fdf4;
        border-left: 4px solid #059669;
        color: #065f46;
    }
    .callout.evidence .callout-label { color: #047857; }
    table {
        width: 100%;
        border-collapse: collapse;
        margin: 16px 0;
        font-size: 10pt;
    }
    th, td {
        border: 1px solid #cbd5e1;
        padding: 8px 12px;
        text-align: left;
    }
    th {
        background: #f1f5f9;
        font-weight: bold;
        color: #0f172a;
    }
    .footer-note {
        margin-top: 40px;
        padding-top: 12px;
        border-top: 1px solid #e2e8f0;
        font-size: 8.5pt;
        color: #94a3b8;
        text-align: center;
    }
</style>
</head>
<body>
<div class="header-block">
    <div class="doc-type-badge">${doc.docType} &bull; ${doc.specialty} &bull; ${doc.targetAudience}</div>
    <h1 class="main-title">${doc.title}</h1>
    <div class="meta-line"><strong>Authors:</strong> ${doc.authors} | <strong>Institution:</strong> ${doc.institution}</div>
    <div class="meta-line"><strong>Last Updated:</strong> $dateStr | <strong>Version:</strong> v${doc.version} | <strong>Word Count:</strong> ${doc.wordCount} words</div>
</div>

$tocHtml

<div class="content-body">
$formattedBody
</div>

<div class="footer-note">
    Generated with DocuMed Studio &bull; Professional AI Medical Book & Note Creation Platform &bull; Validated Medical Content
</div>
</body>
</html>
        """.trimIndent()
    }

    private fun parseMarkdownTableRow(row: String): String {
        if (row.contains("---")) return ""
        val cells = row.split("|").filterIndexed { index, _ -> index > 0 && index < row.split("|").size - 1 }
        val cellTags = cells.joinToString("") { "<td>${it.trim()}</td>" }
        return "<tr>$cellTags</tr>"
    }

    fun shareDocument(context: Context, document: MedicalDocument, format: String) {
        when (format) {
            "DOCX" -> {
                val file = exportToDocx(context, document)
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, document.title)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Export Document as Word (.docx)"))
            }
            "HTML" -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/html"
                    putExtra(Intent.EXTRA_TEXT, buildHtmlDocument(document))
                    putExtra(Intent.EXTRA_SUBJECT, "${document.title} - DocuMed Export")
                }
                context.startActivity(Intent.createChooser(intent, "Share Document as HTML"))
            }
            else -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "${document.title}\n\n${document.content}")
                    putExtra(Intent.EXTRA_SUBJECT, document.title)
                }
                context.startActivity(Intent.createChooser(intent, "Share Document Text"))
            }
        }
    }

    // ==========================================
    // 1. AI PowerPoint / Presentation Deck Generator & Exporter
    // ==========================================

    fun buildPresentationHtml(presentation: MedicalPresentation): String {
        val slidesJson = presentation.slides.joinToString(",") { slide ->
            val bulletsJson = slide.bulletPoints.joinToString(",") { "\"${it.replace("\"", "\\\"")}\"" }
            """
            {
                "num": ${slide.slideNumber},
                "title": "${slide.title.replace("\"", "\\\"")}",
                "subtitle": "${slide.subtitle.replace("\"", "\\\"")}",
                "bullets": [$bulletsJson],
                "pearl": "${slide.clinicalPearl.replace("\"", "\\\"")}",
                "warning": "${slide.redFlag.replace("\"", "\\\"")}",
                "visual": "${slide.visualSuggestion.replace("\"", "\\\"")}",
                "notes": "${slide.speakerNotes.replace("\"", "\\\"")}"
            }
            """.trimIndent()
        }

        return """
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>${presentation.title} - Medical Presentation</title>
<style>
    :root {
        --primary: #0284c7;
        --primary-dark: #0369a1;
        --navy: #0f172a;
        --bg: #090d16;
        --card: #131c2e;
        --card-border: #1e293b;
        --text: #f8fafc;
        --text-muted: #94a3b8;
        --green: #059669;
        --red: #dc2626;
        --amber: #d97706;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }
    body { background-color: var(--bg); color: var(--text); min-height: 100vh; display: flex; flex-direction: column; }
    
    /* Top Toolbar */
    .top-bar {
        background: #0d1527;
        border-bottom: 1px solid var(--card-border);
        padding: 12px 24px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .deck-title { font-size: 16px; font-weight: 700; color: #38bdf8; display: flex; align-items: center; gap: 8px; }
    .badge { background: rgba(56, 189, 248, 0.15); color: #38bdf8; font-size: 11px; padding: 3px 8px; border-radius: 12px; font-weight: 600; }
    .controls { display: flex; gap: 10px; align-items: center; }
    button {
        background: #1e293b; color: white; border: 1px solid #334155; padding: 8px 16px; border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all 0.2s;
    }
    button:hover { background: #334155; }
    button.primary { background: var(--primary); border-color: var(--primary); }
    button.primary:hover { background: var(--primary-dark); }
    
    /* Presentation Stage */
    .stage {
        flex: 1;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 30px;
    }
    .slide-frame {
        width: 100%;
        max-width: 960px;
        aspect-ratio: 16 / 9;
        background: var(--card);
        border: 1px solid var(--card-border);
        border-radius: 16px;
        box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.7);
        padding: 44px 52px;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        position: relative;
        overflow: hidden;
    }
    .slide-frame::before {
        content: '';
        position: absolute;
        top: 0; left: 0; right: 0; height: 5px;
        background: linear-gradient(90deg, #0284c7, #0d9488, #38bdf8);
    }
    
    .slide-header { border-bottom: 1px solid rgba(255,255,255,0.08); padding-bottom: 14px; margin-bottom: 18px; }
    .slide-category { font-size: 12px; font-weight: 700; color: #38bdf8; text-transform: uppercase; letter-spacing: 1px; }
    .slide-title { font-size: 26px; font-weight: 800; color: #ffffff; margin-top: 4px; }
    
    .slide-body { flex: 1; display: flex; flex-direction: column; gap: 14px; }
    .bullet-list { list-style: none; display: flex; flex-direction: column; gap: 12px; }
    .bullet-list li {
        font-size: 15.5px;
        line-height: 1.5;
        color: #e2e8f0;
        position: relative;
        padding-left: 24px;
    }
    .bullet-list li::before {
        content: '■';
        position: absolute;
        left: 0;
        color: #38bdf8;
        font-size: 12px;
        top: 2px;
    }
    
    .callout-box {
        padding: 12px 16px;
        border-radius: 10px;
        font-size: 13.5px;
        margin-top: 8px;
    }
    .callout-pearl {
        background: rgba(2, 132, 199, 0.15);
        border-left: 4px solid #0284c7;
        color: #bae6fd;
    }
    .callout-warning {
        background: rgba(220, 38, 38, 0.15);
        border-left: 4px solid #dc2626;
        color: #fecaca;
    }
    .callout-tag { font-weight: 800; font-size: 11px; text-transform: uppercase; margin-bottom: 2px; }
    
    .slide-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-top: 1px solid rgba(255,255,255,0.08);
        padding-top: 12px;
        font-size: 11px;
        color: var(--text-muted);
    }
    
    /* Notes & Navigation */
    .speaker-drawer {
        background: #0f172a;
        border-top: 1px solid var(--card-border);
        padding: 14px 24px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .notes-text { font-size: 13px; color: #cbd5e1; font-style: italic; max-width: 80%; }
</style>
</head>
<body>

<div class="top-bar">
    <div class="deck-title">
        <span>DocuMed Medical Slide Deck</span>
        <span class="badge">${presentation.slides.size} Slides</span>
    </div>
    <div class="controls">
        <button onclick="prevSlide()">◀ Previous</button>
        <span id="counter" style="font-size: 13px; font-weight: 700; color: #38bdf8;">Slide 1 of ${presentation.slides.size}</span>
        <button class="primary" onclick="nextSlide()">Next ▶</button>
        <button onclick="window.print()">Print Deck</button>
    </div>
</div>

<div class="stage">
    <div class="slide-frame" id="slideContainer">
        <!-- Rendered via JS -->
    </div>
</div>

<div class="speaker-drawer">
    <div>
        <strong style="color: #38bdf8; font-size: 12px;">PRESENTER NOTES:</strong>
        <div class="notes-text" id="speakerNotes">Notes loading...</div>
    </div>
    <div style="font-size: 12px; color: #64748b;">
        Use ← → arrow keys or Spacebar to navigate
    </div>
</div>

<script>
    const slides = [$slidesJson];
    let currentIndex = 0;

    function renderSlide(index) {
        if (index < 0 || index >= slides.length) return;
        currentIndex = index;
        const slide = slides[index];

        document.getElementById('counter').innerText = "Slide " + (index + 1) + " of " + slides.length;
        document.getElementById('speakerNotes').innerText = slide.notes || "No additional presenter notes for this slide.";

        let bulletsHtml = slide.bullets.map(b => "<li>" + b + "</li>").join("");
        let extraHtml = "";

        if (slide.pearl) {
            extraHtml += '<div class="callout-box callout-pearl"><div class="callout-tag">CLINICAL PEARL</div>' + slide.pearl + '</div>';
        }
        if (slide.warning) {
            extraHtml += '<div class="callout-box callout-warning"><div class="callout-tag">RED FLAG & CONTRAINDICATION</div>' + slide.warning + '</div>';
        }
        if (slide.visual) {
            extraHtml += '<div style="font-size: 12px; color: #94a3b8; margin-top: 6px;"><strong>[Visual Diagram]:</strong> ' + slide.visual + '</div>';
        }

        var subtitle = slide.subtitle || "${presentation.title.replace("\"", "\\\"")}";
        document.getElementById('slideContainer').innerHTML =
            '<div>' +
                '<div class="slide-header">' +
                    '<div class="slide-category">' + subtitle + '</div>' +
                    '<div class="slide-title">' + slide.title + '</div>' +
                '</div>' +
                '<div class="slide-body">' +
                    '<ul class="bullet-list">' +
                        bulletsHtml +
                    '</ul>' +
                    extraHtml +
                '</div>' +
            '</div>' +
            '<div class="slide-footer">' +
                '<span>DocuMed Medical AI Studio &bull; Clinical Presentation</span>' +
                '<span>Slide ' + slide.num + ' of ' + slides.length + '</span>' +
            '</div>';
    }

    function nextSlide() {
        if (currentIndex < slides.length - 1) renderSlide(currentIndex + 1);
    }

    function prevSlide() {
        if (currentIndex > 0) renderSlide(currentIndex - 1);
    }

    document.addEventListener('keydown', (e) => {
        if (e.key === 'ArrowRight' || e.key === ' ' || e.key === 'PageDown') nextSlide();
        if (e.key === 'ArrowLeft' || e.key === 'PageUp') prevSlide();
    });

    renderSlide(0);
</script>

</body>
</html>
        """.trimIndent()
    }

    fun exportPresentationToHtmlFile(context: Context, presentation: MedicalPresentation): File {
        val sanitized = presentation.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(35)
        val file = File(context.cacheDir, "${sanitized}_Presentation.html")
        val content = buildPresentationHtml(presentation)
        FileOutputStream(file).use { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun sharePresentation(context: Context, presentation: MedicalPresentation, format: String = "HTML_DECK") {
        val file = exportPresentationToHtmlFile(context, presentation)
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "${presentation.title} - Clinical Presentation Deck")
            putExtra(Intent.EXTRA_TEXT, "Medical PowerPoint Slide Deck for '${presentation.title}' generated by DocuMed Studio.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Clinical Presentation Deck"))
    }

    // Google Drive Integration & Direct File Sharing
    fun shareFileToGoogleDrive(context: Context, file: File, mimeType: String, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Saving $title to Google Drive via DocuMed Studio")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Save & Upload to Google Drive"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openGoogleDrive(context: Context, driveUrl: String = "https://drive.google.com/drive/my-drive") {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(driveUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun slidesToJson(slides: List<com.example.data.model.PresentationSlide>): String {
        val array = JSONArray()
        slides.forEach { s ->
            val obj = JSONObject().apply {
                put("slideNumber", s.slideNumber)
                put("title", s.title)
                put("subtitle", s.subtitle)
                val bulletsArr = JSONArray()
                s.bulletPoints.forEach { bulletsArr.put(it) }
                put("bullets", bulletsArr)
                put("pearl", s.clinicalPearl)
                put("warning", s.redFlag)
                put("visual", s.visualSuggestion)
                put("notes", s.speakerNotes)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun slidesFromJson(json: String): List<com.example.data.model.PresentationSlide> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<com.example.data.model.PresentationSlide>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val bulletsList = mutableListOf<String>()
                if (obj.has("bullets")) {
                    val bArr = obj.getJSONArray("bullets")
                    for (j in 0 until bArr.length()) {
                        bulletsList.add(bArr.getString(j))
                    }
                }
                list.add(
                    com.example.data.model.PresentationSlide(
                        slideNumber = obj.optInt("slideNumber", i + 1),
                        title = obj.optString("title", "Slide ${i + 1}"),
                        subtitle = obj.optString("subtitle", ""),
                        bulletPoints = if (bulletsList.isNotEmpty()) bulletsList else listOf("Key clinical review point"),
                        clinicalPearl = obj.optString("pearl", ""),
                        redFlag = obj.optString("warning", ""),
                        visualSuggestion = obj.optString("visual", ""),
                        speakerNotes = obj.optString("notes", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun exportRawTextToDocxFile(context: Context, title: String, content: String): File {
        val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(35)
        val file = File(context.cacheDir, "${sanitizedTitle}.docx")
        val dummyDoc = MedicalDocument(
            title = title,
            content = content,
            docType = "Medical Word Document",
            specialty = "General Medicine"
        )
        val docHtml = buildHtmlDocument(dummyDoc)
        FileOutputStream(file).use { out ->
            out.write(docHtml.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun exportRawTextToPdfFile(context: Context, title: String, content: String): File {
        val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(35)
        val file = File(context.cacheDir, "${sanitizedTitle}.html")
        val dummyDoc = MedicalDocument(
            title = title,
            content = content,
            docType = "Medical PDF Document",
            specialty = "General Medicine"
        )
        val docHtml = buildHtmlDocument(dummyDoc)
        FileOutputStream(file).use { out ->
            out.write(docHtml.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    // ==========================================
    // 2. Save App Data Offline (Backup) & Restore
    // ==========================================

    fun exportAppDataBackupJson(
        documents: List<MedicalDocument>,
        sources: List<SourceMaterial>,
        versions: List<DocumentVersion>
    ): String {
        val root = JSONObject()
        root.put("app", "DocuMed Studio")
        root.put("version", 1)
        root.put("backupDate", System.currentTimeMillis())
        root.put("backupDateFormatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        // Documents Array
        val docArray = JSONArray()
        documents.forEach { doc ->
            val obj = JSONObject().apply {
                put("id", doc.id)
                put("title", doc.title)
                put("specialty", doc.specialty)
                put("docType", doc.docType)
                put("targetAudience", doc.targetAudience)
                put("authors", doc.authors)
                put("institution", doc.institution)
                put("content", doc.content)
                put("version", doc.version)
                put("wordCount", doc.wordCount)
                put("isFavorite", doc.isFavorite)
                put("createdAt", doc.createdAt)
                put("updatedAt", doc.updatedAt)
            }
            docArray.put(obj)
        }
        root.put("documents", docArray)

        // Sources Array
        val srcArray = JSONArray()
        sources.forEach { src ->
            val obj = JSONObject().apply {
                put("id", src.id)
                put("documentId", src.documentId)
                put("title", src.title)
                put("fileType", src.fileType)
                put("rawText", src.rawText)
                put("extractedSummary", src.extractedSummary)
                put("extractedKeyPoints", src.extractedKeyPoints)
                put("extractedClassifications", src.extractedClassifications)
                put("extractedTables", src.extractedTables)
                put("fileSize", src.fileSize)
                put("isProcessed", src.isProcessed)
                put("uploadedAt", src.uploadedAt)
            }
            srcArray.put(obj)
        }
        root.put("sources", srcArray)

        // Versions Array
        val verArray = JSONArray()
        versions.forEach { ver ->
            val obj = JSONObject().apply {
                put("id", ver.id)
                put("documentId", ver.documentId)
                put("versionNumber", ver.versionNumber)
                put("changeDescription", ver.changeDescription)
                put("contentSnapshot", ver.contentSnapshot)
                put("timestamp", ver.timestamp)
            }
            verArray.put(obj)
        }
        root.put("versions", verArray)

        return root.toString(2)
    }

    fun exportAppDataBackupFile(
        context: Context,
        documents: List<MedicalDocument>,
        sources: List<SourceMaterial>,
        versions: List<DocumentVersion>
    ): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.cacheDir, "DocuMed_Offline_Backup_$timeStamp.json")
        val json = exportAppDataBackupJson(documents, sources, versions)
        FileOutputStream(file).use { out ->
            out.write(json.toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun shareOfflineBackup(context: Context, backupFile: File) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", backupFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "DocuMed Offline App Backup - ${backupFile.name}")
            putExtra(Intent.EXTRA_TEXT, "DocuMed complete offline data backup file.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Save / Share Offline App Data Backup"))
    }

    fun parseBackupData(jsonString: String): BackupData {
        val root = JSONObject(jsonString)
        val documents = mutableListOf<MedicalDocument>()
        val sources = mutableListOf<SourceMaterial>()
        val versions = mutableListOf<DocumentVersion>()

        if (root.has("documents")) {
            val docArray = root.getJSONArray("documents")
            for (i in 0 until docArray.length()) {
                val obj = docArray.getJSONObject(i)
                documents.add(
                    MedicalDocument(
                        id = obj.optLong("id", 0),
                        title = obj.optString("title", "Untitled"),
                        specialty = obj.optString("specialty", "General Medicine"),
                        docType = obj.optString("docType", "Clinical Notes"),
                        targetAudience = obj.optString("targetAudience", "General Physicians"),
                        authors = obj.optString("authors", "DocuMed Author"),
                        institution = obj.optString("institution", "Clinical Institute"),
                        content = obj.optString("content", ""),
                        version = obj.optInt("version", 1),
                        wordCount = obj.optInt("wordCount", 0),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        }

        if (root.has("sources")) {
            val srcArray = root.getJSONArray("sources")
            for (i in 0 until srcArray.length()) {
                val obj = srcArray.getJSONObject(i)
                sources.add(
                    SourceMaterial(
                        id = obj.optLong("id", 0),
                        documentId = obj.optLong("documentId", 0),
                        title = obj.optString("title", "Source Material"),
                        fileType = obj.optString("fileType", "PDF"),
                        rawText = obj.optString("rawText", ""),
                        extractedSummary = obj.optString("extractedSummary", ""),
                        extractedKeyPoints = obj.optString("extractedKeyPoints", ""),
                        extractedClassifications = obj.optString("extractedClassifications", ""),
                        extractedTables = obj.optString("extractedTables", ""),
                        fileSize = obj.optString("fileSize", "1.0 MB"),
                        isProcessed = obj.optBoolean("isProcessed", true),
                        uploadedAt = obj.optLong("uploadedAt", System.currentTimeMillis())
                    )
                )
            }
        }

        if (root.has("versions")) {
            val verArray = root.getJSONArray("versions")
            for (i in 0 until verArray.length()) {
                val obj = verArray.getJSONObject(i)
                versions.add(
                    DocumentVersion(
                        id = obj.optLong("id", 0),
                        documentId = obj.optLong("documentId", 0),
                        versionNumber = obj.optInt("versionNumber", 1),
                        changeDescription = obj.optString("changeDescription", "Restored Snapshot"),
                        contentSnapshot = obj.optString("contentSnapshot", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }

        return BackupData(documents, sources, versions)
    }

    // ==========================================
    // 3. Send Installed App APK File
    // ==========================================

    fun shareAppApkFile(context: Context) {
        try {
            val appInfo = context.applicationInfo
            val sourceApk = File(appInfo.sourceDir)
            val cacheApk = File(context.cacheDir, "DocuMed_Studio.apk")

            // Copy source APK to readable cache directory
            FileInputStream(sourceApk).use { input ->
                FileOutputStream(cacheApk).use { output ->
                    input.copyTo(output)
                }
            }

            val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheApk)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                putExtra(Intent.EXTRA_SUBJECT, "DocuMed Studio Android App APK")
                putExtra(Intent.EXTRA_TEXT, "Here is the DocuMed Studio APK installer file. Install to run the offline medical book & note creation platform.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Send DocuMed APK via"))
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback plain message if direct apk copy is restricted
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "DocuMed Studio")
                putExtra(Intent.EXTRA_TEXT, "Install DocuMed Studio: AI Medical Book & Note Creation Studio.")
            }
            context.startActivity(Intent.createChooser(intent, "Share DocuMed Studio"))
        }
    }

    fun exportFlashcardsToAnkiCsv(context: Context, flashcards: List<com.example.data.model.FlashcardItem>, deckName: String): File {
        val sanitized = deckName.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(35)
        val file = File(context.cacheDir, "Anki_${sanitized}.txt")

        val sb = StringBuilder()
        // Anki header: #separator:tab, #html:true, #tags column:3
        sb.append("#separator:tab\n")
        sb.append("#html:true\n")
        sb.append("#tags column:3\n")

        flashcards.forEach { card ->
            val front = card.front.replace("\t", " ").replace("\n", "<br>")
            val back = "${card.back}<br><br><small style='color:#0284c7;'><b>High-Yield Pearl:</b> ${card.highYieldPearl}</small>"
                .replace("\t", " ").replace("\n", "<br>")
            val tags = "DocuMed::${card.category.replace(" ", "_")} DocuMed::SpacedRepetition"
            sb.append("$front\t$back\t$tags\n")
        }

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }
        return file
    }

    fun shareAnkiDeck(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/tab-separated-values"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "DocuMed Anki Flashcard Deck")
            putExtra(Intent.EXTRA_TEXT, "Import this tab-separated text file into Anki / AnkiDroid to study with active spaced repetition.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Anki Deck to"))
    }

    fun buildPatientLeafletHtml(leaflet: com.example.data.model.PatientInformationLeaflet): String {
        return """
            <!DOCTYPE html>
            <html lang="${leaflet.language.code}">
            <head>
                <meta charset="utf-8">
                <title>${leaflet.title}</title>
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #1e293b; max-width: 800px; margin: 0 auto; padding: 24px; }
                    .header-card { background: linear-gradient(135deg, #0284c7, #0369a1); color: white; padding: 24px; border-radius: 12px; margin-bottom: 20px; }
                    .badge { display: inline-block; background: rgba(255,255,255,0.25); padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: bold; margin-bottom: 10px; }
                    .section { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 10px; padding: 18px; margin-bottom: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
                    .section-title { font-size: 18px; font-weight: bold; color: #0369a1; margin-top: 0; margin-bottom: 12px; display: flex; align-items: center; }
                    .emergency-card { background: #fef2f2; border: 2px solid #ef4444; border-radius: 10px; padding: 18px; margin-bottom: 16px; }
                    .emergency-title { color: #dc2626; font-size: 18px; font-weight: bold; margin-top: 0; }
                    ul { padding-left: 20px; margin-bottom: 0; }
                    li { margin-bottom: 8px; }
                    .footer { text-align: center; font-size: 12px; color: #64748b; margin-top: 30px; border-top: 1px solid #e2e8f0; padding-top: 15px; }
                </style>
            </head>
            <body>
                <div class="header-card">
                    <div class="badge">${leaflet.language.flagEmoji} ${leaflet.language.displayName} · ${leaflet.readingLevel.label}</div>
                    <h1 style="margin: 0; font-size: 24px;">${leaflet.title}</h1>
                </div>

                <div class="section">
                    <div class="section-title">ℹ️ About Your Condition</div>
                    <p style="font-size: 16px; margin: 0;">${leaflet.conditionSummary}</p>
                </div>

                <div class="section">
                    <div class="section-title">🔍 Symptoms to Watch For</div>
                    <ul>
                        ${leaflet.symptomsToWatch.joinToString("") { "<li>$it</li>" }}
                    </ul>
                </div>

                <div class="section">
                    <div class="section-title">📋 Your Care and Treatment Plan</div>
                    <ul>
                        ${leaflet.treatmentPlan.joinToString("") { "<li>$it</li>" }}
                    </ul>
                </div>

                <div class="section">
                    <div class="section-title">💊 Medication Instructions</div>
                    <ul>
                        ${leaflet.medicationInstructions.joinToString("") { "<li>$it</li>" }}
                    </ul>
                </div>

                <div class="emergency-card">
                    <div class="emergency-title">🚨 When to Seek Emergency Medical Attention</div>
                    <ul style="color: #991b1b; font-weight: 500;">
                        ${leaflet.emergencyRedFlags.joinToString("") { "<li>$it</li>" }}
                    </ul>
                </div>

                <div class="section">
                    <div class="section-title">❓ Questions to Ask Your Doctor</div>
                    <ul>
                        ${leaflet.questionsForDoctor.joinToString("") { "<li>$it</li>" }}
                    </ul>
                </div>

                <div class="section">
                    <div class="section-title">🌿 Daily Lifestyle & Home Advice</div>
                    <ul>
                        ${leaflet.lifestyleAdvice.joinToString("") { "<li>$it</li>" }}
                    </ul>
                </div>

                <div class="footer">
                    Provided by DocuMed Clinical Platform. This patient leaflet is for educational guidance. Always contact your healthcare provider for medical advice.
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}

data class BackupData(
    val documents: List<MedicalDocument>,
    val sources: List<SourceMaterial>,
    val versions: List<DocumentVersion>
)

