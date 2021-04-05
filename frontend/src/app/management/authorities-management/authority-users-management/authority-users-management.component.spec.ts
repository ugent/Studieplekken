import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthorityUsersManagementComponent } from './authority-users-management.component';

describe('AuthorityUsersManagementComponent', () => {
  let component: AuthorityUsersManagementComponent;
  let fixture: ComponentFixture<AuthorityUsersManagementComponent>;

  beforeEach(async(() => {
    void TestBed.configureTestingModule({
      declarations: [AuthorityUsersManagementComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthorityUsersManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
