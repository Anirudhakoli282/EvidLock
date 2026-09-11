import { Component, Output } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AlertService } from '../../../service/alert/alert.service';
import { DipendentiService } from '../../../service/dipendenti/dipendenti.service';
import { EventEmitter } from '@angular/core';



@Component({
  selector: 'app-add-dipendenti-lista-modal',
  templateUrl: './add-dipendenti-lista-modal.component.html',
  styleUrl: './add-dipendenti-lista-modal.component.css'
})
export class AddDipendentiListaModalComponent {

  private file: File | undefined;
  addDipendentiForm: FormGroup;
  @Output() refreshData: EventEmitter<void> = new EventEmitter<void>();



  constructor(
    public activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private alert: AlertService,
    private dipendentiService: DipendentiService,) {
    this.addDipendentiForm = this.fb.group({
      file: ['', Validators.required],
    });
  }

  submit() {
    this.activeModal.close();
    //get file extension
    const fileType = this.file?.name.split('.')[1];
    if (fileType)
      this.dipendentiService.addListaDipendentiFile(this.file, fileType).subscribe(
        data => {
          this.alert.setAlertDipendenti("success", "Lista dipendenti aggiunta con successo!");
          this.refreshData.emit();
        },
        error => {
          this.alert.setAlertDipendenti("danger", "Errore durante l'aggiunta della lista dipendenti!");
          this.refreshData.emit();
        }
      );

  }

  onFileSelected(event: any) {
    this.file = event.target.files[0];
  }

}
