import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  timeout: number = 10000;

  // Alert Page Aziende
  alertAziende = { show: false, type: '', message: '' };
  setAlertAziende(type: string, message: string) {
    this.alertAziende = { show: true, type: type, message: message };
    setTimeout(() => {
      this.alertAziende = { show: false, type: '', message: '' };
    }, this.timeout);
  }

  // Alert Page Documenti
  alertDocumenti = { show: false, type: '', message: '' };
  setAlertDocumenti(type: string, message: string) {
    this.alertDocumenti = { show: true, type: type, message: message };
    setTimeout(() => {
      this.alertDocumenti = { show: false, type: '', message: '' };
    }, this.timeout);
  }

  // Alert Page Dipendenti
  alertDipendenti = { show: false, type: '', message: '' };
  setAlertDipendenti(type: string, message: string) {
    this.alertDipendenti = { show: true, type: type, message: message };
    setTimeout(() => {
      this.alertDipendenti = { show: false, type: '', message: '' };
    }, this.timeout);
  }

  // Alert Page Corsi
  alertCorsi = { show: false, type: '', message: '' };
  setAlertCorsi(type: string, message: string) {
    this.alertCorsi = { show: true, type: type, message: message };
    setTimeout(() => {
      this.alertCorsi = { show: false, type: '', message: '' };
    }, this.timeout);
  }

  // Alert Page Profilo
  alertProfilo = { show: false, type: '', message: '' };
  setAlertProfilo(type: string, message: string) {
    this.alertProfilo = { show: true, type: type, message: message };
    setTimeout(() => {
      this.alertProfilo = { show: false, type: '', message: '' };
    }, this.timeout);
  }

  // Alert Page Contatti
  alertContatti = { show: false, type: '', message: '' };
  setAlertContatti(type: string, message: string) {
    this.alertContatti = { show: true, type: type, message: message };
    setTimeout(() => {
      this.alertContatti = { show: false, type: '', message: '' };
    }, this.timeout);
  }
}
