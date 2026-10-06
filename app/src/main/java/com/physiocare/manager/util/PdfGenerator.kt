package com.physiocare.manager.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.physiocare.manager.data.local.entity.PatientEntity
import com.physiocare.manager.data.local.entity.SessionEntity
import java.io.File
import java.io.FileOutputStream

class PdfGenerator(private val context: Context) {

    fun generateBill(
        patient: PatientEntity,
        sessions: List<SessionEntity>,
        monthYear: String,
        totalCharges: Int,
        totalPaid: Int,
        clinicName: String
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 0).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
        }
        val headingPaint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            textSize = 12f
        }
        val smallPaint = Paint().apply {
            textSize = 10f
        }

        var y = 40f

        // Clinic Name
        canvas.drawText(clinicName, 40f, y, titlePaint)
        y += 30f

        // Title
        canvas.drawText("BILL STATEMENT", 40f, y, headingPaint)
        y += 25f

        // Patient details
        canvas.drawText("Patient: ${patient.fullName}", 40f, y, bodyPaint)
        y += 20f
        canvas.drawText("Condition: ${patient.condition}", 40f, y, bodyPaint)
        y += 20f
        canvas.drawText("Mobile: ${patient.mobileNumber}", 40f, y, bodyPaint)
        y += 20f
        canvas.drawText("Period: $monthYear", 40f, y, bodyPaint)
        y += 20f
        canvas.drawText("Session Rate: ${CurrencyUtils.format(patient.perSessionCharge)}", 40f, y, bodyPaint)
        y += 30f

        // Table header
        canvas.drawText("Date", 40f, y, headingPaint)
        canvas.drawText("Status", 200f, y, headingPaint)
        canvas.drawText("Charge", 350f, y, headingPaint)
        canvas.drawText("Payment", 470f, y, headingPaint)
        y += 5f
        canvas.drawLine(40f, y, 555f, y, bodyPaint)
        y += 15f

        // Session rows
        var currentPage = page
        var currentCanvas = canvas
        var currentY = y
        for (session in sessions.filter { it.status == "Present" }) {
            if (currentY > 750f) {
                // End page and start new one if needed
                pdfDocument.finishPage(currentPage)
                val newPage = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create())
                currentPage = newPage
                currentCanvas = newPage.canvas
                currentY = 40f
            }
            currentCanvas.drawText(DateUtils.formatEpoch(session.date), 40f, currentY, bodyPaint)
            currentCanvas.drawText(session.status, 200f, currentY, bodyPaint)
            currentCanvas.drawText(CurrencyUtils.format(session.charge), 350f, currentY, bodyPaint)
            currentCanvas.drawText(session.paymentStatus, 470f, currentY, bodyPaint)
            currentY += 20f
        }

        currentY += 15f
        currentCanvas.drawLine(40f, currentY, 555f, currentY, bodyPaint)
        currentY += 20f

        // Summary
        currentCanvas.drawText("Total Sessions: ${sessions.count { it.status == "Present" }}", 40f, currentY, headingPaint)
        currentY += 25f
        currentCanvas.drawText("Total Charges: ${CurrencyUtils.format(totalCharges)}", 40f, currentY, headingPaint)
        currentY += 25f
        currentCanvas.drawText("Total Paid: ${CurrencyUtils.format(totalPaid)}", 40f, currentY, headingPaint)
        currentY += 25f
        val balance = totalCharges - totalPaid
        currentCanvas.drawText("Balance Due: ${CurrencyUtils.format(balance)}", 40f, currentY, headingPaint)
        currentY += 30f

        currentCanvas.drawText("Thank you for your visit!", 40f, currentY, smallPaint)
        currentY += 15f
        currentCanvas.drawText("Generated on ${DateUtils.formatEpoch(System.currentTimeMillis())}", 40f, currentY, smallPaint)

        pdfDocument.finishPage(currentPage)

        val billDir = File(context.cacheDir, "bills")
        if (!billDir.exists()) billDir.mkdirs()

        val fileName = "bill_${patient.fullName.replace(" ", "_")}_${monthYear.replace(" ", "_")}.pdf"
        val file = File(billDir, fileName)
        FileOutputStream(file).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()

        return file
    }
}
