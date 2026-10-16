export interface User {
  id?: number;
  email: string;
  name: string;
  badgeNumber: string;
  role: string; // 'SUPERVISOR', 'INVESTIGATOR', 'CUSTODIAN', 'AUDITOR'
  agencyCode: string;
  phone: string;
  password?: string;
}
