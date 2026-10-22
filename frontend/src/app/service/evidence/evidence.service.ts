import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Settings } from '../../Settings';
import { AuthService } from '../auth/auth.service';
import { EvidenceItem } from '../../model/EvidenceItem';

@Injectable({
  providedIn: 'root'
})
export class EvidenceService {

  constructor(private http: HttpClient, private authService: AuthService) { }

  getEvidence(agencyCode?: string, caseId?: number): Observable<EvidenceItem[]> {
    let url = Settings.API_ENDPOINT + 'evidence';
    const params: string[] = [];
    if (agencyCode) params.push(`agencyCode=${encodeURIComponent(agencyCode)}`);
    if (caseId) params.push(`caseId=${caseId}`);
    if (params.length > 0) url += '?' + params.join('&');

    return this.http.get<EvidenceItem[]>(url, { headers: this.authService.getHeaders() });
  }

  getEvidenceById(id: number): Observable<EvidenceItem> {
    return this.http.get<EvidenceItem>(`${Settings.API_ENDPOINT}evidence/${id}`, { headers: this.authService.getHeaders() });
  }

  uploadEvidence(file: File, caseId: number, tag: string, description: string, retentionDate?: string): Observable<EvidenceItem> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('caseId', caseId.toString());
    formData.append('tag', tag);
    formData.append('description', description);
    if (retentionDate) formData.append('retentionDate', retentionDate);

    return this.http.post<EvidenceItem>(`${Settings.API_ENDPOINT}evidence/upload`, formData, { headers: this.authService.getHeaders() });
  }

  updateEvidence(item: EvidenceItem): Observable<EvidenceItem> {
    return this.http.post<EvidenceItem>(`${Settings.API_ENDPOINT}evidence/update`, item, { headers: this.authService.getHeaders() });
  }

  deleteEvidence(id: number): Observable<any> {
    return this.http.delete(`${Settings.API_ENDPOINT}evidence/${id}`, { headers: this.authService.getHeaders(), responseType: 'text' });
  }

  requestRemoval(id: number, reason: string, password: string, legalRef?: string): Observable<string> {
    return this.http.post(`${Settings.API_ENDPOINT}evidence/${id}/request-removal`, 
      { reason, password, legalRef }, 
      { headers: this.authService.getHeaders(), responseType: 'text' }
    );
  }

  approveRemoval(id: number, legalRef: string, password: string): Observable<EvidenceItem> {
    return this.http.post<EvidenceItem>(`${Settings.API_ENDPOINT}evidence/${id}/approve-removal`, 
      { legalRef, password }, 
      { headers: this.authService.getHeaders() }
    );
  }

  rejectRemoval(id: number, reason: string, password: string): Observable<EvidenceItem> {
    return this.http.post<EvidenceItem>(`${Settings.API_ENDPOINT}evidence/${id}/reject-removal`, 
      { reason, password }, 
      { headers: this.authService.getHeaders() }
    );
  }

  getPendingRemovals(): Observable<EvidenceItem[]> {
    return this.http.get<EvidenceItem[]>(`${Settings.API_ENDPOINT}evidence/pending-removals`, 
      { headers: this.authService.getHeaders() }
    );
  }

  toggleLegalHold(id: number, hold: boolean): Observable<EvidenceItem> {
    return this.http.post<EvidenceItem>(`${Settings.API_ENDPOINT}evidence/${id}/legal-hold?hold=${hold}`, {}, { headers: this.authService.getHeaders() });
  }

  search(keywords: string[]): Observable<EvidenceItem[]> {
    const query = keywords.map(k => `q=${encodeURIComponent(k)}`).join('&');
    return this.http.get<EvidenceItem[]>(`${Settings.API_ENDPOINT}evidence/search?${query}`, { headers: this.authService.getHeaders() });
  }

  downloadFile(path: string, evidenceId?: number): Observable<Blob> {
    let url = `${Settings.API_ENDPOINT}get-file?path=${encodeURIComponent(path)}`;
    if (evidenceId) url += `&evidenceId=${evidenceId}`;
    return this.http.get(url, { headers: this.authService.getHeaders(), responseType: 'blob' });
  }
}
