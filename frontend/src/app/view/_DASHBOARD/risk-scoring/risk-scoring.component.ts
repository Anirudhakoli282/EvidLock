import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { EvidenceItem } from '../../../model/EvidenceItem';
import { CustodyEvent } from '../../../model/CustodyEvent';

export interface RiskScore {
  item: EvidenceItem;
  score: number;           // 0-100
  level: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  factors: RiskFactor[];
}

export interface RiskFactor {
  label: string;
  points: number;
  description: string;
  icon: string;
}

@Component({
  selector: 'app-risk-scoring',
  templateUrl: './risk-scoring.component.html',
  styleUrls: ['./risk-scoring.component.css']
})
export class RiskScoringComponent implements OnInit, OnChanges {

  @Input() evidenceItems: EvidenceItem[] = [];
  @Input() recentEvents: CustodyEvent[] = [];

  riskScores: RiskScore[] = [];
  showAll: boolean = false;

  // How many items to show by default
  displayCount: number = 5;

  get displayedScores(): RiskScore[] {
    return this.showAll ? this.riskScores : this.riskScores.slice(0, this.displayCount);
  }

  get criticalCount(): number {
    return this.riskScores.filter(r => r.level === 'CRITICAL').length;
  }
  get highCount(): number {
    return this.riskScores.filter(r => r.level === 'HIGH').length;
  }
  get mediumCount(): number {
    return this.riskScores.filter(r => r.level === 'MEDIUM').length;
  }
  get lowCount(): number {
    return this.riskScores.filter(r => r.level === 'LOW').length;
  }

  get averageScore(): number {
    if (this.riskScores.length === 0) return 0;
    return Math.round(this.riskScores.reduce((sum, r) => sum + r.score, 0) / this.riskScores.length);
  }

