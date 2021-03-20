import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { VolunteerManagementPanelComponent } from './volunteer-management-panel.component';

describe('VolunteerManagementPanelComponent', () => {
  let component: VolunteerManagementPanelComponent;
  let fixture: ComponentFixture<VolunteerManagementPanelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ VolunteerManagementPanelComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VolunteerManagementPanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
