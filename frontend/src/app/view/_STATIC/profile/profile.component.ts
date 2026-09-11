import { Component } from '@angular/core';
import { Azienda } from '../../../model/Azienda';
import { AziendeService } from '../../../service/aziende/aziende.service';
import { AlertService } from '../../../service/alert/alert.service';

import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { FileService } from '../../../service/file/file.service';
import { FormCheck } from '../../../FormCheck';
import { AuthService } from '../../../service/auth/auth.service';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { forkJoin } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmModalComponent } from '../../GENERIC/confirm-modal/confirm-modal.component';


@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent {
  success: boolean = true; //PER IL CONTROLLO DELLA PASSWORD
  serverError: boolean = false;

  private file: File | undefined; //modifica
  modificaProfiloForm: FormGroup; //modifica
  modificaPasswordForm: FormGroup; //modifica

  azienda!: Azienda;
  isInitialized: boolean = false; // Add the flag

  constructor(
    private aziendeService: AziendeService,
    private alert: AlertService,    //modifica
    private fb: FormBuilder, //modifica
    private fileService: FileService,
    private auth: AuthService,
    private modalService: NgbModal
  ) {
    this.modificaProfiloForm = this.fb.group({
      ragioneSociale: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', Validators.required],
      indirizzo: ['', Validators.required],
      img: ['']
    }, { validators: this.customValidationModificaProfilo }
    );

    this.modificaPasswordForm = this.fb.group({
      password: ['', Validators.required],
      nuovaPassword: ['', Validators.required],
      confermaPassword: ['', Validators.required]
    }, { validators: this.customValidationPassword }
    );
  }

  ngOnInit(): void {
    this.getAzienda();
  }

  setAziendaImage(): Observable<void[]> {
    const observables: Observable<void>[] = [];

    if (this.azienda.img !== '' && this.azienda.img !== null) {
      const observable = this.fileService.getFile(this.azienda.img).pipe(
        map((img) => {
          let objectURL = URL.createObjectURL(img);
          this.azienda.img = objectURL;
        }),
        catchError((err) => {
          this.azienda.img = '';
          return [];
        })
      );
      observables.push(observable);
    }else {
      // If azienda.img is empty, create an empty observable
      observables.push(of(null).pipe(map(() => {})));
    }

    // Use forkJoin to wait for all observables to complete
    return forkJoin(observables);
  }

  customValidationModificaProfilo(group: FormGroup) {
    const emailControl = group.get('email');
    const telefonoControl = group.get('telefono');

    if (emailControl && telefonoControl) {
      const email = emailControl.value;
      const telefono = telefonoControl.value;

      // Email
      if (email && !FormCheck.checkEmail(email)) {
        emailControl.setErrors({ 'invalidEmail': true });
      } else {
        if (emailControl.hasError('invalidEmail')) {
          emailControl.setErrors(null);
        }
      }
      // Telefono
      if (telefono && !FormCheck.checkTelefono(telefono)) {
        telefonoControl?.setErrors({ 'invalidTelefono': true });
      } else {
        if (telefonoControl?.hasError('invalidTelefono')) {
          telefonoControl?.setErrors(null);
        }
      }
    }
  }

  customValidationPassword(group: FormGroup) {
    const nuovaPasswordControl = group.get('nuovaPassword');
    const confermaPasswordfControl = group.get('confermaPassword');

    if (nuovaPasswordControl && confermaPasswordfControl) {
      const nuovaPassword = nuovaPasswordControl.value;
      const confermaPassword = confermaPasswordfControl.value;

      // Verifica sicurezza password
      if (nuovaPassword && !FormCheck.checkPassword(nuovaPassword)) {
        nuovaPasswordControl.setErrors({ 'invalidPassword': true });
      } else {
        if (nuovaPasswordControl.hasError('invalidPassword')) {
          nuovaPasswordControl.setErrors(null);
        }
      }

      // Confronto password
      if (nuovaPassword && confermaPassword && !FormCheck.compareTwoPasswords(nuovaPassword, confermaPassword)) {
        confermaPasswordfControl.setErrors({ 'differentPasswords': true });
      } else {
        if (confermaPasswordfControl.hasError('differentPasswords')) {
          confermaPasswordfControl.setErrors(null);
        }
      }
    }
  }

  getAzienda() {
    this.azienda = undefined as any;
    this.aziendeService.getProfilo().subscribe(azienda => {
      this.azienda = azienda;
      this.setModificaProfiloForm();
      this.setAziendaImage().subscribe(() => {
        this.isInitialized = true; // Set the flag to true after initialization
      });
    })
  }

  setModificaProfiloForm() {
    this.modificaProfiloForm.patchValue({
      ragioneSociale: this.azienda.ragioneSociale,
      email: this.azienda.email,
      telefono: this.azienda.telefono.trim(),
      indirizzo: this.azienda.indirizzo,
    });
  }

  onFileSelected(event: any) {
    this.file = event.target.files[0];
    this.aziendeService.addImgAzienda(this.azienda.piva, this.file).subscribe(
      response => {
        this.alert.setAlertProfilo('success', 'Immagine aggiornata con successo');
        this.getAzienda();
      },
      error => {
        this.alert.setAlertProfilo('danger', 'Errore nel salvataggio delle modifiche');
      });
  }

  salvaModifiche() {
    this.modificaAzienda(this.modificaProfiloForm.value, this.file);
  }

  private modificaAzienda(aziendaData: any, file: File | undefined) {
    aziendaData.piva = this.azienda.piva;
    this.aziendeService.modificaProfilo(aziendaData, this.file).subscribe(
      response => {
        this.alert.setAlertProfilo('success', 'Modifiche salvate con successo');
        this.getAzienda();
      },
      error => {
        this.alert.setAlertProfilo('danger', 'Errore nel salvataggio delle modifiche');
      });
  }

  modificaPassword() {
    let user = {
      oldPw: this.modificaPasswordForm.value.password,
      newPw: this.modificaPasswordForm.value.nuovaPassword
    }
    this.auth.updatePassword(user.oldPw, user.newPw).subscribe(
      response => {
        this.alert.setAlertProfilo('success', 'Password modificata con successo');
        this.modificaPasswordForm.reset();
      },
      error => {
        this.alert.setAlertProfilo('danger', 'Errore nel salvataggio delle modifiche');
      }
    );
  }

  openDeleteImg() {
    const modalRef = this.modalService.open(ConfirmModalComponent, {
      size: 'md'
    });

    // Passa il this.dipendente al modal
    modalRef.componentInstance.azienda = this.azienda;
    modalRef.componentInstance.function = 'deleteImgAzienda';

    modalRef.componentInstance.refreshData.subscribe(() => {
      // Aggiorna i dati richiamando nuovamente ngOnInit
      this.ngOnInit();
    });
  }

}
