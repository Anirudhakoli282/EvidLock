import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddAziendaModalComponent } from './add-azienda-modal.component';

describe('AddAziendaModalComponent', () => {
  let component: AddAziendaModalComponent;
  let fixture: ComponentFixture<AddAziendaModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AddAziendaModalComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddAziendaModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
