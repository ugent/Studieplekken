import {BrowserModule} from '@angular/platform-browser';
import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';

import {RouterModule} from '@angular/router';
import {AppRoutingModule} from './app-routing.module';

import {AppComponent} from './app.component';
import {LocationItemComponent} from './location-item/location-item.component';
import {DashboardComponent} from './dashboard/dashboard.component';
import {ProfileComponent} from './profile/profile.component';
import {LocationComponent} from './location/location.component';
import {LoginComponent} from './login/login.component';
import {RegistrationComponent} from './registration/registration.component';
import {ScanComponent} from './scan/scan.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ManagementComponent} from './management/management.component';
import {VerificationComponent} from './verification/verification.component';
import {DashboardBoardComponent} from './dashboard-board/dashboard-board.component';
import {LockerOverviewComponent} from './locker-overview/locker-overview.component';
import {LocationOverviewComponent} from './location-overview/location-overview.component';
import {CalendarOverviewComponent} from './calendar-overview/calendar-overview.component';
import {InformationComponent} from "./information/information.component";
import {PenaltiesComponent} from './penalties/penalties.component';
import {MyCalendarComponent} from './my-calendar/my-calendar.component';
import {CountdownComponent} from './countdown/countdown.component';

// import ngx-translate and the http loader
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from '@angular/common/http';
import {NgxGaugeModule} from 'ngx-gauge';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {NgxChartsModule} from '@swimlane/ngx-charts';
import {ErrorComponent} from './error/error.component';
import {EditProfileComponent} from './edit-profile/edit-profile.component';
import {UserOverviewComponent} from './user-overview/user-overview.component';
import {LocationOverviewManComponent} from './location-overview-man/location-overview-man.component';
import {ValidatorsDirective} from './shared/validators.directive';
import {LocationReservationOverviewComponent} from './location-reservation-overview/location-reservation-overview.component';
import {LockerReservationOverviewComponent} from './locker-reservation-overview/locker-reservation-overview.component';
import {CalendarModule, DateAdapter} from "angular-calendar";
import {adapterFactory} from "angular-calendar/date-adapters/date-fns";
import {SearchbarComponent} from './searchbar/searchbar.component';
import {RouteGuardService} from "../services/route-guard.service";
import {AuthenticationService} from "../services/authentication.service";
import {LocationService} from "../services/location.service";
import {LocationReservationService} from "../services/location-reservation.service";
import {LockerReservationService} from "../services/locker-reservation.service";
import {AutoFocusDirective} from './shared/auto-focus.directive';
import {XsfrInterceptor} from "../http-interceptors/xsfr-interceptor";
import {RecaptchaFormsModule, RecaptchaModule} from "ng-recaptcha";
import {LockerOverviewManComponent} from './locker-overview-man/locker-overview-man.component';
import {UserOverviewPenaltyComponent} from './user-overview-penalty/user-overview-penalty.component';
import {UserOverviewPenaltyAddComponent} from './user-overview-penalty-add/user-overview-penalty-add.component';
import {ProfilePenaltiesOverviewComponent} from './profile-penalties-overview/profile-penalties-overview.component';
import {ScanForStudentComponent} from './scan-for-student/scan-for-student.component';
import {LoginGuardService} from "../services/login-guard.service";
import { VerifyComponent } from './verify/verify.component';
import { ManagementHelpComponent } from './management-help/management-help.component';
import { ProcessComponent } from './process/process.component';
import { ProfileHelpComponent } from './profile-help/profile-help.component';


