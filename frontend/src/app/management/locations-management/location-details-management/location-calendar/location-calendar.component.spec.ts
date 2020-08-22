import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationCalendarComponent } from './location-calendar.component';

describe('LocationCalendarComponent', () => {
  let component: LocationCalendarComponent;
  let fixture: ComponentFixture<LocationCalendarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationCalendarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
