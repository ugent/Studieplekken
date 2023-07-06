import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LocationsManagementComponent } from './locations-management.component';

describe('LocationsManagementComponent', () => {
  let component: LocationsManagementComponent;
  let fixture: ComponentFixture<LocationsManagementComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [LocationsManagementComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationsManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
