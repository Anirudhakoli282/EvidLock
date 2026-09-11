import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DipendentiDettagliComponent } from './dipendenti-dettagli.component';

describe('DipendentiDettagliComponent', () => {
  let component: DipendentiDettagliComponent;
  let fixture: ComponentFixture<DipendentiDettagliComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DipendentiDettagliComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DipendentiDettagliComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
