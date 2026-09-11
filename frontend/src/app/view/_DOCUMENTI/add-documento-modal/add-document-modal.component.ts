import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EvidenceService } from '../../../service/evidence/evidence.service';
import { CasesService } from '../../../service/cases/cases.service';
import { Case } from '../../../model/Case';
import { EvidenceItem } from '../../../model/EvidenceItem';

@Component({
  selector: 'app-add-document-modal',
  templateUrl: './add-document-modal.component.html',
  styleUrls: ['./add-document-modal.component.css']
})
export class AddDocumentModalComponent implements OnInit {
  @Input() item?: EvidenceItem;
  @Input() defaultCaseId?: number;
  @Output() refreshData: EventEmitter<void> = new EventEmitter<void>();

  evidenceForm!: FormGroup;
  cases: Case[] = [];
  selectedFile?: File;
  loading: boolean = false;
  errorMessage: string = '';

  constructor(
    public activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private evidenceService: EvidenceService,
    private casesService: CasesService
  ) {}

  ngOnInit(): void {
    const nextYear = new Date();
    nextYear.setFullYear(nextYear.getFullYear() + 3);
    const defaultRetention = nextYear.toISOString().substring(0, 10);

    this.evidenceForm = this.fb.group({
      caseId: [this.defaultCaseId || '', Validators.required],
      evidenceTag: ['', [Validators.required, Validators.pattern(/^[A-Za-z0-9\-_]+$/)]],
      description: ['', [Validators.required, Validators.minLength(5)]],
      retentionDate: [defaultRetention, Validators.required]
    });

    this.loadCases();
  }

  loadCases(): void {
    this.casesService.getCases().subscribe({
      next: (cases) => {
        this.cases = cases;
        if (!this.evidenceForm.value.caseId && cases.length > 0) {
          this.evidenceForm.patchValue({ caseId: cases[0].id });
        }
      },
      error: (err) => console.error('Error fetching cases', err)
    });
  }

  onFileSelected(event: any): void {
    if (event.target.files && event.target.files.length > 0) {
      this.selectedFile = event.target.files[0];
      // Generate a suggested tag if empty
      if (!this.evidenceForm.value.evidenceTag) {
        const rand = Math.floor(100 + Math.random() * 900);
        this.evidenceForm.patchValue({ evidenceTag: `EVD-${new Date().getFullYear()}-${rand}` });
      }
    }
  }

  submit(): void {
    if (this.evidenceForm.invalid || !this.selectedFile) {
      this.errorMessage = 'Please complete all required fields and select an evidence file.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const { caseId, evidenceTag, description, retentionDate } = this.evidenceForm.value;

    this.evidenceService.uploadEvidence(
      this.selectedFile,
      Number(caseId),
      evidenceTag,
      description,
      retentionDate
    ).subscribe({
      next: () => {
        this.loading = false;
        this.refreshData.emit();
        this.activeModal.close('saved');
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || 'Failed to ingest evidence item. Verify permissions and file integrity.';
      }
    });
  }
}
