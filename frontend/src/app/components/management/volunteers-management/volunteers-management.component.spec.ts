import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { VolunteersManagementComponent } from './volunteers-management.component';

describe('VolunteersManagementComponent', () => {
  let component: VolunteersManagementComponent;
  let fixture: ComponentFixture<VolunteersManagementComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [VolunteersManagementComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VolunteersManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
