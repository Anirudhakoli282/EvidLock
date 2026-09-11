import { Component } from '@angular/core';
import { Azienda } from '../../../model/Azienda';
import { Documento } from '../../../model/Documento';
import { Dipendente } from '../../../model/Dipendente';

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

  aziende?: Azienda[];
  aziendeInitialized: boolean = false;

  documenti?: Documento[];

  dipendenti?: Dipendente[];
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
      this.setAziendeImages().subscribe(() => {
        this.aziendeInitialized = true;
      });

      this.documenti = data.documenti;

      this.dipendenti = data.dipendenti;
      this.setDipendentiImages().subscribe(() => {
        this.dipendentiInitialized = true;
      });
    });
  }

  clear() {
    this.aziende = undefined;
    this.documenti = undefined;
    this.dipendenti = undefined;
  }

  setDipendentiImages(): Observable<void[]> {
    const observables: Observable<void>[] = [];

    this.dipendenti?.forEach((dipendente) => {
      if (dipendente.img !== '' && dipendente.img !== null) {
        const observable = this.fileService.getFile(dipendente.img).pipe(
          map((img) => {
            let objectURL = URL.createObjectURL(img);
            dipendente.img = objectURL;
          }),
          catchError((error) => {
            // Handle the 404 error or any other error
            if (error.status === 404) {
              // Change the image URL to a default one
              console.warn(`Image for dipendente ${dipendente.id} not found, using default image instead`);
              dipendente.img = "";
            } else {
              console.error(`Error loading image for dipendente: ${dipendente.id}`, error);
            }
            // Continue with the observable by returning an empty observable
            return [];
          })
        );
        observables.push(observable);
      }else {
        // If azienda.img is empty, create an empty observable
        observables.push(of(null).pipe(map(() => {})));
      }
    });

    // Use forkJoin to wait for all observables to complete
    return forkJoin(observables);
  }

  setAziendeImages(): Observable<void[]> {
    const observables: Observable<void>[] = [];

    this.aziende?.forEach((azienda) => {
      if (azienda.img !== '' && azienda.img !== null) {
        const observable = this.fileService.getFile(azienda.img).pipe(
          map((img) => {
            let objectURL = URL.createObjectURL(img);
            azienda.img = objectURL;
          }),
          catchError((err) => {
            // Handle the 404 error or any other error
            if (err.status === 404) {
              // Change the image URL to a default one
              console.warn(`Image for dipendente ${azienda.piva} not found, using default image instead`);
              azienda.img = "";
            } else {
              console.error(`Error loading image for dipendente: ${azienda.piva}`, err);
            }
            // Continue with the observable by returning an empty observable
            return [];
          })
        );
        observables.push(observable);
      }
      else {
        // If azienda.img is empty, create an empty observable
        observables.push(of(null).pipe(map(() => {})));
      }
    });

    return forkJoin(observables);
  }

}
