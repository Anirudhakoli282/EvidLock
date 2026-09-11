import { Component } from '@angular/core';

import { AddCorsoModalComponent } from '../add-corso-modal/add-corso-modal.component';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../../service/auth/auth.service';
import { CorsiService } from '../../../service/corsi/corsi.service';
import { Corso } from '../../../model/Corso';


@Component({
  selector: 'app-corsi',
  templateUrl: './corsi.component.html',
  styleUrl: './corsi.component.css'
})
export class CorsiComponent {

  constructor(
    private modalService: NgbModal,
    public auth: AuthService,
    private corsiService: CorsiService) { }

  corsi: Corso[] = [];

  ngOnInit(): void {
    var pIva: string = '';
    if (this.auth.getRole() == 'A') {
      pIva = this.auth.getCurrentPIva()!;
    }
    else {
      pIva = this.auth.getSelectedPIva()!;
    }

    //this.corsiService.getCorsiProposti(pIva).subscribe(result => this.corsi = result);
    this.corsiService.getCorsiAcquistati(pIva).subscribe(result => { this.corsi = result; });
  }

  openAddCorsoModal() {
    const modalRef = this.modalService.open(AddCorsoModalComponent, {
      size: 'md'
    });

    modalRef.componentInstance.refreshData.subscribe(() => {
      this.corsi = [];
      this.ngOnInit();
    });
    
  }
}
