import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css'
})
export class ReportsComponent implements OnInit {
  @Input() openCases: number = 2;
  @Input() closedCases: number = 1;
  @Input() archivedCases: number = 1;

  @Input() verifiedCount: number = 3;
  @Input() compromisedCount: number = 0;
  @Input() legalHoldCount: number = 1;

  @Input() nearingRetentionCount: number = 1;
  @Input() disposalEligibleCount: number = 0;

  // Compatibility inputs
  @Input() documentiValidi?: any[];
  @Input() documentiInScadenza?: any[];
  @Input() documentiScaduti?: any[];

  ngOnInit(): void {
    if (this.documentiValidi) this.verifiedCount = this.documentiValidi.length;
    if (this.documentiInScadenza) this.nearingRetentionCount = this.documentiInScadenza.length;
    if (this.documentiScaduti) this.disposalEligibleCount = this.documentiScaduti.length;
  }
}
