package com.crossborder.hospitalB.service;

import com.crossborder.hospitalB.model.PatientEntity;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.EncryptionConstants;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class EmailNotificationService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPatientDataPDF(String to, String doctorId, PatientEntity patient, boolean granted) {
        try {
            // Generate encrypted PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            WriterProperties props = new WriterProperties()
                    .setStandardEncryption(
                            "doctor123".getBytes(),
                            "owner123".getBytes(),
                            EncryptionConstants.ALLOW_PRINTING,
                            EncryptionConstants.ENCRYPTION_AES_128);

            PdfWriter writer = new PdfWriter(baos, props);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Load fonts
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Title
            Paragraph title = new Paragraph("🩺 Patient Health Report")
                    .setFont(boldFont)
                    .setFontSize(16)
                    .setMarginBottom(20);
            document.add(title);

            // Table with patient details
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                    .useAllAvailableWidth();

            table.addCell(new Cell().add(new Paragraph("Patient ID").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph(patient.getPatientId()).setFont(regularFont)));

            table.addCell(new Cell().add(new Paragraph("Name").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph(patient.getName()).setFont(regularFont)));

            table.addCell(new Cell().add(new Paragraph("Age").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(patient.getAge())).setFont(regularFont)));

            table.addCell(new Cell().add(new Paragraph("Gender").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph(patient.getGender()).setFont(regularFont)));

            table.addCell(new Cell().add(new Paragraph("Blood Type").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph(patient.getBloodType()).setFont(regularFont)));

            table.addCell(new Cell().add(new Paragraph("Diagnosis / Condition").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph(patient.getMedicalCondition()).setFont(regularFont)));

            table.addCell(new Cell().add(new Paragraph("Medication").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph(patient.getMedication()).setFont(regularFont)));

            table.addCell(new Cell().add(new Paragraph("Test Results").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph(patient.getTestResults()).setFont(regularFont)));

            table.addCell(new Cell().add(new Paragraph("Date of Admission").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph(patient.getDateOfAdmission().toString()).setFont(regularFont))); // ✅ Fix

            table.addCell(new Cell().add(new Paragraph("Discharge Date").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph(patient.getDischargeDate().toString()).setFont(regularFont))); // ✅ Fix

            table.addCell(new Cell().add(new Paragraph("Doctor").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph(patient.getDoctor()).setFont(regularFont)));

            table.addCell(new Cell().add(new Paragraph("Hospital").setFont(boldFont)));
            table.addCell(new Cell().add(new Paragraph(patient.getHospital()).setFont(regularFont)));

            document.add(table);
            document.close();

            // Send email with PDF attached
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("kshitijdghodekar@gmail.com");
            helper.setTo(to);
            helper.setSubject(granted ? "Access Granted: Encrypted Patient Report" : "Access Denied");
            helper.setText("Doctor " + doctorId + ",\n\nPlease find the encrypted patient report attached.\nPassword to open PDF: doctor123");

            InputStreamSource pdfAttachment = new ByteArrayResource(baos.toByteArray());
            helper.addAttachment("patient-report.pdf", pdfAttachment);

            mailSender.send(message);
            System.out.println("Encrypted PDF email sent to: " + to);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send PDF email.");
        }
    }
}
