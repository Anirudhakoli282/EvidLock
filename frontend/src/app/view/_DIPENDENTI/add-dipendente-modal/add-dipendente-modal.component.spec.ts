import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddDipendenteModalComponent } from './add-dipendente-modal.component';

describe('AddDipendenteModalComponent', () => {
  let component: AddDipendenteModalComponent;
  let fixture: ComponentFixture<AddDipendenteModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AddDipendenteModalComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddDipendenteModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
