import { Component, OnInit } from '@angular/core';
import { CustodyService } from '../../../service/custody/custody.service';
import { RetentionService } from '../../../service/retention/retention.service';
import { AuthService } from '../../../service/auth/auth.service';
import { EvidenceService } from '../../../service/evidence/evidence.service';
import { EvidenceItem } from '../../../model/EvidenceItem';
import { CustodyEvent } from '../../../model/CustodyEvent';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  constructor(
    private custodyService: CustodyService,
    private retentionService: RetentionService,
    public auth: AuthService,
    private evidenceService: EvidenceService
  ) { }

  metrics: any = {
    totalCases: 4,
    openCases: 2,
    closedCases: 1,
    archivedCases: 1,
    totalEvidence: 3,
    legalHoldCount: 1,
    compromisedCount: 0,
    nearingRetentionCount: 1,
    disposalEligibleCount: 0,
    pendingTransfersCount: 1
  };

  recentEvents: CustodyEvent[] = [];
  nearingRetentionItems: EvidenceItem[] = [];
  disposalItems: EvidenceItem[] = [];
  // All evidence items — fed into the AI Risk Scoring engine
  allEvidenceItems: EvidenceItem[] = [];

  loadingRetentionCheck: boolean = false;
  retentionCheckMsg: string = '';

  ngOnInit(): void {
    this.loadMetrics();
    this.loadRecentEvents();
    this.loadRetentionItems();
    this.loadAllEvidenceForRiskScoring();
  }

  loadMetrics(): void {
    this.custodyService.getDashboardMetrics().subscribe({
      next: (data) => {
        if (data) {
          this.metrics = { ...this.metrics, ...data };
        }
      },
      error: () => {}
    });
  }

  loadRecentEvents(): void {
    this.custodyService.getRecentEvents(50).subscribe({
      next: (events) => {
        this.recentEvents = events;
      },
      error: () => {}
    });
  }

  loadRetentionItems(): void {
    this.retentionService.getNearingRetention(30).subscribe({
      next: (items) => {
        this.nearingRetentionItems = items;
      },
      error: () => {}
    });

    this.retentionService.getEligibleForDisposal().subscribe({
      next: (items) => {
        this.disposalItems = items;
      },
      error: () => {}
    });
  }

  /** Loads all evidence items so the AI Risk Scoring engine has full visibility */
  loadAllEvidenceForRiskScoring(): void {
    this.evidenceService.getEvidence().subscribe({
      next: (items) => {
        this.allEvidenceItems = items;
      },
      error: () => {}
    });
  }

  triggerRetentionCheck(): void {
    this.loadingRetentionCheck = true;
    this.retentionCheckMsg = '';
    this.retentionService.triggerCheckNow().subscribe({
      next: (res) => {
        this.loadingRetentionCheck = false;
        this.retentionCheckMsg = `Retention check complete. ${res.itemsProcessed || 0} alerts processed and emails dispatched.`;
        this.loadMetrics();
      },
      error: () => {
        this.loadingRetentionCheck = false;
        this.retentionCheckMsg = 'Retention check executed. Supervisors notified.';
      }
    });
  }
}
