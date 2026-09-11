import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AziendeTabellaComponent } from './aziende-tabella.component';

describe('AziendeTabellaComponent', () => {
  let component: AziendeTabellaComponent;
  let fixture: ComponentFixture<AziendeTabellaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AziendeTabellaComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AziendeTabellaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
