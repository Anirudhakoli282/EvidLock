import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AddDocumentModalComponent } from '../add-documento-modal/add-document-modal.component';
import { AcceptTransferModalComponent } from '../accept-transfer-modal/accept-transfer-modal.component';
import { EvidenceService } from '../../../service/evidence/evidence.service';
import { TransfersService } from '../../../service/transfers/transfers.service';
import { AuthService } from '../../../service/auth/auth.service';
import { EvidenceItem } from '../../../model/EvidenceItem';
import { CustodyTransferRequest } from '../../../model/CustodyTransferRequest';

@Component({
  selector: 'app-documenti',
  templateUrl: './documenti.component.html',
  styleUrls: ['./documenti.component.css']
})
export class DocumentiComponent implements OnInit {
  evidenceItems: EvidenceItem[] = [];
  filteredItems: EvidenceItem[] = [];
  filterText: string = '';
  pendingTransfers: CustodyTransferRequest[] = [];
  loading: boolean = false;

  totalCount: number = 0;
  legalHoldCount: number = 0;
  compromisedCount: number = 0;

  applyFilter(text: string): void {
    const q = text.toLowerCase().trim();
    this.filteredItems = this.evidenceItems.filter(item =>
      (item.evidenceTag || '').toLowerCase().includes(q) ||
      (item.description || '').toLowerCase().includes(q) ||
      (item.caseNumber || '').toLowerCase().includes(q) ||
      (item.currentCustodianId || '').toLowerCase().includes(q)
    );
  }

  clearFilter(): void {
    this.filterText = '';
    this.filteredItems = [];
  }

  constructor(
    private modalService: NgbModal,
    private evidenceService: EvidenceService,
    private transfersService: TransfersService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.evidenceService.getEvidence().subscribe({
      next: (items) => {
        this.evidenceItems = items;
        this.totalCount = items.length;
        this.legalHoldCount = items.filter(i => i.legalHold).length;
        this.compromisedCount = items.filter(i => i.integrityStatus === 'COMPROMISED').length;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });

    this.loadPendingTransfers();
  }

  loadPendingTransfers(): void {
    this.transfersService.getPendingTransfers().subscribe({
      next: (requests) => {
        this.pendingTransfers = requests.filter(r => r.status === 'PENDING');
      },
      error: () => {}
    });
  }

  openAddEvidenceModal(): void {
    const modalRef = this.modalService.open(AddDocumentModalComponent, {
      size: 'lg',
      backdrop: 'static'
    });

    modalRef.componentInstance.refreshData.subscribe(() => {
      this.loadData();
    });
  }

  openAcceptTransferModal(req: CustodyTransferRequest): void {
    const modalRef = this.modalService.open(AcceptTransferModalComponent, {
      size: 'md',
      backdrop: 'static'
    });
    modalRef.componentInstance.request = req;
    modalRef.componentInstance.refreshData.subscribe(() => {
      this.loadData();
    });
  }
}
