import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OperazioniVelociComponent } from './operazioni-veloci.component';

describe('OperazioniVelociComponent', () => {
  let component: OperazioniVelociComponent;
  let fixture: ComponentFixture<OperazioniVelociComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [OperazioniVelociComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(OperazioniVelociComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
