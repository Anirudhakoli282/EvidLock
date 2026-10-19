import { Injectable } from '@angular/core';

export interface PiiDetectionResult {
  originalText: string;
  redactedText: string;
  piiCount: number;
  detections: {
    type: 'AADHAAR' | 'PAN' | 'PHONE' | 'EMAIL' | 'CONFIDENTIAL_NOTE';
    matchedValue: string;
    redactedValue: string;
  }[];
}

export interface DocumentClassification {
  category: 'FIR' | 'CHARGESHEET' | 'FORENSIC_DUMP' | 'WIRETAP_AUDIO' | 'CCTV_FOOTAGE' | 'PHOTO_EXHIBIT' | 'FINANCIAL_LEDGER';
  label: string;
  badgeClass: string;
  confidence: number;
  recommendedTagPrefix: string;
  suggestedRetentionYears: number;
  legalContext: string;
}

@Injectable({
  providedIn: 'root'
})
export class AiRedactorService {

  // Auto-classify document based on file name, extension, and content hints
  classifyDocument(filename: string, contentHint: string = ''): DocumentClassification {
    const fn = (filename || '').toLowerCase();
    const hint = (contentHint || '').toLowerCase();

    if (fn.includes('fir') || hint.includes('first information report') || hint.includes('police station')) {
      return {
        category: 'FIR',
        label: 'FIR (First Information Report)',
        badgeClass: 'bg-primary',
        confidence: 96,
        recommendedTagPrefix: 'FIR',
        suggestedRetentionYears: 10,
        legalContext: 'Section 154 CrPC / BNSS Sec. 173 - Initial Cognizable Crime Ingestion'
      };
    }

    if (fn.includes('charge') || fn.includes('sheet') || hint.includes('final report') || hint.includes('173')) {
      return {
        category: 'CHARGESHEET',
        label: 'Chargesheet / Final Police Report',
        badgeClass: 'bg-indigo text-white',
        confidence: 94,
        recommendedTagPrefix: 'CS',
        suggestedRetentionYears: 15,
        legalContext: 'Section 173(2) CrPC / BNSS Sec. 193 - Judicial Submission'
      };
    }

    if (fn.includes('dump') || fn.includes('raw') || fn.includes('bin') || fn.includes('forensic') || fn.includes('image') || fn.includes('dd') || fn.includes('e01')) {
      return {
        category: 'FORENSIC_DUMP',
        label: 'Digital Forensics Clone / Bitstream Image',
        badgeClass: 'bg-danger text-white',
        confidence: 98,
        recommendedTagPrefix: 'DFD',
        suggestedRetentionYears: 20,
        legalContext: 'ISO/IEC 27037 Forensic Soundness - NIST SP 800-86 Compliant'
      };
    }

    if (fn.endsWith('.mp3') || fn.endsWith('.wav') || fn.endsWith('.m4a') || fn.includes('audio') || fn.includes('wiretap') || fn.includes('voip')) {
      return {
        category: 'WIRETAP_AUDIO',
        label: 'Wiretap / Intercepted VoIP Recording',
        badgeClass: 'bg-warning text-dark',
        confidence: 95,
        recommendedTagPrefix: 'WTR',
        suggestedRetentionYears: 7,
        legalContext: 'Indian Telegraph Act Sec. 5(2) / Evidence Act Sec. 65B Electronic Speech'
      };
    }

    if (fn.endsWith('.mp4') || fn.endsWith('.avi') || fn.endsWith('.mkv') || fn.includes('cctv') || fn.includes('video') || fn.includes('surveillance')) {
      return {
        category: 'CCTV_FOOTAGE',
        label: 'CCTV Surveillance / Body-Worn Video',
        badgeClass: 'bg-info text-dark',
        confidence: 97,
        recommendedTagPrefix: 'CCTV',
        suggestedRetentionYears: 8,
        legalContext: 'Digital Video Recording - Frame Timestamp & Hash Synchronized'
      };
    }

    if (fn.endsWith('.jpg') || fn.endsWith('.jpeg') || fn.endsWith('.png') || fn.includes('photo') || fn.includes('scene') || fn.includes('exhibit')) {
      return {
        category: 'PHOTO_EXHIBIT',
        label: 'Crime Scene Photograph Exhibit',
        badgeClass: 'bg-success text-white',
        confidence: 92,
        recommendedTagPrefix: 'PHO',
        suggestedRetentionYears: 10,
        legalContext: 'Visual Locus In Quo Physical Record - EXIF Metadata Intact'
      };
    }

    // Default
    return {
      category: 'FINANCIAL_LEDGER',
      label: 'Digital Case Evidentiary File',
      badgeClass: 'bg-secondary text-white',
      confidence: 88,
      recommendedTagPrefix: 'EVD',
      suggestedRetentionYears: 10,
      legalContext: 'General Electronic Evidence Item - Tamper-Evident SHA-256'
    };
  }

