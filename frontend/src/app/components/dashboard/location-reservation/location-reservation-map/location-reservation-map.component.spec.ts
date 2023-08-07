import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationReservationMapComponent } from './location-reservation-map.component';

describe('LocationReservationMapComponent', () => {
  let component: LocationReservationMapComponent;
  let fixture: ComponentFixture<LocationReservationMapComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LocationReservationMapComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LocationReservationMapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
