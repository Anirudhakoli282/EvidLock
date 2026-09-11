import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CorsiIconComponent } from './corsi-icon.component';

describe('CorsiIconComponent', () => {
  let component: CorsiIconComponent;
  let fixture: ComponentFixture<CorsiIconComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CorsiIconComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CorsiIconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
