import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Settings } from '../../Settings';
import { AuthService } from '../auth/auth.service';
import { Case } from '../../model/Case';

@Injectable({
  providedIn: 'root'
})
export class CasesService {

  constructor(private http: HttpClient, private authService: AuthService) { }

  getCases(): Observable<Case[]> {
    return this.http.get<Case[]>(Settings.API_ENDPOINT + 'cases', { headers: this.authService.getHeaders() });
  }

  getCaseById(id: number): Observable<Case> {
    return this.http.get<Case>(`${Settings.API_ENDPOINT}cases/${id}`, { headers: this.authService.getHeaders() });
  }

  createCase(c: Case): Observable<Case> {
    return this.http.post<Case>(Settings.API_ENDPOINT + 'cases', c, { headers: this.authService.getHeaders() });
  }

  updateCase(id: number, c: Case): Observable<Case> {
    return this.http.put<Case>(`${Settings.API_ENDPOINT}cases/${id}`, c, { headers: this.authService.getHeaders() });
  }
}
