import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProfileCalendarComponent } from './profile-calendar.component';

describe('ProfileCalendarComponent', () => {
  let component: ProfileCalendarComponent;
  let fixture: ComponentFixture<ProfileCalendarComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [ProfileCalendarComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
