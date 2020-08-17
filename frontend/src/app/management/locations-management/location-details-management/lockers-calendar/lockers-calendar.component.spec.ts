import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LockersCalendarComponent } from './lockers-calendar.component';

describe('LockersCalendarComponent', () => {
  let component: LockersCalendarComponent;
  let fixture: ComponentFixture<LockersCalendarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LockersCalendarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LockersCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
