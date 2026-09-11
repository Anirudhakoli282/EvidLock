import { Component } from '@angular/core';
import { AlertService } from '../../../service/alert/alert.service';

import { AziendeService } from '../../../service/aziende/aziende.service';
import { Azienda } from '../../../model/Azienda';
import { AddAziendaModalComponent } from '../add-azienda-modal/add-azienda-modal.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { Router } from '@angular/router';
import { AuthService } from '../../../service/auth/auth.service';
import { FileService } from '../../../service/file/file.service';
import { Observable } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { forkJoin } from 'rxjs';
import { of } from 'rxjs';

@Component({
  selector: 'app-aziende',
  templateUrl: './aziende.component.html',
  styleUrl: './aziende.component.css'
})
export class AziendeComponent {

  constructor(
    public alert: AlertService,
    private aziendeService: AziendeService,
    private modalService: NgbModal,
    private router: Router,
    public auth: AuthService,
    private fileService: FileService
  ) { }

  isInitialized: boolean = false; // Add the flag
  aziende: Azienda[] = [];

  ngOnInit(): void {
    this.aziendeService.getAziende().subscribe(
      aziende => { 
        this.aziende = aziende; 
        this.setAziendeImages().subscribe(() => {
          this.isInitialized = true; // Set the flag to true after initialization
        });
      },
      errror => {
        if (errror.status == 401) {
          this.router.navigate(['/401']);
        }
      });
  }

  openAddAzienda() {
    const modalRef = this.modalService.open(AddAziendaModalComponent, {
      size: 'md'
    });

    modalRef.componentInstance.refreshData.subscribe(() => {
      this.updateAziende();
      
    // Aggiungi un ritardo di due secondi prima del reload
    setTimeout(() => {
      window.location.reload();
    }, 2000);
  });
  }

  setAziendeImages(): Observable<void[]> {
    const observables: Observable<void>[] = [];

    this.aziende.forEach((azienda) => {
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

  updateAziende() {
    this.aziende = [];
    this.ngOnInit();
  }

}
