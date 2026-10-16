import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Settings } from '../../Settings';
import { AuthService } from '../auth/auth.service';
import { Agency } from '../../model/Agency';

@Injectable({
  providedIn: 'root'
})
export class AgencyService {

  constructor(private http: HttpClient, private authService: AuthService) {}

  getAgencies(): Observable<Agency[]> {
    return this.http.get<Agency[]>(Settings.API_ENDPOINT + 'agencies', {
      headers: this.authService.getHeaders()
    });
  }

  getAgency(code: string): Observable<Agency> {
    return this.http.get<Agency>(`${Settings.API_ENDPOINT}agency/${code}`, {
      headers: this.authService.getHeaders()
    });
  }
}
