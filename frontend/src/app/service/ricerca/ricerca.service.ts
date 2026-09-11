import { Injectable } from '@angular/core';
import { Azienda } from '../../model/Azienda';
import { Documento } from '../../model/Documento';
import { Dipendente } from '../../model/Dipendente';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { Settings } from '../../Settings';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RicercaService {

  constructor(
    private http: HttpClient,
    private auth: AuthService
  ) { }

  search(pIva: string, text: string): Observable<any>{
    return this.http.get<any>(Settings.API_ENDPOINT + "search?pIva=" + pIva + "&q=" + text, { headers: this.auth.headers });
  }
}
