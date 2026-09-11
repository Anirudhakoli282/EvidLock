export interface CustodyTransferRequest {
  id?: number;
  evidenceItemId: number;
  evidenceTag?: string;
  evidenceDescription?: string;
  fromCustodianId: string;
  toCustodianId: string;
  reason: string;
  status: string; // 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED'
  releasedAt?: string;
  acceptedAt?: string;
  createdAt?: string;
}

export interface TransferStepUpDto {
  requestId?: number;
  evidenceItemId?: number;
  toCustodianId?: string;
  reason?: string;
  password?: string;
  token?: string;
}
