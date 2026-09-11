export interface Agency {
  agencyCode: string;
  name: string;
  email: string;
  phone?: string;
  address?: string;
  badgePrefix?: string;
  logo?: string;

  // Compatibility aliases
  piva?: string;
  ragionesociale?: string;
  telefono?: string;
  indirizzo?: string;
  immagine?: string;
}
