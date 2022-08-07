import { BrowserModule } from '@angular/platform-browser';
import { NgModule, SecurityContext } from '@angular/core';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {
  HttpClient,
  HttpClientModule,
  HTTP_INTERCEPTORS,
} from '@angular/common/http';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ProfileComponent } from './profile/profile.component';
import { ScanComponent } from './scan/scan.component';
import { ManagementComponent } from './management/management.component';
import { InformationComponent } from './information/information.component';
import { NavigationComponent } from './navigation/navigation.component';
import { MarkdownModule } from 'ngx-markdown';
import { DashboardItemComponent } from './dashboard/dashboard-item/dashboard-item.component';
import { LocationDetailsComponent } from './dashboard/location-details/location-details.component';
import { CalendarComponent } from './calendar/calendar.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CalendarModule, DateAdapter } from 'angular-calendar';
import { adapterFactory } from 'angular-calendar/date-adapters/date-fns';
import { ProfileOverviewComponent } from './profile/profile-overview/profile-overview.component';
import { ProfileReservationsComponent } from './profile/profile-reservations/profile-reservations.component';
import { ProfileCalendarComponent } from './profile/profile-calendar/profile-calendar.component';
import { ProfilePenaltiesComponent } from './profile/profile-penalties/profile-penalties.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ProfileChangePasswordComponent } from './profile/profile-change-password/profile-change-password.component';
import { LocationsManagementComponent } from './management/locations-management/locations-management.component';
import { UsersManagementComponent } from './management/users-management/users-management.component';
import { LocationDetailsManagementComponent } from './management/locations-management/location-details-management/location-details-management.component';
import { DetailsFormComponent } from './management/locations-management/location-details-management/details-form/details-form.component';
import { LocationCalendarComponent } from './management/locations-management/location-details-management/location-calendar/location-calendar.component';
import { FlatpickrModule } from 'angularx-flatpickr';
import { LockersTableComponent } from './management/locations-management/location-details-management/lockers-table/lockers-table.component';
import { UserDetailsManagementComponent } from './management/users-management/user-details-management/user-details-management.component';
import { UserDetailsFormComponent } from './management/users-management/user-details-management/user-details-form/user-details-form.component';
import { UserRolesComponent } from './management/users-management/user-details-management/user-roles/user-roles.component';
import { ApplicationTypeGuardService } from './services/guard/functionality/application-type-guard/application-type-guard.service';
import { LocationDescriptionComponent } from './management/locations-management/location-details-management/location-description/location-description.component';
import { CKEditorModule } from '@ckeditor/ckeditor5-angular';
import { TagsManagementComponent } from './management/tags-management/tags-management.component';
import { LocationTagsManagementComponent } from './management/locations-management/location-details-management/location-tags-management/location-tags-management.component';
import { MatSelectModule } from '@angular/material/select';
import { LoginComponent } from './login/login.component';
import { AuthorizationGuardService } from './services/guard/authentication/authorization-guard/authorization-guard.service';
import { AuthoritiesManagementComponent } from './management/authorities-management/authorities-management.component';
import { UserAuthoritiesManagementComponent } from './management/users-management/user-details-management/user-authorities-management/user-authorities-management.component';
import { AuthorityUsersManagementComponent } from './management/authorities-management/authority-users-management/authority-users-management.component';
import { registerLocaleData } from '@angular/common';
import { MatDialogModule } from '@angular/material/dialog';
import localeNl from '@angular/common/locales/nl-BE';
import { NgxMatDatetimePickerModule } from '@angular-material-components/datetime-picker';
import { NgxMatMomentModule } from '@angular-material-components/moment-adapter';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MomentDateComponent } from './shared/inputs/moment-date/moment-date.component';
import { FormatStatusPipe } from './shared/pipes/FormatStatusPipe';
import { FormatActionPipe } from './shared/pipes/FormatActionPipe';
import { MomentDateTimeComponent } from './shared/inputs/moment-datetime/moment-datetime.component';
import { LocationOpeningperiodDialogComponent } from './management/locations-management/location-details-management/location-calendar/location-openingperiod-dialog/location-openingperiod-dialog.component';
import { BuildingManagementComponent } from './management/building-management/building-management.component';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { LocationReservationsComponent } from './management/locations-management/location-details-management/location-calendar/location-reservations/location-reservations/location-reservations.component';
import { MomentTimeslotSizeComponent } from './shared/inputs/moment-timeslot-size/moment-timeslot-size.component';
import { AdminsManagementComponent } from './management/admins-management/admins-management.component';
import { FlexLayoutModule } from '@angular/flex-layout';
import { TimeslotTableComponent } from './management/locations-management/location-details-management/timeslot-table/timeslot-table.component';
import { MatTooltipModule } from '@angular/material/tooltip';
import { OpeningHoursOverviewComponent } from './miscellaneous/opening-hours-overview/opening-hours-overview.component';
import { ScanningLocationsComponent } from './scan/scanning-locations/scanning-locations.component';
import { ScanningLocationDetailsComponent } from './scan/scanning-location-details/scanning-location-details.component';
import { VolunteersManagementComponent } from './management/volunteers-management/volunteers-management.component';
import { VolunteerManagementPanelComponent } from './management/volunteers-management/volunteer-management-panel/volunteer-management-panel.component';
import { TokenInterceptor } from './services/authentication/token.interceptor';
import { HeaderComponent } from './stad-gent-components/header/header.component';
import { AccordeonComponent } from './stad-gent-components/molecules/accordeon/accordeon.component';
import { DropdownComponent } from './stad-gent-components/header/dropdown/dropdown.component';
import { FooterComponent } from './stad-gent-components/footer/footer.component';
import { SearchUserComponentComponent } from './shared/search-user-component/search-user-component.component';
import { SearchUserFormComponent } from './shared/search-user-component/search-user-form/search-user-form.component';
import { LocationAddTimeslotDialogComponent } from './management/locations-management/location-details-management/location-calendar/location-add-timeslot-dialog/location-add-timeslot-dialog.component';
import { ModalComponent } from './stad-gent-components/molecules/modal/modal.component';
import { QRCodeModule } from 'angularx-qrcode';
import { QRCodeComponent } from './miscellaneous/qrcode/qrcode.component';
import { ScannerComponent } from './miscellaneous/scanner/scanner.component';
import { ZXingScannerModule } from '@zxing/ngx-scanner';
import { TableComponent } from './stad-gent-components/atoms/table/table.component';
import { DesktopTableComponent } from './stad-gent-components/atoms/table/desktop-table/desktop-table.component';
import { MobileTableComponent } from './stad-gent-components/atoms/table/mobile-table/mobile-table.component';
import { EntryComponent } from './entry/entry.component';
import { ImpersonateInterceptor } from './services/authentication/impersonate.interceptor';
import { ActionLogComponent } from './management/action-log/action-log.component';
import { AfterReservationComponent } from './dashboard/location-details/modals/after-reservation/after-reservation.component';
import { PenaltyTableComponent } from './shared/penalties/penalty-table/penalty-table.component';
import { UserPenaltyManagerComponent } from './shared/penalties/user-penalty-manager/user-penalty-manager.component';
import { PenaltiesManagementComponent } from './management/penalties-management/penalties-management.component';
import { StatsComponent } from './management/stats/stats.component';
import { WaffleComponent } from './management/stats/waffle/waffle.component';
import { LocationReminderComponent } from './management/locations-management/location-details-management/location-reminder/location-reminder.component';
import { TokensComponent } from './management/tokens/tokens.component';


