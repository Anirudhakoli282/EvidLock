import { Component } from '@angular/core';
import { NgbActiveModal, NgbDate, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AlertService } from '../../../service/alert/alert.service';

import { FormCheck } from '../../../FormCheck';
import { DipendentiService } from '../../../service/dipendenti/dipendenti.service';
import { Input, Output, EventEmitter } from '@angular/core';
import { Dipendente } from '../../../model/Dipendente';

import { FileService } from '../../../service/file/file.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-add-dipendente-modal',
  templateUrl: './add-dipendente-modal.component.html',
  styleUrl: './add-dipendente-modal.component.css'
})
export class AddDipendenteModalComponent {
  @Input() dipendente: Dipendente | undefined;
  @Output() refreshData: EventEmitter<void> = new EventEmitter<void>();

  private file: File | undefined;
  private dataNascita: string = '';
  private dataAssunzione: string = '';

  addDipendenteForm: FormGroup;
  model: NgbDateStruct | undefined;

  public minDate: NgbDate;

  constructor(
    public activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private alert: AlertService,
    private dipendentiService: DipendentiService,
    private router: Router
  ) {
    this.addDipendenteForm = this.fb.group({
      nome: ['', Validators.required],
      cognome: ['', Validators.required],
      dataNascita: ['', Validators.required],
      dataAssunzione: ['', Validators.required],
      cf: ['', Validators.required],
      ruolo: ['', Validators.required],
      email: ['', Validators.required],
      residenza: ['',],
      telefono: ['',],
      img: ['',],
    }, { validators: this.customValidation });

    this.minDate = new NgbDate(1900, 1, 1);
  }

  ngOnInit(): void {
    if (this.dipendente) {
      this.initializeFormWithDipendente(this.dipendente);
    }
  }

  private initializeFormWithDipendente(dipendente: Dipendente): void {
    const dataNascitaArray = dipendente.dataNascita.split('-');
    const dataAssunzioneArray = dipendente.dataAssunzione.split('-');

    this.addDipendenteForm.patchValue({
      nome: dipendente.nome,
      cognome: dipendente.cognome,

      dataNascita: {
        year: +dataNascitaArray[0],
        month: +dataNascitaArray[1],
        day: +dataNascitaArray[2]
      },
      dataAssunzione: {
        year: +dataAssunzioneArray[0],
        month: +dataAssunzioneArray[1],
        day: +dataAssunzioneArray[2]
      },

      cf: dipendente.cf,
      ruolo: dipendente.ruolo,
      email: dipendente.email,
      residenza: dipendente.residenza,
      telefono: dipendente.telefono,
    });

  }

