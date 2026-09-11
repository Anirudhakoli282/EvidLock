import { Component } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AlertService } from '../../../service/alert/alert.service';
import { AddDipendenteCorsoModalComponent } from '../add-dipendente-corso-modal/add-dipendente-corso-modal.component';

import { Dipendente } from '../../../model/Dipendente';

import { Corso } from '../../../model/Corso';
import { CorsiService } from '../../../service/corsi/corsi.service';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../service/auth/auth.service';
import { AddCorsoModalComponent } from '../add-corso-modal/add-corso-modal.component';
import { ConfirmModalComponent } from '../../GENERIC/confirm-modal/confirm-modal.component';
import { Observable } from 'rxjs';
import { FileService } from '../../../service/file/file.service';
import { forkJoin } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { map } from 'rxjs/operators';
import { of } from 'rxjs';


@Component({
  selector: 'app-dettaglio-corso',
  templateUrl: './dettaglio-corso.component.html',
  styleUrl: './dettaglio-corso.component.css'
})

export class DettaglioCorsoComponent {
  corso?: Corso;
  dipendentiIscritti?: Dipendente[];

  isInitialized: boolean = false;

  constructor(
    private modalService: NgbModal,
    public alert: AlertService,
    private route: ActivatedRoute,
    private router: Router,
    private corsiService: CorsiService,
    public auth: AuthService,
    private fileService: FileService
  ) { }

  ngOnInit(): void {
    this.getCorso();

    this.getDipendentiIscritti();
  }

  getCorso(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.corsiService.getCorso(id).subscribe(corso => {
      this.corso = corso;
      if (this.corso == undefined) {
        this.router.navigate(['/404']);
      }
    });
  }

  getDipendentiIscritti(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.corsiService.getDipendentiIscritti(id).subscribe(dipendentiIscritti => {
      this.dipendentiIscritti = dipendentiIscritti;
      this.setDipendentiImages().subscribe(() => {
        this.isInitialized = true; // Set the flag to true after initialization
      });
    });
  }

  openUpdateCorsoModal() {
    const modalRef = this.modalService.open(AddCorsoModalComponent, {
      size: 'md' // 'lg' sta per grande, puoi utilizzare anche 'sm' per piccolo
    });

    modalRef.componentInstance.corso = this.corso;

    modalRef.componentInstance.refreshData.subscribe(() => {
      this.getCorso();
    });
  }

  openAddDipendentiCorso() {
    const modalRef = this.modalService.open(AddDipendenteCorsoModalComponent, {
      size: 'md' // 'lg' sta per grande, puoi utilizzare anche 'sm' per piccolo
    });

    modalRef.componentInstance.idCorso = this.corso?.id;

    modalRef.componentInstance.refreshData.subscribe(() => {
      // Aggiorna i dati richiamando nuovamente ngOnInit
      this.corso = undefined;
      this.dipendentiIscritti = [];
      this.ngOnInit();
    });
  }

  openDeleteCorso() {
    const modalRef = this.modalService.open(ConfirmModalComponent, {
      size: 'md' // 'lg' sta per grande, puoi utilizzare anche 'sm' per piccolo
    });

    // Passa il this.dipendente al modal
    modalRef.componentInstance.corso = this.corso;
    modalRef.componentInstance.function = 'deleteCorso';

    modalRef.componentInstance.refreshData.subscribe(() => {
      // Aggiorna i dati richiamando nuovamente ngOnInit
      this.router.navigate(['/corsi']);
    });
  }

  setDipendentiImages(): Observable<void[]> {
    const observables: Observable<void>[] = [];

    this.dipendentiIscritti?.forEach((dipendente) => {
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

}
