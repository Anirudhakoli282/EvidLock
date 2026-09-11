import { Component, Input } from '@angular/core';

import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { LogoutModalComponent } from '../../_LOGIN/logout-modal/logout-modal.component';
import { Router, NavigationStart } from '@angular/router';

import { FormControl } from '@angular/forms';

import { AziendeService } from '../../../service/aziende/aziende.service';
import { Azienda } from '../../../model/Azienda';
import { AuthService } from '../../../service/auth/auth.service';
import { ActivatedRoute } from '@angular/router';
import { FileService } from '../../../service/file/file.service';
import { Observable, forkJoin, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { CommunicationService } from '../../../service/communication/communication.service';





@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  text = new FormControl('');

  role?: string;
  azienda!: Azienda;
  aziende?: Azienda[];
  aziendaSelezionata: string = '';  // Aggiunto per memorizzare la selezione

  isDashboard: boolean = false;
  isInitialized: boolean = false;

  @Input() testString?: string;

  constructor(
    private modalService: NgbModal,
    private router: Router,
    private aziendeService: AziendeService,
    public auth: AuthService,
    private route: ActivatedRoute,
    private fileService: FileService,
    private communication: CommunicationService) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        this.isDashboard = event.url === '/' || event.url === '/aziende' || event.url === '/profile';
        if (!this.isDashboard) {
          this.selectFirstAzienda();
        }
      }
    });
  }

  toggleSidebar() {
    document.body.classList.toggle('toggle-sidebar');
  }

  ngOnInit(): void {
    this.getAzienda();

    this.getAziende();
  }

  getAzienda(): void {
    this.aziendeService.getProfilo().subscribe(azienda => {
      this.azienda = azienda;
      this.setAziendaImage().subscribe(() => {
        this.isInitialized = true;
      });
      this.role = this.auth.getRole() as string | undefined;
    });
  }

  setAziendaImage(): Observable<void[]> {
    const observables: Observable<void>[] = [];

    if (this.azienda != null)
      if (this.azienda.img !== null && this.azienda.img !== '') {
        const observable = this.fileService.getFile(this.azienda.img).pipe(
          map((img) => {
            let objectURL = URL.createObjectURL(img);
            this.azienda.img = objectURL;
          }),
          catchError((err) => {
            this.azienda.img = '';
            return [];
          })
        );
        observables.push(observable);
      } else {
        // If azienda.img is empty, create an empty observable
        observables.push(of(null).pipe(map(() => { })));
      }

    // Use forkJoin to wait for all observables to complete
    return forkJoin(observables);
  }

  getAziende(): void {
    if (this.auth.getRole() == 'C') {
      this.aziendeService.getAziende().subscribe(
        aziende => {
          this.aziende = aziende;

          this.selectFirstAzienda();

          // Ricarica la pagina
          // Necessario per nascondere la comboBox al login
          this.router.routeReuseStrategy.shouldReuseRoute = () => false;
          const currentUrl = this.router.url + '/';
          this.router.navigateByUrl(currentUrl).then(() => {
            this.router.navigated = false;
            this.router.navigate([this.router.url]);
          });
        }
      );
    }
  }

  selectFirstAzienda(): void {
    if (this.aziende && this.aziende.length > 0) {
      let pIva = this.auth.getSelectedPIva();
      if (pIva) {
        let index = this.aziende.findIndex(azienda => azienda.piva === pIva);
        if (index >= 0) {
          this.aziendaSelezionata = this.aziende[index].ragioneSociale;
          this.auth.setSelectedPIva(this.aziende[index].piva);
        }
      }
      else {
        this.aziendaSelezionata = this.aziende[0].ragioneSociale;
        this.auth.setSelectedPIva(this.aziende[0].piva);
      }
    }
  }

  onAziendaChange() {
    let index = this.aziende?.findIndex(azienda => azienda.ragioneSociale === this.aziendaSelezionata);
    if (index !== undefined && index >= 0) {
      this.auth.setSelectedPIva(this.aziende![index].piva);
    }

    // Ricarica la pagina
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    const currentUrl = this.router.url + '?';
    if (currentUrl.includes('corsi')) {
      this.router.navigateByUrl(currentUrl).then(() => {
        this.router.navigated = false;
        this.router.navigate(['/corsi']);
      });
    }
    else if (currentUrl.includes('dipendenti')) {
      this.router.navigateByUrl(currentUrl).then(() => {
        this.router.navigated = false;
        this.router.navigate(['/dipendenti']);
      });
    }
    else {
      this.router.navigateByUrl(currentUrl).then(() => {
        this.router.navigated = false;
        this.router.navigate([this.router.url]);
      });
    }
  }

  search() {
    if (this.text.value === '') {
      this.router.navigate(['/']);
      return;
    }
    this.router.navigate(['/search/', this.text.value ?? '']);
    this.communication.setSearchText(this.text.value ?? '');
  }


  logout() {
    const modalRef = this.modalService.open(LogoutModalComponent, {
      size: 'md'
    });
  }
}
