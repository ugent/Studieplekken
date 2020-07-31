import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationComponent } from './location.component';
import {ActivatedRoute} from '@angular/router';
import {LocationService} from '../../services/location.service';
import {LocationReservationService} from '../../services/location-reservation.service';
import {DomSanitizer} from '@angular/platform-browser';
import {AuthenticationService} from '../../services/authentication.service';
import {TranslateModule, TranslateService, TranslateStore} from '@ngx-translate/core';
import {HttpClientModule} from '@angular/common/http';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';
import LocationServiceStub from '../../services/stubs/LocationServiceStub';
import LocationReservationServiceStub from '../../services/stubs/LocationReservationServiceStub';
import {CalendarModule, DateAdapter} from 'angular-calendar';
import {adapterFactory} from 'angular-calendar/date-adapters/date-fns';
import {LockerReservationService} from '../../services/locker-reservation.service';
import LockerReservationServiceStub from '../../services/stubs/LockerReservationServiceStub';
import {RecaptchaComponent, RecaptchaLoaderService} from 'ng-recaptcha';
import {PenaltyService} from "../../services/penalty.service";
import {of} from "rxjs";
import {IUser} from "../../interfaces/IUser";
import Spy = jasmine.Spy;

describe('LocationComponent', () => {
  let component: LocationComponent;
  let fixture: ComponentFixture<LocationComponent>;


  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationComponent, RecaptchaComponent ],
      providers: [{ provide: LocationService, useClass: LocationServiceStub},
        { provide:LocationReservationService, useClass: LocationReservationServiceStub}, DomSanitizer, { provide: AuthenticationService, useClass: AuthenticationServiceStub}, TranslateService, {
        provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: (name) => {return "Therminal"} }}}
      }, {provide: LockerReservationService, useClass: LockerReservationServiceStub}, HttpClientModule, TranslateStore, RecaptchaLoaderService],
      imports: [HttpClientModule, TranslateModule.forChild(), CalendarModule.forRoot(
        {
          provide: DateAdapter,
          useFactory: adapterFactory,
        }
      )]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not let a student make a reservation if he has too many penalty points', () => {
    let service = TestBed.inject(AuthenticationService);

    //the getCurrentUser returns a user with way too many points so the showToManyPoints method should be called
    spyOn(service, "getCurrentUser").and.returnValue({
      'lastName': 'admin', 'firstName': 'admin', 'mail': 'admin', 'password': '',
      'institution': 'UGent', barcode: '0000000006002', 'augentID': '0000000006002', 'penaltyPoints': 1000000, 'birthDate': {'year': 1999, 'month': 12, 'day': 27, 'hrs': 0, 'min': 0, 'sec': 0}, 'roles': ['EMPLOYEE', 'ADMIN']
    } as IUser);

    spyOn(component,"showToManyPoints");

    component.addDate({'year': 1999, 'month': 12, 'day': 27, 'hrs': 0, 'min': 0, 'sec': 0});

    expect(component.showToManyPoints).toHaveBeenCalled();
  });

});
