import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {combineLatest, interval, Observable, of, Subscription} from 'rxjs';
import {LocationService} from '@/services/api/locations/location.service';
import {ActivatedRoute, Router} from '@angular/router';
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
import { LoginRedirectService } from '@/services/authentication/login-redirect.service';

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
        private loginRedirectService: LoginRedirectService,
        private router: Router,
        private route: ActivatedRoute
    ) {
        this.language = translateService.currentLang;
        this.subscription = new Subscription();
    }

    public ngOnInit(): void {
        // Get the location ID from the route.
        const locationID = Number(this.route.snapshot.paramMap.get('locationId'));

        // Update the reservations and events every minute.
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

        // Combine the user and location observables.
        this.contextSub$ = combineLatest([
            this.authenticationService.getUserObs(),
            this.locationService.getLocation(locationID)
        ]).pipe(
            tap(([user, location]) => {
                this.user = user;
                this.location = location;
                this.isNotFound = location === null || location === undefined;

                this.showEdit = user.isAdmin() || user.hasAuthority(
                    location.authority
                );

                this.breadcrumbService.setCurrentBreadcrumbs([{
                    pageName: 'Details',
                    url: `/dashboard/${location?.locationId}`
                }]);

                if (!this.user.isLoggedIn()) {
                    this.loginRedirectService.registerUrl(this.router.url);
                }

                this.updateReservations();
            })
        );
    }

    
    /**
     * Updates the reservations for the current location.
     * 
     * This method fetches the reservations for the current location from the 
     * authentication service, filters and sorts them, and updates the internal 
     * reservations list. Optionally, it can reset the new and removed reservations.
     * 
     * @param {boolean} [reset=false] - If true, resets the new and removed reservations.
     * @returns {void}
     */
    public updateReservations(reset: boolean = false): void {
        if (this.location === null || this.location === undefined) {
            return;
        }

        this.authenticationService.getLocationReservations().subscribe({
            next: (reservations) => {
                // Update the reservations list and sort it.
                this.allReservations = reservations.filter(reservation =>
                    reservation.timeslot.locationId === this.location.locationId
                ).sort((locationA: LocationReservation, locationB: LocationReservation) =>
                    Number(locationB.timeslot.getStartMoment()) - Number(locationA.timeslot.getStartMoment())
                );

                // Reset the new and removed reservations if requested.
                if (reset === true) {
                    this.removedReservations = [];
                    this.newReservations = [];
                }
            },
            complete: () => {
                // Update the calendar events.
                void this.updateEvents();
            }
        });
    }

    
    /**
     * Updates the events for the location reservation component.
     * 
     * @param {boolean} [fetchTimeslots=true] - Determines whether to fetch and reload the timeslots from the backend.
     * @returns {Promise<void>} A promise that resolves when the events have been updated.
     * 
     * @remarks
     * - If the location is null or undefined, the function will return early.
     * - If `fetchTimeslots` is true, it fetches the timeslots from the backend and updates the `isReservable` property.
     * - Converts the fetched timeslots into calendar events and updates the `events` property.
     */
    public updateEvents(fetchTimeslots: boolean = true): Promise<void> {
        if (this.location === null || this.location === undefined) {
            return;
        }

        // Fetch and reload the timeslots from the backend.
        let timeslotsObs = of(this.timeslots);

        if (fetchTimeslots === true) {
            timeslotsObs = this.timeslotsService.getTimeslotsOfLocation(this.location.locationId).pipe(
                tap((timeslots: Timeslot[]) =>
                    this.isReservable = timeslots.some(timeslot =>
                        timeslot.reservableFrom?.isSameOrBefore(moment.now()) && !timeslot.isInPast()
                    )
                )
            );
        }

        timeslotsObs.subscribe((timeslots: Timeslot[]) => {
            // Update the timeslots.
            this.timeslots = timeslots;

            // We convert all existing and new reservations to 
            // calendar events and filter out the removed reservations.
            this.events = this.timeslots.map(timeslot =>
                this.timeslotCalendarEventService.timeslotToCalendarEvent(timeslot, this.language, [
                    ...this.newReservations, ...this.allReservations.filter(res =>
                        !this.removedReservations.some(res2 =>
                            timeslotEquals(res.timeslot, res2.timeslot)
                        )
                    )
                ])
            );
        });
    }

    /**
     * Select a timeslot.
     *
     * @param event The event containing the selected timeslot.
     */
    public timeslotPicked(event: { timeslot: Timeslot }): void {
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
     * Lifecycle hook that is called when the component is destroyed.
     * Unsubscribes from the subscription to prevent memory leaks.
     */
    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }
}
