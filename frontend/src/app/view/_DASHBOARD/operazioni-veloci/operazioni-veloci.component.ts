import { Component, Output } from '@angular/core';
import { AuthService } from '../../../service/auth/auth.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AddDocumentModalComponent } from '../../_DOCUMENTI/add-documento-modal/add-document-modal.component';
import { AddDipendenteModalComponent } from '../../_DIPENDENTI/add-dipendente-modal/add-dipendente-modal.component';
import { AddAziendaModalComponent } from '../../_AZIENDE/add-azienda-modal/add-azienda-modal.component';
import { AddCorsoModalComponent } from '../../_CORSI/add-corso-modal/add-corso-modal.component';
import { EventEmitter } from '@angular/core';

@Component({
  selector: 'app-operazioni-veloci',
  templateUrl: './operazioni-veloci.component.html',
  styleUrl: './operazioni-veloci.component.css'
})
export class OperazioniVelociComponent {
  @Output() refreshCorsi: EventEmitter<void> = new EventEmitter<void>();
  @Output() refreshDocumenti: EventEmitter<void> = new EventEmitter<void>();


  constructor(public auth: AuthService,
    private modalService: NgbModal) { }

    openAddAziendaModal() {
      const modalRef = this.modalService.open(AddAziendaModalComponent, {
        size: 'md'
      });
  
      modalRef.componentInstance.refreshData.subscribe(() => {
      // Aggiungi un ritardo di due secondi prima del reload
      setTimeout(() => {
        window.location.reload();
      }, 2000);
    });
    }

  openAddDocumentoModal() {
    const modalRef = this.modalService.open(AddDocumentModalComponent, {
      size: 'md' // 'lg' sta per grande, puoi utilizzare anche 'sm' per piccolo
    });

    modalRef.componentInstance.refreshData.subscribe(() => {
      this.refreshDocumenti.emit();
    });
  }

  openAddCorsoModal() {
    const modalRef = this.modalService.open(AddCorsoModalComponent, {
      size: 'md'
    });

    modalRef.componentInstance.refreshData.subscribe(() => {
      this.refreshCorsi.emit();
    });
  }

  openAddDipendenteModal() {
    const modalRef = this.modalService.open(AddDipendenteModalComponent, {
      size: 'md' // 'lg' sta per grande, puoi utilizzare anche 'sm' per piccolo
    });
  }
}
