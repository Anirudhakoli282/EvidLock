import os
import hashlib
from PIL import Image, ImageDraw, ImageFont
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, HRFlowable
)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch

os.makedirs("evidence_demo", exist_ok=True)

# ==============================================================================
# Helper for Watermark / Header
# ==============================================================================
def draw_header_footer(canvas, doc):
    canvas.saveState()
    # Watermark at top
    canvas.setFont("Helvetica-Bold", 8)
    canvas.setFillColor(colors.HexColor("#b30000"))
    canvas.drawRightString(A4[0] - 36, A4[1] - 25, "[ DEMO - FICTIONAL CASE STUDY FOR HACKATHON EVALUATION ]")
    # Footer
    canvas.setFont("Helvetica", 8)
    canvas.setFillColor(colors.gray)
    canvas.drawString(36, 20, "EvidLock Forensic Archive | Demo Case: FIR 245/2026 | Fictional Exhibit")
    canvas.drawRightString(A4[0] - 36, 20, "Page 1 of 1")
    canvas.restoreState()

# ==============================================================================
# 1. 01_FIR_245_2026.pdf
# ==============================================================================
def create_fir_pdf(filename):
    doc = SimpleDocTemplate(
        filename, pagesize=A4,
        leftMargin=36, rightMargin=36, topMargin=40, bottomMargin=36
    )
    styles = getSampleStyleSheet()
    
    title_style = ParagraphStyle(
        'FIRTitle',
        parent=styles['Normal'],
        fontName='Helvetica-Bold',
        fontSize=14,
        alignment=1,
        textColor=colors.HexColor("#0f2043"),
        spaceAfter=4
    )
    sub_style = ParagraphStyle(
        'FIRSub',
        parent=styles['Normal'],
        fontName='Helvetica',
        fontSize=9,
        alignment=1,
        textColor=colors.HexColor("#444444"),
        spaceAfter=12
    )
    label_style = ParagraphStyle('FLabel', fontName='Helvetica-Bold', fontSize=8, textColor=colors.HexColor("#1c3b70"))
    val_style = ParagraphStyle('FVal', fontName='Helvetica', fontSize=8, textColor=colors.black)
    body_style = ParagraphStyle('FBody', fontName='Helvetica', fontSize=8.5, leading=12, textColor=colors.black)
    
    elements = []
    
    elements.append(Paragraph("FIRST INFORMATION REPORT (F.I.R.)", title_style))
    elements.append(Paragraph("(Under Section 154 Cr.P.C. / Section 173 BNSS 2023) - [ DEMO - FICTIONAL ]", sub_style))
    
    meta_data = [
        [Paragraph("District / Commission:", label_style), Paragraph("Pune City Police", val_style),
         Paragraph("Police Station:", label_style), Paragraph("Demo Police Station (Shivajinagar)", val_style)],
        [Paragraph("FIR Number:", label_style), Paragraph("245 / 2026", val_style),
         Paragraph("Date & Time of FIR:", label_style), Paragraph("15-Oct-2026 01:15 IST", val_style)],
        [Paragraph("Acts & Sections:", label_style), Paragraph("Demo Penal Code Sec. 302 / 201 [FICTIONAL]", val_style),
         Paragraph("Type of Incident:", label_style), Paragraph("Suspicious Death / Homicide Inquiry", val_style)],
        [Paragraph("Place of Occurrence:", label_style), Paragraph("Flat 402, B-Wing, Sunrise Apts, Pune", val_style),
         Paragraph("Distance from PS:", label_style), Paragraph("3.2 km South-East", val_style)],
    ]
    t_meta = Table(meta_data, colWidths=[105, 155, 105, 155])
    t_meta.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#f8f9fa")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#dcdfe6")),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    elements.append(t_meta)
    elements.append(Spacer(1, 10))
    
    parties_data = [
        [Paragraph("Complainant / Informant:", label_style), Paragraph("Ramesh K. More (Society Security Supervisor, Sunrise Apts)", val_style)],
        [Paragraph("Victim:", label_style), Paragraph("Priya Demo, Age: 23 years, Resident of Flat 402, Sunrise Apartments", val_style)],
        [Paragraph("Named Suspect / Accused:", label_style), Paragraph("Karan Demo, Age: 26 years, Resident of Pune [Fictional]", val_style)],
        [Paragraph("Investigating Officer (I.O.):", label_style), Paragraph("PI Priya Sharma, Badge #MPD-INV-402, Demo Police Station", val_style)],
    ]
    t_parties = Table(parties_data, colWidths=[130, 390])
    t_parties.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,-1), colors.HexColor("#eef2f7")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#dcdfe6")),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    elements.append(t_parties)
    elements.append(Spacer(1, 10))
    
    elements.append(Paragraph("<b>Brief Circumstances & Information Received:</b>", label_style))
    narrative = (
        "On 14-Oct-2026 at approximately 23:45 hours, the complainant Ramesh More, security supervisor at Sunrise Apartments, "
        "reported that resident Priya Demo (23) was found unresponsive on the floor of her apartment (Flat 402, B-Wing). "
        "The apartment main door was found unlatched. Emergency medical responders declared her deceased at the scene with no immediate "
        "signs of forced external entry. Initial examination indicated foul play.<br/><br/>"
        "Complainant stated that building visitor registers and perimeter gate cameras (CAM-01) record all vehicular and pedestrian ingress. "
        "Suspect Karan Demo was reportedly seen entering the compound during late evening hours. Investigation initiated under "
        "Demo Sections 302 and 201. Case assigned to PI Priya Sharma. Digital surveillance records and telecommunication logs "
        "ordered to be seized immediately under chain of custody."
    )
    elements.append(Paragraph(narrative, body_style))
    elements.append(Spacer(1, 18))
    
    sig_data = [
        [Paragraph("<b>Ramesh K. More</b><br/>Signature of Complainant<br/>Thumb Impression / Sign", val_style),
         Paragraph("<b>PI Priya Sharma</b><br/>Inspector of Police, Badge MPD-INV-402<br/>Demo Police Station, Pune City", val_style)]
    ]
    t_sig = Table(sig_data, colWidths=[260, 260])
    t_sig.setStyle(TableStyle([
        ('LINEABOVE', (0,0), (0,0), 1, colors.HexColor("#333333")),
        ('LINEABOVE', (1,0), (1,0), 1, colors.HexColor("#333333")),
        ('TOPPADDING', (0,0), (-1,-1), 6),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
    ]))
    elements.append(t_sig)
    
    doc.build(elements, onFirstPage=draw_header_footer)
    print(f"Created {filename}")

