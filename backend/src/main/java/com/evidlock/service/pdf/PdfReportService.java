package com.evidlock.service.pdf;

import com.evidlock.persistence.DBManager;
import com.evidlock.persistence.model.Case;
import com.evidlock.persistence.model.ChainVerificationResult;
import com.evidlock.persistence.model.CustodyEvent;
import com.evidlock.persistence.model.EvidenceItem;
import com.evidlock.service.crypto.HashChainService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class PdfReportService {

    private static PdfReportService instance = null;

    private PdfReportService() {}

    public static synchronized PdfReportService getInstance() {
        if (instance == null) {
            instance = new PdfReportService();
        }
        return instance;
    }

    public byte[] generateChainOfCustodyReport(Long evidenceItemId, String requestingActor) {
        EvidenceItem item = DBManager.getInstance().getEvidenceItemDao().findById(evidenceItemId);
        if (item == null) {
            throw new IllegalArgumentException("Evidence item not found: " + evidenceItemId);
        }

        Case c = DBManager.getInstance().getCaseDao().findById(item.getCaseId());
        ChainVerificationResult verification = HashChainService.getInstance().verifyChain(evidenceItemId);
        List<CustodyEvent> events = verification.getEvents();

        Document document = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            // Colors
            Color primaryNavy = new Color(15, 32, 67);
            Color accentBlue = new Color(28, 79, 156);
            Color lightBg = new Color(245, 247, 250);
            Color borderGray = new Color(210, 215, 225);
            Color statusGreen = new Color(24, 134, 75);
            Color statusRed = new Color(192, 41, 43);

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, primaryNavy);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, accentBlue);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font codeFont = FontFactory.getFont(FontFactory.COURIER, 7, Color.DARK_GRAY);
            Font badgeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            // 1. Header Banner
            Paragraph header = new Paragraph("EVIDLOCK DIGITAL EVIDENCE CHAIN OF CUSTODY REPORT", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph sub = new Paragraph("Certificate under Section 65B of the Indian Evidence Act, 1872 & Section 63 of Bharatiya Sakshya Adhiniyam (BSA), 2023\n" +
                    "Court-Admissible Cryptographic Electronic Record | ISO/IEC 27037 Standards Compliant\n" +
                    "Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Timestamp(System.currentTimeMillis())) +
                    " | Requested by: " + (requestingActor != null ? requestingActor : "Authorized Officer") + "\n\n", subtitleFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            document.add(sub);

            // 2. Cryptographic Integrity Banner
            PdfPTable sealTable = new PdfPTable(1);
            sealTable.setWidthPercentage(100);
            PdfPCell sealCell = new PdfPCell();
            sealCell.setPadding(8);
            if (verification.isValid()) {
                sealCell.setBackgroundColor(statusGreen);
                Paragraph sealText = new Paragraph("CRYPTOGRAPHIC CHAIN INTEGRITY: VERIFIED & UNBROKEN\n" +
                        "All " + events.size() + " sequential blocks match SHA-256 hash digests. Ledger is tamper-free.", badgeFont);
                sealText.setAlignment(Element.ALIGN_CENTER);
                sealCell.addElement(sealText);
            } else {
                sealCell.setBackgroundColor(statusRed);
                Paragraph sealText = new Paragraph("WARNING: CRYPTOGRAPHIC CHAIN INTEGRITY COMPROMISED\n" +
                        verification.getBrokenReason(), badgeFont);
                sealText.setAlignment(Element.ALIGN_CENTER);
                sealCell.addElement(sealText);
            }
            sealTable.addCell(sealCell);
            document.add(sealTable);
            document.add(new Paragraph("\n"));

            // 3. Evidence & Case Metadata Table
            Paragraph sec1 = new Paragraph("1. EVIDENCE SPECIFICATIONS & CASE INFORMATION", sectionFont);
            document.add(sec1);
            document.add(new Paragraph(" "));

            PdfPTable metaTable = new PdfPTable(4);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{22f, 28f, 22f, 28f});

            addMetaCell(metaTable, "Evidence Tag / ID:", boldFont, lightBg, borderGray);
            addMetaCell(metaTable, item.getEvidenceTag(), bodyFont, Color.WHITE, borderGray);
            addMetaCell(metaTable, "Case Number:", boldFont, lightBg, borderGray);
            addMetaCell(metaTable, c != null ? c.getCaseNumber() : "N/A", bodyFont, Color.WHITE, borderGray);

            addMetaCell(metaTable, "Original Filename:", boldFont, lightBg, borderGray);
            addMetaCell(metaTable, item.getOriginalFilename(), bodyFont, Color.WHITE, borderGray);
            addMetaCell(metaTable, "Case Title:", boldFont, lightBg, borderGray);
            addMetaCell(metaTable, c != null ? c.getTitle() : "N/A", bodyFont, Color.WHITE, borderGray);

            addMetaCell(metaTable, "Current Custodian:", boldFont, lightBg, borderGray);
            addMetaCell(metaTable, item.getCurrentCustodianId(), bodyFont, Color.WHITE, borderGray);
            addMetaCell(metaTable, "Custody Status:", boldFont, lightBg, borderGray);
            addMetaCell(metaTable, item.getStatus(), bodyFont, Color.WHITE, borderGray);

            addMetaCell(metaTable, "Legal Hold Active:", boldFont, lightBg, borderGray);
            addMetaCell(metaTable, item.isLegalHold() ? "YES (LOCKED)" : "NO", bodyFont, Color.WHITE, borderGray);
            addMetaCell(metaTable, "Agency Code:", boldFont, lightBg, borderGray);
            addMetaCell(metaTable, item.getAgencyCode(), bodyFont, Color.WHITE, borderGray);

            addMetaCell(metaTable, "SHA-256 File Digest:", boldFont, lightBg, borderGray);
            PdfPCell hashCell = new PdfPCell(new Phrase(item.getFileHash(), codeFont));
            hashCell.setColspan(3);
            hashCell.setBackgroundColor(Color.WHITE);
            hashCell.setBorderColor(borderGray);
            hashCell.setPadding(5);
            metaTable.addCell(hashCell);

            document.add(metaTable);
            document.add(new Paragraph("\n"));

            // 4. Chronological Custody Event Ledger Table
            Paragraph sec2 = new Paragraph("2. CHRONOLOGICAL CUSTODY EVENT LEDGER (APPEND-ONLY)", sectionFont);
            document.add(sec2);
            document.add(new Paragraph(" "));

            PdfPTable logTable = new PdfPTable(6);
            logTable.setWidthPercentage(100);
            logTable.setWidths(new float[]{8f, 16f, 14f, 20f, 20f, 22f});

            // Table Header
            String[] headers = {"#", "Timestamp", "Action", "Actor / Agency", "Custodian Transfer", "Event Hash (SHA-256)"};
            for (String h : headers) {
                PdfPCell th = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
                th.setBackgroundColor(primaryNavy);
                th.setPadding(5);
                th.setHorizontalAlignment(Element.ALIGN_CENTER);
                logTable.addCell(th);
            }

            int index = 1;
            for (CustodyEvent event : events) {
                Color rowBg = (index % 2 == 0) ? lightBg : Color.WHITE;

                // Event index
                PdfPCell cIdx = new PdfPCell(new Phrase(String.valueOf(index++), boldFont));
                cIdx.setBackgroundColor(rowBg);
                cIdx.setPadding(4);
                cIdx.setHorizontalAlignment(Element.ALIGN_CENTER);
                logTable.addCell(cIdx);

                // Timestamp
                String ts = event.getTimestamp() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(event.getTimestamp()) : "";
                PdfPCell cTs = new PdfPCell(new Phrase(ts, bodyFont));
                cTs.setBackgroundColor(rowBg);
                cTs.setPadding(4);
                logTable.addCell(cTs);

                // Action
                PdfPCell cAct = new PdfPCell(new Phrase(event.getAction(), boldFont));
                cAct.setBackgroundColor(rowBg);
                cAct.setPadding(4);
                logTable.addCell(cAct);

                // Actor
                PdfPCell cActor = new PdfPCell(new Phrase(event.getActorId(), bodyFont));
                cActor.setBackgroundColor(rowBg);
                cActor.setPadding(4);
                logTable.addCell(cActor);

                // Custodian Transfer From -> To
                String xfer = "";
                if (event.getFromCustodianId() != null || event.getToCustodianId() != null) {
                    xfer = (event.getFromCustodianId() != null ? event.getFromCustodianId() : "-") + " -> " +
                           (event.getToCustodianId() != null ? event.getToCustodianId() : "-");
                } else {
                    xfer = "N/A";
                }
                PdfPCell cXfer = new PdfPCell(new Phrase(xfer, bodyFont));
                cXfer.setBackgroundColor(rowBg);
                cXfer.setPadding(4);
                logTable.addCell(cXfer);

                // Event Hash (formatted)
                String shortHash = event.getEventHash() != null && event.getEventHash().length() > 16
                        ? event.getEventHash().substring(0, 16) + "..." : event.getEventHash();
                PdfPCell cHash = new PdfPCell(new Phrase(shortHash, codeFont));
                cHash.setBackgroundColor(rowBg);
                cHash.setPadding(4);
                logTable.addCell(cHash);
            }

            document.add(logTable);
            document.add(new Paragraph("\n"));

            // 5. Judicial Attestation & Signature Block
            Paragraph sec3 = new Paragraph("3. STATUTORY EVIDENTIARY ATTESTATION (SEC. 65B EVIDENCE ACT & SEC. 63 BSA)", sectionFont);
            document.add(sec3);
            Paragraph certText = new Paragraph(
                    "I hereby solemnly declare and certify under Section 65B of the Indian Evidence Act, 1872 and Section 63 of Bharatiya " +
                    "Sakshya Adhiniyam, 2023 that:\n" +
                    "1. The electronic record described herein was produced by a computer system during the period over which the computer was " +
                    "used regularly to store or process digital evidence for the purposes of forensic activities carried out by the investigating agency.\n" +
                    "2. Throughout the said period, information of the kind contained in the electronic record was regularly fed into the computer in the ordinary course of activities.\n" +
                    "3. Throughout the material part of the said period, the computer was operating properly and without malfunction affecting electronic custody records.\n" +
                    "4. The digital evidence has been cryptographically sealed using SHA-256 bitstream hash digest and AES-256-GCM cipher standards, verified against the tamper-evident ledger.\n\n",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.DARK_GRAY)
            );
            document.add(certText);

            PdfPTable sigTable = new PdfPTable(2);
            sigTable.setWidthPercentage(100);
            sigTable.setWidths(new float[]{50f, 50f});

            PdfPCell sig1 = new PdfPCell();
            sig1.setBorder(Rectangle.NO_BORDER);
            sig1.addElement(new Paragraph("Officer / Investigator Signature: ____________________________", bodyFont));
            sig1.addElement(new Paragraph("Badge / ID Number: ______________________________________", bodyFont));
            sig1.addElement(new Paragraph("Date: __________________________________________________", bodyFont));

            PdfPCell sig2 = new PdfPCell();
            sig2.setBorder(Rectangle.NO_BORDER);
            sig2.addElement(new Paragraph("Evidence Vault Custodian Signature: ________________________", bodyFont));
            sig2.addElement(new Paragraph("State Forensic Registry: __________________________________", bodyFont));
            sig2.addElement(new Paragraph("Verification Hash Seal: [" + (events.isEmpty() ? "GENESIS" : events.get(events.size() - 1).getEventHash().substring(0, 20) + "...") + "]", codeFont));

            sigTable.addCell(sig1);
            sigTable.addCell(sig2);
            document.add(sigTable);

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    private void addMetaCell(PdfPTable table, String text, Font font, Color bg, Color border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setBorderColor(border);
        cell.setPadding(5);
        table.addCell(cell);
    }
}