  // Scan text and redact sensitive Indian personal identity information (Aadhaar, PAN, phone, etc.)
  redactSensitivePii(text: string): PiiDetectionResult {
    if (!text) {
      return { originalText: '', redactedText: '', piiCount: 0, detections: [] };
    }

    let redacted = text;
    const detections: PiiDetectionResult['detections'] = [];

    // 1. Aadhaar Number (12 digits, often formatted as 4-4-4)
    // Matches e.g., 3671 0385 6111 or 3671-0385-6111 or 367103856111
    const aadhaarRegex = /\b(\d{4})[\s\-](\d{4})[\s\-](\d{4})\b|\b(\d{12})\b/g;
    let aadhaarMatch;
    while ((aadhaarMatch = aadhaarRegex.exec(text)) !== null) {
      const fullVal = aadhaarMatch[0];
      const last4 = fullVal.replace(/\D/g, '').slice(-4);
      const masked = `XXXX-XXXX-${last4} [AADHAAR REDACTED]`;
      detections.push({
        type: 'AADHAAR',
        matchedValue: fullVal,
        redactedValue: masked
      });
      redacted = redacted.split(fullVal).join(masked);
    }

    // 2. PAN Card Number (5 letters, 4 numbers, 1 letter: e.g. ABCDE1234F)
    const panRegex = /\b[A-Z]{5}[0-9]{4}[A-Z]{1}\b/g;
    let panMatch;
    while ((panMatch = panRegex.exec(text)) !== null) {
      const fullVal = panMatch[0];
      const masked = `${fullVal.slice(0, 2)}XXXX${fullVal.slice(-2)} [PAN REDACTED]`;
      detections.push({
        type: 'PAN',
        matchedValue: fullVal,
        redactedValue: masked
      });
      redacted = redacted.split(fullVal).join(masked);
    }

    // 3. Indian Phone Numbers (+91 98201 10001, 98201-10001, etc.)
    const phoneRegex = /(?:\+91[\-\s]?)?[6789]\d{4}[\-\s]?\d{5}\b/g;
    let phoneMatch;
    while ((phoneMatch = phoneRegex.exec(text)) !== null) {
      const fullVal = phoneMatch[0];
      const digits = fullVal.replace(/\D/g, '');
      const last3 = digits.slice(-3);
      const masked = `+91 XXXXX-XX${last3} [PHONE REDACTED]`;
      detections.push({
        type: 'PHONE',
        matchedValue: fullVal,
        redactedValue: masked
      });
      redacted = redacted.split(fullVal).join(masked);
    }

    // 4. Email Addresses
    const emailRegex = /\b[A-Za-z0-9._%+-]+@(?!evidlock\.gov)[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b/g;
    let emailMatch;
    while ((emailMatch = emailRegex.exec(text)) !== null) {
      const fullVal = emailMatch[0];
      const parts = fullVal.split('@');
      const masked = `${parts[0].slice(0, 2)}***@${parts[1]} [EMAIL REDACTED]`;
      detections.push({
        type: 'EMAIL',
        matchedValue: fullVal,
        redactedValue: masked
      });
      redacted = redacted.split(fullVal).join(masked);
    }

    return {
      originalText: text,
      redactedText: redacted,
      piiCount: detections.length,
      detections
    };
  }
}
