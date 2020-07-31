import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ScanComponent} from './scan.component';
import {FormBuilder} from '@angular/forms';
import {LocationReservationService} from '../../services/location-reservation.service';
import {LocationService} from '../../services/location.service';
import {TranslateModule, TranslateService, TranslateStore} from '@ngx-translate/core';
import {AuthenticationService} from '../../services/authentication.service';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';
import LocationReservationServiceStub from '../../services/stubs/LocationReservationServiceStub';
import LocationServiceStub from '../../services/stubs/LocationServiceStub';
import {ScanService} from '../../services/scan.service';
import ScanServiceStub from '../../services/stubs/ScanServiceStub';


describe('ScanComponent', () => {
  let component: ScanComponent;
  let fixture: ComponentFixture<ScanComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ScanComponent],
      providers: [FormBuilder, {provide: LocationReservationService, useClass: LocationReservationServiceStub},
        {provide: LocationService, useClass: LocationServiceStub}, TranslateService, TranslateStore,
        {provide: AuthenticationService, useClass: AuthenticationServiceStub},
        {provide: ScanService, useClass: ScanServiceStub}],
      imports: [TranslateModule.forChild()]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ScanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call connect only if the location is filled in', () => {
    let service = TestBed.inject(ScanService);
    const spy = spyOn(service, "connect");
    component.scanLocation = "";

    component.connect();
    expect(service.connect).toHaveBeenCalledTimes(0);

    component.scanLocation = "Therminal";
    component.connect();
    expect(service.connect).toHaveBeenCalled();
    expect(spy.calls.first().args).toContain("Therminal")
  });

  it('should disconnect properly', () => {
    let scanService = TestBed.inject(ScanService);
    component.scanLocation = "Therminal";
    spyOn(scanService, "disconnect");
    component.disconnect();
    expect(scanService.disconnect).toHaveBeenCalled();
  })
});