# ==============================================================================
# 2. 02_Scene_and_DVR_Seizure_Memo.pdf
# ==============================================================================
def create_seizure_memo_pdf(filename):
    doc = SimpleDocTemplate(
        filename, pagesize=A4,
        leftMargin=36, rightMargin=36, topMargin=40, bottomMargin=36
    )
    styles = getSampleStyleSheet()
    
    title_style = ParagraphStyle(
        'MemoTitle', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=13, alignment=1,
        textColor=colors.HexColor("#0f2043"), spaceAfter=4
    )
    sub_style = ParagraphStyle(
        'MemoSub', parent=styles['Normal'],
        fontName='Helvetica', fontSize=8.5, alignment=1,
        textColor=colors.HexColor("#444444"), spaceAfter=10
    )
    label_style = ParagraphStyle('MLabel', fontName='Helvetica-Bold', fontSize=8, textColor=colors.HexColor("#1c3b70"))
    val_style = ParagraphStyle('MVal', fontName='Helvetica', fontSize=8, textColor=colors.black)
    body_style = ParagraphStyle('MBody', fontName='Helvetica', fontSize=8, leading=11.5, textColor=colors.black)
    
    elements = []
    
    elements.append(Paragraph("EVIDENTIARY SEIZURE MEMO / PANCHNAMA", title_style))
    elements.append(Paragraph("Case: FIR No. 245/2026 | Demo Police Station | [ DEMO - FICTIONAL ]", sub_style))
    
    memo_meta = [
        [Paragraph("Date of Seizure:", label_style), Paragraph("15-October-2026", val_style),
         Paragraph("Time of Seizure:", label_style), Paragraph("08:30 to 10:15 IST", val_style)],
        [Paragraph("Location of Seizure:", label_style), Paragraph("Security Control Cabin & Flat 402, Sunrise Apts, Pune", val_style),
         Paragraph("Investigating Officer:", label_style), Paragraph("PI Priya Sharma (MPD-INV-402)", val_style)],
    ]
    t_meta = Table(memo_meta, colWidths=[105, 155, 105, 155])
    t_meta.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#f8f9fa")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#dcdfe6")),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    elements.append(t_meta)
    elements.append(Spacer(1, 8))
    
    elements.append(Paragraph("<b>Independent Witnesses (Panchas):</b>", label_style))
    panch_data = [
        [Paragraph("Witness 1 (Panch):", label_style), Paragraph("Ramesh K. More, Age 48, Resident of Flat 201, Sunrise Apartments, Pune", val_style)],
        [Paragraph("Witness 2 (Panch):", label_style), Paragraph("Suresh N. Patil, Age 39, Resident of Flat 304, Sunrise Apartments, Pune", val_style)],
    ]
    t_panch = Table(panch_data, colWidths=[110, 410])
    t_panch.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,-1), colors.HexColor("#eef2f7")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#dcdfe6")),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
    ]))
    elements.append(t_panch)
    elements.append(Spacer(1, 8))
    
    elements.append(Paragraph("<b>Physical Inspection & Seizure Particulars:</b>", label_style))
    narrative = (
        "We, the undersigned witnesses and Investigating Officer, assembled at the scene of incident at Flat 402, Sunrise Apartments. "
        "In our presence, the apartment main entrance was physically sealed using tamper-evident paper seals bearing our signatures. "
        "Thereafter, we proceeded to the Ground Floor Main Security Cabin housing the digital surveillance infrastructure. "
        "In our direct presence, the CCTV recording apparatus controlling the main perimeter gate camera was seized as described below:<br/>"
        "<b>1. Digital Video Recorder (DVR):</b> Hikvision 8-Channel Surveillance DVR, Model: DS-7208HQHI-K1, Serial Number: HK-982104-DEMO. "
        "Includes one internal 2.0 TB Seagate SkyHawk SATA Hard Disk Drive.<br/>"
        "<b>2. Video Segment Preserved:</b> Master uncompressed video stream from Camera 01 (Main Gate Ingress/Egress) covering "
        "period 14-Oct-2026 21:00:00 to 15-Oct-2026 02:00:00.<br/>"
        "<b>3. Packaging & Sealing:</b> The DVR unit and original storage disk were disconnected, packed in anti-static forensic evidence "
        "bag No. EVID-BAG-2026-9921, and sealed with lac / wax seal bearing the stamp of Demo Police Station.<br/>"
        "<i>(Note: In compliance with statutory seizure procedure, raw checksum generation is designated to the certified Forensic Science Laboratory vault upon intake).</i>"
    )
    elements.append(Paragraph(narrative, body_style))
    elements.append(Spacer(1, 16))
    
    sig_data = [
        [Paragraph("<b>Ramesh K. More</b><br/>Panch Witness 1", val_style),
         Paragraph("<b>Suresh N. Patil</b><br/>Panch Witness 2", val_style),
         Paragraph("<b>PI Priya Sharma</b><br/>Seizing Officer (MPD-INV-402)", val_style)]
    ]
    t_sig = Table(sig_data, colWidths=[173, 173, 174])
    t_sig.setStyle(TableStyle([
        ('LINEABOVE', (0,0), (-1,0), 1, colors.HexColor("#333333")),
        ('TOPPADDING', (0,0), (-1,-1), 6),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
    ]))
    elements.append(t_sig)
    
    doc.build(elements, onFirstPage=draw_header_footer)
    print(f"Created {filename}")

