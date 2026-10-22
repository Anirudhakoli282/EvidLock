import { AfterViewInit, Component, ViewChild, Input, ChangeDetectorRef, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthService } from '../../../service/auth/auth.service';
import { EvidenceService } from '../../../service/evidence/evidence.service';
import { ReportService } from '../../../service/report/report.service';
import { EvidenceItem } from '../../../model/EvidenceItem';
import { CustodyLedgerModalComponent } from '../custody-ledger-modal/custody-ledger-modal.component';
import { TransferModalComponent } from '../transfer-modal/transfer-modal.component';
import { AiRedactorModalComponent } from '../ai-redactor-modal/ai-redactor-modal.component';
import { CourtReviewModalComponent } from '../court-review-modal/court-review-modal.component';
import { RequestRemovalModalComponent } from '../request-removal-modal/request-removal-modal.component';
import { ReviewRemovalModalComponent } from '../review-removal-modal/review-removal-modal.component';

@Component({
  selector: 'app-documenti-tabella',
  templateUrl: './documenti-tabella.component.html',
  styleUrls: ['./documenti-tabella.component.css']
})
export class DocumentiTabellaComponent implements AfterViewInit, OnChanges {
  displayedColumns: string[] = ['evidenceTag', 'caseNumber', 'description', 'currentCustodianId', 'fileHash', 'retentionDate', 'legalHold', 'integrityStatus', 'actions'];
  dataSource: MatTableDataSource<EvidenceItem>;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  @Input() evidenceItems: EvidenceItem[] = [];
  @Input() documenti: any[] = []; // Backward compatibility
  @Input() dipendente?: any;
  @Input() displayDipendente: boolean = false;
  @Output() refreshData: EventEmitter<void> = new EventEmitter<void>();

  integrityAlertMessage: string = '';

  constructor(
    private cdr: ChangeDetectorRef,
    public auth: AuthService,
    private modalService: NgbModal,
    private evidenceService: EvidenceService,
    private reportService: ReportService
  ) {
    this.dataSource = new MatTableDataSource<EvidenceItem>([]);
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.updateDataSource();
  }

  ngAfterViewInit(): void {
    this.updateDataSource();
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    this.cdr.detectChanges();
  }

  private updateDataSource(): void {
    let items = this.evidenceItems;
    if ((!items || items.length === 0) && this.documenti && this.documenti.length > 0) {
      items = this.documenti as EvidenceItem[];
    }
    this.dataSource.data = items || [];
  }

  openLedgerModal(item: EvidenceItem): void {
    const modalRef = this.modalService.open(CustodyLedgerModalComponent, {
      size: 'lg',
      backdrop: 'static'
    });
    modalRef.componentInstance.item = item;
  }

  openTransferModal(item: EvidenceItem): void {
    const modalRef = this.modalService.open(TransferModalComponent, {
      size: 'md',
      backdrop: 'static'
    });
    modalRef.componentInstance.item = item;
    modalRef.componentInstance.refreshData.subscribe(() => {
      this.refreshData.emit();
    });
  }

  toggleLegalHold(item: EvidenceItem): void {
    if (this.auth.isReadOnly()) return;
    if (!item.id) return;

    const newStatus = !item.legalHold;
    const confirmMsg = newStatus 
      ? `Place LEGAL HOLD on item ${item.evidenceTag}? (Prevents alteration, deletion, or premature disposal)`
      : `LIFT Legal Hold on item ${item.evidenceTag}?`;

    if (confirm(confirmMsg)) {
      this.evidenceService.toggleLegalHold(item.id, newStatus).subscribe({
        next: (updated) => {
          item.legalHold = updated.legalHold;
          this.refreshData.emit();
        },
        error: (err) => {
          alert(`Failed to update legal hold: ${err.error || err.message}`);
        }
      });
    }
  }

  downloadEvidenceFile(item: EvidenceItem): void {
    if (!item.filePath || !item.id) return;

    this.integrityAlertMessage = '';
    this.evidenceService.downloadFile(item.filePath, item.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const filename = (item.filePath || '').split('/').pop()?.split('\\').pop() || `${item.evidenceTag}.dat`;
        a.download = filename;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        if (err.status === 409) {
          this.integrityAlertMessage = `CRITICAL INTEGRITY FAILURE: SHA-256 hash mismatch for evidence ${item.evidenceTag}! Access blocked and item marked COMPROMISED.`;
          item.integrityStatus = 'COMPROMISED';
          this.cdr.detectChanges();
        } else {
          alert(`Download error: ${err.statusText || 'Unable to download file'}`);
        }
      }
    });
  }

  downloadPdfReport(item: EvidenceItem): void {
    if (!item.id) return;
    this.reportService.downloadChainOfCustodyReport(item.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Chain_of_Custody_${item.evidenceTag}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        alert('Failed to generate PDF Chain of Custody Report.');
      }
    });
  }

  openRequestRemovalModal(item: EvidenceItem): void {
    if (!this.auth.isSupervisor()) {
      alert('Unauthorized: Only supervisors may request evidence removal.');
      return;
    }
    if (item.legalHold) {
      alert(`LOCKED BY LEGAL HOLD: Item ${item.evidenceTag} is under active legal hold and cannot be removed.`);
      return;
    }
    if (item.integrityStatus === 'COMPROMISED') {
      alert(`COMPROMISED EVIDENCE: Item ${item.evidenceTag} failed cryptographic integrity. Tampered evidence must be preserved as proof and cannot be removed.`);
      return;
    }

    const modalRef = this.modalService.open(RequestRemovalModalComponent, {
      size: 'lg',
      backdrop: 'static'
    });
    modalRef.componentInstance.item = item;
    modalRef.componentInstance.refreshData.subscribe(() => {
      this.refreshData.emit();
    });
  }

  openReviewRemovalModal(item: EvidenceItem): void {
    const modalRef = this.modalService.open(ReviewRemovalModalComponent, {
      size: 'lg',
      backdrop: 'static'
    });
    modalRef.componentInstance.item = item;
    modalRef.componentInstance.refreshData.subscribe(() => {
      this.refreshData.emit();
    });
  }

  deleteEvidence(item: EvidenceItem): void {
    // Zero hard-delete: redirect to Request Removal
    this.openRequestRemovalModal(item);
  }

  openAiRedactor(item: EvidenceItem): void {
    const modalRef = this.modalService.open(AiRedactorModalComponent, {
      size: 'lg',
      backdrop: 'static'
    });
    modalRef.componentInstance.evidenceItem = item;
    modalRef.result.then((res) => {
      if (res && res.redactedText) {
        item.description = res.redactedText;
        this.cdr.detectChanges();
      }
    }, () => {});
  }

  openCourtReview(item: EvidenceItem): void {
    const modalRef = this.modalService.open(CourtReviewModalComponent, {
      size: 'lg',
      backdrop: 'static'
    });
    modalRef.componentInstance.evidenceItem = item;
  }

  simulateTamperAlert(item: EvidenceItem): void {
    this.integrityAlertMessage = `CRITICAL INTEGRITY FAILURE [SIMULATION / TEST]: Cryptographic hash mismatch on Exhibit ${item.evidenceTag}! Unauthorized bitstream modification detected in Evidence Vault. Incident logged in tamper ledger.`;
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.cdr.detectChanges();
  }
}
