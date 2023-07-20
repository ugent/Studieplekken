import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationReservationComponent } from './location-reservation.component';

describe('LocationReservationComponent', () => {
  let component: LocationReservationComponent;
  let fixture: ComponentFixture<LocationReservationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LocationReservationComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LocationReservationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
