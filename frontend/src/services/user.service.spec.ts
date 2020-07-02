import { TestBed } from '@angular/core/testing';

import { UserService } from './user.service';
import {HttpClientModule} from '@angular/common/http';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {urls} from "../environments/environment";
import {IUser} from "../interfaces/IUser";

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;
  let dummyUser: IUser;

  beforeEach(() => {

    TestBed.configureTestingModule({
      providers: [HttpClientModule, UserService],
      imports: [HttpClientModule, HttpClientTestingModule]
    });

    service = TestBed.inject(UserService);
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

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getUserByEmail() should get the correct user and should be a GET method', () => {

    service.getUserByEmail(dummyUser.mail).subscribe((res) => {
      expect(res.mail).toEqual(dummyUser.mail);
    });

    const request = httpMock.expectOne( `${urls.accountEmail}${dummyUser.mail}`);
    expect(request.request.method).toBe('GET');
    request.flush(dummyUser);
  });

  it('addUser() should add the correct user and should be a post method', () => {

    service.addUser(dummyUser)
      .subscribe(user => {
        expect(user.mail).toEqual(dummyUser.mail);
      });

    const req = httpMock.expectOne(urls.newAccount);

    expect(req.request.method).toEqual('POST');
    expect(req.request.body).toEqual(dummyUser);

    req.flush(dummyUser);
  });

  it('updateUser() should update the correct user and should be a PUT method', () => {

    service.updateUser(dummyUser).subscribe((user:IUser) => {
      expect(user.mail).toBe(dummyUser.mail);
    });

    const req = httpMock.expectOne(
      urls.account + dummyUser.mail,
      'put to api'
    );
    expect(req.request.method).toBe('PUT');

    req.flush(dummyUser);

  });

  it('existsUser() should return true when there is a user with the same email address', () => {
    service.existsUser(dummyUser.mail).then(r => {
      expect(r).toBeTrue();
    });

    const request = httpMock.expectOne( `${urls.accountEmail}${dummyUser.mail}`);
    expect(request.request.method).toBe('GET');
    request.flush(dummyUser);

  });

  it('existsUser() should return false when there is not a user with the same email address', () => {

    let user : IUser = null;
    let newEmail: string = "gibberish@ugent.be";

    service.existsUser(newEmail).then(r => {
      expect(r).toBeFalse();
    });

    const request = httpMock.expectOne( `${urls.accountEmail}${newEmail}`);
    expect(request.request.method).toBe('GET');

    //the request in the existsUser mocks returning an empty array
    request.flush(user);

  });

});
