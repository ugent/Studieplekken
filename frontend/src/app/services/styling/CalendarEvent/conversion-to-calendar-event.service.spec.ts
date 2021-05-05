import { TestBed } from '@angular/core/testing';

import { ConversionToCalendarEventService } from './conversion-to-calendar-event.service';

describe('ConversionToCalendarEventService', () => {
  let service: ConversionToCalendarEventService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConversionToCalendarEventService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
