import { TestBed } from '@angular/core/testing';

import { CalendarPeriodsForLockersService } from './calendar-periods-for-lockers.service';

describe('CalendarPeriodsForLockersService', () => {
  let service: CalendarPeriodsForLockersService;

  beforeEach(() => {
    void TestBed.configureTestingModule({});
    service = TestBed.inject(CalendarPeriodsForLockersService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
