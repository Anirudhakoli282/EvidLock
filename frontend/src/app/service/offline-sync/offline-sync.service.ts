import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface OfflineEvidenceItem {
  id: string; // temporary local client UUID
  caseId: number;
  caseNumber: string;
  evidenceTag: string;
  description: string;
  retentionDate: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  simulatedHash: string;
  capturedAt: string;
  officerEmail: string;
  synced: boolean;
  classificationCategory?: string;
  redactedPii?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class OfflineSyncService {
  private readonly STORAGE_KEY = 'evidlock_offline_field_queue';

  private isOnlineSubject = new BehaviorSubject<boolean>(typeof navigator !== 'undefined' ? navigator.onLine : true);
  public isOnline$: Observable<boolean> = this.isOnlineSubject.asObservable();

  private queueSubject = new BehaviorSubject<OfflineEvidenceItem[]>([]);
  public queue$: Observable<OfflineEvidenceItem[]> = this.queueSubject.asObservable();

  constructor() {
    if (typeof window !== 'undefined') {
      window.addEventListener('online', () => this.handleNetworkChange(true));
      window.addEventListener('offline', () => this.handleNetworkChange(false));
      this.loadQueueFromStorage();
    }
  }

  public get isOnline(): boolean {
    return this.isOnlineSubject.value;
  }

  private handleNetworkChange(online: boolean) {
    this.isOnlineSubject.next(online);
    if (online) {
      console.log('[OfflineSyncService] Connection restored. Evaluating offline queue for synchronization...');
    } else {
      console.warn('[OfflineSyncService] Network lost. Switching to remote offline crime scene ingestion mode.');
    }
  }

  public loadQueueFromStorage(): OfflineEvidenceItem[] {
    try {
      const data = localStorage.getItem(this.STORAGE_KEY);
      const items: OfflineEvidenceItem[] = data ? JSON.parse(data) : [];
      this.queueSubject.next(items);
      return items;
    } catch (e) {
      console.error('[OfflineSyncService] Error reading offline queue from localStorage', e);
      return [];
    }
  }

  public queueEvidence(item: Omit<OfflineEvidenceItem, 'id' | 'capturedAt' | 'synced'>): OfflineEvidenceItem {
    const queue = this.loadQueueFromStorage();
    const newItem: OfflineEvidenceItem = {
      ...item,
      id: 'OFFLINE-' + Date.now().toString(36).toUpperCase() + '-' + Math.random().toString(36).substring(2, 6).toUpperCase(),
      capturedAt: new Date().toISOString(),
      synced: false
    };

    queue.unshift(newItem);
    this.saveQueue(queue);
    return newItem;
  }

  public removeQueueItem(id: string): void {
    const queue = this.loadQueueFromStorage().filter(item => item.id !== id);
    this.saveQueue(queue);
  }

  public clearQueue(): void {
    this.saveQueue([]);
  }

  public getPendingCount(): number {
    return this.queueSubject.value.filter(i => !i.synced).length;
  }

  private saveQueue(queue: OfflineEvidenceItem[]): void {
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(queue));
      this.queueSubject.next(queue);
    } catch (e) {
      console.error('[OfflineSyncService] Error saving offline queue', e);
    }
  }
}
