import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {RegistrationComponent} from './registration.component';
import {Router} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {UserService} from '../../services/user.service';
import ManagementServiceStub from '../../services/stubs/ManagementServiceStub';
import {VerificationComponent} from '../verification/verification.component';
import {Location} from '@angular/common';
import {of, throwError} from 'rxjs';
import {VerificationService} from '../../services/verification.service';
import VerificationServiceStub from '../../services/stubs/VerificationServiceStub';

describe('RegistrationComponent', () => {
  let component: RegistrationComponent;
  let fixture: ComponentFixture<RegistrationComponent>;

  let location: Location;
  let router: Router;
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [RegistrationComponent],
      imports: [RouterTestingModule.withRoutes([{path:'verification', component: VerificationComponent}]),],
      providers: [ {provide: UserService, useClass: ManagementServiceStub},
        {provide: VerificationService, useClass: VerificationServiceStub}]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RegistrationComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    location = TestBed.inject(Location);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have correct validation on the form', () => {
    // initially the form should be empty and thus not valid
    expect(component.r.valid).toBeFalse();
    component.r.controls["email"].setValue("email");
    component.r.controls["pwd"].setValue("small");
    expect(component.r.controls.pwd.valid).toBe(false, 'password should be too short');
    component.r.controls["pwd"].setValue("longpassword");
    expect(component.r.controls.pwd.valid).toBe(true, 'password should fine now');

    expect(component.r.controls.confPwd.valid).toBe(false, 'confPwd should be the same as pwd');
    component.r.controls["confPwd"].setValue("longpassword");
    expect(component.r.controls.confPwd.valid).toBe(true, 'confPwd should be the same as pwd');

    // form should be valid now
    expect(component.r.valid).toBeTrue();
  });

  it('should not try to register with an invalid form', fakeAsync(() => {
    let managementservice = TestBed.inject(UserService);
    let verificationService = TestBed.inject(VerificationService);
    spyOn(managementservice, "addUser");
    spyOn(verificationService, "setNewUser");

    component.onSubmit(component.r.value);
    tick();
    // these functions should not have been called as the form is invalid
    expect(managementservice.addUser).toHaveBeenCalledTimes(0);
    expect(verificationService.setNewUser).toHaveBeenCalledTimes(0);
  }));


  it('should check if the user already exists and try to add the user if the form is valid', fakeAsync(() => {
    let managementservice = TestBed.inject(UserService);
    let verificationService = TestBed.inject(VerificationService);
    component.r.controls["email"].setValue("email");
    component.r.controls["pwd"].setValue("longpassword");
    component.r.controls["confPwd"].setValue("longpassword");

    // valid form

    // spy for exists that returns true
     spyOn(verificationService, "setNewUser");
    const spy = spyOn(managementservice, "addUser").and.returnValue(throwError({status: "test"}));
    component.onSubmit(component.r.value);

    // form is valid so it should have been added to the verifcationService
    expect(verificationService.setNewUser).toHaveBeenCalled();
    expect(managementservice.addUser).toHaveBeenCalled();
    // not valid so it should not reroute to verification
    expect(router.url).toBe('/', 'page should not have rerouted to verification');
    // spy of existsUser now returns false
    spy.and.returnValue(of(null));

    // refill form
    component.r.controls["email"].setValue("email");
    component.r.controls["pwd"].setValue("longpassword");
    component.r.controls["confPwd"].setValue("longpassword");

    component.onSubmit(component.r.value);
    tick();

    // should call it for the second time now
    expect(verificationService.setNewUser).toHaveBeenCalledTimes(2);

    // user does not exist so it should be added
    expect(managementservice.addUser).toHaveBeenCalledTimes(2);
    expect(router.url).toBe('/verification', 'page should have rerouted to verification');
  }))
});
