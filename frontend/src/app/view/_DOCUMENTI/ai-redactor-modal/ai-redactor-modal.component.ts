import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { AiRedactorService, PiiDetectionResult, DocumentClassification } from '../../../service/ai-redactor/ai-redactor.service';

@Component({
  selector: 'app-ai-redactor-modal',
  templateUrl: './ai-redactor-modal.component.html',
  styleUrls: []
})
export class AiRedactorModalComponent implements OnInit {
  @Input() evidenceItem: any;

  classification!: DocumentClassification;
  piiResult!: PiiDetectionResult;
  viewMode: 'ORIGINAL' | 'REDACTED' = 'REDACTED';
  analyzing: boolean = true;
  applied: boolean = false;

  constructor(
    public activeModal: NgbActiveModal,
    private aiRedactor: AiRedactorService
  ) {}

  ngOnInit(): void {
    this.runAnalysis();
  }

  runAnalysis(): void {
    this.analyzing = true;
    setTimeout(() => {
      const fileName = this.evidenceItem?.originalFilename || this.evidenceItem?.evidenceTag || 'evidence.raw';
      const textToAnalyze = this.evidenceItem?.description || 'Digital evidence item ingested into EvidLock vault.';

      this.classification = this.aiRedactor.classifyDocument(fileName, textToAnalyze);
      this.piiResult = this.aiRedactor.redactSensitivePii(textToAnalyze);
      this.analyzing = false;
    }, 600);
  }

  applyRedaction(): void {
    this.applied = true;
    setTimeout(() => {
      this.activeModal.close({
        classification: this.classification,
        redactedText: this.piiResult.redactedText,
        piiCount: this.piiResult.piiCount
      });
    }, 400);
  }
}
