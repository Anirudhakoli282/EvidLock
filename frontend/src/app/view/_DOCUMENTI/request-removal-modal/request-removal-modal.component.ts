import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EvidenceService } from '../../../service/evidence/evidence.service';
import { EvidenceItem } from '../../../model/EvidenceItem';

@Component({
  selector: 'app-request-removal-modal',
  templateUrl: './request-removal-modal.component.html',
  styleUrls: ['./request-removal-modal.component.css']
})
export class RequestRemovalModalComponent implements OnInit {
  @Input() item!: EvidenceItem;
  @Output() refreshData: EventEmitter<void> = new EventEmitter<void>();

  removalForm!: FormGroup;
  loading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  isRetentionPassed: boolean = false;

  constructor(
    public activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private evidenceService: EvidenceService
  ) {}

  ngOnInit(): void {
    this.checkRetentionStatus();

    this.removalForm = this.fb.group({
      reason: ['', [Validators.required, Validators.minLength(20)]],
      legalRef: [''],
      password: ['', [Validators.required]]
    });
  }

  checkRetentionStatus(): void {
    if (!this.item.retentionDate) {
      this.isRetentionPassed = false;
      return;
    }
    const retDate = new Date(this.item.retentionDate);
    const now = new Date();
    this.isRetentionPassed = retDate <= now;
  }

  submitRequest(): void {
    if (this.removalForm.invalid || !this.item.id) return;

    const val = this.removalForm.value;

    if (!this.isRetentionPassed && (!val.legalRef || val.legalRef.trim().length === 0)) {
      this.errorMessage = 'Retention date has not passed yet. A mandatory Court Order or Case Closure Legal Reference is required.';
      return;
    }

    if (val.reason.trim().length < 20) {
      this.errorMessage = 'Removal reason must be at least 20 characters explaining justification.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.evidenceService.requestRemoval(
      this.item.id,
      val.reason.trim(),
      val.password,
      val.legalRef?.trim() || ''
    ).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Removal request submitted successfully. Status updated to REMOVAL_REQUESTED awaiting Court approval.';
        this.refreshData.emit();
        setTimeout(() => {
          this.activeModal.close('success');
        }, 1500);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || err.message || 'Failed to submit removal request.';
      }
    });
  }
}
