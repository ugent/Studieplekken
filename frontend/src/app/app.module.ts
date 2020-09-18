import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {RouterModule, Routes} from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ProfileComponent } from './profile/profile.component';
import { ScanComponent } from './scan/scan.component';
import { ManagementComponent } from './management/management.component';
import { InformationComponent } from './information/information.component';
import { MarkdownModule} from 'ngx-markdown';
import { DashboardItemComponent } from './dashboard/dashboard-item/dashboard-item.component';
import { LocationDetailsComponent } from './dashboard/location-details/location-details.component';
import { CalendarComponent } from './calendar/calendar.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {CalendarModule, DateAdapter} from 'angular-calendar';
import {adapterFactory} from 'angular-calendar/date-adapters/date-fns';
import { ProfileOverviewComponent } from './profile/profile-overview/profile-overview.component';
import { ProfileReservationsComponent } from './profile/profile-reservations/profile-reservations.component';
import { ProfileCalendarComponent } from './profile/profile-calendar/profile-calendar.component';
import { ProfilePenaltiesComponent } from './profile/profile-penalties/profile-penalties.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { ProfileChangePasswordComponent } from './profile/profile-change-password/profile-change-password.component';
import { ProfileLocationReservationsComponent } from './profile/profile-reservations/profile-location-reservations/profile-location-reservations.component';
import { ProfileLockerReservationsComponent } from './profile/profile-reservations/profile-locker-reservations/profile-locker-reservations.component';
import { LocationsManagementComponent } from './management/locations-management/locations-management.component';
import { UsersManagementComponent } from './management/users-management/users-management.component';
import { ReservationsManagementComponent } from './management/reservations-management/reservations-management.component';
import { PenaltyEventsManagementComponent } from './management/penalty-events-management/penalty-events-management.component';
import { LocationDetailsManagementComponent } from './management/locations-management/location-details-management/location-details-management.component';
import { DetailsFormComponent } from './management/locations-management/location-details-management/details-form/details-form.component';
import { LocationCalendarComponent } from './management/locations-management/location-details-management/location-calendar/location-calendar.component';
import {FlatpickrModule} from 'angularx-flatpickr';
import { LockersCalendarComponent } from './management/locations-management/location-details-management/lockers-calendar/lockers-calendar.component';
import { LockersTableComponent } from './management/locations-management/location-details-management/lockers-table/lockers-table.component';
import { UserDetailsManagementComponent } from './management/users-management/user-details-management/user-details-management.component';
import { UserDetailsFormComponent } from './management/users-management/user-details-management/user-details-form/user-details-form.component';
import { UserRolesComponent } from './management/users-management/user-details-management/user-roles/user-roles.component';
import { UserDetailsManagementPenaltiesComponent } from './management/users-management/user-details-management/user-details-management-penalties/user-details-management-penalties.component';
import { LocationReservationsManagementComponent } from './management/reservations-management/location-reservations-management/location-reservations-management.component';
import { LockerReservationsManagementComponent } from './management/reservations-management/locker-reservations-management/locker-reservations-management.component';
import {ApplicationTypeGuardService} from './services/guard/functionality/application-type-guard/application-type-guard.service';
import {registerLocaleData} from '@angular/common';
import localeNl from '@angular/common/locales/nl-BE';

// AoT requires an exported function for factories
export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader {
  return new TranslateHttpLoader(http);
}

registerLocaleData(localeNl, 'nl');

const routes: Routes = [
  {path: 'dashboard', component: DashboardComponent},
  {path: 'dashboard/:locationName', component: LocationDetailsComponent},
  {
    path: 'profile', component: ProfileComponent,
    children: [
      {path: '', redirectTo: 'overview', pathMatch: 'full'},
      {path: 'overview', component: ProfileOverviewComponent},
      {
        path: 'reservations',
        component: ProfileReservationsComponent,
        canActivate: [ApplicationTypeGuardService],
        data: {
          applicationPart: 'reservations'
        }
      },
      {path: 'calendar', component: ProfileCalendarComponent},
      {path: 'password', component: ProfileChangePasswordComponent},
      {
        path: 'penalties',
        component: ProfilePenaltiesComponent,
        canActivate: [ApplicationTypeGuardService],
        data: {
          applicationPart: 'penalties'
        }
      }
    ]},
  {
    path: 'scan',
    component: ScanComponent,
    canActivate: [ApplicationTypeGuardService],
    data: {
      applicationPart: 'scanning'
    }
  },
  {
    path: 'management', component: ManagementComponent,
    children: [
      {path: '', redirectTo: 'locations', pathMatch: 'full'},
      {path: 'locations', component: LocationsManagementComponent},
      {path: 'locations/:locationName', component: LocationDetailsManagementComponent},
      {path: 'users', component: UsersManagementComponent},
      {path: 'users/:id', component: UserDetailsManagementComponent},
      {
        path: 'reservations',
        component: ReservationsManagementComponent,
        canActivate: [ApplicationTypeGuardService],
        data: {
          applicationPart: 'reservations'
        }
      },
      {
        path: 'penalties',
        component: PenaltyEventsManagementComponent,
        canActivate: [ApplicationTypeGuardService],
        data: {
          applicationPart: 'penalties'
        }
      }
    ]
  },
  {path: 'information', component: InformationComponent},
  {path: '', redirectTo: '/dashboard', pathMatch: 'full'}
  // , {path: '**', component: PageNotFoundController} TODO: create PageNotFoundController
];

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    ProfileComponent,
    ScanComponent,
    ManagementComponent,
    InformationComponent,
    DashboardItemComponent,
    LocationDetailsComponent,
    CalendarComponent,
    ProfileOverviewComponent,
    ProfileReservationsComponent,
    ProfileCalendarComponent,
    ProfilePenaltiesComponent,
    ProfileChangePasswordComponent,
    ProfileLocationReservationsComponent,
    ProfileLockerReservationsComponent,
    LocationsManagementComponent,
    UsersManagementComponent,
    ReservationsManagementComponent,
    PenaltyEventsManagementComponent,
    LocationDetailsManagementComponent,
    DetailsFormComponent,
    LocationCalendarComponent,
    LockersCalendarComponent,
    LockersTableComponent,
    UserDetailsManagementComponent,
    UserDetailsFormComponent,
    UserRolesComponent,
    UserDetailsManagementPenaltiesComponent,
    LocationReservationsManagementComponent,
    LockerReservationsManagementComponent
  ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        RouterModule.forRoot(routes),
        HttpClientModule,
        TranslateModule.forRoot({
            defaultLanguage: 'nl',
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [HttpClient]
            }
        }),
        MarkdownModule.forRoot(),
        BrowserAnimationsModule,
        CalendarModule.forRoot({
            provide: DateAdapter,
            useFactory: adapterFactory,
        }),
        ReactiveFormsModule,
        FormsModule,
        FlatpickrModule.forRoot()
    ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
