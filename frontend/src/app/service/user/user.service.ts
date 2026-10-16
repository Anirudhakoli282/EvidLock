import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Settings } from '../../Settings';
import { AuthService } from '../auth/auth.service';
import { User } from '../../model/User';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  constructor(private http: HttpClient, private authService: AuthService) {}

  getUsers(agencyCode?: string): Observable<User[]> {
    let url = Settings.API_ENDPOINT + 'personnel';
    if (agencyCode) {
      url += `?agencyCode=${encodeURIComponent(agencyCode)}`;
    }
    return this.http.get<User[]>(url, { headers: this.authService.getHeaders() });
  }

  getUser(id: number): Observable<User> {
    return this.http.get<User>(`${Settings.API_ENDPOINT}personnel/${id}`, { headers: this.authService.getHeaders() });
  }
}
