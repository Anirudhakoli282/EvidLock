import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RiepilogoCorsiComponent } from './riepilogo-corsi.component';

describe('RiepilogoCorsiComponent', () => {
  let component: RiepilogoCorsiComponent;
  let fixture: ComponentFixture<RiepilogoCorsiComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [RiepilogoCorsiComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(RiepilogoCorsiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
