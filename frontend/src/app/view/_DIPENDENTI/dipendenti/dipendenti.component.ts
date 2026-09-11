import { Component } from '@angular/core';
import { AlertService } from '../../../service/alert/alert.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AddDipendenteModalComponent } from '../add-dipendente-modal/add-dipendente-modal.component';

import { Dipendente } from '../../../model/Dipendente';
import { DipendentiService } from '../../../service/dipendenti/dipendenti.service';
import { FileService } from '../../../service/file/file.service';
import { forkJoin, Observable, map, catchError, of } from 'rxjs';
import { AuthService } from '../../../service/auth/auth.service';
import { AddDipendentiListaModalComponent } from '../add-dipendenti-lista-modal/add-dipendenti-lista-modal.component';

@Component({
  selector: 'app-dipendenti',
  templateUrl: './dipendenti.component.html',
  styleUrl: './dipendenti.component.css'
})
export class DipendentiComponent {
  dipendenti: Dipendente[] = [];
  isInitialized: boolean = false; // Add the flag

  constructor(
    private modalService: NgbModal,
    public alert: AlertService,
    private dipendentiService: DipendentiService,
    private fileService: FileService,
    public auth: AuthService
  ) { }

  ngOnInit(): void {
    var pIva: string = '';

    if (this.auth.getRole() == 'A') {
      pIva = this.auth.getCurrentPIva()!;
    }
    else {
      pIva = this.auth.getSelectedPIva()!;
    }

    this.dipendentiService.getDipendenti(pIva).subscribe(
      dipendenti => {
        this.dipendenti = dipendenti;
        this.setDipendentiImages().subscribe(() => {
          this.isInitialized = true; // Set the flag to true after initialization
        });
      },
      error => {
        this.alert.setAlertDipendenti('danger', 'Errore nel recupero dei dipendenti');
      }
    );
  }


  setDipendentiImages(): Observable<void[]> {
    const observables: Observable<void>[] = [];

    this.dipendenti.forEach((dipendente) => {
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

  openAddDipendenteModal() {
    const modalRef = this.modalService.open(AddDipendenteModalComponent, {
      size: 'md'
    });

    modalRef.componentInstance.refreshData.subscribe(() => {
      this.dipendenti = [];
      this.ngOnInit();
    });
  }

  openAddDipendentiLista(){
    const modalRef = this.modalService.open(AddDipendentiListaModalComponent, {
      size: 'md'
    });

    modalRef.componentInstance.refreshData.subscribe(() => {
      this.dipendenti = [];
      this.ngOnInit();
    });
  }

}
