import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationReservationDetailsComponent } from './location-reservation-details.component';

describe('LocationReservationDetailsComponent', () => {
  let component: LocationReservationDetailsComponent;
  let fixture: ComponentFixture<LocationReservationDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LocationReservationDetailsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LocationReservationDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
