import {async, ComponentFixture, fakeAsync, getTestBed, TestBed, tick} from '@angular/core/testing';

import {LoginComponent} from './login.component';
import {Router} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {FormBuilder} from '@angular/forms';
import {TranslateModule, TranslatePipe, TranslateService, TranslateStore} from '@ngx-translate/core';
import {AuthenticationService} from '../../services/authentication.service';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';
import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';
import {Location} from '@angular/common';
import {DashboardBoardComponent} from '../dashboard-board/dashboard-board.component';
import {RegistrationComponent} from '../registration/registration.component';

export function newEvent(eventName: string, bubbles = false, cancelable = false) {
  let evt = document.createEvent('CustomEvent');  // MUST be 'CustomEvent'
  evt.initCustomEvent(eventName, bubbles, cancelable, null);
  return evt;
}

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  const routerSpy = jasmine.createSpyObj('Router', ['navigateByUrl']);
  let location: Location;
  let router: Router;
  // create new instance of FormBuilder
  const formBuilder: FormBuilder = new FormBuilder();

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LoginComponent],
      providers: [{provide: FormBuilder, useValue: formBuilder}, TranslateService, TranslateStore, {provide: AuthenticationService, useClass: AuthenticationServiceStub},
      ],
      imports: [TranslateModule.forChild(), RouterTestingModule.withRoutes([{path: 'dashboard', component: DashboardBoardComponent}, {
        path: 'registration', component: RegistrationComponent
      }])]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    router = TestBed.get(Router);
    location = TestBed.get(Location);
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    component.ngOnInit();
    fixture.detectChanges();
    router.initialNavigation();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should navigate to registration when register is clicked', fakeAsync(() => {
    const bannerDe: DebugElement = fixture.debugElement;
    const bannerEl: HTMLElement = bannerDe.nativeElement;
    component.cas = false;
    fixture.detectChanges();
    tick();
    let btn = fixture.debugElement.query(By.css('#register'));
    btn.triggerEventHandler('click', null);

    fixture.detectChanges();
    tick();
    expect(router.url).toBe('/registration', 'page should have rerouted to dashboard');
  }));


  it('should login when correct info is given', fakeAsync(() => {
    component.loginForm.controls['mail'].setValue('correct');
    component.loginForm.controls['password'].setValue('correct');

    //fixture.componentInstance.loginForm.value.mail = 'correct';
    //fixture.componentInstance.loginForm.value.password = 'correct';
    expect(fixture.componentInstance.loginForm.value.mail).toBe('correct',
      'mail should be filled in');
    expect(fixture.componentInstance.loginForm.value.password).toBe('correct',
      'password should be filled in');


    component.login(component.loginForm.value);

    fixture.detectChanges();
    tick();
    expect(router.url).toBe('/dashboard', 'page should have rerouted to dashboard');

  }));

});
