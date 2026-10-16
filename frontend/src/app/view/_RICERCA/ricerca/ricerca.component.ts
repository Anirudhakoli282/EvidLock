import { Component } from '@angular/core';
import { Agency } from '../../../model/Agency';
import { Documento } from '../../../model/Documento';
import { User } from '../../../model/User';

import { ActivatedRoute } from '@angular/router';
import { CommunicationService } from '../../../service/communication/communication.service';
import { RicercaService } from '../../../service/ricerca/ricerca.service';
import { AuthService } from '../../../service/auth/auth.service';

import { FileService } from '../../../service/file/file.service';
import { forkJoin, Observable, map, catchError, of } from 'rxjs';

@Component({
  selector: 'app-ricerca',
  templateUrl: './ricerca.component.html',
  styleUrl: './ricerca.component.css'
})
export class RicercaComponent {

  constructor(
    private route: ActivatedRoute,
    private communication: CommunicationService,
    private ricercaService: RicercaService,
    private auth: AuthService,
    private fileService: FileService) { }

  aziende?: Agency[];
  aziendeInitialized: boolean = false;

  documenti?: Documento[];

  dipendenti?: User[];
  dipendentiInitialized: boolean = false;

  pIva!:string

  ngOnInit(): void {
    if (this.auth.getRole() == 'A') {
      this.pIva = this.auth.getCurrentPIva()!;
    }
    else {
      this.pIva = this.auth.getSelectedPIva()!;
    }

    const text = this.route.snapshot.paramMap.get('text');
    this.search(text || '');
  
    // Iscriviti agli aggiornamenti del testo di ricerca
    this.communication.searchTextChanged$.subscribe((searchText) => {
      this.search(searchText);
    });
  }

  search(text: string) {
    this.clear();
    this.ricercaService.search(this.pIva, text).subscribe((data) => {
      this.aziende = data.aziende;
      this.aziendeInitialized = true;

      this.documenti = data.documenti;

      this.dipendenti = data.dipendenti;
      this.dipendentiInitialized = true;
    });
  }

  clear() {
    this.aziende = undefined;
    this.documenti = undefined;
    this.dipendenti = undefined;
  }
}


