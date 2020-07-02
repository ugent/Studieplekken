import { TestBed } from '@angular/core/testing';

import { LoginGuardService } from './login-guard.service';
import {AuthenticationService} from './authentication.service';
import AuthenticationServiceStub from './stubs/AuthenticationServiceStub';
import {RouterModule} from '@angular/router';
import {DashboardComponent} from '../app/dashboard/dashboard.component';
import {HttpClient} from '@angular/common/http';
import {HttpClientTestingModule} from '@angular/common/http/testing';

describe('LoginGuardService', () => {
  let service: LoginGuardService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{provide: AuthenticationService, useClass: AuthenticationServiceStub}, HttpClient],
      imports: [RouterModule.forRoot([{path: "dashboard", component: DashboardComponent}]), HttpClientTestingModule]
    });
    service = TestBed.inject(LoginGuardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
