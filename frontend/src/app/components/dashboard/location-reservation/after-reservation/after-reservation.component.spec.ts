import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AfterReservationComponent } from './after-reservation.component';

describe('AfterReservationComponent', () => {
  let component: AfterReservationComponent;
  let fixture: ComponentFixture<AfterReservationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AfterReservationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AfterReservationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
