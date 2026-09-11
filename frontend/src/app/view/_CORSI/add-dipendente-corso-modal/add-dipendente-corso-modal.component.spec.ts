import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddDipendenteCorsoModalComponent } from './add-dipendente-corso-modal.component';

describe('AddDipendenteCorsoModalComponent', () => {
  let component: AddDipendenteCorsoModalComponent;
  let fixture: ComponentFixture<AddDipendenteCorsoModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AddDipendenteCorsoModalComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddDipendenteCorsoModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
