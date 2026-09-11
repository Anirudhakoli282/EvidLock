import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DipendentiTabellaComponent } from './dipendenti-tabella.component';

describe('DipendentiTabellaComponent', () => {
  let component: DipendentiTabellaComponent;
  let fixture: ComponentFixture<DipendentiTabellaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DipendentiTabellaComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(DipendentiTabellaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
