import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DocumentiTabellaComponent } from './documenti-tabella.component';

describe('DocumentiTabellaComponent', () => {
  let component: DocumentiTabellaComponent;
  let fixture: ComponentFixture<DocumentiTabellaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DocumentiTabellaComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DocumentiTabellaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
