import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {BehaviorSubject, combineLatest, Observable, ReplaySubject, Subject, Subscription, timer} from 'rxjs';
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
import {catchError, filter, first, mergeMap, tap} from 'rxjs/operators';
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

    protected locationSub$: Subject<Location>;
    protected userSub$: Subject<User>;
    protected languageSub$: Subject<string>;

    protected showEdit = false;
    protected canMakeReservation = true;

    protected events: CalendarEvent[] = [];
    protected allReservations: LocationReservation[] = [];
    protected newReservations: LocationReservation[] = [];
    protected removedReservations: LocationReservation[] = [];

    protected subscription: Subscription;
    protected reservationsCreatorObs: Observable<any>;

    protected readonly defaultTeaserImage = defaultTeaserImages[
        Math.floor(Math.random() * defaultTeaserImages.length)
    ];

    constructor(
        private locationService: LocationService,
        private timeslotsService: TimeslotsService,
        private timeslotCalendarEventService: TimeslotCalendarEventService,
        private locationReservationsService: LocationReservationsService,
        private authenticationService: AuthenticationService,
        private breadcrumbService: BreadcrumbService,
        private translateService: TranslateService,
        private route: ActivatedRoute
    ) {
        this.locationSub$ = new BehaviorSubject<Location>(undefined);
        this.userSub$ = new ReplaySubject<User>();
        this.languageSub$ = new ReplaySubject<string>();

        this.subscription = new Subscription();
    }

    ngOnInit(): void {
        const locationID = Number(
            this.route.snapshot.paramMap.get('locationId')
        );

        // Fetch the location and logged-in user.
        this.subscription.add(
            combineLatest([
                this.locationService.getLocation(locationID),
                this.authenticationService.user
            ]).pipe(
                catchError(() => {
                    return of([null, null]);
                })
            ).subscribe(([location, user]) => {
                this.locationSub$.next(location);
                this.userSub$.next(user);

                // Update reservations (and calendar) every minute.
                this.subscription.add(
                    timer(0, 1000 * 60).subscribe(() => {
                        this.updateReservations();
                    })
                );
            })
        );

        // Determine whether the admin button should be shown.
        this.subscription.add(
            combineLatest([this.locationSub$, this.userSub$]).pipe(
                filter(([location, user]) => Boolean(location && user))
            ).subscribe(([location, user]) => {
                this.showEdit = user.admin || user.userAuthorities.some(authority =>
                    authority.authorityId === location.authority.authorityId
                );
            })
        );

        // Determine whether reservations can be made.
        this.subscription.add(
            combineLatest([this.locationSub$, this.userSub$]).subscribe(([location, user]) => {
                this.canMakeReservation = !location?.usesPenaltyPoints || user.penaltyPoints < 100;
            })
        );

        // Set up the breadcrumb.
        this.subscription.add(
            this.locationSub$.subscribe((location: Location) => {
                this.breadcrumbService.setCurrentBreadcrumbs([{
                    pageName: 'Details',
                    url: `/dashboard/${ location?.locationId }`
                }]);
            })
        );

        // Listen for language changes.
        this.subscription.add(
            this.translateService.onLangChange.subscribe(() => {
                this.languageSub$.next(
                    this.translateService.currentLang
                );
            })
        );
    }

    isModified(): boolean {
        return this.newReservations.length > 0 || this.removedReservations.length > 0;
    }

    updateReservations(reset = false): void {
        combineLatest([
            this.authenticationService.getLocationReservations(),
            this.locationSub$
        ]).pipe(first()).subscribe(([reservations, location]) => {
            // Update the reservations list and sort it.
            this.allReservations = reservations.filter(reservation =>
                reservation.timeslot.locationId === location.locationId
            ).sort((a, b) =>
                Number(b.timeslot.getStartMoment()) - Number(a.timeslot.getStartMoment())
            );

            if (reset) {
                this.removedReservations = [];
                this.newReservations = [];
            }

            this.updateEvents();
        });
    }

    updateEvents(): void {
        this.locationSub$.pipe(
            mergeMap((location: Location) =>
                combineLatest([
                    this.timeslotsService.getTimeslotsOfLocation(location.locationId),
                    this.languageSub$
                ])
            ), first()
        ).subscribe(([timeslots, language]) => {
            this.events = timeslots.map(timeslot => this.timeslotCalendarEventService.timeslotToCalendarEvent(
                timeslot, language, [
                    ...this.newReservations, ...this.allReservations.filter(res =>
                        !this.removedReservations.some(res2 =>
                            timeslotEquals(res.timeslot, res2.timeslot)
                        )
                    )
                ]
            ));
        });
    }

    timeslotPicked(event: { timeslot: Timeslot }): void {
        this.userSub$.pipe(first()).subscribe(currentUser => {
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

                this.updateEvents();
            }
        });
    }

    commitReservations(): void {
        this.commitModal.close();
        this.afterModal.open();

        this.reservationsCreatorObs = combineLatest([
            this.locationReservationsService.postLocationReservations(
                this.newReservations
            ),
            this.locationReservationsService.deleteLocationReservations(
                this.removedReservations
            )
        ]).pipe(
            first(), tap(() =>
                this.updateReservations(true)
            ), map(([res]) =>
                res
            )
        );
    }

    toggleSubscription(): void {
        let request: Observable<void>;

        combineLatest([
            this.userSub$, this.locationSub$
        ]).pipe(
            first(), filter(([user, _]) =>
                user.isLoggedIn()
            )
        ).subscribe(([user, location]) => {
            this.locationSub$.next({
                ...location,
                subscribed: !location.subscribed
            });

            if (!location.subscribed) {
                request = this.locationService.subscribeToLocation(
                    location.locationId
                );
            } else {
                request = this.locationService.unsubscribeFromLocation(
                    location.locationId
                );
            }

            request.pipe(first()).subscribe();
        });
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();

        this.locationSub$.complete();
        this.languageSub$.complete();
        this.userSub$.complete();
    }
}
