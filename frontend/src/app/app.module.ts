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
import {ReactiveFormsModule} from '@angular/forms';
import { ProfileChangePasswordComponent } from './profile/profile-change-password/profile-change-password.component';
import { ProfileLocationReservationsComponent } from './profile/profile-reservations/profile-location-reservations/profile-location-reservations.component';
import { ProfileLockerReservationsComponent } from './profile/profile-reservations/profile-locker-reservations/profile-locker-reservations.component';

// AoT requires an exported function for factories
export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader {
  return new TranslateHttpLoader(http);
}

const routes: Routes = [
  {path: 'dashboard', component: DashboardComponent},
  {path: 'dashboard/:locationName', component: LocationDetailsComponent},
  {path: 'profile', component: ProfileComponent,
    children: [
      {path: '', redirectTo: 'overview', pathMatch: 'full'},
      {path: 'overview', component: ProfileOverviewComponent},
      {path: 'reservations', component: ProfileReservationsComponent},
      {path: 'calendar', component: ProfileCalendarComponent},
      {path: 'password', component: ProfileChangePasswordComponent},
      {path: 'penalties', component: ProfilePenaltiesComponent}
    ]},
  {path: 'scan', component: ScanComponent},
  {path: 'management', component: ManagementComponent},
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
    ProfileLockerReservationsComponent
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
        ReactiveFormsModule
    ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
