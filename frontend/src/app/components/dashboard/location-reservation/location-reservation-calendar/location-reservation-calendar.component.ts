import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {User} from '@/model/User';
import {CalendarEvent} from 'angular-calendar';
import {Location} from '@/model/Location';
import {Timeslot} from '@/model/Timeslot';
import {combineLatest, Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {map} from 'rxjs/internal/operators/map';
import {Moment} from 'moment/moment';
import {ModalComponent} from '../../../stad-gent-components/molecules/modal/modal.component';
import {LocationReservation} from '@/model/LocationReservation';
import {
    LocationReservationsService
} from '@/services/api/location-reservations/location-reservations.service';
import { penaltyPointsLimit } from '@/app.constants';

@Component({
    selector: 'app-location-reservation-calendar',
    templateUrl: './location-reservation-calendar.component.html',
    styleUrls: ['./location-reservation-calendar.component.scss']
})
export class LocationReservationCalendarComponent {

    @ViewChild('commitModal') commitModal: ModalComponent;
    @ViewChild('afterModal') afterModal: ModalComponent;

    @Input() isReservable: boolean;
    @Input() user: User;
    @Input() location: Location;
    @Input() newReservations: LocationReservation[] = [];
    @Input() removedReservations: LocationReservation[] = [];
    @Input() events: CalendarEvent[] = [];

    @Output() committedReservations = new EventEmitter<void>();
    @Output() timeslotPicked = new EventEmitter<{ timeslot: Timeslot }>();

    protected creatorObs$: Observable<Moment[]>;

    constructor(
        private locationReservationsService: LocationReservationsService
    ) {
    }

    /**
     * Checks if there are any modified reservations.
     *
     * This method returns `true` if there are any new or removed reservations,
     * indicating that the reservations have been modified.
     *
     * @returns {boolean} `true` if there are new or removed reservations, otherwise `false`.
     */
    public hasModifiedReservations(): boolean {
        return this.newReservations.length > 0 || this.removedReservations.length > 0;
    }

    /**
     * Determines if the user can make a reservation.
     * 
     * @returns {boolean} `true` if the user is logged in and has fewer than 100 penalty points, otherwise `false`.
     */
    public canMakeReservation(): boolean {
        return this.user.isLoggedIn() && this.user.penaltyPoints < penaltyPointsLimit;
    }

    /**
     * Commits the new and removed reservations by sending them to the server.
     * Closes the commit modal and opens the after modal.
     * Emits an event after the reservations have been successfully committed.
     * 
     * @returns {void}
     */
    public commitReservations(): void {
        this.commitModal.close();
        this.afterModal.open();

        this.creatorObs$ = combineLatest([
            this.locationReservationsService.postLocationReservations(
                this.newReservations
            ),
            this.locationReservationsService.deleteLocationReservations(
                this.removedReservations
            )
        ]).pipe(
            tap(() => this.committedReservations.emit()),
            map(([reservationTimes]) => reservationTimes)
        );
    }
}
