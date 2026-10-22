import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { Settings } from '../../Settings';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';

export interface AuthResponse {
  token: string;
  role: string;
  name?: string;
  agencyCode?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(@Inject(PLATFORM_ID) private platformId: Object, private http: HttpClient) { }

  token?: string | null;
  role?: string | null;
  name?: string | null;
  agencyCode?: string | null;
  currentPIva?: string | null;
  selectedPIva?: string | null;

  getHeaders(): HttpHeaders {
    return new HttpHeaders().set('Authorization', `Basic ${this.getToken() || ''}`);
  }

  get headers(): HttpHeaders {
    return this.getHeaders();
  }

  autologin(): Observable<boolean> {
    if (isPlatformBrowser(this.platformId)) {
      let email = localStorage.getItem('admin-email') || sessionStorage.getItem('admin-email');
      let password = localStorage.getItem('admin-password') || sessionStorage.getItem('admin-password');

      if (email && password) {
        return this.login(email, password, false);
      } else {
        return of(false);
      }
    }
    return of(false);
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      if (!this.token) {
        this.token = sessionStorage.getItem('admin-token') || localStorage.getItem('admin-token');
      }
      return this.token;
    }
    return null;
  }

  setToken(token: string, rememberMe: boolean): void {
    this.token = token;
    if (rememberMe) {
      localStorage.setItem('admin-token', token);
    }
    sessionStorage.setItem('admin-token', token);
  }

  getRole(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      if (!this.role) {
        this.role = sessionStorage.getItem('admin-role') || localStorage.getItem('admin-role');
      }
      return this.role;
    }
    return null;
  }

  setRole(role: string, rememberMe: boolean): void {
    this.role = role;
    if (rememberMe) {
      localStorage.setItem('admin-role', role);
    }
    sessionStorage.setItem('admin-role', role);
  }

  getName(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return sessionStorage.getItem('admin-name') || localStorage.getItem('admin-name') || 'User';
    }
    return 'User';
  }

  setName(name: string, rememberMe: boolean): void {
    this.name = name;
    if (rememberMe) localStorage.setItem('admin-name', name);
    sessionStorage.setItem('admin-name', name);
  }

  getAgencyCode(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return sessionStorage.getItem('admin-agency') || localStorage.getItem('admin-agency') || 'MPD-01';
    }
    return 'MPD-01';
  }

  setAgencyCode(agency: string, rememberMe: boolean): void {
    this.agencyCode = agency;
    if (rememberMe) localStorage.setItem('admin-agency', agency);
    sessionStorage.setItem('admin-agency', agency);
  }

  isSupervisor(): boolean {
    const r = this.getRole();
    return r === 'SUPERVISOR' || r === 'A';
  }

  isInvestigator(): boolean {
    return this.getRole() === 'INVESTIGATOR';
  }

  isCustodian(): boolean {
    const r = this.getRole();
    return r === 'CUSTODIAN' || r === 'C';
  }

  isAuditor(): boolean {
    return this.getRole() === 'AUDITOR';
  }

  isCourt(): boolean {
    const r = this.getRole();
    return r === 'COURT';
  }

  isReadOnly(): boolean {
    return this.isAuditor();
  }

  login(email: string, password: string, rememberMe: boolean): Observable<boolean> {
    const user = { email: email, password: password };

    return this.http.post<AuthResponse>(Settings.API_ENDPOINT + 'login', user, { withCredentials: true })
      .pipe(
        map(response => {
          if (!response || !response.token) {
            return false;
          }
          this.setEmailPassword(email, password, rememberMe);
          this.setToken(response.token, rememberMe);
          this.setRole(response.role, rememberMe);
          if (response.name) this.setName(response.name, rememberMe);
          if (response.agencyCode) {
            this.setAgencyCode(response.agencyCode, rememberMe);
            this.currentPIva = response.agencyCode;
          }
          return true;
        })
      );
  }

  logout(): Observable<any> {
    return this.http.post(Settings.API_ENDPOINT + 'logout', {}, { headers: this.getHeaders(), withCredentials: true }).pipe(
      map(response => {
        this.clear();
        return response;
      })
    );
  }

  private clear(): void {
    this.token = null;
    this.role = null;
    this.name = null;
    this.agencyCode = null;
    this.currentPIva = null;
    sessionStorage.clear();
    localStorage.removeItem('admin-token');
    localStorage.removeItem('admin-role');
    localStorage.removeItem('admin-name');
    localStorage.removeItem('admin-agency');
    localStorage.removeItem('admin-email');
    localStorage.removeItem('admin-password');
  }

  setEmailPassword(email: string, password: string, rememberMe: boolean): void {
    if (rememberMe) {
      localStorage.setItem('admin-email', email);
      localStorage.setItem('admin-password', password);
    }
    sessionStorage.setItem('admin-email', email);
    sessionStorage.setItem('admin-password', password);
  }

  getCurrentPIva(): string | null {
    return this.getAgencyCode();
  }

  setCurrentPIva(rememberMe: boolean): void {
    // Kept for backward compatibility
  }

  getSelectedPIva(): string | null {
    return this.selectedPIva || this.getAgencyCode();
  }

  setSelectedPIva(pIva: string): void {
    this.selectedPIva = pIva;
    sessionStorage.setItem('admin-selectedPIva', pIva);
  }

  updatePassword(oldPw: string, newPw: string): Observable<any> {
    const body = { oldPw: oldPw, newPw: newPw };
    return this.http.post(Settings.API_ENDPOINT + 'cambio-password', body, { headers: this.getHeaders() });
  }
}