import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LocationReservationsComponent } from './location-reservations.component';

describe('LocationReservationsComponent', () => {
  let component: LocationReservationsComponent;
  let fixture: ComponentFixture<LocationReservationsComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [LocationReservationsComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationReservationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
