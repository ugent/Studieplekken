import {async, ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {UserOverviewComponent} from './user-overview.component';
import {AuthenticationService} from '../../services/authentication.service';
import {UserService} from '../../services/user.service';
import {VerificationService} from '../../services/verification.service';
import {HttpClientModule} from '@angular/common/http';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';
import ManagementServiceStub from '../../services/stubs/ManagementServiceStub';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {By} from '@angular/platform-browser';
import {of} from 'rxjs';

describe('UserOverviewComponent', () => {
  let component: UserOverviewComponent;
  let fixture: ComponentFixture<UserOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [UserOverviewComponent],
      providers: [{provide: AuthenticationService, useClass: AuthenticationServiceStub},
        {provide: UserService, useClass: ManagementServiceStub}, VerificationService],
      imports: [HttpClientModule, BrowserAnimationsModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update the resulsttable when new results come in', fakeAsync(() => {
    let users;
    component.userService.getUsersByName("test").subscribe(value => {
      component.resultsChanged(value);
      users = value;
    });
    // wait for asynchronous operation
    tick();
    fixture.detectChanges();
    // results variable should be filled in
    expect(component.results).toEqual(users);

    // there should be a row for each result + 1 for the header
    let rows = fixture.debugElement.queryAll(By.css('tr'));
    expect(rows.length).toBe(users.length  + 1);
  }));

  it('should have correct validation on the add new user form', () => {
    // initially all should be empty and the form should be invalid
    expect(component.userForm.valid).toBe(false);

    // fill all the required fields in
    component.userForm.controls["mail"].setValue("testEmail");

    component.userForm.controls["pwd"].setValue("small");
    expect(component.userForm.controls.pwd.valid).toBe(false, 'password should be longer than 8 chars');

    component.userForm.controls["pwd"].setValue("longpassword");
    expect(component.userForm.controls.pwd.valid).toBe(true, 'password longer than 8 chars should be correct');

    expect(component.userForm.controls.confPwd.valid).toBe(false, 'should be the same as password');
    component.userForm.controls["confPwd"].setValue("longpassword");
    expect(component.userForm.controls.confPwd.valid).toBe(true, 'should be the same as password');

    component.userForm.controls.role.setValue("STUDENT");
    // all fields are filled in so it should be valid
    expect(component.userForm.valid).toBe(true, 'if all fields are filled in correct, the form should be valid');
  });

  it('should not let you add a user with an invalid form', fakeAsync(() => {
    component.userForm.controls["pwd"].setValue("bad");
    component.userForm.controls["confPwd"].setValue("even worse");

    component.userForm.controls["mail"].setValue("");
    spyOn(component.userService, 'addUserByEmployee');


    // form is riddled with problems, adding a user should not work
    component.addUser(component.userForm.value);
    tick();
    // these functions should not have been called yet

    expect(component.userService.addUserByEmployee).toHaveBeenCalledTimes(0);

  }));

  it('should call the correct functions when adding a new user', fakeAsync(() => {
    component.userForm.controls["mail"].setValue("testEmail");

    component.userForm.controls["pwd"].setValue("longpassword");
    component.userForm.controls["confPwd"].setValue("longpassword");
    component.userForm.controls.role.setValue("STUDENT");

    let verification = TestBed.inject(VerificationService);
    const spy = spyOn(verification, 'setNewUser');
    spyOn(component.userService, 'addUserByEmployee').and.returnValue(of(null));
    component.addUser(component.userForm.value);
    tick();
    // should have checked if user already existed
    expect(verification.setNewUser).toHaveBeenCalled();
    // existsUser gave true so the user should not have been added
    expect(component.userService.addUserByEmployee).toHaveBeenCalled();

  }))
});
