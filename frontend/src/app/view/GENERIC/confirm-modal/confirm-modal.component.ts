import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Router } from '@angular/router';

import { DipendentiService } from '../../../service/dipendenti/dipendenti.service';
import { AlertService } from '../../../service/alert/alert.service';
import { Dipendente } from '../../../model/Dipendente';

import { Documento } from '../../../model/Documento';
import { DocumentiService } from '../../../service/documenti/documenti.service';
import { Output, EventEmitter } from '@angular/core';
import { Azienda } from '../../../model/Azienda';
import { AziendeService } from '../../../service/aziende/aziende.service';
import { Corso } from '../../../model/Corso';
import { CorsiService } from '../../../service/corsi/corsi.service';

@Component({
  selector: 'app-confirm-modal',
  templateUrl: './confirm-modal.component.html',
  styleUrl: './confirm-modal.component.css'
})
export class ConfirmModalComponent {
  @Output() refreshData: EventEmitter<void> = new EventEmitter<void>();


  @Input() function?: string;
  @Input() dipendente?: Dipendente;
  @Input() documento?: Documento;
  @Input() azienda?: Azienda;
  @Input() corso?: Corso;

  constructor(
    public activeModal: NgbActiveModal,
    private router: Router,
    private alert: AlertService,
    private dipendentiService: DipendentiService,
    private documentiService: DocumentiService,
    private aziendeService: AziendeService,
    private corsiService: CorsiService) { }

  submit() {
    if (this.function == 'deleteImgDipendente') {
      this.deleteImgDipendente();
    }
    else if (this.function == 'deleteDipendente') {
      this.deleteDipendente();
    }
    else if (this.function == 'deleteDocumento') {
      this.deleteDocumento();
    }
    else if (this.function == 'deleteAzienda') {
      this.deleteAzienda();
    }
    else if (this.function == 'deleteCorso') {
      this.deleteCorso();
    }
    else if(this.function == 'deleteImgAzienda'){
      this.deleteImgAzienda();
    }


    this.activeModal.close();
  }

  private deleteImgDipendente() {
    if (this.dipendente != undefined ) {
      this.dipendentiService.deleteImgDipendente(this.dipendente.id).subscribe(
        data => {
          this.alert.setAlertDipendenti("success", "Immagine rimossa con successo");
          this.refreshData.emit();
        },
        error => {
          this.alert.setAlertDipendenti("danger", "Errore durante la rimozione dell'immagine");
        }
      );
    }
  }

  private deleteDipendente() {
    if (this.dipendente != undefined ) {
      this.dipendentiService.deleteDipendente(this.dipendente.id).subscribe(
        data => {
          this.alert.setAlertDipendenti("success", `Dipendente <strong>${this.dipendente?.nome} ${this.dipendente?.cognome}</strong> rimosso con successo`);
          this.refreshData.emit();
        },
        error => {
          this.alert.setAlertDipendenti("danger", `Errore durante la rimozione del dipendente <strong>${this.dipendente?.nome} ${this.dipendente?.cognome}</strong>`);
        }
      );
    }
  }

  private deleteDocumento() {
    if (this.documento != undefined ) {
      this.documentiService.deleteDocumento(this.documento.id).subscribe(
        data => {
          this.alert.setAlertDocumenti("success", `Documento <strong>${this.documento?.nome}</strong> rimosso con successo`);
          this.refreshData.emit();
        },
        error => {
          this.alert.setAlertDocumenti("danger", `Errore durante la rimozione del documento <strong>${this.documento?.nome}</strong>`);
        }
      );
    }
  }

  private deleteAzienda() {
    if (this.azienda != undefined ) {
      this.aziendeService.deleteAzienda(this.azienda.piva).subscribe(
        data => {
          this.alert.setAlertAziende("success", `Azienda <strong>${this.azienda?.ragioneSociale}</strong> rimossa con successo`);
          this.refreshData.emit();
        },
        error => {
          this.alert.setAlertAziende("danger", `Errore durante la rimozione dell'azienda <strong>${this.azienda?.ragioneSociale}</strong>`);
        }
      );
    }
  }

  private deleteCorso() {
    if (this.corso != undefined ) {
      this.corsiService.deleteCorso(this.corso.id).subscribe(
        data => {
          this.alert.setAlertCorsi("success", `Corso <strong>${this.corso?.nome}</strong> rimosso con successo`);
          this.refreshData.emit();
        },
        error => {
          this.alert.setAlertCorsi("danger", `Errore durante la rimozione del corso <strong>${this.corso?.nome}</strong>`);
        }
      );
    }
  }

  private deleteImgAzienda() {
    if (this.azienda != undefined ) {
      this.aziendeService.deleteImgAzienda(this.azienda.piva).subscribe(
        data => {
          this.alert.setAlertProfilo("success", `Immagine rimossa con successo`);
          this.refreshData.emit();
        },
        error => {
          this.alert.setAlertProfilo("danger", `Errore durante la rimozione dell'immagine`);
        }
      );
    }
  }
}
