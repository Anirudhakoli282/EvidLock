import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';
import { Corso } from '../../model/Corso';

import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { Settings } from '../../Settings';

import { Dipendente } from '../../model/Dipendente';

@Injectable({
  providedIn: 'root'
})
export class CorsiService {

  constructor(
    private http: HttpClient,
    private auth: AuthService
  ) { }

  getCorsiAcquistati(pIva: string): Observable<Corso[]>{
    return this.http.get<Corso[]>(Settings.API_ENDPOINT + "corsi-acquistati?pIva=" + pIva, { headers: this.auth.headers });
  }

  getDipendentiIscritti(id: number): Observable<Dipendente[]>{
    return this.http.get<Dipendente[]>(Settings.API_ENDPOINT + "dipendenti-iscritti-corso?id=" + id, { headers: this.auth.headers });
  }

  getCorsiFrequentati(id: number): Observable<Corso[]>{
    return this.http.get<Corso[]>(Settings.API_ENDPOINT + "corsi-frequentati?id=" + id, { headers: this.auth.headers });
  }

  getCorso(id: number): Observable<Corso>{
    return this.http.get<Corso>(Settings.API_ENDPOINT + "corso?id=" + id, { headers: this.auth.headers });
  }

  addDipendentiCorso(id: number, dipendenti: Dipendente[]): Observable<any>{
    return this.http.post(Settings.API_ENDPOINT + "aggiungi-dipendenti-corso?idCorso=" + id, dipendenti, { headers: this.auth.headers });
  }

  addCorso(corso: Corso, pIva: string): Observable<any>{
    return this.http.post(Settings.API_ENDPOINT + "aggiungi-corso?pIva=" + pIva, corso, { headers: this.auth.headers });
  }

  updateCorso(corso: Corso, pIva :string): Observable<any>{
    return this.http.post(Settings.API_ENDPOINT + "modifica-corso?pIva=" + pIva, corso, { headers: this.auth.headers });
  }

  deleteCorso(id: number): Observable<any>{
    return this.http.delete(Settings.API_ENDPOINT + "rimuovi-corso?id=" + id, { headers: this.auth.headers });
  }
}
