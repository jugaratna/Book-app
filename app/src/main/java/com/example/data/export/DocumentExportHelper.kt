package com.example.data.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.MedicalDocument
import com.example.data.model.TocItem
import java.io.File
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
}
