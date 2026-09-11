import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';
import { Azienda } from '../../model/Azienda';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { Settings } from '../../Settings';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AziendeService {

  myAziende?: Azienda[];

  azienda?: Azienda;
  role?: string;

  constructor(
    private auth: AuthService,
    private http: HttpClient) { }

  getAziende(): Observable<Azienda[]> {
    return this.http.get<Azienda[]>(Settings.API_ENDPOINT + 'aziende', { headers: this.auth.headers}).pipe(
      tap(aziende => this.myAziende = aziende)
    );
  }

  getAzienda(pIva: String): Observable<Azienda> {
    return this.http.get<Azienda>(Settings.API_ENDPOINT + 'azienda?pIva=' + pIva, { headers: this.auth.headers});
  }

  getProfilo(): Observable<Azienda> {
    return this.http.get<Azienda>(Settings.API_ENDPOINT + 'profilo', { headers: this.auth.headers}).pipe(
      tap(azienda => this.azienda = azienda),
      tap(azienda => this.role = this.auth.getRole() as string | undefined),
    );
  }

  addAzienda(azienda: Azienda, file: File | undefined): Observable<any> {
    const formData: FormData = new FormData();

    formData.append('azienda', new Blob([JSON.stringify(azienda)], { type: 'application/json' }));

    if(file){
      formData.append('file', file, file.name);
    }
    else{
      formData.append('file', new Blob());
    }

    return this.http.post(Settings.API_ENDPOINT + 'aggiungi-azienda', formData, { headers: this.auth.headers });
  }

  modificaProfilo(azienda: Azienda, file: File | undefined): Observable<any> {
    const formData: FormData = new FormData();

    formData.append('azienda', new Blob([JSON.stringify(azienda)], { type: 'application/json' }));

    if(file){
      formData.append('file', file, file.name);
    }
    else{
      formData.append('file', new Blob());
    }

    return this.http.post(Settings.API_ENDPOINT + 'modifica-profilo', formData, { headers: this.auth.headers });
  }

  deleteAzienda(pIva: String): Observable<any> {
    return this.http.delete(Settings.API_ENDPOINT + 'rimuovi-azienda?pIva=' + pIva, { headers: this.auth.headers });
  }

  addImgAzienda(pIva: String, file: File | undefined): Observable<any> {
    const formData: FormData = new FormData();

    if(file){
      formData.append('file', file, file.name);
    }
    else{
      formData.append('file', new Blob());
    }

    return this.http.post(Settings.API_ENDPOINT + 'modifica-immagine-azienda?pIva=' + pIva, formData, { headers: this.auth.headers });
  }

  deleteImgAzienda(pIva: String): Observable<any> {
    return this.http.delete(Settings.API_ENDPOINT + 'rimuovi-immagine-azienda?pIva=' + pIva, { headers: this.auth.headers });
  }


}
