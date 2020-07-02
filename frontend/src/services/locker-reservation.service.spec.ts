import { TestBed } from '@angular/core/testing';

import { LockerReservationService } from './locker-reservation.service';
import {HttpClientModule} from "@angular/common/http";
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";

describe('LockerReservationService', () => {

  let service: LockerReservationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HttpClientModule, LockerReservationService],
      imports: [HttpClientModule, HttpClientTestingModule]
    });
    service = TestBed.get(LockerReservationService);
    httpMock = TestBed.get(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