# ==============================================================================
# 3. 05_Phone_Forensic_Report_FSL_2026_0612.pdf
# ==============================================================================
def create_fsl_report_pdf(filename):
    doc = SimpleDocTemplate(
        filename, pagesize=A4,
        leftMargin=36, rightMargin=36, topMargin=40, bottomMargin=36
    )
    styles = getSampleStyleSheet()
    
    title_style = ParagraphStyle(
        'FSLTitle', parent=styles['Normal'],
        fontName='Helvetica-Bold', fontSize=13, alignment=1,
        textColor=colors.HexColor("#0f2043"), spaceAfter=3
    )
    sub_style = ParagraphStyle(
        'FSLSub', parent=styles['Normal'],
        fontName='Helvetica', fontSize=8.5, alignment=1,
        textColor=colors.HexColor("#444444"), spaceAfter=8
    )
    label_style = ParagraphStyle('FSLab', fontName='Helvetica-Bold', fontSize=8, textColor=colors.HexColor("#1c3b70"))
    val_style = ParagraphStyle('FSVal', fontName='Helvetica', fontSize=8, textColor=colors.black)
    body_style = ParagraphStyle('FSBody', fontName='Helvetica', fontSize=8, leading=11.5, textColor=colors.black)
    highlight_style = ParagraphStyle('FSHigh', fontName='Courier-Bold', fontSize=8, textColor=colors.HexColor("#b30000"))
    
    elements = []
    
    elements.append(Paragraph("STATE FORENSIC SCIENCE LABORATORY (SFL-02)", title_style))
    elements.append(Paragraph("Digital Forensics & Cyber Analysis Division | FSL Ref: FSL/DIGITAL/2026/0612<br/>[ DEMO - FICTIONAL LABORATORY REPORT ]", sub_style))
    
    fsl_meta = [
        [Paragraph("Forwarding Authority:", label_style), Paragraph("Demo Police Station, Pune (Ref: Case 245/2026)", val_style),
         Paragraph("Date Received at Lab:", label_style), Paragraph("17-October-2026 11:30 IST", val_style)],
        [Paragraph("Tamper Bag Number:", label_style), Paragraph("SEB-7821-DEMO (Seal Intact)", val_style),
         Paragraph("Device Examined:", label_style), Paragraph("OnePlus 11R (Victim Priya Demo)", val_style)],
        [Paragraph("IMEI / Serial:", label_style), Paragraph("864902058192019 / SN: OP11R-DEMO", val_style),
         Paragraph("Operating System:", label_style), Paragraph("OxygenOS 14 (Android 14)", val_style)],
    ]
    t_meta = Table(fsl_meta, colWidths=[105, 155, 105, 155])
    t_meta.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#f8f9fa")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#dcdfe6")),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
    ]))
    elements.append(t_meta)
    elements.append(Spacer(1, 8))
    
    elements.append(Paragraph("<b>Forensic Extraction Methodology:</b>", label_style))
    method_text = (
        "The mobile device was unsealed in the clean digital forensics enclosure. Physical Faraday isolation maintained. "
        "Full bitstream logical and physical extraction performed using certified forensic acquisition tool (UFED v8.4.1). "
        "Extraction output verified against bitstream acquisition hash without write-back alteration."
    )
    elements.append(Paragraph(method_text, body_style))
    elements.append(Spacer(1, 8))
    
    elements.append(Paragraph("<b>Key Recovered Telecommunication Artifact:</b>", label_style))
    msg_data = [
        [Paragraph("Artifact ID:", label_style), Paragraph("MSG-EXTRACTION-ITEM-041", val_style)],
        [Paragraph("Application / Source:", label_style), Paragraph("WhatsApp Messenger (Instant Messaging Database: msgstore.db)", val_style)],
        [Paragraph("Sender Contact:", label_style), Paragraph("+91-98202-DEMO (Priya Demo - Victim)", val_style)],
        [Paragraph("Exact Timestamp:", label_style), Paragraph("<b>14-Oct-2026 23:30:19 IST</b>", highlight_style)],
        [Paragraph("Recovered Message Content:", label_style), Paragraph("<i>\"Who is at the door?\"</i>", highlight_style)],
        [Paragraph("Delivery Status:", label_style), Paragraph("Sent from device; two checkmarks confirmed on network server.", val_style)],
    ]
    t_msg = Table(msg_data, colWidths=[120, 400])
    t_msg.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,-1), colors.HexColor("#eef2f7")),
        ('GRID', (0,0), (-1,-1), 0.5, colors.HexColor("#dcdfe6")),
        ('TOPPADDING', (0,0), (-1,-1), 3.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3.5),
    ]))
    elements.append(t_msg)
    elements.append(Spacer(1, 8))
    
    elements.append(Paragraph("<b>Forensic Conclusion & Significance:</b>", label_style))
    conclusion = (
        "Forensic timeline reconstruction establishes active user interaction on victim Priya Demo's device at 23:30:19 IST. "
        "This critical finding corroborates gate camera surveillance logs confirming an authorized entry into the building "
        "prior to 23:30. Digital extraction image preserved under certified evidence archive hash."
    )
    elements.append(Paragraph(conclusion, body_style))
    elements.append(Spacer(1, 16))
    
    sig_data = [
        [Paragraph("<b>Dr. Vikram Desai</b><br/>Assistant Director (Digital Forensics)<br/>State Forensic Science Laboratory (SFL-02), Maharashtra", val_style),
         Paragraph("<b>Official Laboratory Seal</b><br/>Government of Maharashtra<br/>Directorate of Forensic Science Laboratories", val_style)]
    ]
    t_sig = Table(sig_data, colWidths=[260, 260])
    t_sig.setStyle(TableStyle([
        ('LINEABOVE', (0,0), (-1,0), 1, colors.HexColor("#333333")),
        ('TOPPADDING', (0,0), (-1,-1), 6),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
    ]))
    elements.append(t_sig)
    
    doc.build(elements, onFirstPage=draw_header_footer)
    print(f"Created {filename}")

