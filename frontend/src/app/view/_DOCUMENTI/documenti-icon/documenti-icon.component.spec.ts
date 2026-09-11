import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DocumentiIconComponent } from './documenti-icon.component';

describe('DocumentiIconComponent', () => {
  let component: DocumentiIconComponent;
  let fixture: ComponentFixture<DocumentiIconComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DocumentiIconComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DocumentiIconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