// AoT requires an exported function for factories
export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader {
  return new TranslateHttpLoader(http);
}

registerLocaleData(localeNl, 'nl');

const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [AuthorizationGuardService],
  },
  {
    path: 'navigation',
    component: NavigationComponent,
    // canActivate: [AuthorizationGuardService, ApplicationTypeGuardService],
  },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthorizationGuardService],
  },

  {
    path: 'dashboard/:locationId',
    component: LocationDetailsComponent,
    canActivate: [AuthorizationGuardService],
  },

  {
    path: 'profile',
    component: ProfileComponent,
    children: [
      {
        path: '',
        redirectTo: 'overview',
        pathMatch: 'full',
      },
      {
        path: 'overview',
        component: ProfileOverviewComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'reservations',
        component: ProfileReservationsComponent,
        canActivate: [AuthorizationGuardService, ApplicationTypeGuardService],
      },
      {
        path: 'calendar',
        component: ProfileCalendarComponent,
        canActivate: [AuthorizationGuardService, ApplicationTypeGuardService],
      },
      {
        path: 'password',
        component: ProfileChangePasswordComponent,
        canActivate: [AuthorizationGuardService, ApplicationTypeGuardService],
      },
      {
        path: 'penalties',
        component: ProfilePenaltiesComponent,
        canActivate: [AuthorizationGuardService, ApplicationTypeGuardService],
      },
    ],
  },

  {
    path: 'scan',
    component: ScanComponent,
    canActivate: [AuthorizationGuardService, ApplicationTypeGuardService],
    children: [
      {
        path: '',
        redirectTo: 'locations',
        pathMatch: 'full',
      },
      {
        path: 'locations',
        component: ScanningLocationsComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'locations/:locationId',
        component: ScanningLocationDetailsComponent,
        canActivate: [AuthorizationGuardService],
      },
    ],
  },

  {
    path: 'management',
    component: ManagementComponent,
    children: [
      {
        path: '',
        redirectTo: 'locations',
        pathMatch: 'full',
      },
      {
        path: 'locations',
        component: LocationsManagementComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'locations/:locationId',
        component: LocationDetailsManagementComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'locations/:locationId/timeslot/:seqnr',
        component: TimeslotTableComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'buildings',
        component: BuildingManagementComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'tags',
        component: TagsManagementComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'users',
        component: UsersManagementComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'users/:id',
        component: UserDetailsManagementComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'authorities',
        component: AuthoritiesManagementComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'authorities/:authorityId',
        component: AuthorityUsersManagementComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'admins',
        component: AdminsManagementComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'volunteers',
        component: VolunteersManagementComponent,
        canActivate: [AuthorizationGuardService],
      },
      {
        path: 'penalties',
        component: PenaltiesManagementComponent,
        canActivate: [AuthorizationGuardService]
      }
    ,
    {
        path: 'actionlog',
        component: ActionLogComponent,
        canActivate: [AuthorizationGuardService]
      },
      {
        path: 'stats',
        component: StatsComponent,
        canActivate: [AuthorizationGuardService]
      },
      {
        path: 'tokens',
        component: TokensComponent,
        canActivate: [AuthorizationGuardService]
      },
    ],
  },

  {
    path: 'information',
    component: InformationComponent,
    canActivate: [AuthorizationGuardService],
  },

  {
    path: 'opening/overview/:year/:weekNr',
    component: OpeningHoursOverviewComponent,
    canActivate: [AuthorizationGuardService],
  },

  {
    path: '',
    component: EntryComponent,
    pathMatch: 'full',
  },
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
    NavigationComponent,
    DashboardItemComponent,
    LocationDetailsComponent,
    CalendarComponent,
    ProfileOverviewComponent,
    ProfileReservationsComponent,
    ProfileCalendarComponent,
    ProfilePenaltiesComponent,
    ProfileChangePasswordComponent,
    LocationsManagementComponent,
    UsersManagementComponent,
    LocationDetailsManagementComponent,
    DetailsFormComponent,
    LocationCalendarComponent,
    LockersTableComponent,
    UserDetailsManagementComponent,
    UserDetailsFormComponent,
    UserRolesComponent,
    LocationDescriptionComponent,
    TagsManagementComponent,
    LocationTagsManagementComponent,
    LoginComponent,
    AuthoritiesManagementComponent,
    UserAuthoritiesManagementComponent,
    AuthorityUsersManagementComponent,
    MomentDateComponent,
    MomentDateTimeComponent,
    LocationOpeningperiodDialogComponent,
    BuildingManagementComponent,
    LocationReservationsComponent,
    MomentTimeslotSizeComponent,
    AdminsManagementComponent,
    TimeslotTableComponent,
    FormatStatusPipe,
    FormatActionPipe,
    OpeningHoursOverviewComponent,
    ScanningLocationsComponent,
    ScanningLocationDetailsComponent,
    VolunteersManagementComponent,
    VolunteerManagementPanelComponent,
    HeaderComponent,
    AccordeonComponent,
    DropdownComponent,
    FooterComponent,
    SearchUserComponentComponent,
    SearchUserFormComponent,
    LocationAddTimeslotDialogComponent,
    ModalComponent,
    QRCodeComponent,
    ScannerComponent,
    TableComponent,
    DesktopTableComponent,
    MobileTableComponent,
    EntryComponent,
    AfterReservationComponent,
    PenaltyTableComponent,
    UserPenaltyManagerComponent,
    PenaltiesManagementComponent,
    ActionLogComponent,
    AfterReservationComponent,
    StatsComponent,
    WaffleComponent,
    LocationReminderComponent,
    TokensComponent,
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
        deps: [HttpClient],
      },
    }),
    BrowserAnimationsModule,
    CalendarModule.forRoot({
      provide: DateAdapter,
      useFactory: adapterFactory,
    }),
    ReactiveFormsModule,
    FormsModule,
    FlatpickrModule.forRoot(),
    CKEditorModule,
    MatSelectModule,
    NgxMatDatetimePickerModule,
    NgxMatMomentModule,
    MatDatepickerModule,
    MatInputModule,
    MatDialogModule,
    MatCheckboxModule,
    MatChipsModule,
    FlexLayoutModule,
    MarkdownModule.forRoot({
      sanitize: SecurityContext.NONE,
    }),
    MatTabsModule,
    MatTooltipModule,
    QRCodeModule,
    ZXingScannerModule
  ],
  providers: [
    FormatStatusPipe,
    FormatActionPipe,
    { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ImpersonateInterceptor, multi: true },


  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
