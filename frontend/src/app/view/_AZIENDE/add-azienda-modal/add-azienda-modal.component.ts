import { Component, Output } from '@angular/core';
import { NgbActiveModal, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AlertService } from '../../../service/alert/alert.service';
import { FormCheck } from '../../../FormCheck';
import { AziendeService } from '../../../service/aziende/aziende.service';
import { EventEmitter } from '@angular/core';

@Component({
  selector: 'app-add-azienda-modal',
  templateUrl: './add-azienda-modal.component.html',
  styleUrl: './add-azienda-modal.component.css'
})
export class AddAziendaModalComponent {

  private file: File | undefined;

  addAziendaForm: FormGroup;
  model: NgbDateStruct | undefined;

  @Output() refreshData: EventEmitter<void> = new EventEmitter<void>();

  constructor(
    public activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private azienteService: AziendeService,
    private alert: AlertService) {
    this.addAziendaForm = this.fb.group({
      ragioneSociale: ['', Validators.required],
      piva: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', Validators.required],
      indirizzo: ['',Validators.required],
      img: ['',],
    }, { validators: this.customValidation });
  }
  
  customValidation(group: FormGroup) {
    const pivaControl = group.get('piva');
    const emailControl = group.get('email');
    const telefonoControl = group.get('telefono');

    if(pivaControl && emailControl && telefonoControl){

      //piva validation
      const piva = pivaControl.value;
      if (piva && !FormCheck.checkPIva(piva)) {
        pivaControl.setErrors({ 'invalidpiva': true });
      }
      else if(pivaControl.hasError('invalidpiva')){
        pivaControl.setErrors(null);
      }

      //email validation
      const email = emailControl.value;
      if (email && !FormCheck.checkEmail(email)) {
        emailControl.setErrors({ 'invalidEmail': true });
      }
      else if(emailControl.hasError('invalidEmail')){
        emailControl.setErrors(null);
      }

      //telefono validation
      const telefono = telefonoControl.value;
      if (telefono && !FormCheck.checkTelefono(telefono)) {
        telefonoControl.setErrors({ 'invalidTelefono': true });
      }
      else if(telefonoControl.hasError('invalidTelefono')){
        telefonoControl.setErrors(null);
      }
    }

  }


  submitForm() { 
    this.activeModal.close(this.addAziendaForm.value);
    this.azienteService.addAzienda(this.addAziendaForm.value, this.file).subscribe(
      response => {
        this.alert.setAlertAziende("success", `Azienda <strong>${this.addAziendaForm.get("ragioneSociale")?.value}</strong> aggiunta con successo`);
        this.refreshData.emit();
      },
      error => {
        this.alert.setAlertAziende("danger", `Errore durante l'aggiunta dell'azienda <strong>${this.addAziendaForm.get("ragioneSociale")?.value}</strong>`);
      }
    );
  }

  onFileSelected(event: any) {
    this.file = event.target.files[0];
  }

}
