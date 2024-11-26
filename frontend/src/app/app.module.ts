import {
    DetailsFormComponent
} from '@/components/management/locations-management/location-details-management/details-form/details-form.component';
import {
    LocationCalendarComponent
} from '@/components/management/locations-management/location-details-management/location-calendar/location-calendar.component';
import {
    LocationDetailsManagementComponent
} from '@/components/management/locations-management/location-details-management/location-details-management.component';
import {
    LocationsManagementComponent
} from '@/components/management/locations-management/locations-management.component';
import {
    UserDetailsManagementComponent
} from '@/components/management/users-management/user-details-management/user-details-management.component';
import { UsersManagementComponent } from '@/components/management/users-management/users-management.component';
import {
    HTTP_INTERCEPTORS,
    HttpClient,
    HttpClientModule,
} from '@angular/common/http';
import { NgModule, SecurityContext } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule, Routes } from '@angular/router';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { CalendarModule, DateAdapter } from 'angular-calendar';
import { adapterFactory } from 'angular-calendar/date-adapters/date-fns';
import { FlatpickrModule } from 'angularx-flatpickr';
import { MarkdownModule } from 'ngx-markdown';
import { AppComponent } from './app.component';
import { CalendarComponent } from './components/calendar/calendar.component';
import { DashboardItemComponent } from './components/dashboard/dashboard-item/dashboard-item.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ManagementComponent } from './components/management/management.component';
import { NavigationComponent } from './components/navigation/navigation.component';
import { ProfileCalendarComponent } from './components/profile/profile-calendar/profile-calendar.component';
import {
    ProfileChangePasswordComponent
} from './components/profile/profile-change-password/profile-change-password.component';
import { ProfileOverviewComponent } from './components/profile/profile-overview/profile-overview.component';
import { ProfilePenaltiesComponent } from './components/profile/profile-penalties/profile-penalties.component';
import { ProfileReservationsComponent } from './components/profile/profile-reservations/profile-reservations.component';
import { ProfileComponent } from './components/profile/profile.component';
import { ScanComponent } from './components/scan/scan.component';
// tslint:disable-next-line:max-line-length
import { AdminsManagementComponent } from '@/components/management/admins-management/admins-management.component';
import {
    AuthoritiesManagementComponent
} from '@/components/management/authorities-management/authorities-management.component';
import {
    AuthorityUsersManagementComponent
} from '@/components/management/authorities-management/authority-users-management/authority-users-management.component';
import {
    BuildingManagementComponent
} from '@/components/management/building-management/building-management.component';
import {
    LocationOpeningperiodDialogComponent
} from '@/components/management/locations-management/location-details-management/location-calendar/location-openingperiod-dialog/location-openingperiod-dialog.component';
import {
    LocationReservationsComponent
} from '@/components/management/locations-management/location-details-management/location-calendar/location-reservations/location-reservations/location-reservations.component';
import {
    LocationDescriptionComponent
} from '@/components/management/locations-management/location-details-management/location-description/location-description.component';
import {
    LocationTagsManagementComponent
} from '@/components/management/locations-management/location-details-management/location-tags-management/location-tags-management.component';
import { TagsManagementComponent } from '@/components/management/tags-management/tags-management.component';
import {
    UserAuthoritiesManagementComponent
} from '@/components/management/users-management/user-details-management/user-authorities-management/user-authorities-management.component';
import {
    UserDetailsFormComponent
} from '@/components/management/users-management/user-details-management/user-details-form/user-details-form.component';
import {
    UserRolesComponent
} from '@/components/management/users-management/user-details-management/user-roles/user-roles.component';
import { NgxMatDatetimePickerModule } from '@angular-material-components/datetime-picker';
import { NgxMatMomentModule } from '@angular-material-components/moment-adapter';
import { DatePipe, NgOptimizedImage, registerLocaleData } from '@angular/common';
import localeNl from '@angular/common/locales/nl-BE';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { CKEditorModule } from '@ckeditor/ckeditor5-angular';
import { LoginComponent } from './components/login/login.component';
import { MomentDateComponent } from './components/shared/inputs/moment-date/moment-date.component';
import { MomentDateTimeComponent } from './components/shared/inputs/moment-datetime/moment-datetime.component';
import {
    MomentTimeslotSizeComponent
} from './components/shared/inputs/moment-timeslot-size/moment-timeslot-size.component';
import { FormatActionPipe } from './pipes/FormatActionPipe';
import { FormatStatusPipe } from './pipes/FormatStatusPipe';
// tslint:disable-next-line:max-line-length
import { ActionLogComponent } from '@/components/management/actions-managament/action-log.component';
import {
    LocationAddTimeslotDialogComponent
} from '@/components/management/locations-management/location-details-management/location-calendar/location-add-timeslot-dialog/location-add-timeslot-dialog.component';
import {
    LocationReminderComponent
} from '@/components/management/locations-management/location-details-management/location-reminder/location-reminder.component';
import {
    TimeslotTableComponent
} from '@/components/management/locations-management/location-details-management/timeslot-table/timeslot-table.component';
import {
    LocationVolunteersManagementComponent
} from '@/components/management/locations-management/location-volunteers-management/location-volunteers-management.component';
import {
    PenaltiesManagementComponent
} from '@/components/management/penalties-management/penalties-management.component';
import { TokensComponent } from '@/components/management/tokens-management/tokens.component';
import {
    VolunteerManagementPanelComponent
} from '@/components/management/volunteers-management/volunteer-management-panel/volunteer-management-panel.component';
import {
    VolunteersManagementComponent
} from '@/components/management/volunteers-management/volunteers-management.component';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ZXingScannerModule } from '@zxing/ngx-scanner';
import { QRCodeModule } from 'angularx-qrcode';
import { NgxBarcodeModule } from 'ngx-barcode';
import {
    AfterReservationComponent
} from './components/dashboard/location-reservation/after-reservation/after-reservation.component';
import {
    LocationReservationCalendarComponent
} from './components/dashboard/location-reservation/location-reservation-calendar/location-reservation-calendar.component';
import {
    LocationReservationDetailsComponent
} from './components/dashboard/location-reservation/location-reservation-details/location-reservation-details.component';
import {
    LocationReservationListComponent
} from './components/dashboard/location-reservation/location-reservation-list/location-reservation-list.component';
import {
    LocationReservationMapComponent
} from './components/dashboard/location-reservation/location-reservation-map/location-reservation-map.component';
import { LocationReservationComponent } from './components/dashboard/location-reservation/location-reservation.component';
import { MapComponent } from './components/dashboard/location-reservation/map/map.component';
import { EntryComponent } from './components/entry/entry.component';
import { FaqItemComponent } from './components/faq/faq-item/faq-item.component';
import { FaqSearchComponent } from './components/faq/faq-search/faq-search.component';
import { FaqSidebarItemComponent } from './components/faq/faq-sidebar/faq-sidebar-item/faq-sidebar-item.component';
import { FaqSidebarComponent } from './components/faq/faq-sidebar/faq-sidebar.component';
import { FaqComponent } from './components/faq/faq.component';
import { CategoriesManagementComponent } from './components/management/faq-management/categories-management/categories-management.component';
import { FaqManagementComponent } from './components/management/faq-management/faq-management.component';
import { ManagementTableComponent } from './components/management/shared/management-table/management-table.component';
import { ChartComponent } from './components/management/stats/chart/chart.component';
import { StatsComponent } from './components/management/stats/stats.component';
import { WaffleComponent } from './components/management/stats/waffle/waffle.component';
import { PenaltyTableComponent } from './components/penalties/penalty-table/penalty-table.component';
import { UserPenaltyManagerComponent } from './components/penalties/user-penalty-manager/user-penalty-manager.component';
import {
    ScanningLocationDetailsComponent
} from './components/scan/scanning-location-details/scanning-location-details.component';
import { ScanningLocationsComponent } from './components/scan/scanning-locations/scanning-locations.component';
import { BarCodeComponent } from './components/shared/barcode/barcode.component';
import { OpeningHoursOverviewComponent } from './components/shared/opening-hours/opening-hours-overview.component';
import { QRCodeComponent } from './components/shared/qrcode/qrcode.component';
import { ScannerComponent } from './components/shared/scanner/scanner.component';
import { SearchUserComponentComponent } from './components/shared/search-user/search-user-component.component';
import { SearchUserFormComponent } from './components/shared/search-user/search-user-form/search-user-form.component';
import {
    DesktopTableComponent
} from './components/stad-gent-components/atoms/table/desktop-table/desktop-table.component';
import { MobileTableComponent } from './components/stad-gent-components/atoms/table/mobile-table/mobile-table.component';
import { TableComponent } from './components/stad-gent-components/atoms/table/table.component';
import { FooterComponent } from './components/stad-gent-components/footer/footer.component';
import { DropdownComponent } from './components/stad-gent-components/header/dropdown/dropdown.component';
import { HeaderComponent } from './components/stad-gent-components/header/header.component';
import { AccordeonComponent } from './components/stad-gent-components/molecules/accordeon/accordeon.component';
import { ModalComponent } from './components/stad-gent-components/molecules/modal/modal.component';
import { TeaserComponent } from './components/stad-gent-components/molecules/teaser/teaser.component';
import { EmptyComponent } from './components/status/empty/empty.component';
import { LoadingComponent } from './components/status/loading/loading.component';
import { ImpersonateInterceptor } from './services/authentication/impersonate.interceptor';
import { TokenInterceptor } from './services/authentication/token.interceptor';
import { AuthorizationGuardService } from './services/guard/authorization/authorization-guard.service';
import { HighlightComponent } from './components/stad-gent-components/molecules/highlight/highlight.component';
import { ContactComponent } from './components/stad-gent-components/molecules/contact/contact.component';
import { NotFoundErrorPageComponent } from './components/error-pages/not-found-error-page/not-found-error-page.component';
import { UnauthorizedErrorPageComponent } from './components/error-pages/unauthorized-error-page/unauthorized-error-page.component';
import { CallToActionComponent } from './components/stad-gent-components/call-to-action/call-to-action.component';
import { CheckboxComponent } from './components/stad-gent-components/molecules/forms/checkbox/checkbox.component';

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
        children: [
            {
                path: '',
                component: DashboardComponent
            },
            {
                path: ':locationId',
                component: LocationReservationComponent
            }
        ]
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
                path: 'overview',
                component: ProfileOverviewComponent,
            }
        ],
    },

    {
        path: 'scan',
        component: ScanComponent,
        canActivate: [AuthorizationGuardService],
        data: {
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
        children: [
            {
                path: '',
                redirectTo: 'locations',
                pathMatch: 'full',
            },
            {
                path: 'locations',
                canActivate: [AuthorizationGuardService],
                data: {
                    guards: [['authorities']]
                },
                children: [
                    {
                        path: '',
                        component: LocationsManagementComponent
                    },
                    {
                        path: ':locationId',
                        component: LocationDetailsManagementComponent
                    },
                    {
                        path: ':locationId/timeslot/:seqnr',
                        component: TimeslotTableComponent
                    }
                ]
            },
            {
                path: 'buildings',
                component: BuildingManagementComponent,
                canActivate: [AuthorizationGuardService],
                data: {
                    guards: [['authorities']]
                }
            },
            {
                path: 'users',
                canActivate: [AuthorizationGuardService],
                data: {
                    guards: [['authorities']]
                },
                children: [
                    {
                        path: '',
                        component: UsersManagementComponent
                    },
                    {
                        path: ':id',
                        component: UserDetailsManagementComponent
                    }
                ]
            },
            {
                path: 'faq',
                component: FaqManagementComponent,
                canActivate: [AuthorizationGuardService],
                data: {
                    guards: [['admin']]
                }
            },
            {
                path: 'tags',
                component: TagsManagementComponent,
                canActivate: [AuthorizationGuardService],
                data: {
                    guards: [['authorities']]
                }
            },
            {
                path: 'authorities',
                canActivate: [AuthorizationGuardService],
                data: {
                    guards: [['admin']]
                },
                children: [
                    {
                        path: '',
                        component: AuthoritiesManagementComponent
                    },
                    {
                        path: ':authorityId',
                        component: AuthorityUsersManagementComponent
                    }
                ]
            },
            {
                path: 'admins',
                component: AdminsManagementComponent,
                canActivate: [AuthorizationGuardService],
                data: {
                    guards: [['admin']]
                }
            },
            {
                path: 'volunteers',
                component: VolunteersManagementComponent,
                canActivate: [AuthorizationGuardService],
                data: {
                    guards: [['authorities']]
                }
            },
            {
                path: 'penalties',
                component: PenaltiesManagementComponent,
                canActivate: [AuthorizationGuardService],
                data: {
                    guards: [['admin']]
                }
            },
            {
                path: 'actionlog',
                component: ActionLogComponent,
                canActivate: [AuthorizationGuardService],
                data: {
                    guards: [['admin']]
                }
            },
            {
                path: 'stats',
                component: StatsComponent,
                canActivate: [AuthorizationGuardService],
                data: {
                    guards: [['admin']]
                }
            },
            {
                path: 'tokens',
                component: TokensComponent,
                canActivate: [AuthorizationGuardService],
                data: {
                    guards: [['admin']]
                }
            },
        ],
    },
    {
        path: 'faq',
        component: FaqComponent,
        children: [
            {
                path: '',
                component: FaqSearchComponent
            },
            {
                path: ':id',
                component: FaqItemComponent
            }
        ]
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
    {
        path: 'unauthorized',
        pathMatch: 'full',
        component: UnauthorizedErrorPageComponent
    },
    {
        path: '**',
        pathMatch: 'full',
        component: NotFoundErrorPageComponent
    }
];

@NgModule({
    declarations: [
        AppComponent,
        DashboardComponent,
        ProfileComponent,
        ScanComponent,
        ManagementComponent,
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
        ManagementTableComponent,
        LocationVolunteersManagementComponent,
        LocationReservationDetailsComponent,
        LocationReservationCalendarComponent,
        LocationReservationListComponent,
        LocationReservationMapComponent,
        FaqComponent,
        FaqSearchComponent,
        FaqItemComponent,
        FaqSidebarComponent,
        FaqSidebarItemComponent,
        LoadingComponent,
        EmptyComponent,
        TeaserComponent,
        FaqManagementComponent,
        CategoriesManagementComponent,
        HighlightComponent,
        ContactComponent,
        NotFoundErrorPageComponent,
        UnauthorizedErrorPageComponent,
        CallToActionComponent,
        CheckboxComponent,
    ],
    imports: [
        BrowserModule,
        RouterModule.forRoot(routes, {}),
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
    bootstrap: [AppComponent]
})
export class AppModule {
}
