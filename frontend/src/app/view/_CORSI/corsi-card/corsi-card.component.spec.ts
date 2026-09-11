import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CorsiCardComponent } from './corsi-card.component';

describe('CorsiCardComponent', () => {
  let component: CorsiCardComponent;
  let fixture: ComponentFixture<CorsiCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CorsiCardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CorsiCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
