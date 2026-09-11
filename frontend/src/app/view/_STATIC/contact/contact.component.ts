import { Component } from '@angular/core';
import { NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FormCheck } from '../../../FormCheck';
import { AlertService } from '../../../service/alert/alert.service';
import { EmailService } from '../../../service/email/email.service';

import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-contact',
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.css'
})
export class ContactComponent {

  contactForm: FormGroup;
  model: NgbDateStruct | undefined;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private alert: AlertService,
    private emailService: EmailService
  ) {
    this.contactForm = this.fb.group({
      nome: ['', Validators.required],
      email: ['', Validators.required],
      oggetto: ['', Validators.required],
      messaggio: ['', Validators.required],
    }, { validators: this.customValidation });
  }

  customValidation(group: FormGroup) {
    const nomeControl = group.get('nome');
    const emailControl = group.get('email');

    if (nomeControl && emailControl) {
      const nome = nomeControl.value;
      const email = emailControl.value;

      // Nome
      if (nome && !FormCheck.checkNome(nome)) {
        nomeControl.setErrors({ 'invalidName': true });
      } else {
        if (nomeControl.hasError('invalidName')) {
          nomeControl.setErrors(null);
        }
      }

      // Email
      if (email && !FormCheck.checkEmail(email)) {
        emailControl.setErrors({ 'invalidEmail': true });
      } else {
        if (emailControl.hasError('invalidEmail')) {
          emailControl.setErrors(null);
        }
      }
    }
  }

  submitForm() {
    const body = {
      access_key: "1b394588-7402-439f-95a8-0cba7d5bc80e", // --> Mapped with testforwebapplication@gmail.com
      name: this.contactForm.value.nome,
      email: this.contactForm.value.email.toLowerCase(),
      subject: this.contactForm.value.oggetto,
      message: this.contactForm.value.messaggio,
    };
    this.http.post('https://api.web3forms.com/submit', body)
      .subscribe(
        (response) => {
          this.alert.setAlertContatti('success', 'Messaggio inviato con successo! Una email contenente la tua richiesta è stata inviata al tuo indirizzo email.');
          this.emailService.sendConfirmEmail(this.contactForm.value.nome, this.contactForm.value.email, this.contactForm.value.oggetto, this.contactForm.value.messaggio).subscribe(
            (response) => { this.contactForm.reset(); },
            (error) => { }
          );
        },
        (error) => {
          this.alert.setAlertContatti('danger', 'Errore! Il messaggio non è stato inviato correttamente.');
        }
      );
  }
}