package com.spark.ui.screens.main

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SparkPdfExporter {

    private const val PAGE_WIDTH = 595 // Standard A4 width in pt
    private const val PAGE_HEIGHT = 842 // Standard A4 height in pt
    private const val MARGIN_X = 36f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN_X * 2)

    fun exportDossier(
        context: Context,
        selectedTopics: Set<String>,
        journalEntries: List<JournalEntry>,
        stats: MindPalaceStats,
        userName: String
    ): File? {
        if (journalEntries.isEmpty()) {
            Toast.makeText(context, "No sparks in journal to export", Toast.LENGTH_SHORT).show()
            return null
        }

        // Filter entries matching selected topics
        val isAllSelected = selectedTopics.isEmpty() || selectedTopics.contains("ALL")
        val filteredEntries = if (isAllSelected) {
            journalEntries
        } else {
            val normalizedSelected = selectedTopics.map { it.uppercase().trim() }
            journalEntries.filter { entry ->
                normalizedSelected.any { sel -> entry.category.uppercase().contains(sel) }
            }
        }

        if (filteredEntries.isEmpty()) {
            Toast.makeText(context, "No matching sparks found for selected domains", Toast.LENGTH_SHORT).show()
            return null
        }

        val pdfDoc = PdfDocument()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas

        // Drawing paints
        val bgPaint = Paint().apply { isAntiAlias = true }
        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }

        val titlePaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.parseColor("#0F172A")
            textSize = 12f
            isFakeBoldText = true
        }

        val bodyPaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.parseColor("#334155")
            textSize = 9.5f
        }

        val quotePaint = TextPaint().apply {
            isAntiAlias = true
            color = Color.parseColor("#475569")
            textSize = 9.5f
            isFakeBoldText = false
        }

        var currentY = 0f

        fun drawHeader() {
            // Draw dark cyber header banner
            bgPaint.color = Color.parseColor("#070B14")
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 110f, bgPaint)

            // Draw cyan accent top line
            bgPaint.color = Color.parseColor("#00E5FF")
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 4f, bgPaint)

            // Draw Spark Logo icon (Diamond + Core)
            val logoX = MARGIN_X + 16f
            val logoY = 56f
            val path = Path().apply {
                moveTo(logoX, logoY - 18f)
                lineTo(logoX + 14f, logoY)
                lineTo(logoX, logoY + 18f)
                lineTo(logoX - 14f, logoY)
                close()
            }
            bgPaint.color = Color.parseColor("#00E5FF")
            canvas.drawPath(path, bgPaint)

            bgPaint.color = Color.parseColor("#FFFFFF")
            canvas.drawCircle(logoX, logoY, 4f, bgPaint)

            // Brand Text
            val brandPaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = 20f
                isFakeBoldText = true
                letterSpacing = 0.05f
            }
            canvas.drawText("SPARK", logoX + 24f, 50f, brandPaint)

            val tagPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#00E5FF")
                textSize = 9.5f
                isFakeBoldText = true
                letterSpacing = 0.12f
            }
            canvas.drawText("MIND PALACE • INTELLECTUAL DOSSIER", logoX + 24f, 66f, tagPaint)

            val subTagPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#94A3B8")
                textSize = 8f
            }
            val dateStr = SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(Date())
            canvas.drawText("Synthesized Knowledge Record • $dateStr", logoX + 24f, 80f, subTagPaint)

            // Metadata summary card in header (Learner, Neurons, Rank)
            val infoRight = PAGE_WIDTH - MARGIN_X
            val rightAlignPaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = 10f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("LEARNER: $userName", infoRight, 46f, rightAlignPaint)

            rightAlignPaint.color = Color.parseColor("#00E5FF")
            rightAlignPaint.textSize = 9f
            canvas.drawText("DNA RANK: ${stats.intellectDnaRank}", infoRight, 62f, rightAlignPaint)

            rightAlignPaint.color = Color.parseColor("#94A3B8")
            rightAlignPaint.textSize = 8.5f
            canvas.drawText("Active Neurons: ${stats.activeNeurons}  |  Sync: ${stats.syncRatio}%  |  XP: ${stats.totalXp}", infoRight, 78f, rightAlignPaint)

            currentY = 126f
        }

        fun drawFooter() {
            strokePaint.color = Color.parseColor("#E2E8F0")
            strokePaint.strokeWidth = 0.8f
            canvas.drawLine(MARGIN_X, PAGE_HEIGHT - 32f, PAGE_WIDTH - MARGIN_X, PAGE_HEIGHT - 32f, strokePaint)

            val footPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#94A3B8")
                textSize = 8f
            }
            canvas.drawText("SPARK • Cognitive Synthesis Engine", MARGIN_X, PAGE_HEIGHT - 18f, footPaint)

            val pagePaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#64748B")
                textSize = 8f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("Page $pageNumber", PAGE_WIDTH - MARGIN_X, PAGE_HEIGHT - 18f, pagePaint)
        }

        fun newPage() {
            drawFooter()
            pdfDoc.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = pdfDoc.startPage(pageInfo)
            canvas = page.canvas

            // Mini header for continuation pages
            bgPaint.color = Color.parseColor("#070B14")
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 42f, bgPaint)
            bgPaint.color = Color.parseColor("#00E5FF")
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 2.5f, bgPaint)

            val miniBrandPaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = 10f
                isFakeBoldText = true
            }
            canvas.drawText("SPARK MIND PALACE DOSSIER (Cont.)", MARGIN_X, 26f, miniBrandPaint)

            val miniRightPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#00E5FF")
                textSize = 8.5f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("Learner: $userName", PAGE_WIDTH - MARGIN_X, 26f, miniRightPaint)

            currentY = 56f
        }

        drawHeader()

        // Group entries by topic
        val groupedByTopic = filteredEntries.groupBy { it.category }

        groupedByTopic.forEach { (topic, sparks) ->
            // Check if room for Section Header
            if (currentY > PAGE_HEIGHT - 140f) {
                newPage()
            }

            // Draw Section Header
            val topicColorCode = when (topic.uppercase()) {
                "TECH" -> "#0284C7"
                "SCIENCE" -> "#0284C7"
                "PSYCHOLOGY" -> "#7C3AED"
                "HISTORY" -> "#D97706"
                "BIOLOGY" -> "#059669"
                "PHILOSOPHY" -> "#6D28D9"
                "MATH" -> "#DB2777"
                "ARTS" -> "#E11D48"
                else -> "#0284C7"
            }

            bgPaint.color = Color.parseColor(topicColorCode)
            canvas.drawRoundRect(RectF(MARGIN_X, currentY, MARGIN_X + 4f, currentY + 16f), 2f, 2f, bgPaint)

            val secTitlePaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#0F172A")
                textSize = 11f
                isFakeBoldText = true
                letterSpacing = 0.05f
            }
            val countLabel = if (sparks.size == 1) "1 Spark" else "${sparks.size} Sparks"
            canvas.drawText("${topic.uppercase()} ($countLabel)", MARGIN_X + 12f, currentY + 13f, secTitlePaint)
            currentY += 26f

            sparks.forEach { entry ->
                // Measure card height
                val titleLayout = StaticLayout.Builder.obtain(entry.title, 0, entry.title.length, titlePaint, (CONTENT_WIDTH - 24f).toInt())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .build()

                val quoteText = "“${entry.quote}”"
                val quoteLayout = StaticLayout.Builder.obtain(quoteText, 0, quoteText.length, quotePaint, (CONTENT_WIDTH - 30f).toInt())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .build()

                var stepsHeight = 0f
                val stepLayouts = mutableListOf<StaticLayout>()
                if (entry.steps.isNotEmpty()) {
                    entry.steps.take(3).forEachIndexed { i, step ->
                        val stepStr = "${i + 1}. $step"
                        val sl = StaticLayout.Builder.obtain(stepStr, 0, stepStr.length, bodyPaint, (CONTENT_WIDTH - 32f).toInt())
                            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                            .build()
                        stepLayouts.add(sl)
                        stepsHeight += sl.height + 4f
                    }
                }

                var funFactHeight = 0f
                var funFactLayout: StaticLayout? = null
                if (entry.funFact.isNotBlank()) {
                    val ffText = "💡 Fact: ${entry.funFact}"
                    funFactLayout = StaticLayout.Builder.obtain(ffText, 0, ffText.length, bodyPaint, (CONTENT_WIDTH - 30f).toInt())
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .build()
                    funFactHeight = funFactLayout.height + 10f
                }

                val cardContentHeight = 22f + titleLayout.height + 8f + quoteLayout.height +
                    (if (stepLayouts.isNotEmpty()) 16f + stepsHeight else 0f) +
                    (if (funFactLayout != null) 6f + funFactHeight else 0f) + 16f

                // Page break check
                if (currentY + cardContentHeight > PAGE_HEIGHT - 50f) {
                    newPage()
                }

                val cardTop = currentY
                val cardBottom = cardTop + cardContentHeight
                val cardRect = RectF(MARGIN_X, cardTop, MARGIN_X + CONTENT_WIDTH, cardBottom)

                // Card background & border
                bgPaint.color = Color.parseColor("#F8FAFC")
                canvas.drawRoundRect(cardRect, 8f, 8f, bgPaint)

                strokePaint.color = Color.parseColor("#E2E8F0")
                strokePaint.strokeWidth = 0.9f
                canvas.drawRoundRect(cardRect, 8f, 8f, strokePaint)

                // Category pill
                val pillLeft = MARGIN_X + 12f
                val pillTop = cardTop + 10f
                bgPaint.color = Color.parseColor(topicColorCode)
                val pillPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.WHITE
                    textSize = 7.5f
                    isFakeBoldText = true
                }
                val pillText = entry.category.uppercase()
                val pillW = pillPaint.measureText(pillText) + 12f
                canvas.drawRoundRect(RectF(pillLeft, pillTop, pillLeft + pillW, pillTop + 14f), 4f, 4f, bgPaint)
                canvas.drawText(pillText, pillLeft + 6f, pillTop + 10.5f, pillPaint)

                // XP badge
                val xpText = "+${entry.xpGained} XP"
                val xpPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#059669")
                    textSize = 8.5f
                    isFakeBoldText = true
                }
                canvas.drawText(xpText, pillLeft + pillW + 10f, pillTop + 11f, xpPaint)

                // Date
                val datePaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.parseColor("#94A3B8")
                    textSize = 8f
                    textAlign = Paint.Align.RIGHT
                }
                canvas.drawText(entry.dateStr, MARGIN_X + CONTENT_WIDTH - 12f, pillTop + 11f, datePaint)

                // Draw Title
                var elemY = pillTop + 22f
                canvas.save()
                canvas.translate(MARGIN_X + 12f, elemY)
                titleLayout.draw(canvas)
                canvas.restore()
                elemY += titleLayout.height + 6f

                // Draw Quote / Hook
                bgPaint.color = Color.parseColor(topicColorCode)
                canvas.drawRect(MARGIN_X + 12f, elemY, MARGIN_X + 14f, elemY + quoteLayout.height, bgPaint)

                canvas.save()
                canvas.translate(MARGIN_X + 20f, elemY)
                quoteLayout.draw(canvas)
                canvas.restore()
                elemY += quoteLayout.height + 8f

                // Draw Steps
                if (stepLayouts.isNotEmpty()) {
                    val stepHeaderPaint = Paint().apply {
                        isAntiAlias = true
                        color = Color.parseColor("#64748B")
                        textSize = 8f
                        isFakeBoldText = true
                    }
                    canvas.drawText("KEY TAKEAWAYS:", MARGIN_X + 14f, elemY + 2f, stepHeaderPaint)
                    elemY += 10f

                    stepLayouts.forEach { sl ->
                        canvas.save()
                        canvas.translate(MARGIN_X + 16f, elemY)
                        sl.draw(canvas)
                        canvas.restore()
                        elemY += sl.height + 4f
                    }
                }

                // Draw Fun Fact
                if (funFactLayout != null) {
                    val ffRect = RectF(MARGIN_X + 12f, elemY, MARGIN_X + CONTENT_WIDTH - 12f, elemY + funFactHeight)
                    bgPaint.color = Color.parseColor("#EEF2F6")
                    canvas.drawRoundRect(ffRect, 6f, 6f, bgPaint)

                    canvas.save()
                    canvas.translate(MARGIN_X + 18f, elemY + 5f)
                    funFactLayout.draw(canvas)
                    canvas.restore()
                    elemY += funFactHeight + 4f
                }

                currentY = cardBottom + 12f
            }

            currentY += 8f
        }

        drawFooter()
        pdfDoc.finishPage(page)

        // Write PDF file to app cache and downloads
        val fileName = "Spark_Mind_Palace_Dossier.pdf"
        val outputFile = File(context.cacheDir, fileName)

        try {
            FileOutputStream(outputFile).use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()

            // Also copy to Downloads if possible
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (downloadsDir != null && downloadsDir.exists()) {
                    val target = File(downloadsDir, fileName)
                    outputFile.copyTo(target, overwrite = true)
                }
            } catch (e: Exception) { /* ignore */ }

            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDoc.close()
            Toast.makeText(context, "Failed to generate PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            return null
        }
    }

    fun sharePdf(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Spark Mind Palace Dossier")
                putExtra(Intent.EXTRA_TEXT, "Here is my Mind Palace Intellectual Dossier exported from Spark!")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Export & Share Spark Dossier"))
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open share chooser: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
