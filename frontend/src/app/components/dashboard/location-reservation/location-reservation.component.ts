import {
    AfterViewInit, ChangeDetectorRef,
    Component,
    ElementRef,
    OnDestroy,
    OnInit, QueryList,
    TemplateRef,
    ViewChild,
    ViewChildren
} from '@angular/core';
import {BehaviorSubject, combineLatest, forkJoin, Observable, Subject, Subscription, throwError} from 'rxjs';
import {LocationService} from '../../../extensions/services/api/locations/location.service';
import {ActivatedRoute} from '@angular/router';
import {User} from '../../../extensions/model/User';
import {Location} from '../../../extensions/model/Location';
import {AuthenticationService} from '../../../extensions/services/authentication/authentication.service';
import {defaultTeaserImages} from '../../../app.constants';
import {BreadcrumbService} from '../../../stad-gent-components/header/breadcrumbs/breadcrumb.service';
import {TranslateService} from '@ngx-translate/core';
import {TimeslotsService} from '../../../extensions/services/api/calendar-periods/timeslot.service';
import {LocationReservation, LocationReservationState} from '../../../extensions/model/LocationReservation';
import {map} from 'rxjs/internal/operators/map';
import {CalendarEvent} from 'angular-calendar';
import {Timeslot, timeslotEquals} from '../../../extensions/model/Timeslot';
import * as moment from 'moment';
import {
    TimeslotCalendarEventService
} from '../../../extensions/services/timeslots/timeslot-calendar-event/timeslot-calendar-event.service';
import {MatDialog} from '@angular/material/dialog';
import {
    LocationReservationsService
} from '../../../extensions/services/api/location-reservations/location-reservations.service';
import {timer} from 'rxjs';
import {catchError, filter, first, shareReplay, take, tap} from 'rxjs/operators';
import {of} from 'rxjs/internal/observable/of';
import * as Leaf from 'leaflet';
import {Dir} from '@angular/cdk/bidi';
import {
    LocationReservationsComponent
} from '../../management/locations-management/location-details-management/location-calendar/location-reservations/location-reservations/location-reservations.component';

@Component({
    selector: 'app-location-reservation',
    templateUrl: './location-reservation.component.html',
    styleUrls: ['./location-reservation.component.scss']
})
export class LocationReservationComponent implements OnInit, OnDestroy {

    protected readonly LocationReservationState = LocationReservationState;

    // The current selected language.
    protected languageSub: BehaviorSubject<string> = new BehaviorSubject(
        this.translateService.currentLang
    );
    // The current location.
    protected locationSub: BehaviorSubject<Location> = new BehaviorSubject(
        undefined
    );
    // The current logged-in user.
    protected userSub: BehaviorSubject<User> = new BehaviorSubject(
        undefined
    );

    // Whether the edit button should be shown.
    protected showEdit = false;
    // Whether the user can make reservations.
    protected canMakeReservation = true;

    // All events to be displayed on the calendar.
    protected events: CalendarEvent[] = [];
    // Already committed reservations.
    protected allReservations: LocationReservation[] = [];
    // Reservations to be added after selecting.
    protected newReservations: LocationReservation[] = [];
    // Reservations to be removed after selecting.
    protected removedReservations: LocationReservation[] = [];

    // Main subscription.
    protected subscription: Subscription = new Subscription();

    // Observable for updating reservations.
    protected reservationsCreatorObs: Observable<any>;

    // The default teaser image in case of errors.
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
        private modalService: MatDialog,
        private route: ActivatedRoute
    ) {
    }

    ngOnInit(): void {
        const locationID = Number(
            this.route.snapshot.paramMap.get('locationId')
        );

        // Fetch the location and logged-in user.
        this.subscription.add(
            combineLatest([
                this.locationService.getLocation(locationID), this.authenticationService.user
            ]).pipe(
                catchError(() => {
                    return of([null, null]);
                })
            ).subscribe(([location, user]) => {
                this.locationSub.next(location);
                this.userSub.next(user);

                // Update reservations (and calendar) every 10 seconds.
                this.subscription.add(
                    timer(0, 10000).subscribe(() => {
                        this.updateReservations();
                    })
                );
            })
        );

        // Determine whether the admin button should be shown.
        this.subscription.add(
            combineLatest([this.locationSub, this.userSub]).pipe(
                filter(([location, user]) => Boolean(location && user))
            ).subscribe(([location, user]) => {
                this.showEdit = user.admin || user.userAuthorities.some(authority =>
                    authority.authorityId === location.authority.authorityId
                );
            })
        );

        // Determine whether reservations can be made.
        this.subscription.add(
            combineLatest([
                this.locationSub, this.authenticationService.penaltyObservable
            ]).subscribe(([location, penalties]) => {
                this.canMakeReservation = !location?.usesPenaltyPoints || penalties.points < 100;
            })
        );

        // Set up the breadcrumb.
        this.subscription.add(
            this.locationSub.pipe(
                filter(location => Boolean(location))
            ).subscribe((location: Location) => {
                this.breadcrumbService.setCurrentBreadcrumbs([{
                    pageName: 'Details', url: `/dashboard/${ location.locationId }`
                }]);
            })
        );

        // Listen for language changes.
        this.subscription.add(
            this.translateService.onLangChange.subscribe(() => {
                this.languageSub.next(
                    this.translateService.currentLang
                );
            })
        );
    }

    updateReservations(reset = false): void {
        const location: Location = this.locationSub.value;

        // The take(1) automatically unsubscribes after the first emission.
        this.authenticationService.getLocationReservations().pipe(
            first()
        ).subscribe((reservations: LocationReservation[]) => {
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
        this.timeslotsService.getTimeslotsOfLocation(this.locationSub.value.locationId).pipe(
            first()
        ).subscribe((timeslots: Timeslot[]) => {
            this.events = timeslots.map(timeslot => this.timeslotCalendarEventService.timeslotToCalendarEvent(
                timeslot, this.languageSub.value, [
                    ...this.newReservations, ...this.allReservations.filter(res =>
                        !this.removedReservations.some(res2 =>
                            timeslotEquals(res.timeslot, res2.timeslot)
                        )
                    )
                ]
            ));
        });
    }

    timeslotPicked(event: { timeslot: Timeslot; }): void {
        const currentUser: User = this.userSub.value;
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
    }

    cancelReservations(): void {
        this.modalService.closeAll();
    }

    prepareReservations(template: TemplateRef<unknown>): void {
        this.modalService.open(template);
    }

    commitReservations(template: TemplateRef<unknown>): void {
        this.modalService.closeAll();

        this.modalService.open(template);

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
            ),
            map(([res]) =>
                res
            )
        );
    }

    toggleSubscription(): void {
        let request: Observable<void>;

        // Only toggle subscription in case of a logged-in user.
        if (this.userSub.value) {
            const location = this.locationSub.value;

            this.locationSub.value.subscribed = !location.subscribed;

            if (this.locationSub.value.subscribed) {
                request = this.locationService.subscribeToLocation(
                    location.locationId
                );
            } else {
                request = this.locationService.unsubscribeFromLocation(
                    location.locationId
                );
            }

            request.pipe(first()).subscribe();
        }
    }

    isModified(): boolean {
        return this.newReservations.length > 0 || this.removedReservations.length > 0;
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();

        this.locationSub.complete();
        this.languageSub.complete();
        this.userSub.complete();
    }
}
