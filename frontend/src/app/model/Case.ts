export interface Case {
  id?: number;
  caseNumber: string;
  title: string;
  description?: string;
  status: string; // 'Open' | 'Closed' | 'Archived'
  agencyCode: string;
  createdBy?: string;
  createdAt?: string;
}
