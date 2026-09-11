import { Component } from '@angular/core';
import { Dipendente } from '../../../model/Dipendente';

import { Router, ActivatedRoute } from '@angular/router';
import { DipendentiService } from '../../../service/dipendenti/dipendenti.service';

import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AddDipendenteModalComponent } from '../add-dipendente-modal/add-dipendente-modal.component';
import { FileService } from '../../../service/file/file.service';
import { AlertService } from '../../../service/alert/alert.service';
import { DatePipe } from '@angular/common';
import { ConfirmModalComponent } from '../../GENERIC/confirm-modal/confirm-modal.component';

import { forkJoin, Observable, map } from 'rxjs';
import { AuthService } from '../../../service/auth/auth.service';

import { AddDocumentModalComponent } from '../../_DOCUMENTI/add-documento-modal/add-document-modal.component';
import { Documento } from '../../../model/Documento';
import { DocumentiService } from '../../../service/documenti/documenti.service';

@Component({
  selector: 'app-dipendenti-dettagli',
  templateUrl: './dipendenti-dettagli.component.html',
  styleUrl: './dipendenti-dettagli.component.css'
})
export class DipendentiDettagliComponent {

  dipendente!: Dipendente;
  documenti?: Documento[];
  public dataNascitaIT: string = '';
  public dataAssunzioneIT: string = '';

  public isInitialized: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private dipendentiService: DipendentiService,
    private router: Router,
    private modalService: NgbModal,
    private fileService: FileService,
    public alert: AlertService,
    private datePipe: DatePipe,
    public auth: AuthService,
    private documentiService: DocumentiService
  ) { }

  ngOnInit(): void {
    this.getDipendente();

    this.getDocumenti();
  }

  getDipendente(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.dipendentiService.getDipendente(id).subscribe(dipendente => {
      this.dipendente = dipendente;
      if (this.dipendente == undefined) {
        this.router.navigate(['/404']);
      }
      this.setDipendenteImage().subscribe(() => {
        this.isInitialized = true;
      });

      this.dataNascitaIT = this.formatItalianDate(dipendente.dataNascita)
      this.dataAssunzioneIT = this.formatItalianDate(dipendente.dataAssunzione)
    });
  }

  getDocumenti(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.documentiService.getDocumentiDipendente(id).subscribe(documenti => {
      this.documenti = documenti;
    });
  }

  setDipendenteImage(): Observable<void[]> {
    const observables: Observable<void>[] = [];

    if (this.dipendente.img !== '' && this.dipendente.img !== null) {
      const observable = this.fileService.getFile(this.dipendente.img).pipe(
        map(img => {
          let objectURL = URL.createObjectURL(img);
          this.dipendente.img = objectURL;
        })
      );
      observables.push(observable);
    }
    // Use forkJoin to wait for all observables to complete
    return forkJoin(observables);
  }

  openUpdateDipendente() {
    const modalRef = this.modalService.open(AddDipendenteModalComponent, {
      size: 'md' // 'lg' sta per grande, puoi utilizzare anche 'sm' per piccolo
    });

    // Passa il this.dipendente al modal
    modalRef.componentInstance.dipendente = this.dipendente;

    modalRef.componentInstance.refreshData.subscribe(() => {
      // Aggiorna i dati richiamando nuovamente ngOnInit
      this.ngOnInit();
    });
  }

  openDeleteImg() {
    const modalRef = this.modalService.open(ConfirmModalComponent, {
      size: 'md'
    });

    // Passa il this.dipendente al modal
    modalRef.componentInstance.dipendente = this.dipendente;
    modalRef.componentInstance.function = 'deleteImgDipendente';

    modalRef.componentInstance.refreshData.subscribe(() => {
      // Aggiorna i dati richiamando nuovamente ngOnInit
      this.ngOnInit();
    });
  }

  openDeleteDipendente() {
    const modalRef = this.modalService.open(ConfirmModalComponent, {
      size: 'md'
    });

    // Passa il this.dipendente al modal
    modalRef.componentInstance.dipendente = this.dipendente;
    modalRef.componentInstance.function = 'deleteDipendente';

    modalRef.componentInstance.refreshData.subscribe(() => {
      // Aggiorna i dati richiamando nuovamente ngOnInit
      this.router.navigate(['/dipendenti']);
    });
  }

  private formatItalianDate(date: string): string {
    const formattedDate = this.datePipe.transform(date, 'dd/MM/yyyy');
    return formattedDate || '';
  }

  openAddDocumentoDipendente() {
    const modalRef = this.modalService.open(AddDocumentModalComponent, {
      size: 'md' 
    });

    modalRef.componentInstance.dipendente = this.dipendente;

    // Voglio aggiungere un documento del dipendente, quindi passo il dipendente
    modalRef.componentInstance.dipendente = this.dipendente;

    modalRef.componentInstance.refreshData.subscribe(() => {
      // Aggiorna i dati richiamando nuovamente ngOnInit
      this.documenti = [];
      this.getDocumenti()
    });
  }

  updateDocumenti(){
    this.documenti = [];
    this.getDocumenti();
  }
}
