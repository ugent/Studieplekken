import { TestBed } from '@angular/core/testing';

import { LockersService } from './lockers.service';

describe('LockersService', () => {
  let service: LockersService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LockersService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
