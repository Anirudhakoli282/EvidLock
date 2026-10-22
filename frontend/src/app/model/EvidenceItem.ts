export interface EvidenceItem {
  id?: number;
  caseId: number;
  caseNumber?: string;
  evidenceTag: string;
  description: string;
  fileHash?: string;
  originalFilename?: string;
  filePath?: string;
  fileType?: string;
  fileSize?: number;
  uploadedBy?: string;
  uploadedAt?: string | Date;
  currentCustodianId?: string;
  status: string; // 'Collected' | 'In Custody' | 'Under Analysis' | 'Released' | 'Disposed'
  legalHold: boolean;
  integrityStatus: string; // 'VERIFIED' | 'COMPROMISED'
  retentionDate?: string;
  agencyCode?: string;

  // Controlled Removal Governance fields
  removalRequestedBy?: string;
  removalApprovedBy?: string;
  removalReason?: string;
  legalRef?: string;
  removalRequestedAt?: string | Date;
  removedAt?: string | Date;

  // Compatibility aliases
  nome?: string;
  stato?: string;
  formato?: string;
  rilascio?: string;
  scadenza?: string;
}
