export interface CustodyEvent {
  id?: number;
  evidenceItemId: number;
  actorId: string;
  action: string; // 'UPLOADED' | 'VIEWED' | 'DOWNLOADED' | 'TRANSFERRED' | 'ANALYZED' | 'RELEASED' | 'DISPOSED' | 'INTEGRITY_ALERT'
  timestamp: string;
  fromCustodianId?: string;
  toCustodianId?: string;
  notes?: string;
  previousEventHash: string;
  eventHash: string;
}

export interface ChainVerificationResult {
  evidenceItemId: number;
  evidenceTag?: string;
  valid: boolean;
  statusMessage: string;
  totalEvents: number;
  brokenAtEventId?: number;
  brokenReason?: string;
  events?: CustodyEvent[];
}
