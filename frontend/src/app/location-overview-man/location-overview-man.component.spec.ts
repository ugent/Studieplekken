import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocationOverviewManComponent } from './location-overview-man.component';
import {LocationService} from '../../services/location.service';
import {FormBuilder} from '@angular/forms';
import LocationServiceStub from '../../services/stubs/LocationServiceStub';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {AuthenticationService} from '../../services/authentication.service';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';
import {ILocation} from '../../interfaces/ILocation';
import {of} from "rxjs";
import {UserService} from '../../services/user.service';
import ManagementServiceStub from '../../services/stubs/ManagementServiceStub';
import {TranslateModule, TranslateService, TranslateStore} from '@ngx-translate/core';
import {appLanguages} from "../../environments/environment.prod";

describe('LocationOverviewManComponent', () => {
  let component: LocationOverviewManComponent;
  let fixture: ComponentFixture<LocationOverviewManComponent>;

  let setFormInvalid  = () => {
    component.locationForm.controls["name"].setValue("");
    component.locationForm.controls["address"].setValue("");
    component.locationForm.controls["imageUrl"].setValue("newLocation");
    component.locationForm.controls["numberOfLockers"].setValue(10);
    component.locationForm.controls["numberOfSeats"].setValue("");

    for(let o of Object.keys(appLanguages)){
      component.locationForm.controls.descriptions['controls'][appLanguages[o]].setValue("Description");
    }
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LocationOverviewManComponent ],
      providers: [{provide: LocationService, useClass: LocationServiceStub},
        {provide: AuthenticationService, useClass: AuthenticationServiceStub},
        {provide: UserService, useClass: ManagementServiceStub},FormBuilder, TranslateService, TranslateStore],
      imports: [BrowserAnimationsModule, TranslateModule.forChild()]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LocationOverviewManComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have correct validation on the locationForm', () => {
    setFormInvalid();
    // name, address, description and numberOfSeats are all required fields so this form should be invalid
    expect(component.locationForm.valid).toBeFalse();

    component.locationForm.controls["name"].setValue("testName");
    component.locationForm.controls["address"].setValue("testAddress");
    component.locationForm.controls["numberOfSeats"].setValue(100);
    component.locationForm.controls["mapsFrameUrl"].setValue("https://www.google.com/maps/embed/testt");

    expect(component.locationForm.valid).toBeTrue();
  });

  it('should only add a location if the form is valid ', () => {
    let service = TestBed.inject(LocationService);
    spyOn(service, "addLocation").and.returnValue(of(null));
    setFormInvalid();
    component.addLocation(component.locationForm.value);
    expect(service.addLocation).toHaveBeenCalledTimes(0);

    component.locationForm.controls["name"].setValue("testName");
    component.locationForm.controls["address"].setValue("testAddress");
    component.locationForm.controls["numberOfSeats"].setValue(100);
    component.locationForm.controls["mapsFrameUrl"].setValue("https://www.google.com/maps/embed/testt");

    // form is valid now adding should work
    component.addLocation(component.locationForm.value);
    expect(service.addLocation).toHaveBeenCalled();
  });

  it('should fill in default values of numberOfLockers and imageUrl if necessary ', () => {
    component.locationForm.controls["imageUrl"].setValue("");
    component.locationForm.controls["numberOfLockers"].setValue("");
    component.locationForm.controls["numberOfSeats"].setValue("");
    for(let o of Object.keys(appLanguages)){
      component.locationForm.controls.descriptions['controls'][appLanguages[o]].setValue("Description");
    }
    component.locationForm.controls["name"].setValue("testName");
    component.locationForm.controls["address"].setValue("testAddress");
    component.locationForm.controls["numberOfSeats"].setValue(100);
    component.locationForm.controls["mapsFrameUrl"].setValue("https://www.google.com/maps/embed/testt");

    // both empty, so it should replace it with the standard value
    let service = TestBed.inject(LocationService);
    const spy = spyOn(service, "addLocation").and.returnValue(of(null));
    fixture.detectChanges();
    component.addLocation(component.locationForm.value);
    expect(service.addLocation).toHaveBeenCalled();
    let location= {
      name: "testName",
      address: "testAddress",
      descriptions: {ENGLISH : "Description", DUTCH : "Description"},
      numberOfSeats: 100,
      imageUrl: component.defaultImageUrl,
      numberOfLockers: 0,
      mapsFrame: 'https://www.google.com/maps/embed/testt',
      calendar: null,
      lockers: null,
      endPeriodLockers: null,
      startPeriodLockers: null
    } as ILocation;
    expect(service.addLocation).toHaveBeenCalledWith(location);

    // if they are actually filled in it should not replace them
    component.locationForm.controls["imageUrl"].setValue("urltest");
    component.locationForm.controls["numberOfLockers"].setValue(100);
    for(let o of Object.keys(appLanguages)){
      component.locationForm.controls.descriptions['controls'][appLanguages[o]].setValue("Description");
    }
    component.locationForm.controls["name"].setValue("testName");
    component.locationForm.controls["address"].setValue("testAddress");
    component.locationForm.controls["numberOfSeats"].setValue(100);
    component.locationForm.controls["mapsFrameUrl"].setValue("https://www.google.com/maps/embed/testt");

    // non default values
    location.imageUrl = "urltest";
    location.numberOfLockers = 100;
    spy.calls.reset();
    fixture.detectChanges();
    component.addLocation(component.locationForm.value);

    expect(service.addLocation).toHaveBeenCalledWith(location);

  })

});


