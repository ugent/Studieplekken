import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationVolunteersManagementComponent } from './location-volunteers-management.component';

describe('LocationVolunteersManagementComponent', () => {
  let component: LocationVolunteersManagementComponent;
  let fixture: ComponentFixture<LocationVolunteersManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LocationVolunteersManagementComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LocationVolunteersManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
