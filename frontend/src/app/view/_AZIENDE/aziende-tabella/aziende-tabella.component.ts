import { Component, ViewChild, Input, ChangeDetectorRef } from '@angular/core';

import { Azienda } from '../../../model/Azienda';

import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmModalComponent } from '../../GENERIC/confirm-modal/confirm-modal.component';
import { EventEmitter } from '@angular/core';
import { Output } from '@angular/core';

@Component({
  selector: 'app-aziende-tabella',
  templateUrl: './aziende-tabella.component.html',
  styleUrl: './aziende-tabella.component.css'
})
export class AziendeTabellaComponent {
  displayedColumns: string[] = ['ragioneSociale', 'pIva', 'indirizzo', 'telefono', 'azioni'];
  dataSource: MatTableDataSource<Azienda>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  @Input() aziende: Azienda[] = [];
  @Output() refreshData: EventEmitter<void> = new EventEmitter<void>();


  constructor
    (
      private modalService: NgbModal,
      private cdr: ChangeDetectorRef
    ) {
    this.dataSource = new MatTableDataSource(this.aziende);
  }

  ngAfterViewInit() {
    // If necessario per aspettare che il componente sia caricato
    if (this.aziende.length > 0) {
      this.dataSource.data = this.aziende;
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    }
    this.cdr.detectChanges();
  }

  openDeleteAzienda(azienda: Azienda) {
    const modalRef = this.modalService.open(ConfirmModalComponent, {
      size: 'md' // 'lg' sta per grande, puoi utilizzare anche 'sm' per piccolo
    });

    // Passa il this.dipendente al modal
    modalRef.componentInstance.azienda = azienda;
    modalRef.componentInstance.function = 'deleteAzienda';

    modalRef.componentInstance.refreshData.subscribe(() => {
      this.refreshData.emit();
    });
  }



}
