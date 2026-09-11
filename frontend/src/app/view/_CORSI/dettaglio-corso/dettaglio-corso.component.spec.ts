import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DettaglioCorsoComponent } from './dettaglio-corso.component';

describe('DettaglioCorsoComponent', () => {
  let component: DettaglioCorsoComponent;
  let fixture: ComponentFixture<DettaglioCorsoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DettaglioCorsoComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DettaglioCorsoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
