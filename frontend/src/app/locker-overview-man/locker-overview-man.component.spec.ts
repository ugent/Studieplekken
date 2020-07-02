import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LockerOverviewManComponent } from './locker-overview-man.component';
import {LockerReservationService} from '../../services/locker-reservation.service';
import LockerReservationServiceStub from '../../services/stubs/LockerReservationServiceStub';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {LocationService} from '../../services/location.service';
import LocationServiceStub from '../../services/stubs/LocationServiceStub';
import {HttpClientModule} from "@angular/common/http";
import {AuthenticationService} from "../../services/authentication.service";
import AuthenticationServiceStub from "../../services/stubs/AuthenticationServiceStub";

describe('LockerOverviewManComponent', () => {
  let component: LockerOverviewManComponent;
  let fixture: ComponentFixture<LockerOverviewManComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LockerOverviewManComponent, ],
      imports: [HttpClientModule, BrowserAnimationsModule],
      providers: [{provide: LockerReservationService, useClass: LockerReservationServiceStub} ,
        { provide: LocationService, useClass: LocationServiceStub}, {provide: AuthenticationService, useClass: AuthenticationServiceStub}]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LockerOverviewManComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
