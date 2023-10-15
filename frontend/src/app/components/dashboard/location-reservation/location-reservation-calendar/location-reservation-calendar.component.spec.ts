import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationReservationCalendarComponent } from './location-reservation-calendar.component';

describe('LocationReservationCalendarComponent', () => {
  let component: LocationReservationCalendarComponent;
  let fixture: ComponentFixture<LocationReservationCalendarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LocationReservationCalendarComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LocationReservationCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