  customValidation(group: FormGroup) {
    const nomeControl = group.get('nome');
    const cognomeControl = group.get('cognome');
    const dataNascitaControl = group.get('dataNascita');
    const dataAssunzioneControl = group.get('dataAssunzione');
    const cfControl = group.get('cf');
    const ruoloControl = group.get('ruolo');
    const emailControl = group.get('email');
    const telefonoControl = group.get('telefono');

    if (nomeControl && cognomeControl && dataNascitaControl && dataAssunzioneControl && cfControl && ruoloControl && emailControl && telefonoControl) {
      const nome = nomeControl.value;
      const cognome = cognomeControl.value;
      const dataNascita = dataNascitaControl.value;
      const dataAssunzione = dataAssunzioneControl.value;
      const cf = cfControl.value;
      const ruolo = ruoloControl.value;
      const email = emailControl.value;
      const telefono = telefonoControl?.value;

      // Nome
      if (nome && !FormCheck.checkNome(nome)) {
        nomeControl.setErrors({ 'invalidName': true });
      } else {
        if (nomeControl.hasError('invalidName')) {
          nomeControl.setErrors(null);
        }
      }

      // Cognome
      if (cognome && !FormCheck.checkNome(cognome)) {
        cognomeControl.setErrors({ 'invalidSurname': true });
      } else {
        if (cognomeControl.hasError('invalidSurname')) {
          cognomeControl.setErrors(null);
        }
      }

      // Data di nascita
      if (dataNascita && !FormCheck.dataNascitaMinima(dataNascita)) {
        dataNascitaControl.setErrors({ 'underage': true });
      } else {
        if (dataNascitaControl.hasError('underage')) {
          dataNascitaControl.setErrors(null);
        }
      }

      // Data di assunzione
      var dataAssunzioneMaggiorenne = new NgbDate(dataNascita.year + 18, dataNascita.month, dataNascita.day);
      if (dataNascita && dataAssunzione && FormCheck.compareTwoDates(dataAssunzioneMaggiorenne, dataAssunzione)) {
        dataAssunzioneControl.setErrors({ 'invalidDate': true });
      }
      else {
        if (dataAssunzioneControl.hasError('invalidDate')) {
          dataAssunzioneControl.setErrors(null);
        }
      }
      if (dataAssunzione && !FormCheck.compareFutureDate(dataAssunzione)) {
        dataAssunzioneControl.setErrors({ 'invalidDate': true });
      }

      // CF
      if (cf && !FormCheck.checkCodiceFiscale(cf)) {
        cfControl.setErrors({ 'invalidCFLength': true });
      } else {
        if (cfControl.hasError('invalidCFLength')) {
          cfControl.setErrors(null);
        }
      }

      // Ruolo
      if (ruolo && !FormCheck.checkNome(ruolo)) {
        ruoloControl.setErrors({ 'invalidRole': true });
      } else {
        if (ruoloControl.hasError('invalidRole')) {
          ruoloControl.setErrors(null);
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

      // Telefono
      if (telefono && !FormCheck.checkTelefono(telefono)) {
        telefonoControl?.setErrors({ 'invalidPhone': true });
      } else {
        if (telefonoControl?.hasError('invalidPhone')) {
          telefonoControl?.setErrors(null);
        }
      }
    }
  }

  onFileSelected(event: any) {
    this.file = event.target.files[0];
  }

  formatData(){
    const dipendenteData = this.addDipendenteForm.value;
    if (this.dataNascita == '' || this.dataAssunzione == '') {
      this.dataNascita = this.NgbDateToDateString(dipendenteData.dataNascita);
      this.dataAssunzione = this.NgbDateToDateString(dipendenteData.dataAssunzione);
    }
    dipendenteData.dataNascita = this.dataNascita;
    dipendenteData.dataAssunzione = this.dataAssunzione;
    dipendenteData.nome = dipendenteData.nome.charAt(0).toUpperCase() + dipendenteData.nome.slice(1).toLowerCase().trim();
    dipendenteData.cognome = dipendenteData.cognome.charAt(0).toUpperCase() + dipendenteData.cognome.slice(1).toLowerCase().trim();
    dipendenteData.ruolo = dipendenteData.ruolo.charAt(0).toUpperCase() + dipendenteData.ruolo.slice(1).toLowerCase().trim();
    dipendenteData.cf = dipendenteData.cf.toUpperCase().trim();
    dipendenteData.email = dipendenteData.email.toLowerCase().trim();

    return dipendenteData;
  }

  submitForm() {
    this.activeModal.close('Save click');

    const dipendenteData = this.formatData();

    if (this.dipendente)
      this.updateDipendente(dipendenteData);
    else
      this.addDipendente(dipendenteData);
  }

  private addDipendente(dipendenteData: any) {
    this.dipendentiService.addDipendente(dipendenteData, this.file).subscribe(
      response => {
        this.alert.setAlertDipendenti("success", `Dipendente <strong>${dipendenteData.nome} ${dipendenteData.cognome}</strong> aggiunto con successo`);
        this.refreshData.emit();
      },
      error => {
        this.alert.setAlertDipendenti("danger", `Errore durante l'aggiunta del dipendente <strong>${dipendenteData.nome} ${dipendenteData.cognome}</strong>`);
        this.refreshData.emit();
      }
    );
  }

  private updateDipendente(dipendenteData: any) {
    dipendenteData.id = this.dipendente?.id;
    this.dipendentiService.updateDipendente(dipendenteData, this.file).subscribe(
      result => {
        this.alert.setAlertDipendenti("success", `Dipendente <strong>${dipendenteData.nome} ${dipendenteData.cognome}</strong> modificato con successo`);
        this.refreshData.emit();
      },
      error => {
        this.alert.setAlertDipendenti("danger", `Errore durante la modifica del dipendente <strong>${dipendenteData.nome} ${dipendenteData.cognome}</strong>`);
        this.refreshData.emit();
      });
  }

  NgbDateToDateString(ngbDate: NgbDate): string {
    // Converte un oggetto NgbDate in una stringa nel formato 'yyyy-MM-dd'
    let month = ngbDate.month.toString();
    let day = ngbDate.day.toString();
    if (ngbDate.month < 10) {
      month = '0' + month;
    }
    if (ngbDate.day < 10) {
      day = '0' + day;
    }
    return ngbDate.year + '-' + month + '-' + day;
  }
}