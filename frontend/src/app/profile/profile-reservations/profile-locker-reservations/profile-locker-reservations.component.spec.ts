import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProfileLockerReservationsComponent } from './profile-locker-reservations.component';

describe('ProfileLockerReservationsComponent', () => {
  let component: ProfileLockerReservationsComponent;
  let fixture: ComponentFixture<ProfileLockerReservationsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProfileLockerReservationsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileLockerReservationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
