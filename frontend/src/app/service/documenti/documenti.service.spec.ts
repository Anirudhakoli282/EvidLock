import { TestBed } from '@angular/core/testing';

import { DocumentiService } from './documenti.service';

describe('DocumentiService', () => {
  let service: DocumentiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DocumentiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
