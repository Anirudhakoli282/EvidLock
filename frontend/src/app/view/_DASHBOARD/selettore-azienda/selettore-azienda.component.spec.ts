import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelettoreAziendaComponent } from './selettore-azienda.component';

describe('SelettoreAziendaComponent', () => {
  let component: SelettoreAziendaComponent;
  let fixture: ComponentFixture<SelettoreAziendaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SelettoreAziendaComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SelettoreAziendaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
