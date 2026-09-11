import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { Settings } from '../../Settings';


@Injectable({
  providedIn: 'root'
})
export class EmailService {

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  sendConfirmEmail(nome:string, email: string, oggetto: string, messaggio: string) {
    return this.http.post(Settings.API_ENDPOINT + "send-confirm-mail", {nome, email, oggetto, messaggio}, { headers: this.authService.headers });
  }

}
