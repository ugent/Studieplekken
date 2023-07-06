import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ProfileOverviewComponent } from './profile-overview.component';

describe('ProfileOverviewComponent', () => {
  let component: ProfileOverviewComponent;
  let fixture: ComponentFixture<ProfileOverviewComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [ProfileOverviewComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ProfileOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
