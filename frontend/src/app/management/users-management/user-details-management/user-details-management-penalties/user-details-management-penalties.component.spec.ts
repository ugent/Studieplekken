import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UserDetailsManagementPenaltiesComponent } from './user-details-management-penalties.component';

describe('UserDetailsManagementPenaltiesComponent', () => {
  let component: UserDetailsManagementPenaltiesComponent;
  let fixture: ComponentFixture<UserDetailsManagementPenaltiesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UserDetailsManagementPenaltiesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserDetailsManagementPenaltiesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
