import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CustodyService } from '../../../service/custody/custody.service';
import { ReportService } from '../../../service/report/report.service';
import { CustodyEvent, ChainVerificationResult } from '../../../model/CustodyEvent';
import { EvidenceItem } from '../../../model/EvidenceItem';

@Component({
  selector: 'app-custody-ledger-modal',
  templateUrl: './custody-ledger-modal.component.html',
  styleUrls: ['./custody-ledger-modal.component.css']
})
export class CustodyLedgerModalComponent implements OnInit {
  @Input() item!: EvidenceItem;

  events: CustodyEvent[] = [];
  verificationResult?: ChainVerificationResult;
  loading: boolean = false;
  downloadingReport: boolean = false;

  constructor(
    public activeModal: NgbActiveModal,
    private custodyService: CustodyService,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    if (this.item && this.item.id) {
      this.loadEvents();
      this.verifyChain();
    }
  }

  loadEvents(): void {
    this.loading = true;
    this.custodyService.getCustodyEvents(this.item.id!).subscribe({
      next: (events) => {
        this.events = events;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  verifyChain(): void {
    this.custodyService.verifyChain(this.item.id!).subscribe({
      next: (res) => {
        this.verificationResult = res;
      },
      error: () => {}
    });
  }

  downloadPdfReport(): void {
    if (!this.item.id) return;
    this.downloadingReport = true;
    this.reportService.downloadChainOfCustodyReport(this.item.id).subscribe({
      next: (blob) => {
        this.downloadingReport = false;
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Chain_of_Custody_${this.item.evidenceTag}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.downloadingReport = false;
        alert('Failed to generate PDF Chain of Custody Report');
      }
    });
  }
}
