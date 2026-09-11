import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddDipendentiListaModalComponent } from './add-dipendenti-lista-modal.component';

describe('AddDipendentiListaModalComponent', () => {
  let component: AddDipendentiListaModalComponent;
  let fixture: ComponentFixture<AddDipendentiListaModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AddDipendentiListaModalComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddDipendentiListaModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
