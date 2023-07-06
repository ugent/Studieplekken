import { TestBed } from '@angular/core/testing';

import { TimeslotCalendarEventService } from './timeslot-calendar-event.service';

describe('TimeslotCalendarEventService', () => {
  let service: TimeslotCalendarEventService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TimeslotCalendarEventService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
