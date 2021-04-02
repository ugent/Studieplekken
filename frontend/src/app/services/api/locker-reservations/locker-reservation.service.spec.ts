import { TestBed } from '@angular/core/testing';

import { LockerReservationService } from './locker-reservation.service';

describe('LockerReservationService', () => {
  let service: LockerReservationService;

  beforeEach(() => {
    void TestBed.configureTestingModule({});
    service = TestBed.inject(LockerReservationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
