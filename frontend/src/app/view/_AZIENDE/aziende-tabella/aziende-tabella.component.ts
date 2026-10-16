import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Agency } from '../../../model/Agency';

@Component({
  selector: 'app-aziende-tabella',
  templateUrl: './aziende-tabella.component.html',
  styleUrl: './aziende-tabella.component.css'
})
export class AziendeTabellaComponent implements OnInit {
  @Input() agencies: Agency[] = [];

  displayedColumns: string[] = ['logo', 'name', 'agencyCode', 'phone', 'address'];
  dataSource!: MatTableDataSource<Agency>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource(this.agencies);
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }
}
