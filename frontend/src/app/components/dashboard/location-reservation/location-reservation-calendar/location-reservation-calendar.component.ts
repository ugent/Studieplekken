import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {User} from '../../../../model/User';
import {CalendarEvent} from 'angular-calendar';
import {Location} from '../../../../model/Location';
import {Timeslot, timeslotEquals} from '../../../../model/Timeslot';
import {combineLatest, Observable} from 'rxjs';
import {first, tap} from 'rxjs/operators';
import {map} from 'rxjs/internal/operators/map';
import {Moment} from 'moment/moment';
import {ModalComponent} from '../../../stad-gent-components/molecules/modal/modal.component';
import {LocationReservation} from '../../../../model/LocationReservation';
import {
    LocationReservationsService
} from '../../../../services/api/location-reservations/location-reservations.service';

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

    modifiedReservations(): boolean {
        return this.newReservations.length > 0 || this.removedReservations.length > 0;
    }

    canMakeReservation(): boolean {
        return this.user.isLoggedIn() && this.user.penaltyPoints < 100;
    }

    commitReservations(): void {
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
            first(), tap(() => {
                this.committedReservations.emit();
            }), map(([res]) =>
                res
            )
        );
    }
}
