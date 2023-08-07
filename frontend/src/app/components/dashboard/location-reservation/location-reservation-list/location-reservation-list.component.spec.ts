import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationReservationListComponent } from './location-reservation-list.component';

describe('LocationReservationListComponent', () => {
  let component: LocationReservationListComponent;
  let fixture: ComponentFixture<LocationReservationListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LocationReservationListComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LocationReservationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
