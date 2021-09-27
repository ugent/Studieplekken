import { TestBed } from '@angular/core/testing';

import { TimeslotsService } from './timeslot.service';

describe('CalendarPeriodsService', () => {
  let service: TimeslotsService;

  beforeEach(() => {
    void TestBed.configureTestingModule({});
    service = TestBed.inject(TimeslotsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
