import {TestBed, async, ComponentFixture} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {AppComponent} from './app.component';
import {MissingTranslationHandler, TranslateCompiler, TranslateLoader, TranslateModule, TranslateParser, TranslateService, TranslateStore} from '@ngx-translate/core';
import {HttpClientModule} from '@angular/common/http';
import {AuthenticationService} from '../services/authentication.service';
import AuthenticationServiceStub from '../services/stubs/AuthenticationServiceStub';
import {LoginComponent} from './login/login.component';
import {DashboardComponent} from './dashboard/dashboard.component';
import {ProfileComponent} from './profile/profile.component';
import {ScanComponent} from './scan/scan.component';
import {ManagementComponent} from './management/management.component';
import {InformationComponent} from './information/information.component';
import {IUser} from '../interfaces/IUser';
import {roles} from '../environments/environment';
import {PenaltiesComponent} from './penalties/penalties.component';
import {By} from '@angular/platform-browser';

describe('AppComponent', () => {

  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;

  let user = {
    'lastName': 'admin', 'firstName': 'admin', 'mail': 'admin', 'password': '',
    'institution': 'UGent', barcode: '0000000006002', 'augentID': '0000000006002', 'penaltyPoints': -1, 'birthDate': {'year': 1999, 'month': 12, 'day': 27, 'hrs': 0, 'min': 0, 'sec': 0}, 'roles': ['EMPLOYEE', 'ADMIN']
  } as IUser;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([{path: 'login', component: LoginComponent},
          {path: 'dashboard', component: DashboardComponent},
          {path: 'profile', component: ProfileComponent},
          {path: 'scan', component: ScanComponent},
          {path: 'management', component: ManagementComponent},
          {path: 'information', component: InformationComponent},
        ]), TranslateModule.forRoot(),
      ],
      declarations: [
        AppComponent
      ],
      providers: [
        {provide: AuthenticationService, useClass: AuthenticationServiceStub}
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have as title 'blokAtUGent'`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('blokAtUGent');
  });

  it('should show correct navigation when the user is a student', () => {
    user.roles = [roles.student];
    let authenticationService = TestBed.inject(AuthenticationService);
    const spy = spyOn(authenticationService, "getCurrentUser").and.returnValue(user);
    fixture.detectChanges();

    let logout = fixture.debugElement.query(By.css('li[id="logOut"]'));
    let scan = fixture.debugElement.query(By.css('li[id="scan"]'));
    let dashboard = fixture.debugElement.query(By.css('li[id="dashboard"]'));
    let profile = fixture.debugElement.query(By.css('li[id="profile"]'));
    let management = fixture.debugElement.query(By.css('li[id="management"]'));
    let login = fixture.debugElement.query(By.css('li[id="login"]'));
    let information = fixture.debugElement.query(By.css('li[id="information"]'));

    expect(logout).toBeTruthy();
    expect(dashboard).toBeTruthy();
    expect(profile).toBeTruthy();

    expect(scan).toBeFalsy();
    expect(management).toBeFalsy();
    expect(login).toBeFalsy();

  });

  it('should show correct roles when the user is an employee', () => {
    user.roles = [roles.employee];
    let authenticationService = TestBed.inject(AuthenticationService);
    const spy = spyOn(authenticationService, "getCurrentUser").and.returnValue(user);
    fixture.detectChanges();

    let logout = fixture.debugElement.query(By.css('li[id="logOut"]'));
    let scan = fixture.debugElement.query(By.css('li[id="scan"]'));
    let dashboard = fixture.debugElement.query(By.css('li[id="dashboard"]'));
    let profile = fixture.debugElement.query(By.css('li[id="profile"]'));
    let management = fixture.debugElement.query(By.css('li[id="management"]'));
    let login = fixture.debugElement.query(By.css('li[id="login"]'));
    let information = fixture.debugElement.query(By.css('li[id="information"]'));

    expect(logout).toBeTruthy();
    expect(dashboard).toBeTruthy();
    expect(profile).toBeTruthy();
    expect(scan).toBeTruthy();
    expect(management).toBeTruthy();
    expect(information).toBeTruthy();

    expect(login).toBeFalsy();
  });

  it('should show correct navigation if the user is not logged in', () => {
    let authenticationService = TestBed.inject(AuthenticationService);
    const spy = spyOn(authenticationService, "getCurrentUser").and.returnValue(null);
    fixture.detectChanges();

    let logout = fixture.debugElement.query(By.css('li[id="logOut"]'));
    let scan = fixture.debugElement.query(By.css('li[id="scan"]'));
    let dashboard = fixture.debugElement.query(By.css('li[id="dashboard"]'));
    let profile = fixture.debugElement.query(By.css('li[id="profile"]'));
    let management = fixture.debugElement.query(By.css('li[id="management"]'));
    let login = fixture.debugElement.query(By.css('li[id="login"]'));
    let information = fixture.debugElement.query(By.css('li[id="information"]'));

    expect(scan).toBeFalsy();
    expect(logout).toBeFalsy();
    expect(dashboard).toBeFalsy();
    expect(profile).toBeFalsy();
    expect(management).toBeFalsy();

    expect(login).toBeTruthy();
    expect(information).toBeTruthy();
  });

});
