import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ManagementHelpComponent } from './management-help.component';

describe('ManagementHelpComponent', () => {
  let component: ManagementHelpComponent;
  let fixture: ComponentFixture<ManagementHelpComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ManagementHelpComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ManagementHelpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
