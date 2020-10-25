import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BuildingManagementComponent } from './building-management.component';

describe('BuildingManagementComponent', () => {
  let component: BuildingManagementComponent;
  let fixture: ComponentFixture<BuildingManagementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BuildingManagementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BuildingManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
