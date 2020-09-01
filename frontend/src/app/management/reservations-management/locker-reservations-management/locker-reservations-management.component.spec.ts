import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LockerReservationsManagementComponent } from './locker-reservations-management.component';

describe('LockerReservationsManagementComponent', () => {
  let component: LockerReservationsManagementComponent;
  let fixture: ComponentFixture<LockerReservationsManagementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LockerReservationsManagementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LockerReservationsManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
