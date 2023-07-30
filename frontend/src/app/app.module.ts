import {BrowserModule} from '@angular/platform-browser';
import {NgModule, SecurityContext} from '@angular/core';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {
    HttpClient,
    HttpClientModule,
    HTTP_INTERCEPTORS,
} from '@angular/common/http';
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {RouterModule, Routes} from '@angular/router';
import {DashboardComponent} from './components/dashboard/dashboard.component';
import {ProfileComponent} from './components/profile/profile.component';
import {ScanComponent} from './components/scan/scan.component';
import {ManagementComponent} from './components/management/management.component';
import {InformationComponent} from './components/information/information.component';
import {NavigationComponent} from './components/navigation/navigation.component';
import {MarkdownModule} from 'ngx-markdown';
import {DashboardItemComponent} from './components/dashboard/dashboard-item/dashboard-item.component';
import {CalendarComponent} from './components/calendar/calendar.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {CalendarModule, DateAdapter} from 'angular-calendar';
import {adapterFactory} from 'angular-calendar/date-adapters/date-fns';
import {ProfileOverviewComponent} from './components/profile/profile-overview/profile-overview.component';
import {ProfileReservationsComponent} from './components/profile/profile-reservations/profile-reservations.component';
import {ProfileCalendarComponent} from './components/profile/profile-calendar/profile-calendar.component';
import {ProfilePenaltiesComponent} from './components/profile/profile-penalties/profile-penalties.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ProfileChangePasswordComponent} from './components/profile/profile-change-password/profile-change-password.component';
import {LocationsManagementComponent} from './components/management/locations-management/locations-management.component';
import {UsersManagementComponent} from './components/management/users-management/users-management.component';
import {LocationDetailsManagementComponent} from './components/management/locations-management/location-details-management/location-details-management.component';
import {DetailsFormComponent} from './components/management/locations-management/location-details-management/details-form/details-form.component';
import {LocationCalendarComponent} from './components/management/locations-management/location-details-management/location-calendar/location-calendar.component';
import {FlatpickrModule} from 'angularx-flatpickr';
import {LockersTableComponent} from './components/management/locations-management/location-details-management/lockers-table/lockers-table.component';
import {UserDetailsManagementComponent} from './components/management/users-management/user-details-management/user-details-management.component';
// tslint:disable-next-line:max-line-length
import {UserDetailsFormComponent} from './components/management/users-management/user-details-management/user-details-form/user-details-form.component';
import {UserRolesComponent} from './components/management/users-management/user-details-management/user-roles/user-roles.component';
import {LocationDescriptionComponent} from './components/management/locations-management/location-details-management/location-description/location-description.component';
import {CKEditorModule} from '@ckeditor/ckeditor5-angular';
import {TagsManagementComponent} from './components/management/tags-management/tags-management.component';
import {LocationTagsManagementComponent} from './components/management/locations-management/location-details-management/location-tags-management/location-tags-management.component';
import {MatSelectModule} from '@angular/material/select';
import {LoginComponent} from './components/login/login.component';
import {AuthoritiesManagementComponent} from './components/management/authorities-management/authorities-management.component';
import {UserAuthoritiesManagementComponent} from './components/management/users-management/user-details-management/user-authorities-management/user-authorities-management.component';
import {AuthorityUsersManagementComponent} from './components/management/authorities-management/authority-users-management/authority-users-management.component';
import {DatePipe, NgOptimizedImage, registerLocaleData} from '@angular/common';
import {MatDialogModule} from '@angular/material/dialog';
import localeNl from '@angular/common/locales/nl-BE';
import {NgxMatDatetimePickerModule} from '@angular-material-components/datetime-picker';
import {NgxMatMomentModule} from '@angular-material-components/moment-adapter';
import {MatInputModule} from '@angular/material/input';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MomentDateComponent} from './components/shared/inputs/moment-date/moment-date.component';
import {FormatStatusPipe} from './extensions/pipes/FormatStatusPipe';
import {FormatActionPipe} from './extensions/pipes/FormatActionPipe';
import {MomentDateTimeComponent} from './components/shared/inputs/moment-datetime/moment-datetime.component';
import {LocationOpeningperiodDialogComponent} from './components/management/locations-management/location-details-management/location-calendar/location-openingperiod-dialog/location-openingperiod-dialog.component';
import {BuildingManagementComponent} from './components/management/building-management/building-management.component';
import {MatChipsModule} from '@angular/material/chips';
import {MatTabsModule} from '@angular/material/tabs';
import {LocationReservationsComponent} from './components/management/locations-management/location-details-management/location-calendar/location-reservations/location-reservations/location-reservations.component';
import {MomentTimeslotSizeComponent} from './components/shared/inputs/moment-timeslot-size/moment-timeslot-size.component';
import {AdminsManagementComponent} from './components/management/admins-management/admins-management.component';
import {FlexLayoutModule} from '@angular/flex-layout';
// tslint:disable-next-line:max-line-length
import {TimeslotTableComponent} from './components/management/locations-management/location-details-management/timeslot-table/timeslot-table.component';
import {MatTooltipModule} from '@angular/material/tooltip';
import {OpeningHoursOverviewComponent} from './components/shared/opening-hours/opening-hours-overview.component';
import {ScanningLocationsComponent} from './components/scan/scanning-locations/scanning-locations.component';
import {ScanningLocationDetailsComponent} from './components/scan/scanning-location-details/scanning-location-details.component';
import {VolunteersManagementComponent} from './components/management/volunteers-management/volunteers-management.component';
import {VolunteerManagementPanelComponent} from './components/management/volunteers-management/volunteer-management-panel/volunteer-management-panel.component';
import {TokenInterceptor} from './extensions/services/authentication/token.interceptor';
import {HeaderComponent} from './stad-gent-components/header/header.component';
import {AccordeonComponent} from './stad-gent-components/molecules/accordeon/accordeon.component';
import {DropdownComponent} from './stad-gent-components/header/dropdown/dropdown.component';
import {FooterComponent} from './stad-gent-components/footer/footer.component';
import {SearchUserComponentComponent} from './components/shared/search-user/search-user-component.component';
import {SearchUserFormComponent} from './components/shared/search-user/search-user-form/search-user-form.component';
import {LocationAddTimeslotDialogComponent} from './components/management/locations-management/location-details-management/location-calendar/location-add-timeslot-dialog/location-add-timeslot-dialog.component';
import {ModalComponent} from './stad-gent-components/molecules/modal/modal.component';
import {QRCodeModule} from 'angularx-qrcode';
import {QRCodeComponent} from './components/shared/qrcode/qrcode.component';
import {ScannerComponent} from './components/shared/scanner/scanner.component';
import {ZXingScannerModule} from '@zxing/ngx-scanner';
import {TableComponent} from './stad-gent-components/atoms/table/table.component';
import {DesktopTableComponent} from './stad-gent-components/atoms/table/desktop-table/desktop-table.component';
import {MobileTableComponent} from './stad-gent-components/atoms/table/mobile-table/mobile-table.component';
import {EntryComponent} from './components/entry/entry.component';
import {ImpersonateInterceptor} from './extensions/services/authentication/impersonate.interceptor';
import {ActionLogComponent} from './components/management/action-log/action-log.component';
import {AfterReservationComponent} from './components/dashboard/location-reservation/after-reservation/after-reservation.component';
import {PenaltyTableComponent} from './components/penalties/penalty-table/penalty-table.component';
import {UserPenaltyManagerComponent} from './components/penalties/user-penalty-manager/user-penalty-manager.component';
import {PenaltiesManagementComponent} from './components/management/penalties-management/penalties-management.component';
import {StatsComponent} from './components/management/stats/stats.component';
import {WaffleComponent} from './components/management/stats/waffle/waffle.component';
import {LocationReminderComponent} from './components/management/locations-management/location-details-management/location-reminder/location-reminder.component';
import {TokensComponent} from './components/management/tokens/tokens.component';
import {AuthorizationGuardService} from './extensions/services/guard/authorization/authorization-guard.service';
import { ChartComponent } from './components/management/stats/chart/chart.component';
import {BarCodeComponent} from './components/shared/barcode/barcode.component';
import {NgxBarcodeModule} from 'ngx-barcode';
import { LocationReservationComponent } from './components/dashboard/location-reservation/location-reservation.component';
import { MapComponent } from './components/dashboard/location-reservation/map/map.component';


