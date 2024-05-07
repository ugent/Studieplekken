import { TestBed } from '@angular/core/testing';

import { TimeslotGroupService } from './timeslot-group.service';

describe('TimeslotGroupService', () => {
  let service: TimeslotGroupService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TimeslotGroupService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
