import { Injectable } from '@angular/core';

import { Documento } from '../../model/Documento';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { Settings } from '../../Settings';

@Injectable({
  providedIn: 'root'
})
export class DocumentiService {

  constructor(
    private http: HttpClient,
    private auth: AuthService
  ) { }

  getDocumentiAzienda(pIva: string): Observable<Documento[]> {
    return this.http.get<Documento[]>(Settings.API_ENDPOINT + "documenti-azienda?pIva=" + pIva, { headers: this.auth.headers });
  }

  getDocumentiDipendente(id: number): Observable<Documento[]> {
    return this.http.get<Documento[]>(Settings.API_ENDPOINT + "documenti-dipendente?id=" + id, { headers: this.auth.headers });
  }

  getDocumento(id: number): Observable<Documento> {
    return this.http.get<Documento>(Settings.API_ENDPOINT + "documento?id=" + id, { headers: this.auth.headers });
  }

  addDocumentoAzienda(documento: Documento, file: File | undefined, pIva: string): Observable<any>{
    const formData: FormData = new FormData();

    formData.append('documento', new Blob([JSON.stringify(documento)], { type: 'application/json' }));

    if(file){
      formData.append('file', file, file.name);
    }
    else{
      formData.append('file', new Blob());
    }

    return this.http.post(Settings.API_ENDPOINT + "aggiungi-documento-azienda?pIva=" + pIva, formData, { headers: this.auth.headers });
  }

  addDocumentoDipendente(documento: Documento, file: File | undefined, cf: string): Observable<any>{
    const formData: FormData = new FormData();

    formData.append('documento', new Blob([JSON.stringify(documento)], { type: 'application/json' }));

    if(file){
      formData.append('file', file, file.name);
    }
    else{
      formData.append('file', new Blob());
    }

    return this.http.post(Settings.API_ENDPOINT + "aggiungi-documento-dipendente?cf=" + cf, formData, { headers: this.auth.headers });
  }

  updateDocumentoAzienda(documento: Documento, file: File | undefined, pIva: string): Observable<any>{
    const formData: FormData = new FormData();

    formData.append('documento', new Blob([JSON.stringify(documento)], { type: 'application/json' }));

    if(file){
      formData.append('file', file, file.name);
    }
    else{
      formData.append('file', new Blob());
    }

    return this.http.post(Settings.API_ENDPOINT + "modifica-documento-azienda?pIva=" + pIva, formData, { headers: this.auth.headers });
  }

  updateDocumentoDipendente(documento: Documento, file: File | undefined, cf: string): Observable<any>{
    const formData: FormData = new FormData();

    formData.append('documento', new Blob([JSON.stringify(documento)], { type: 'application/json' }));

    if(file){
      formData.append('file', file, file.name);
    }
    else{
      formData.append('file', new Blob());
    }

    return this.http.post(Settings.API_ENDPOINT + "modifica-documento-dipendente?cf=" + cf, formData, { headers: this.auth.headers });
  }

  deleteDocumento(id: number): Observable<any>{
    return this.http.delete(Settings.API_ENDPOINT + "rimuovi-documento?id=" + id, { headers: this.auth.headers });
  }

  getDocumentiValidi(pIva: string): Observable<Documento[]> {
    return this.http.get<Documento[]>(Settings.API_ENDPOINT + "documenti-validi?pIva=" + pIva, { headers: this.auth.headers });
  }

  getDocumentiInScadenza(pIva: string): Observable<Documento[]> {
    return this.http.get<Documento[]>(Settings.API_ENDPOINT + "documenti-in-scadenza?pIva=" + pIva, { headers: this.auth.headers });
  }

  getDocumentiScaduti(pIva: string): Observable<Documento[]> {
    return this.http.get<Documento[]>(Settings.API_ENDPOINT + "documenti-scaduti?pIva=" + pIva, { headers: this.auth.headers });
  }

}