@NgModule({
  declarations: [
    AppComponent,
    LocationItemComponent,
    DashboardComponent,
    ProfileComponent,
    LocationComponent,
    LoginComponent,
    RegistrationComponent,
    ScanComponent,
    ManagementComponent,
    VerificationComponent,
    DashboardBoardComponent,
    LockerOverviewComponent,
    LocationOverviewComponent,
    InformationComponent,
    CalendarOverviewComponent,
    ErrorComponent,
    UserOverviewComponent,
    LocationOverviewManComponent,
    UserOverviewComponent,
    EditProfileComponent,
    ValidatorsDirective,
    LocationReservationOverviewComponent,
    LockerReservationOverviewComponent,
    SearchbarComponent,
    AutoFocusDirective,
    PenaltiesComponent,
    MyCalendarComponent,
    CountdownComponent,
    LockerOverviewManComponent,
    UserOverviewPenaltyComponent,
    UserOverviewPenaltyAddComponent,
    ProfilePenaltiesOverviewComponent,
    ScanForStudentComponent,
    VerifyComponent,
    ManagementHelpComponent,
    ProcessComponent,
    ProfileHelpComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    RouterModule.forChild([
      {
        path: 'profile', component: ProfileComponent,
        canActivate: [RouteGuardService], children: [
          {path: '', component: EditProfileComponent, outlet: 'sub'},
          {path: 'edit', component: EditProfileComponent, outlet: 'sub'},
          {path: 'location', component: LocationOverviewComponent, outlet: 'sub'},
          {path: 'locker', component: LockerOverviewComponent, outlet: 'sub'},
          {path: 'calendar', component: MyCalendarComponent, outlet: 'sub'},
          {path: 'penalties', component: ProfilePenaltiesOverviewComponent, outlet: 'sub'},
          {path: 'help', component: ProfileHelpComponent, outlet: 'sub'}
        ]
      }, {
        path: 'profile/:id', component: ProfileComponent,
        canActivate: [RouteGuardService], children: [
          {path: '', component: EditProfileComponent, outlet: 'sub'},
          {path: 'edit', component: EditProfileComponent, outlet: 'sub'},
          {path: 'location', component: LocationOverviewComponent, outlet: 'sub'},
          {path: 'locker', component: LockerOverviewComponent, outlet: 'sub'},
          {path: 'calendar', component: MyCalendarComponent, outlet: 'sub'}]
      }
      , {
        path: 'management', component: ManagementComponent,
        canActivate: [RouteGuardService], children: [
          {path: '', component: UserOverviewComponent, outlet: 'sub'},
          {path: 'users', component: UserOverviewComponent, outlet: 'sub'},
          {path: 'locations', component: LocationOverviewManComponent, outlet: 'sub'},
          {path: 'locationreservations', component: LocationReservationOverviewComponent, outlet: 'sub'},
          {path: 'lockerreservations', component: LockerReservationOverviewComponent, outlet: 'sub'},
          {path: 'locationcalendar', component: CalendarOverviewComponent, outlet: 'sub'},
          {path: 'penalties', component: PenaltiesComponent, outlet: 'sub'},
          {path: 'lockers', component: LockerOverviewManComponent, outlet: 'sub'},
          {path: 'help', component: ManagementHelpComponent, outlet: 'sub'}
        ]
      },

    ]),
    RouterModule.forRoot([
      {path: 'dashboard', component: DashboardComponent, canActivate: [RouteGuardService]},
      {path: 'location/:name', component: LocationComponent, canActivate: [RouteGuardService]},
      {path: '', redirectTo: '/information', pathMatch: 'full'},
      {path: 'login', component: LoginComponent, canActivate: [LoginGuardService]},
      {path: 'registration', component: RegistrationComponent, canActivate: [LoginGuardService]},
      {path: 'verification', component: VerificationComponent},
      {path: 'management', component: ManagementComponent, canActivate: [RouteGuardService]},
      {path: 'scan', component: ScanComponent, canActivate: [RouteGuardService]},
      {path: 'information', component: InformationComponent},
      {path: 'scanStudent/:location', component: ScanForStudentComponent, canActivate: [RouteGuardService]},
      {path: 'verify/:code', component: VerifyComponent},
      {path: '**', component: ErrorComponent},
      {path: 'process', component: ProcessComponent}
    ],),
    FormsModule,

    // configure the imports
    HttpClientModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient]
      }
    }),
    ReactiveFormsModule,
    NgxGaugeModule,
    NgxChartsModule,
    BrowserAnimationsModule,
    CalendarModule.forRoot({provide: DateAdapter, useFactory: adapterFactory}),
    RecaptchaModule,
    RecaptchaFormsModule
  ],
  providers: [RouteGuardService, LoginGuardService, AuthenticationService, LocationService, TranslateService, LocationReservationService, LockerReservationService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: XsfrInterceptor,
      multi: true
    },
  ],
  bootstrap: [AppComponent],
  exports: [RouterModule],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class AppModule {
}

// required for AOT compilation
export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/');
}
