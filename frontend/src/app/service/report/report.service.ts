import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Settings } from '../../Settings';
import { AuthService } from '../auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  constructor(private http: HttpClient, private authService: AuthService) { }

  downloadChainOfCustodyReport(evidenceId: number): Observable<Blob> {
    return this.http.get(`${Settings.API_ENDPOINT}reports/chain-of-custody/${evidenceId}`, {
      headers: this.authService.getHeaders(),
      responseType: 'blob'
    });
  }
}
