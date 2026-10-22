import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AddDocumentModalComponent } from '../add-documento-modal/add-document-modal.component';
import { AcceptTransferModalComponent } from '../accept-transfer-modal/accept-transfer-modal.component';
import { OfflineQueueModalComponent } from '../offline-queue-modal/offline-queue-modal.component';
import { CourtReviewModalComponent } from '../court-review-modal/court-review-modal.component';
import { EvidenceService } from '../../../service/evidence/evidence.service';
import { TransfersService } from '../../../service/transfers/transfers.service';
import { AuthService } from '../../../service/auth/auth.service';
import { OfflineSyncService } from '../../../service/offline-sync/offline-sync.service';
import { EvidenceItem } from '../../../model/EvidenceItem';
import { CustodyTransferRequest } from '../../../model/CustodyTransferRequest';

import { ReviewRemovalModalComponent } from '../review-removal-modal/review-removal-modal.component';

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
  pendingRemovals: EvidenceItem[] = [];
  loading: boolean = false;

  totalCount: number = 0;
  legalHoldCount: number = 0;
  compromisedCount: number = 0;
  pendingRemovalsCount: number = 0;
  removedCount: number = 0;
  showRemoved: boolean = false;
  isOnline: boolean = true;
  offlineCount: number = 0;

  get baseItems(): EvidenceItem[] {
    return this.showRemoved ? this.evidenceItems : this.evidenceItems.filter(i => i.status !== 'REMOVED');
  }

  get displayedItems(): EvidenceItem[] {
    return this.filterText ? this.filteredItems : this.baseItems;
  }

  applyFilter(text: string): void {
    const q = text.toLowerCase().trim();
    if (!q) {
      this.filteredItems = [];
      return;
    }
    this.filteredItems = this.baseItems.filter(item =>
      (item.evidenceTag || '').toLowerCase().includes(q) ||
      (item.description || '').toLowerCase().includes(q) ||
      (item.caseNumber || '').toLowerCase().includes(q) ||
      (item.currentCustodianId || '').toLowerCase().includes(q) ||
      (item.status || '').toLowerCase().includes(q) ||
      (item.removalReason || '').toLowerCase().includes(q)
    );
  }

  toggleShowRemoved(): void {
    this.showRemoved = !this.showRemoved;
    if (this.filterText) {
      this.applyFilter(this.filterText);
    }
  }

  clearFilter(): void {
    this.filterText = '';
    this.filteredItems = [];
  }

  constructor(
    private modalService: NgbModal,
    private evidenceService: EvidenceService,
    private transfersService: TransfersService,
    public auth: AuthService,
    public offlineService: OfflineSyncService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.isOnline = this.offlineService.isOnline;
    this.offlineCount = this.offlineService.getPendingCount();

    this.offlineService.isOnline$.subscribe(online => {
      this.isOnline = online;
    });

    this.offlineService.queue$.subscribe(items => {
      this.offlineCount = items.filter(i => !i.synced).length;
    });
  }

  loadData(): void {
    this.loading = true;
    this.evidenceService.getEvidence().subscribe({
      next: (items) => {
        this.evidenceItems = items;
        this.totalCount = items.length;
        this.legalHoldCount = items.filter(i => i.legalHold).length;
        this.compromisedCount = items.filter(i => i.integrityStatus === 'COMPROMISED').length;
        this.removedCount = items.filter(i => i.status === 'REMOVED').length;
        this.pendingRemovals = items.filter(i => i.status === 'REMOVAL_REQUESTED');
        this.pendingRemovalsCount = this.pendingRemovals.length;
        if (this.filterText) {
          this.applyFilter(this.filterText);
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });

    this.loadPendingTransfers();
  }

  openReviewRemovalModal(item: EvidenceItem): void {
    const modalRef = this.modalService.open(ReviewRemovalModalComponent, {
      size: 'lg',
      backdrop: 'static'
    });
    modalRef.componentInstance.item = item;
    modalRef.componentInstance.refreshData.subscribe(() => {
      this.loadData();
    });
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

  openOfflineQueueModal(): void {
    const modalRef = this.modalService.open(OfflineQueueModalComponent, {
      size: 'lg',
      backdrop: 'static'
    });
    modalRef.result.then((synced) => {
      if (synced) {
        this.loadData();
      }
    }, () => {});
  }

  openCourtReviewMode(item?: EvidenceItem): void {
    const targetItem = item || (this.evidenceItems.length > 0 ? this.evidenceItems[0] : null);
    const modalRef = this.modalService.open(CourtReviewModalComponent, {
      size: 'lg',
      backdrop: 'static'
    });
    modalRef.componentInstance.evidenceItem = targetItem;
  }
}
