import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Settings } from '../../Settings';
import { AuthService } from '../auth/auth.service';
import { EvidenceItem } from '../../model/EvidenceItem';

@Injectable({
  providedIn: 'root'
})
export class RetentionService {

  constructor(private http: HttpClient, private authService: AuthService) { }

  getEligibleForDisposal(): Observable<EvidenceItem[]> {
    return this.http.get<EvidenceItem[]>(Settings.API_ENDPOINT + 'retention/eligible', { headers: this.authService.getHeaders() });
  }

  getNearingRetention(days: number = 30): Observable<EvidenceItem[]> {
    return this.http.get<EvidenceItem[]>(`${Settings.API_ENDPOINT}retention/nearing?days=${days}`, { headers: this.authService.getHeaders() });
  }

  getCompromisedItems(): Observable<EvidenceItem[]> {
    return this.http.get<EvidenceItem[]>(Settings.API_ENDPOINT + 'retention/compromised', { headers: this.authService.getHeaders() });
  }

  triggerCheckNow(): Observable<any> {
    return this.http.post(Settings.API_ENDPOINT + 'retention/check-now', {}, { headers: this.authService.getHeaders() });
  }
}
