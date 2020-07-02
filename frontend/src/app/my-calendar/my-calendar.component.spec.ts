import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MyCalendarComponent } from './my-calendar.component';
import {AuthenticationService} from '../../services/authentication.service';
import {TranslateModule, TranslateService, TranslateStore} from '@ngx-translate/core';
import {LocationReservationService} from '../../services/location-reservation.service';
import {HttpClientModule} from '@angular/common/http';
import {CalendarDatePipe} from 'angular-calendar/modules/common/calendar-date.pipe';
import {CalendarDateFormatter, CalendarModule, CalendarMomentDateFormatter, DateAdapter, MOMENT} from 'angular-calendar';
import {adapterFactory} from 'angular-calendar/date-adapters/date-fns';
import {IUser} from '../../interfaces/IUser';
import AuthenticationServiceStub from '../../services/stubs/AuthenticationServiceStub';
import LocationReservationServiceStub from '../../services/stubs/LocationReservationServiceStub';

describe('MyCalendarComponent', () => {
  let component: MyCalendarComponent;
  let fixture: ComponentFixture<MyCalendarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MyCalendarComponent ],
      providers: [ { provide: AuthenticationService, useClass: AuthenticationServiceStub}, TranslateService,
        { provide: LocationReservationService, useClass: LocationReservationServiceStub}, HttpClientModule, TranslateStore,
        ],
      imports: [HttpClientModule, TranslateModule.forChild(),  CalendarModule.forRoot(
        {
          provide: DateAdapter,
          useFactory: adapterFactory,
        }
      ),]
      // import to do , CalendarModule.forRoot() provide dateAdapter
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MyCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
