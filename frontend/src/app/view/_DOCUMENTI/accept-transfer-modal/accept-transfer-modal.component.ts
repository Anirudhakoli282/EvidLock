import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TransfersService } from '../../../service/transfers/transfers.service';
import { CustodyTransferRequest } from '../../../model/CustodyTransferRequest';

@Component({
  selector: 'app-accept-transfer-modal',
  templateUrl: './accept-transfer-modal.component.html',
  styleUrls: ['./accept-transfer-modal.component.css']
})
export class AcceptTransferModalComponent implements OnInit {
  @Input() request!: CustodyTransferRequest;
  @Output() refreshData: EventEmitter<void> = new EventEmitter<void>();

  acceptForm!: FormGroup;
  loading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  constructor(
    public activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private transfersService: TransfersService
  ) {}

  ngOnInit(): void {
    this.acceptForm = this.fb.group({
      password: ['', [Validators.required]]
    });
  }

  acceptTransfer(): void {
    if (this.acceptForm.invalid || !this.request.id) return;

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const dto = {
      requestId: this.request.id,
      password: this.acceptForm.value.password
    };

    this.transfersService.acceptTransfer(dto).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Custody successfully accepted! Cryptographic ledger updated with new custodian block.';
        this.refreshData.emit();
        setTimeout(() => {
          this.activeModal.close('accepted');
        }, 1200);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || 'Failed to accept transfer. Please verify your password.';
      }
    });
  }

  rejectTransfer(): void {
    if (!this.request.id) return;
    const reason = prompt('Enter reason for rejecting custody transfer:');
    if (reason === null) return; // cancelled prompt

    this.loading = true;
    this.errorMessage = '';

    this.transfersService.rejectTransfer(this.request.id, reason).subscribe({
      next: () => {
        this.loading = false;
        this.refreshData.emit();
        this.activeModal.close('rejected');
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || 'Failed to reject transfer.';
      }
    });
  }
}
