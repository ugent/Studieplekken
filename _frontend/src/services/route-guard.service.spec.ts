import {fakeAsync, TestBed, tick} from '@angular/core/testing';

import { RouteGuardService } from './route-guard.service';
import {HttpClientModule} from "@angular/common/http";
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {AuthenticationService} from "./authentication.service";
import {RouterTestingModule} from "@angular/router/testing";
import {ActivatedRouteSnapshot, Router, RouterStateSnapshot} from "@angular/router";
import {LoginComponent} from "../app/login/login.component";
import {urls} from "../environments/environment";
import AuthenticationServiceStub from "./stubs/AuthenticationServiceStub";
import {Location} from "@angular/common";
import {Test} from 'tslint';
import {of} from 'rxjs';


describe('RouteGuardService', () => {
  let mockSnapshot:any = jasmine.createSpyObj<RouterStateSnapshot>("RouterStateSnapshot", ['toString']);
  let service: RouteGuardService;
  let httpMock: HttpTestingController;
  let authService;
  let router: Router;
  let location: Location;

  beforeEach(() => {

    TestBed.configureTestingModule({
      providers: [HttpClientModule, RouteGuardService,{provide: AuthenticationService, useClass: AuthenticationServiceStub} , LoginComponent],
      imports: [HttpClientModule, HttpClientTestingModule, RouterTestingModule.withRoutes([{path: 'login', component: LoginComponent}])]
    });

    service = TestBed.get(RouteGuardService);
    httpMock = TestBed.get(HttpTestingController);
    authService = TestBed.get(AuthenticationService);
    router = TestBed.get(Router);
    router.initialNavigation();
    location= TestBed.get(Location);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should return true for a logged in user', fakeAsync(() => {

    let t = service.canActivate(new ActivatedRouteSnapshot(),mockSnapshot) as Promise<boolean>;
    t.then(val => {
      expect(val).toBeTrue();
    });
    tick();
    const request = httpMock.expectOne(urls.session + authService.getCookie('mapping') );
    expect(request.request.method).toEqual('GET');
    expect(location.path()).toBe('');

  }));
});