// AoT requires an exported function for factories
export function HttpLoaderFactory(http: HttpClient): TranslateHttpLoader {
    return new TranslateHttpLoader(http);
}

registerLocaleData(localeNl, 'nl');

const routes: Routes = [
    {
        path: 'login',
        component: LoginComponent
    },
    {
        path: 'navigation',
        component: NavigationComponent
    },
    {
        path: 'dashboard',
        component: DashboardComponent
    },
    {
        path: 'dashboard/:locationId',
        component: LocationReservationComponent
    },
    {
        path: 'profile',
        component: ProfileComponent,
        canActivate: [AuthorizationGuardService],
        data: {
            guards: [['user']]
        },
        children: [
            {
                path: '',
                redirectTo: 'overview',
                pathMatch: 'full',
            },
            {
                path: 'overview',
                component: ProfileOverviewComponent,
            },
            {
                path: 'reservations',
                component: ProfileReservationsComponent
            },
            {
                path: 'calendar',
                component: ProfileCalendarComponent
            },
            {
                path: 'password',
                component: ProfileChangePasswordComponent
            },
            {
                path: 'penalties',
                component: ProfilePenaltiesComponent
            },
        ],
    },

    {
        path: 'scan',
        component: ScanComponent,
        canActivate: [AuthorizationGuardService],
        data:  {
            guards: [['scanner']]
        },
        children: [
            {
                path: '',
                redirectTo: 'locations',
                pathMatch: 'full'
            },
            {
                path: 'locations',
                component: ScanningLocationsComponent,
            },
            {
                path: 'locations/:locationId',
                component: ScanningLocationDetailsComponent,
            },
        ],
    },
    {
        path: 'management',
        component: ManagementComponent,
        data: {
            guards: [['authorities']]
        },
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
                data:  {
                    guards: [['authorities']]
                },
            },
            {
                path: 'locations/:locationId',
                component: LocationDetailsManagementComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['authorities']]
                },
            },
            {
                path: 'locations/:locationId/timeslot/:seqnr',
                component: TimeslotTableComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['authorities']]
                },
            },
            {
                path: 'buildings',
                component: BuildingManagementComponent,
                data:  {
                    guards: [['authorities']]
                }
            },
            {
                path: 'tags',
                component: TagsManagementComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['authorities']]
                }
            },
            {
                path: 'users',
                component: UsersManagementComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['authorities']]
                }
            },
            {
                path: 'users/:id',
                component: UserDetailsManagementComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['authorities']]
                }
            },
            {
                path: 'authorities',
                component: AuthoritiesManagementComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['admin']]
                }
            },
            {
                path: 'authorities/:authorityId',
                component: AuthorityUsersManagementComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['admin']]
                }
            },
            {
                path: 'admins',
                component: AdminsManagementComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['admin']]
                }
            },
            {
                path: 'volunteers',
                component: VolunteersManagementComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['authorities']]
                }
            },
            {
                path: 'penalties',
                component: PenaltiesManagementComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['admin']]
                }
            },
            {
                path: 'actionlog',
                component: ActionLogComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['admin']]
                }
            },
            {
                path: 'stats',
                component: StatsComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['admin']]
                }
            },
            {
                path: 'tokens',
                component: TokensComponent,
                canActivate: [AuthorizationGuardService],
                data:  {
                    guards: [['admin']]
                }
            },
        ],
    },
    {
        path: 'information',
        component: InformationComponent
    },
    {
        path: 'opening/overview/:year/:weekNr',
        component: OpeningHoursOverviewComponent
    },
    {
        path: '',
        component: EntryComponent,
        pathMatch: 'full',
    },
    // TODO: create PageNotFoundController
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
        ChartComponent,
        BarCodeComponent,
        LocationReservationComponent,
        MapComponent,
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        RouterModule.forRoot(routes, {relativeLinkResolution: 'legacy'}),
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
        ZXingScannerModule,
        NgxBarcodeModule,
        NgOptimizedImage
    ],
    providers: [
        FormatStatusPipe,
        FormatActionPipe,
        DatePipe,
        {provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true},
        {provide: HTTP_INTERCEPTORS, useClass: ImpersonateInterceptor, multi: true},
    ],
    bootstrap: [AppComponent],
})
export class AppModule {
}
