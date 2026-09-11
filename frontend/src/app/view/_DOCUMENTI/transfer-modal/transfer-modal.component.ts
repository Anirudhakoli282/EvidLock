import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TransfersService } from '../../../service/transfers/transfers.service';
import { EvidenceItem } from '../../../model/EvidenceItem';

@Component({
  selector: 'app-transfer-modal',
  templateUrl: './transfer-modal.component.html',
  styleUrls: ['./transfer-modal.component.css']
})
export class TransferModalComponent implements OnInit {
  @Input() item!: EvidenceItem;
  @Output() refreshData: EventEmitter<void> = new EventEmitter<void>();

  transferForm!: FormGroup;
  loading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  commonReasons: string[] = [
    'Forensic lab examination and acquisition',
    'Courtroom evidence exhibit presentation',
    'Off-site secure vault archiving',
    'Prosecutor case review',
    'Agency transfer under subpoena'
  ];

  constructor(
    public activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private transfersService: TransfersService
  ) {}

  ngOnInit(): void {
    this.transferForm = this.fb.group({
      toCustodianId: ['', [Validators.required, Validators.email]],
      reason: ['', [Validators.required, Validators.minLength(5)]],
      password: ['', [Validators.required]]
    });
  }

  setReason(reason: string): void {
    this.transferForm.patchValue({ reason });
  }

  submitTransfer(): void {
    if (this.transferForm.invalid || !this.item.id) return;

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const dto = {
      evidenceItemId: this.item.id,
      toCustodianId: this.transferForm.value.toCustodianId,
      reason: this.transferForm.value.reason,
      password: this.transferForm.value.password
    };

    this.transfersService.initiateTransfer(dto).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Transfer request initiated successfully. Status set to PENDING_ACCEPTANCE.';
        this.refreshData.emit();
        setTimeout(() => {
          this.activeModal.close('success');
        }, 1200);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || 'Failed to initiate transfer. Verify step-up password and permissions.';
      }
    });
  }
}
