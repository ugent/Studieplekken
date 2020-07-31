import { TestBed } from '@angular/core/testing';

import { AuthenticationService } from './authentication.service';
import {HttpClientModule} from "@angular/common/http";
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {UserService} from "./user.service";
import {urls} from "../environments/environment";
import {IUser} from "../interfaces/IUser";

describe('AuthenticationService', () => {
  let service: AuthenticationService;
  let httpMock: HttpTestingController;
  let dummyUser: IUser;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HttpClientModule, AuthenticationService, UserService],
      imports: [HttpClientModule, HttpClientTestingModule]
    });

    service = TestBed.inject(AuthenticationService);
    httpMock = TestBed.get(HttpTestingController);
    dummyUser = {
      augentID: "505",
      institution: "UGent",
      lastName: "Doe",
      mail: "john.doe@ugent.be",
      password: "",
      penaltyPoints: 0,
      roles: [],
      firstName: "John",
      barcode:""
    };
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('logout() should set currentUser to null and do a POST request', () =>
  {
    service.logout().then( ()=> {
        expect(service.currentUser.value).toEqual(null);
    });

    const request = httpMock.expectOne( `${urls.signout}`);
    expect(request.request.method).toBe('POST');

    httpMock.verify();
  });

  it('updateUser() should change currentUser', () =>
  {
    service.updateOwnProfile(dummyUser).then( () => {
        expect(service.currentUser.getValue()).toEqual(dummyUser);
    });

    const req = httpMock.expectOne(
      urls.account + dummyUser.mail,
      'put to api'
    );
    expect(req.request.method).toBe('PUT');

    req.flush(dummyUser);
  })
});
