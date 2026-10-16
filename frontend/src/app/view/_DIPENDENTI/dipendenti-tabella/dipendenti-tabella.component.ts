import { Component, Input, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { User } from '../../../model/User';

@Component({
  selector: 'app-dipendenti-tabella',
  templateUrl: './dipendenti-tabella.component.html',
  styleUrl: './dipendenti-tabella.component.css'
})
export class DipendentiTabellaComponent implements OnInit, AfterViewInit {
  @Input() users: User[] = [];

  displayedColumns: string[] = ['name', 'role', 'badgeNumber', 'agencyCode', 'phone'];
  dataSource!: MatTableDataSource<User>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  ngOnInit(): void {
    this.dataSource = new MatTableDataSource(this.users);
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }
}
