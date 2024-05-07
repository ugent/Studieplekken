import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { UserDetailsFormComponent } from './user-details-form.component';

describe('UserDetailsFormComponent', () => {
  let component: UserDetailsFormComponent;
  let fixture: ComponentFixture<UserDetailsFormComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [UserDetailsFormComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserDetailsFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
