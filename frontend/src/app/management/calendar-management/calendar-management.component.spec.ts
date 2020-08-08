import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CalendarManagementComponent } from './calendar-management.component';

describe('CalendarManagementComponent', () => {
  let component: CalendarManagementComponent;
  let fixture: ComponentFixture<CalendarManagementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CalendarManagementComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CalendarManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
