import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddCorsoModalComponent } from './add-corso-modal.component';

describe('AddCorsoModalComponent', () => {
  let component: AddCorsoModalComponent;
  let fixture: ComponentFixture<AddCorsoModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AddCorsoModalComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(AddCorsoModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
