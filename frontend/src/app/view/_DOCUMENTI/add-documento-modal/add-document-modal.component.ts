import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EvidenceService } from '../../../service/evidence/evidence.service';
import { CasesService } from '../../../service/cases/cases.service';
import { AiRedactorService, DocumentClassification } from '../../../service/ai-redactor/ai-redactor.service';
import { OfflineSyncService } from '../../../service/offline-sync/offline-sync.service';
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

  aiClassification?: DocumentClassification;
  piiRedactedMessage: string = '';
  isOfflineMode: boolean = false;

  constructor(
    public activeModal: NgbActiveModal,
    private fb: FormBuilder,
    private evidenceService: EvidenceService,
    private casesService: CasesService,
    private aiRedactor: AiRedactorService,
    public offlineService: OfflineSyncService
  ) {}

  ngOnInit(): void {
    this.isOfflineMode = !this.offlineService.isOnline;

    const nextYear = new Date();
    nextYear.setFullYear(nextYear.getFullYear() + 3);
    const defaultRetention = nextYear.toISOString().substring(0, 10);

    this.evidenceForm = this.fb.group({
      caseId: [this.defaultCaseId || '', Validators.required],
      category: ['FIR', Validators.required],
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
      const fileName = this.selectedFile?.name || '';

      // Run AI Classification
      this.aiClassification = this.aiRedactor.classifyDocument(fileName, this.evidenceForm.value.description);
      this.evidenceForm.patchValue({ category: this.aiClassification.category });

      // Generate suggested tag
      const rand = Math.floor(100 + Math.random() * 900);
      const tagPrefix = this.aiClassification.recommendedTagPrefix || 'EVD';
      this.evidenceForm.patchValue({ evidenceTag: `${tagPrefix}-${new Date().getFullYear()}-${rand}` });

      // Auto-set retention based on classification
      const retentionYear = new Date();
      retentionYear.setFullYear(retentionYear.getFullYear() + (this.aiClassification.suggestedRetentionYears || 5));
      this.evidenceForm.patchValue({ retentionDate: retentionYear.toISOString().substring(0, 10) });
    }
  }

  triggerAiPiiRedaction(): void {
    const currentDesc = this.evidenceForm.value.description;
    if (!currentDesc) return;

    const result = this.aiRedactor.redactSensitivePii(currentDesc);
    if (result.piiCount > 0) {
      this.evidenceForm.patchValue({ description: result.redactedText });
      this.piiRedactedMessage = `AI Auto-Redacted ${result.piiCount} sensitive personal tokens (Aadhaar / PAN / Phone / Email).`;
    } else {
      this.piiRedactedMessage = 'AI Scan complete: No sensitive personal identifiers detected in description.';
    }
  }

  submit(): void {
    if (this.evidenceForm.invalid || !this.selectedFile) {
      this.errorMessage = 'Please complete all required fields and select an evidence file.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const { caseId, evidenceTag, description, retentionDate, category } = this.evidenceForm.value;

    // Check if offline: if offline, queue locally!
    if (!this.offlineService.isOnline || this.isOfflineMode) {
      const selectedCase = this.cases.find(c => c.id === Number(caseId));
      this.offlineService.queueEvidence({
        caseId: Number(caseId),
        caseNumber: selectedCase?.caseNumber || ('Case #' + caseId),
        evidenceTag,
        description: `[${category}] ${description}`,
        retentionDate,
        fileName: this.selectedFile.name,
        fileSize: this.selectedFile.size,
        fileType: this.selectedFile.type || 'application/octet-stream',
        simulatedHash: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
        officerEmail: 'officer@evidlock.gov',
        classificationCategory: category,
        redactedPii: this.piiRedactedMessage.length > 0
      });

      this.loading = false;
      this.refreshData.emit();
      this.activeModal.close('queued_offline');
      return;
    }

    // Online submission:
    this.evidenceService.uploadEvidence(
      this.selectedFile,
      Number(caseId),
      evidenceTag,
      `[${category}] ${description}`,
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
