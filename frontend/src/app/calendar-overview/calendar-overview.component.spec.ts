import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CalendarOverviewComponent } from './calendar-overview.component';
import {LocationService} from '../../services/location.service';
import {TranslateModule, TranslateService, TranslateStore} from '@ngx-translate/core';
import {HttpClientModule} from '@angular/common/http';
import {CalendarModule, DateAdapter} from 'angular-calendar';
import {adapterFactory} from 'angular-calendar/date-adapters/date-fns';
import {AuthenticationService} from '../../services/authentication.service';
import LocationServiceStub from '../../services/stubs/LocationServiceStub';
import AuthenticationServiceStub from "../../services/stubs/AuthenticationServiceStub";

describe('CalendarOverviewComponent', () => {
  let component: CalendarOverviewComponent;
  let fixture: ComponentFixture<CalendarOverviewComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CalendarOverviewComponent ],
      providers: [{ provide: LocationService, useClass: LocationServiceStub}, {provide: AuthenticationService, useClass: AuthenticationServiceStub}, TranslateService, HttpClientModule, TranslateStore],
      imports: [HttpClientModule, TranslateModule.forChild(),  CalendarModule.forRoot(
        {
          provide: DateAdapter,
          useFactory: adapterFactory,
        }
      )]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CalendarOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
