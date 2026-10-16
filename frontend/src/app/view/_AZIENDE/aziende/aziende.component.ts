import { Component, OnInit } from '@angular/core';
import { AgencyService } from '../../../service/agency/agency.service';
import { Agency } from '../../../model/Agency';
import { Router } from '@angular/router';

@Component({
  selector: 'app-aziende',
  templateUrl: './aziende.component.html',
  styleUrl: './aziende.component.css'
})
export class AziendeComponent implements OnInit {

  isInitialized: boolean = false;
  agencies: Agency[] = [];

  constructor(
    private agencyService: AgencyService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.agencyService.getAgencies().subscribe({
      next: (data) => { 
        this.agencies = data;
        this.isInitialized = true;
      },
      error: (error) => {
        if (error.status == 401) {
          this.router.navigate(['/login']);
        } else {
            console.error("Error fetching agencies:", error);
        }
      }
    });
  }

}
