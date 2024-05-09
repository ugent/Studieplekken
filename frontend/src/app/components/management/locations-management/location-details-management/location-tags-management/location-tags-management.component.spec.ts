import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { LocationTagsManagementComponent } from './location-tags-management.component';

describe('LocationTagsManagementComponent', () => {
  let component: LocationTagsManagementComponent;
  let fixture: ComponentFixture<LocationTagsManagementComponent>;

  beforeEach(waitForAsync(() => {
    void TestBed.configureTestingModule({
      declarations: [LocationTagsManagementComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationTagsManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