  ngOnInit(): void {
    this.computeRiskScores();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['evidenceItems'] || changes['recentEvents']) {
      this.computeRiskScores();
    }
  }

  /**
   * Core AI Risk Scoring Engine
   * Analyzes multiple dimensions of each evidence item to produce a 0-100 risk score.
   * Factors considered:
   *   1. Retention deadline proximity (higher risk = closer to expiry)
   *   2. Integrity status (COMPROMISED = max risk)
   *   3. Legal hold missing on old items (items > 180 days without hold)
   *   4. Suspicious transfer patterns (> 3 transfers in recent events)
   *   5. Inactivity (no recent event logged)
   *   6. Status (Released or Disposed items still in vault)
   */
  computeRiskScores(): void {
    if (!this.evidenceItems || this.evidenceItems.length === 0) {
      this.riskScores = [];
      return;
    }

    const today = new Date();

    // Build transfer frequency map from recent events
    const transferCountMap: Map<number, number> = new Map();
    const lastEventMap: Map<number, Date> = new Map();

    for (const event of this.recentEvents) {
      if (event.action === 'TRANSFERRED') {
        const count = transferCountMap.get(event.evidenceItemId) || 0;
        transferCountMap.set(event.evidenceItemId, count + 1);
      }
      const existing = lastEventMap.get(event.evidenceItemId);
      const evDate = new Date(event.timestamp);
      if (!existing || evDate > existing) {
        lastEventMap.set(event.evidenceItemId, evDate);
      }
    }

    this.riskScores = this.evidenceItems.map(item => {
      const factors: RiskFactor[] = [];
      let score = 0;

      // ── Factor 1: Integrity Status ──────────────────────────────────────
      if (item.integrityStatus === 'COMPROMISED') {
        factors.push({
          label: 'INTEGRITY COMPROMISED',
          points: 40,
          description: 'SHA-256 hash mismatch detected. File has been tampered.',
          icon: 'bi-shield-slash-fill text-danger'
        });
        score += 40;
      }

      // ── Factor 2: Retention Deadline Proximity ──────────────────────────
      if (item.retentionDate) {
        const retention = new Date(item.retentionDate);
        const daysLeft = Math.floor((retention.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

        if (daysLeft < 0) {
          factors.push({
            label: 'RETENTION OVERDUE',
            points: 30,
            description: `Retention expired ${Math.abs(daysLeft)} day(s) ago.`,
            icon: 'bi-calendar-x-fill text-danger'
          });
          score += 30;
        } else if (daysLeft <= 7) {
          factors.push({
            label: 'RETENTION CRITICAL (≤7d)',
            points: 25,
            description: `Only ${daysLeft} day(s) until retention expiry.`,
            icon: 'bi-calendar-exclamation-fill text-danger'
          });
          score += 25;
        } else if (daysLeft <= 30) {
          factors.push({
            label: 'RETENTION EXPIRING SOON (≤30d)',
            points: 15,
            description: `${daysLeft} days until retention expiry.`,
            icon: 'bi-calendar-event text-warning'
          });
          score += 15;
        }
      } else {
        factors.push({
          label: 'NO RETENTION DATE SET',
          points: 10,
          description: 'No statutory retention deadline has been recorded.',
          icon: 'bi-calendar2-x text-warning'
        });
        score += 10;
      }

      // ── Factor 3: Status Anomaly (released/disposed items still visible) ──
      if (item.status === 'Released' || item.status === 'Disposed') {
        factors.push({
          label: `STATUS: ${item.status.toUpperCase()}`,
          points: 15,
          description: `Evidence is marked "${item.status}" but remains in the active vault.`,
          icon: 'bi-exclamation-triangle-fill text-warning'
        });
        score += 15;
      }

      // ── Factor 4: Suspicious Transfer Pattern ────────────────────────────
      const transferCount = transferCountMap.get(item.id || 0) || 0;
      if (transferCount >= 4) {
        factors.push({
          label: `HIGH TRANSFER FREQUENCY (${transferCount}×)`,
          points: 20,
          description: `${transferCount} custody transfers detected — unusually high churn.`,
          icon: 'bi-arrow-left-right text-danger'
        });
        score += 20;
      } else if (transferCount >= 2) {
        factors.push({
          label: `MULTIPLE TRANSFERS (${transferCount}×)`,
          points: 8,
          description: `${transferCount} custody transfers logged.`,
          icon: 'bi-arrow-left-right text-warning'
        });
        score += 8;
      }

      // ── Factor 5: Inactivity (no event in last 90 days) ─────────────────
      const lastEvent = lastEventMap.get(item.id || 0);
      const uploadDate = item.uploadedAt ? new Date(item.uploadedAt) : null;
      const referenceDate = lastEvent || uploadDate;

      if (referenceDate) {
        const daysSinceActivity = Math.floor((today.getTime() - referenceDate.getTime()) / (1000 * 60 * 60 * 24));
        if (daysSinceActivity > 180) {
          factors.push({
            label: `STALE EVIDENCE (${daysSinceActivity}d inactive)`,
            points: 10,
            description: `No activity recorded for ${daysSinceActivity} days.`,
            icon: 'bi-hourglass-bottom text-secondary'
          });
          score += 10;
        } else if (daysSinceActivity > 90) {
          factors.push({
            label: `LOW ACTIVITY (${daysSinceActivity}d)`,
            points: 5,
            description: `No activity in ${daysSinceActivity} days.`,
            icon: 'bi-hourglass-split text-secondary'
          });
          score += 5;
        }
      }

      // ── Factor 6: No Legal Hold on old item ─────────────────────────────
      if (!item.legalHold && uploadDate) {
        const ageInDays = Math.floor((today.getTime() - uploadDate.getTime()) / (1000 * 60 * 60 * 24));
        if (ageInDays > 180 && item.status !== 'Disposed') {
          factors.push({
            label: 'NO LEGAL HOLD (OLD ITEM)',
            points: 5,
            description: `Item is ${ageInDays} days old with no legal hold placed.`,
            icon: 'bi-unlock text-muted'
          });
          score += 5;
        }
      }

      // Cap score at 100
      score = Math.min(score, 100);

      // Determine risk level
      let level: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
      if (score >= 60) level = 'CRITICAL';
      else if (score >= 35) level = 'HIGH';
      else if (score >= 15) level = 'MEDIUM';
      else level = 'LOW';

      // If no risk factors found, add a default "No issues" factor
      if (factors.length === 0) {
        factors.push({
          label: 'ALL CHECKS PASSED',
          points: 0,
          description: 'No risk indicators detected for this evidence item.',
          icon: 'bi-check-circle-fill text-success'
        });
      }

      return { item, score, level, factors };
    });

    // Sort by score descending (highest risk first)
    this.riskScores.sort((a, b) => b.score - a.score);
  }

  getLevelBadgeClass(level: string): string {
    switch (level) {
      case 'CRITICAL': return 'badge-risk-critical';
      case 'HIGH': return 'badge-risk-high';
      case 'MEDIUM': return 'badge-risk-medium';
      case 'LOW': return 'badge-risk-low';
      default: return 'badge-risk-low';
    }
  }

  getScoreBarClass(score: number): string {
    if (score >= 60) return 'bg-danger';
    if (score >= 35) return 'bg-warning';
    if (score >= 15) return 'bg-info';
    return 'bg-success';
  }

  getLevelIcon(level: string): string {
    switch (level) {
      case 'CRITICAL': return 'bi-exclamation-octagon-fill';
      case 'HIGH': return 'bi-exclamation-triangle-fill';
      case 'MEDIUM': return 'bi-exclamation-circle-fill';
      case 'LOW': return 'bi-check-circle-fill';
      default: return 'bi-check-circle-fill';
    }
  }
}
