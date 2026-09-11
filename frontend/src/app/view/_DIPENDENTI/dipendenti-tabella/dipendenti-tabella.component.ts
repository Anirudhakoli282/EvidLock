import { AfterViewInit, Component, ViewChild, Input, ChangeDetectorRef } from '@angular/core';
import { Dipendente } from '../../../model/Dipendente';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-dipendenti-tabella',
  templateUrl: './dipendenti-tabella.component.html',
  styleUrl: './dipendenti-tabella.component.css'
})
export class DipendentiTabellaComponent implements AfterViewInit {
  displayedColumns: string[] = ['cognome', 'ruolo', 'dataAssunzione', 'azioni'];
  dataSource: MatTableDataSource<Dipendente>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  @Input() dipendenti: Dipendente[] = [];

  constructor(
    private cdr: ChangeDetectorRef,
    private datePipe: DatePipe
  ) {
    this.dataSource = new MatTableDataSource(this.dipendenti);
  }

  ngAfterViewInit() {
    // Wait for the component to be loaded
    if (this.dipendenti.length > 0) {
      this.dataSource.data = this.dipendenti.map(dipendente => ({
        ...dipendente,
        dataAssunzione: this.formatItalianDate(dipendente.dataAssunzione)
      }));
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
    }
    this.cdr.detectChanges();
  }

  private formatItalianDate(date: string): string {
    const formattedDate = this.datePipe.transform(date, 'dd/MM/yyyy');
    return formattedDate || '';
  }
}
