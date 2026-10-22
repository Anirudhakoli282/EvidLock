import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EvidenceService } from '../../../service/evidence/evidence.service';
import { EvidenceItem } from '../../../model/EvidenceItem';

@Component({
  selector: 'app-review-removal-modal',
  templateUrl: './review-removal-modal.component.html',
  styleUrls: ['./review-removal-modal.component.css']
})
export class ReviewRemovalModalComponent implements OnInit {
  @Input() item!: EvidenceItem;
  @Output() refreshData: EventEmitter<void> = new EventEmitter<void>();

  reviewForm!: FormGroup;
  loading: boolean = false;
  actionType: 'approve' | 'reject' | null = null;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    public activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private evidenceService: EvidenceService
  ) {}

  ngOnInit(): void {
    this.reviewForm = this.fb.group({
      legalRef: [this.item.legalRef || '', [Validators.required, Validators.minLength(3)]],
      reason: ['', [Validators.required, Validators.minLength(5)]],
      password: ['', [Validators.required]]
    });
  }

  approveDisposal(): void {
    if (this.reviewForm.invalid || !this.item.id) return;

    this.actionType = 'approve';
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const val = this.reviewForm.value;

    this.evidenceService.approveRemoval(
      this.item.id,
      val.legalRef.trim(),
      val.password
    ).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Evidence removal APPROVED. Exhibit marked REMOVED, binary payload archived/purged, and tombstone ledger record sealed.';
        this.refreshData.emit();
        setTimeout(() => {
          this.activeModal.close('approved');
        }, 1500);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || err.message || 'Failed to approve evidence removal.';
      }
    });
  }

  rejectDisposal(): void {
    const pwd = this.reviewForm.get('password')?.value;
    const reason = this.reviewForm.get('reason')?.value;

    if (!pwd) {
      this.errorMessage = 'Password re-entry is required to sign removal rejection.';
      return;
    }
    if (!reason || reason.trim().length < 5) {
      this.errorMessage = 'Please provide a reason for rejecting the removal request.';
      return;
    }
    if (!this.item.id) return;

    this.actionType = 'reject';
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.evidenceService.rejectRemoval(
      this.item.id,
      reason.trim(),
      pwd
    ).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Removal request REJECTED. Evidence restored to ACTIVE status.';
        this.refreshData.emit();
        setTimeout(() => {
          this.activeModal.close('rejected');
        }, 1500);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || err.message || 'Failed to reject evidence removal.';
      }
    });
  }
}
