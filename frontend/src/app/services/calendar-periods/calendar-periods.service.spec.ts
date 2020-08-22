import { TestBed } from '@angular/core/testing';

import { CalendarPeriodsService } from './calendar-periods.service';

describe('CalendarPeriodsService', () => {
  let service: CalendarPeriodsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CalendarPeriodsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
