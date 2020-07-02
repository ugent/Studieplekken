import { TestBed } from '@angular/core/testing';

import {LocationReservationService} from './location-reservation.service';
import {HttpClientModule} from "@angular/common/http";
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {urls} from "../environments/environment";

describe('LocationReservationService', () => {
  let service: LocationReservationService;
  let httpMock: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HttpClientModule, LocationReservationService],
      imports: [HttpClientModule, HttpClientTestingModule]
    });
    service = TestBed.get(LocationReservationService);
    httpMock= TestBed.get(HttpTestingController);

    const request = httpMock.expectOne(urls.locationReservation + '/maxPenaltyPoints');
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('sendMailsToAbsentStudents() should POST to the correct URL (email addresses as RequestParam in url)', () => {
    let names: Set<string> = new Set<string>();
    names.add("a");
    names.add("b");

    service.sendMailsToAbsentStudents(names);

    const request = httpMock.expectOne(urls.locationReservation + '/sendMails?mails=a,b');
    expect(request.request.method).toEqual('POST');
  });

});