# ==============================================================================
# 3 & 4. 03_CCTV_Frame_ORIGINAL_2230.png & 04_CCTV_Frame_EDITED_BY_DEFENSE_2130.png
# ==============================================================================
def create_cctv_images():
    width = 1280
    height = 720
    
    # Base CCTV Night-Vision / Infrared Image
    img = Image.new("RGB", (width, height), color=(18, 26, 22))
    draw = ImageDraw.Draw(img)
    
    # Ground pavement
    draw.rectangle([0, 480, 1280, 720], fill=(28, 38, 32))
    for y in range(480, 720, 30):
        draw.line([0, y, 1280, y], fill=(22, 30, 26), width=1)
        
    # Compound Wall & Gate Pillars
    # Left Pillar
    draw.rectangle([100, 200, 220, 520], fill=(45, 55, 48), outline=(30, 40, 35), width=2)
    # Right Pillar
    draw.rectangle([800, 200, 920, 520], fill=(45, 55, 48), outline=(30, 40, 35), width=2)
    
    # Iron Gate (Sliding gate open halfway)
    # Gate Frame
    draw.rectangle([220, 230, 600, 500], outline=(70, 85, 75), width=3)
    for x in range(240, 600, 25):
        draw.line([x, 230, x, 500], fill=(65, 80, 70), width=2)
    # Gate diagonal support
    draw.line([220, 230, 600, 500], fill=(60, 75, 65), width=2)
    draw.line([220, 500, 600, 230], fill=(60, 75, 65), width=2)
    
    # Security Kiosk on left
    draw.rectangle([0, 240, 100, 500], fill=(35, 45, 40), outline=(25, 35, 30))
    draw.rectangle([15, 270, 85, 330], fill=(55, 70, 60)) # Kiosk window
    
    # Background apartment building silhouette
    draw.rectangle([300, 80, 750, 450], fill=(22, 30, 25))
    # Some building windows
    for wx in range(330, 720, 60):
        for wy in range(110, 380, 50):
            draw.rectangle([wx, wy, wx+30, wy+35], fill=(35, 48, 40))
            
    # Streetlight beam cone
    draw.polygon([(860, 180), (450, 650), (950, 650)], fill=(32, 44, 36))
    
    # Person Silhouette entering through the gate opening (around x=620, y=340 to 520)
    # Head
    draw.ellipse([640, 340, 675, 375], fill=(12, 16, 14))
    # Torso (Jacket/Hoodie)
    draw.polygon([(630, 375), (685, 375), (695, 460), (620, 460)], fill=(10, 14, 12))
    # Legs walking
    draw.polygon([(625, 460), (655, 460), (640, 530), (620, 530)], fill=(8, 12, 10))
    draw.polygon([(660, 460), (690, 460), (715, 525), (695, 525)], fill=(8, 12, 10))
    
    # CCTV Noise / Scanlines
    for y in range(0, 720, 4):
        draw.line([0, y, 1280, y], fill=(15, 22, 18), width=1)
        
    # Standard CCTV Monospace / OSD Overlay font
    # Try default or load standard font
    try:
        font_cctv = ImageFont.truetype("courbd.ttf", 26)
        font_watermark = ImageFont.truetype("arialbd.ttf", 16)
    except:
        font_cctv = ImageFont.load_default()
        font_watermark = ImageFont.load_default()
        
    # Watermark (DEMO - FICTIONAL)
    draw.rectangle([20, 20, 410, 50], fill=(0, 0, 0, 180))
    draw.text((25, 25), "[ DEMO - FICTIONAL COURT EXHIBIT ]", fill=(255, 80, 80), font=font_watermark)
    
    draw.text((25, 680), "EVIDLOCK FORENSIC SURVEILLANCE ARCHIVE | SUNRISE APTS CAM-01", fill=(160, 180, 170), font=font_watermark)
    
    # Save base copy
    base_img = img.copy()
    
    # Image 1: 03_CCTV_Frame_ORIGINAL_2230.png
    draw_orig = ImageDraw.Draw(base_img)
    # Timestamp overlay box (top right)
    draw_orig.rectangle([760, 20, 1260, 58], fill=(0, 0, 0))
    draw_orig.text((770, 24), "CAM 01 GATE  15-10-2026  22:30:12", fill=(255, 255, 255), font=font_cctv)
    orig_path = os.path.join("evidence_demo", "03_CCTV_Frame_ORIGINAL_2230.png")
    base_img.save(orig_path, "PNG")
    print(f"Created {orig_path}")
    
    # Image 2: 04_CCTV_Frame_EDITED_BY_DEFENSE_2130.png
    # EXACT same base image, ONLY the timestamp string changes to 21:30:12
    edited_img = img.copy()
    draw_edit = ImageDraw.Draw(edited_img)
    draw_edit.rectangle([760, 20, 1260, 58], fill=(0, 0, 0))
    draw_edit.text((770, 24), "CAM 01 GATE  15-10-2026  21:30:12", fill=(255, 255, 255), font=font_cctv)
    edited_path = os.path.join("evidence_demo", "04_CCTV_Frame_EDITED_BY_DEFENSE_2130.png")
    edited_img.save(edited_path, "PNG")
    print(f"Created {edited_path}")

# Run generation
create_fir_pdf(os.path.join("evidence_demo", "01_FIR_245_2026.pdf"))
create_seizure_memo_pdf(os.path.join("evidence_demo", "02_Scene_and_DVR_Seizure_Memo.pdf"))
create_fsl_report_pdf(os.path.join("evidence_demo", "05_Phone_Forensic_Report_FSL_2026_0612.pdf"))
create_cctv_images()
print("All 5 demo evidence assets generated successfully!")
