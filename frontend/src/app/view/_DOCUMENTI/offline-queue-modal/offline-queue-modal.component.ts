import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { OfflineSyncService, OfflineEvidenceItem } from '../../../service/offline-sync/offline-sync.service';

@Component({
  selector: 'app-offline-queue-modal',
  templateUrl: './offline-queue-modal.component.html',
  styleUrls: []
})
export class OfflineQueueModalComponent implements OnInit {
  items: OfflineEvidenceItem[] = [];
  isOnline: boolean = true;
  syncing: boolean = false;
  syncSuccess: boolean = false;

  constructor(
    public activeModal: NgbActiveModal,
    private offlineService: OfflineSyncService
  ) {}

  ngOnInit(): void {
    this.items = this.offlineService.loadQueueFromStorage();
    this.isOnline = this.offlineService.isOnline;
    this.offlineService.isOnline$.subscribe(online => {
      this.isOnline = online;
    });
  }

  syncAllNow(): void {
    this.syncing = true;
    setTimeout(() => {
      // Simulate pushing queued records to server
      this.offlineService.clearQueue();
      this.items = [];
      this.syncing = false;
      this.syncSuccess = true;
      setTimeout(() => {
        this.activeModal.close(true);
      }, 1200);
    }, 1500);
  }

  removeItem(id: string): void {
    this.offlineService.removeQueueItem(id);
    this.items = this.offlineService.loadQueueFromStorage();
  }
}
