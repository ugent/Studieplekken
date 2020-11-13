import {TestBed} from '@angular/core/testing';

import {LocationReservationsService} from './location-reservations.service';

describe('LocationReservationsService', () => {
  let service: LocationReservationsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LocationReservationsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
