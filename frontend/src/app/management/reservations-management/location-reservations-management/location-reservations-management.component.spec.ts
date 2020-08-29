import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationReservationsManagementComponent } from './location-reservations-management.component';

describe('LocationReservationsManagementComponent', () => {
  let component: LocationReservationsManagementComponent;
  let fixture: ComponentFixture<LocationReservationsManagementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationReservationsManagementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationReservationsManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
