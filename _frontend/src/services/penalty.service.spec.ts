import {TestBed} from '@angular/core/testing';

import {PenaltyService} from './penalty.service';
import {HttpClientModule} from '@angular/common/http';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {urls} from "../environments/environment";
import {IUser} from "../interfaces/IUser";
import {IPenaltyEvent} from "../interfaces/IPenaltyEvent";

describe('PenaltyService', () => {
  let service: PenaltyService;
  let httpMock: HttpTestingController;
  let penaltyEventsMock: IPenaltyEvent[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HttpClientModule, PenaltyService],
      imports: [HttpClientModule, HttpClientTestingModule]
    });

    service = TestBed.inject(PenaltyService);
    httpMock = TestBed.get(HttpTestingController);
    penaltyEventsMock = [{code: 1666, descriptions:'bad behaviour occurred', points: 20, publicAccessible: true}] as IPenaltyEvent[];
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAllPenaltyEvents() should get the correct PenaltyEvents and should be a GET method', () => {

    service.getAllPenaltyEvents().subscribe((res) => {
      expect(res[0].code).toEqual(penaltyEventsMock[0].code);
      expect(res.length).toEqual(1);
    });

    const request = httpMock.expectOne( `${urls.penaltyEvent}`);
    expect(request.request.method).toBe('GET');
    request.flush(penaltyEventsMock);
  });

  it('addUser() should add the correct user and should be a post method.', () => {

    service.addPenaltyEvent(penaltyEventsMock[0])
      .subscribe(penaltyEvent => {
        expect(penaltyEvent.code).toEqual(penaltyEventsMock[0].code);
      });

    const req = httpMock.expectOne(urls.penaltyEvent + '/' + penaltyEventsMock[0].code);

    expect(req.request.method).toEqual('POST');
    expect(req.request.body).toEqual(penaltyEventsMock[0]);

    req.flush(penaltyEventsMock[0]);
  });

  it('changePenaltyEvent() should update the correct event and should be a PUT method.', () => {

    service.changePenaltyEvent(penaltyEventsMock[0]).subscribe(changedPenaltyEvent => {
      expect(changedPenaltyEvent.code).toBe(penaltyEventsMock[0].code);
    });

    const req = httpMock.expectOne(
      urls.penaltyEvent + '/'+ penaltyEventsMock[0].code,
      'put to api'
    );

    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toBe(penaltyEventsMock[0]);

    req.flush(penaltyEventsMock[0]);

  });

});
