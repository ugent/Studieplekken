import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ProfileReservationsComponent } from './profile-reservations.component';

describe('ProfileReservationsComponent', () => {
  let component: ProfileReservationsComponent;
  let fixture: ComponentFixture<ProfileReservationsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ProfileReservationsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileReservationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
