import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UserAuthoritiesManagementComponent } from './user-authorities-management.component';

describe('UserAuthoritiesManagementComponent', () => {
  let component: UserAuthoritiesManagementComponent;
  let fixture: ComponentFixture<UserAuthoritiesManagementComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [UserAuthoritiesManagementComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserAuthoritiesManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
