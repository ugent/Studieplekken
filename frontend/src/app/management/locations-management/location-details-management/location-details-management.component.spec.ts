import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationDetailsManagementComponent } from './location-details-management.component';

describe('LocationDetailsManagementComponent', () => {
  let component: LocationDetailsManagementComponent;
  let fixture: ComponentFixture<LocationDetailsManagementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationDetailsManagementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationDetailsManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
