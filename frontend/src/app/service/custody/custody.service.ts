import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Settings } from '../../Settings';
import { AuthService } from '../auth/auth.service';
import { CustodyEvent, ChainVerificationResult } from '../../model/CustodyEvent';

@Injectable({
  providedIn: 'root'
})
export class CustodyService {

  constructor(private http: HttpClient, private authService: AuthService) { }

  getCustodyEvents(evidenceId: number): Observable<CustodyEvent[]> {
    return this.http.get<CustodyEvent[]>(`${Settings.API_ENDPOINT}custody/events/${evidenceId}`, { headers: this.authService.getHeaders() });
  }

  verifyChain(evidenceId: number): Observable<ChainVerificationResult> {
    return this.http.get<ChainVerificationResult>(`${Settings.API_ENDPOINT}custody/verify/${evidenceId}`, { headers: this.authService.getHeaders() });
  }

  getRecentEvents(limit: number = 15): Observable<CustodyEvent[]> {
    return this.http.get<CustodyEvent[]>(`${Settings.API_ENDPOINT}custody/recent?limit=${limit}`, { headers: this.authService.getHeaders() });
  }

  getDashboardMetrics(): Observable<any> {
    return this.http.get<any>(`${Settings.API_ENDPOINT}dashboard/metrics`, { headers: this.authService.getHeaders() });
  }
}
