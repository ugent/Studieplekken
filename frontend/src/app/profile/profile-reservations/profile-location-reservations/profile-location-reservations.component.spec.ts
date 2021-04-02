import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProfileLocationReservationsComponent } from './profile-location-reservations.component';

describe('ProfileLocationReservationsComponent', () => {
  let component: ProfileLocationReservationsComponent;
  let fixture: ComponentFixture<ProfileLocationReservationsComponent>;

  beforeEach(async(() => {
    void TestBed.configureTestingModule({
      declarations: [ProfileLocationReservationsComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileLocationReservationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
