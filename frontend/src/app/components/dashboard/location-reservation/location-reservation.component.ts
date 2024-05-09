import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {combineLatest, interval, Observable, Subscription} from 'rxjs';
import {LocationService} from '@/services/api/locations/location.service';
import {ActivatedRoute} from '@angular/router';
import {User} from '@/model/User';
import {Location} from '@/model/Location';
import {AuthenticationService} from '@/services/authentication/authentication.service';
import {BreadcrumbService} from '../../stad-gent-components/header/breadcrumbs/breadcrumb.service';
import {TranslateService} from '@ngx-translate/core';
import {TimeslotsService} from '@/services/api/calendar-periods/timeslot.service';
import {LocationReservation} from '@/model/LocationReservation';
import {CalendarEvent} from 'angular-calendar';
import {Timeslot, timeslotEquals} from '@/model/Timeslot';
import * as moment from 'moment';
import {
    TimeslotCalendarEventService
} from '@/services/timeslots/timeslot-calendar-event/timeslot-calendar-event.service';
import {tap} from 'rxjs/operators';
import {ModalComponent} from '../../stad-gent-components/molecules/modal/modal.component';
import {AfterReservationComponent} from './after-reservation/after-reservation.component';
import {Authority} from '@/model/Authority';
import {Authority} from '../../../model/Authority';
import {now} from 'd3';

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
    protected isReservable = false;
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
        const locationID = Number(
            this.route.snapshot.paramMap.get('locationId')
        );

        // Fetch the context.
        this.setupSubscriptions();

        this.contextSub$ = combineLatest([
            this.authenticationService.getUserObs(),
            this.locationService.getLocation(locationID)
        ]).pipe(
            tap(([user, location]) => {

                this.location = location;
                this.user = user;

                this.isNotFound = !!!location;

                this.showEdit = user.isAdmin() || user.userAuthorities.some((authority: Authority) =>
                    authority.authorityId === location.authority.authorityId
                );
                this.breadcrumbService.setCurrentBreadcrumbs([{
                    pageName: 'Details',
                    url: `/dashboard/${location?.locationId}`
                }]);

                this.updateReservations();
            })
        );
    }

    /**
     * Set up the subscriptions for the component.
     */
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

    /**
     * Update the reservations list with data from the backend.
     */
    updateReservations(reset = false): void {
        if (!this.location) {
            return;
        }

        this.authenticationService.getLocationReservations().subscribe((reservations) => {
            // Update the reservations list and sort it.
            this.allReservations = reservations.filter(reservation =>
                reservation.timeslot.locationId === this.location.locationId
            ).sort((a, b) =>
                Number(b.timeslot.getStartMoment()) - Number(a.timeslot.getStartMoment())
            );

            // Reset the new and removed reservations if requested.
            if (reset) {
                this.removedReservations = [];
                this.newReservations = [];
            }
        });

        // Update the calendar events.
        void this.updateEvents();
    }

    /**
     * Update the events list with data from the backend.
     */
    async updateEvents(fetchTimeslots = true): Promise<void> {
        if (!this.location) {
            return;
        }

        // Fetch and reload the timeslots from the backend.
        if (fetchTimeslots) {
            this.timeslots = await this.timeslotsService.getTimeslotsOfLocation(this.location.locationId).pipe(
                tap((timeslots: Timeslot[]) =>
                    this.isReservable = timeslots.some(timeslot =>
                        timeslot.reservableFrom?.isSameOrBefore(
                            moment()
                        ) && !timeslot.isInPast()
                    )
                )
            ).toPromise();
        }

        // Convert the timeslots into calendar events.
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

    /**
     * Select a timeslot.
     *
     * @param event The event containing the selected timeslot.
     */
    timeslotPicked(event: { timeslot: Timeslot }): void {
        const currentUser = this.user;

        if (!currentUser.isLoggedIn()) {
            return;
        }

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

            void this.updateEvents(false);
        }
    }

    /**
     * Destroy the component and its subscriptions.
     */
    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }
}
