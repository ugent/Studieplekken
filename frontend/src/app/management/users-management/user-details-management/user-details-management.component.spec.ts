import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserDetailsManagementComponent } from './user-details-management.component';

describe('UserDetailsManagementComponent', () => {
  let component: UserDetailsManagementComponent;
  let fixture: ComponentFixture<UserDetailsManagementComponent>;

  beforeEach(async(() => {
    void TestBed.configureTestingModule({
      declarations: [UserDetailsManagementComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserDetailsManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
