import {Component, Input, OnInit, TemplateRef} from '@angular/core';
import {AuthenticationService} from '../../../extensions/services/authentication/authentication.service';
import {LocationReservationsService} from '../../../extensions/services/api/location-reservations/location-reservations.service';
import {LocationReservation, LocationReservationState} from '../../../extensions/model/LocationReservation';
import {Timeslot} from '../../../extensions/model/Timeslot';
import * as moment from 'moment';
import {User} from '../../../extensions/model/User';
import {Observable} from 'rxjs';
import {LocationService} from 'src/app/extensions/services/api/locations/location.service';
import {map} from 'rxjs/operators';
import {MatDialog} from '@angular/material/dialog';
import {TableDataService} from 'src/app/stad-gent-components/atoms/table/data-service/table-data-service.service';
import {TabularData} from 'src/app/stad-gent-components/atoms/table/tabular-data';
import {api} from '../../../extensions/services/api/endpoints';

@Component({
    selector: 'app-profile-reservations',
    templateUrl: './profile-reservations.component.html',
    styleUrls: ['./profile-reservations.component.scss'],
})
export class ProfileReservationsComponent implements OnInit {
    @Input() userObs?: Observable<User>;
    user: User;

    locationReservations: LocationReservation[] = [];

    locationReservationToDelete: LocationReservation = undefined;

    successGettingLocationReservations: boolean = undefined;
    successDeletingLocationReservation: boolean = undefined;

    constructor(
        private authenticationService: AuthenticationService,
        private locationReservationService: LocationReservationsService,
        private modalService: MatDialog,
        private locationService: LocationService,
        private tableDataService: TableDataService
    ) {
    }

    ngOnInit(): void {
        if (this.userObs) {
            this.userObs.subscribe((next) => {
                if (next.userId) {
                    this.user = next;
                    this.setup();
                }
            });
        } else {
            this.setup();
        }
    }

    setup(): void {
        // don't setup if user is not logged in (or logged in user isn't loaded yet)
        if (!this.authenticationService.userValue().isLoggedIn()) {
            return;
        }

        // let the user know that the location reservations are loading
        this.successGettingLocationReservations = null;

        // load the location reservations
        this.locationReservationsAndCalendarPeriodsObservable()
            .subscribe(
                (next) => {
                    this.successGettingLocationReservations = true;
                    this.locationReservations = next;
                },
                () => {
                    this.successGettingLocationReservations = false;
                });
    }

    prepareToDeleteLocationReservation(
        locationReservation: LocationReservation,
        template: TemplateRef<any>
    ): void {
        this.successDeletingLocationReservation = undefined;
        this.locationReservationToDelete = locationReservation;
        this.modalService.open(template, {panelClass: ['cs--cyan', 'bigmodal']});
    }

    deleteLocationReservation(): void {
        this.successDeletingLocationReservation = null;
        this.locationReservationService
            .deleteLocationReservation(this.locationReservationToDelete)
            .subscribe(
                () => {
                    this.successDeletingLocationReservation = true;
                    this.setup();
                    this.modalService.closeAll();
                },
                () => {
                    this.successDeletingLocationReservation = false;
                }
            );
    }

    // /******************
    // *   AUXILIARIES   *
    // *******************/
    closeModal(): void {
        this.modalService.closeAll();
    }

    locationReservationsAndCalendarPeriodsObservable(): Observable<LocationReservation[]> {
        if (this.user === undefined) {
            return this.authenticationService
                .getLocationReservations();
        } else {
            return this.locationReservationService
                .getLocationReservationsOfUser(this.user.userId);
        }
    }

    getLocation(locationReservation: LocationReservation): Observable<string> {
        return this.locationService.getLocation(locationReservation.timeslot.locationId).pipe(map(l => l.name));
    }

    sortedLocationReservations(lres: LocationReservation[]): Array<LocationReservation> {
        return Array.from(lres).sort((a, b) => a.timeslot.getStartMoment().isBefore(b.timeslot.getStartMoment()) ? 1 : -1);
    }

    getTabularData(locationReservations: LocationReservation[]): Observable<TabularData<LocationReservation>> {
        return this.tableDataService.reservationsToProfileTable(this.sortedLocationReservations(locationReservations));
    }

    getCalendarLink(): string {
        let user = this.user;
        if (!user) {
            this.authenticationService.user.subscribe(u => {
                user = u;
            });
        }
        return window.location.protocol + '//' +
            window.location.host + api.calendarLink.replace('{userId}', user.userId).replace('{calendarId}', user.calendarId);
    }

}
