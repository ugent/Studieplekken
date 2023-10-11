import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {BehaviorSubject, combineLatest, interval, Observable, ReplaySubject, Subject, Subscription, timer} from 'rxjs';
import {LocationService} from '../../../extensions/services/api/locations/location.service';
import {ActivatedRoute} from '@angular/router';
import {User} from '../../../model/User';
import {Location} from '../../../model/Location';
import {AuthenticationService} from '../../../extensions/services/authentication/authentication.service';
import {defaultTeaserImages} from '../../../app.constants';
import {BreadcrumbService} from '../../stad-gent-components/header/breadcrumbs/breadcrumb.service';
import {TranslateService} from '@ngx-translate/core';
import {TimeslotsService} from '../../../extensions/services/api/calendar-periods/timeslot.service';
import {LocationReservation} from '../../../model/LocationReservation';
import {map} from 'rxjs/internal/operators/map';
import {CalendarEvent} from 'angular-calendar';
import {Timeslot, timeslotEquals} from '../../../model/Timeslot';
import * as moment from 'moment';
import {
    TimeslotCalendarEventService
} from '../../../extensions/services/timeslots/timeslot-calendar-event/timeslot-calendar-event.service';
import {
    LocationReservationsService
} from '../../../extensions/services/api/location-reservations/location-reservations.service';
import {catchError, filter, first, mergeMap, share, tap} from 'rxjs/operators';
import {of} from 'rxjs/internal/observable/of';
import {ModalComponent} from '../../stad-gent-components/molecules/modal/modal.component';
import {AfterReservationComponent} from './after-reservation/after-reservation.component';

@Component({
    selector: 'app-location-reservation',
    templateUrl: './location-reservation.component.html',
    styleUrls: ['./location-reservation.component.scss']
})
export class LocationReservationComponent implements OnInit, OnDestroy {

    @ViewChild('commitModal') commitModal: ModalComponent;
    @ViewChild('afterModal') afterModal: AfterReservationComponent;

    protected contextSub$: Observable<any>;
    protected location: Location;
    protected user: User;

    protected showEdit = false;
    protected isNotFound = false;
    protected canMakeReservation = true;
    protected language = '';

    protected timeslots: Timeslot[] = [];
    protected events: CalendarEvent[] = [];
    protected allReservations: LocationReservation[] = [];
    protected newReservations: LocationReservation[] = [];
    protected removedReservations: LocationReservation[] = [];

    protected subscription: Subscription;

    constructor(
        private locationService: LocationService,
        private timeslotsService: TimeslotsService,
        private timeslotCalendarEventService: TimeslotCalendarEventService,
        private authenticationService: AuthenticationService,
        private breadcrumbService: BreadcrumbService,
        private translateService: TranslateService,
        private route: ActivatedRoute
    ) {
        this.language = translateService.currentLang;
        this.subscription = new Subscription();
    }

    ngOnInit(): void {
        this.setup();
    }

    setup(): void {
        this.subscription.unsubscribe();

        const locationID = Number(
            this.route.snapshot.paramMap.get('locationId')
        );

        // Fetch the context.
        this.contextSub$ = combineLatest([
            this.authenticationService.getUserObs(),
            this.locationService.getLocation(locationID)
        ]).pipe(
            tap(([user, location]) => {
                this.isNotFound = !!!location;

                this.location = location;
                this.user = user;

                this.showEdit = user.isAdmin() || user.userAuthorities.some(authority =>
                    authority.authorityId === location.authority.authorityId
                );

                this.canMakeReservation = !location?.usesPenaltyPoints || user.penaltyPoints < 100;

                this.breadcrumbService.setCurrentBreadcrumbs([{
                    pageName: 'Details',
                    url: `/dashboard/${ location?.locationId }`
                }]);

                this.updateEvents().then(() => this.updateReservations());
            })
        );

        this.setupSubscriptions();
    }

    setupSubscriptions(): void {
        // Update reservations (and calendar) every minute.
        this.subscription.add(
            interval(1000 * 60).subscribe(() => {
                this.updateEvents().then(() => this.updateReservations());
            })
        );

        // Update the language on language change.
        this.subscription.add(
            this.translateService.onLangChange.subscribe(() => {
                this.language = this.translateService.currentLang;
            })
        );
    }

    updateReservations(reset = false): void {
        if (this.location) {
            this.authenticationService.getLocationReservations().subscribe((reservations) => {
                // Update the reservations list and sort it.
                this.allReservations = reservations.filter(reservation =>
                    reservation.timeslot.locationId === this.location.locationId
                ).sort((a, b) =>
                    Number(b.timeslot.getStartMoment()) - Number(a.timeslot.getStartMoment())
                );

                if (reset) {
                    this.removedReservations = [];
                    this.newReservations = [];
                }
            });
        }
    }

    async updateEvents(hard = true): Promise<void> {
        if (this.location) {
            if (hard) {
                this.timeslots = await this.timeslotsService.getTimeslotsOfLocation(this.location.locationId).toPromise();
            }

            this.events = this.timeslots.map(timeslot => this.timeslotCalendarEventService.timeslotToCalendarEvent(
                timeslot, this.language, [
                    ...this.newReservations, ...this.allReservations.filter(res =>
                        !this.removedReservations.some(res2 =>
                            timeslotEquals(res.timeslot, res2.timeslot)
                        )
                    )
                ]
            ));
        }
    }

    timeslotPicked(event: { timeslot: Timeslot }): void {
        const currentUser = this.user;

        if (currentUser.isLoggedIn()) {
            const currentTimeslot: Timeslot = event.timeslot;

            // Only logged-in users can select timeslots.
            if (!currentUser.userId || !currentTimeslot || !currentTimeslot.reservableFrom) {
                return;
            }

            // The timeslot should be in the future.
            if (moment().isBefore(currentTimeslot.reservableFrom) || currentTimeslot.isInPast()) {
                return;
            }

            const currentReservation: LocationReservation = new LocationReservation(
                currentUser, currentTimeslot
            );

            const selected = [...this.allReservations, ...this.newReservations].some((other: LocationReservation) =>
                timeslotEquals(currentReservation.timeslot, other.timeslot) &&
                !other.isCanceled() && !other.timeslot.isInPast() &&
                !this.removedReservations.some(other1 =>
                    timeslotEquals(other1.timeslot, other.timeslot)
                )
            );

            // Only continue if there are free seats or when the slot already was selected.
            if (currentTimeslot.amountOfReservations < currentTimeslot.seatCount || selected) {
                // Try to find an existing reservation for this timeslot.
                const oldReservation = this.allReservations.find(
                    reservation => reservation.timeslot.timeslotSequenceNumber === currentTimeslot.timeslotSequenceNumber
                );

                if (!selected || !oldReservation?.isCommitted()) {
                    if (selected) {
                        // Deselect already selected timeslots.
                        if (oldReservation && !oldReservation.isCanceled()) {
                            this.removedReservations.push(oldReservation);
                        }
                        this.newReservations = this.newReservations.filter(reservation =>
                            !timeslotEquals(reservation.timeslot, currentReservation.timeslot)
                        );
                    } else {
                        // Select unselected timeslots.
                        if (!oldReservation || oldReservation.isCanceled()) {
                            this.newReservations.push(currentReservation);
                        }
                        this.removedReservations = this.removedReservations.filter(reservation =>
                            !timeslotEquals(reservation.timeslot, currentReservation.timeslot)
                        );
                    }
                }

                this.updateEvents(false);
            }
        }
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }
}
