import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthoritiesManagementComponent } from './authorities-management.component';

describe('AuthoritiesManagementComponent', () => {
  let component: AuthoritiesManagementComponent;
  let fixture: ComponentFixture<AuthoritiesManagementComponent>;

  beforeEach(async(() => {
    void TestBed.configureTestingModule({
      declarations: [AuthoritiesManagementComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthoritiesManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
