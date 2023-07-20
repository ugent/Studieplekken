import {Component, OnDestroy, OnInit, TemplateRef} from '@angular/core';
import {combineLatest, Observable, Subscription, throwError} from 'rxjs';
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
import {tap} from 'rxjs/operators';

@Component({
    selector: 'app-location-reservation',
    templateUrl: './location-reservation.component.html',
    styleUrls: ['./location-reservation.component.scss']
})
export class LocationReservationComponent implements OnInit, OnDestroy {

    // The auto-reload interval.
    private updateInterval = 5000; // 10 seconds

    // The default teaser image in case of errors.
    protected readonly defaultTeaserImage = defaultTeaserImages[
        Math.floor(Math.random() * defaultTeaserImages.length)
    ];

    // The current selected language.
    protected language: string;
    protected languageSubscription: Subscription;

    // The current location.
    protected location: Location;
    protected locationSubscription: Subscription;

    // The current logged-in user.
    protected user: User;
    protected userSubscription: Subscription;

    // All events to be displayed on the calendar.
    protected events: CalendarEvent[] = [];
    protected eventsSubscription: Subscription;

    // Already committed reservations.
    protected allReservations: LocationReservation[];
    // Reservations to be added after selecting.
    protected newReservations: LocationReservation[] = [];
    // Reservations to be removed after selecting.
    protected removedReservations: LocationReservation[] = [];

    // Observable for updating reservations.
    protected newReservationsCreator: Observable<moment.Moment[]>;

    protected get description(): string {
        if (this.location) {
            return this.language === 'nl' ?
                this.location.descriptionDutch : this.location.descriptionEnglish;
        }
    }

    protected get isModified(): boolean {
        return this.newReservations.length > 0 || this.removedReservations.length > 0;
    }

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

        this.locationSubscription = this.locationService.getLocation(locationID).subscribe((location: Location) => {
            this.location = location;

            this.breadcrumbService.setCurrentBreadcrumbs([{
                pageName: 'Details', url: `/dashboard/${ this.location.locationId }`
            }]);

            this.eventsSubscription = timer(0, this.updateInterval).subscribe(() => {
                this.updateReservations();
            });
        });

        this.userSubscription = this.authenticationService.user.subscribe((user: User) => {
            this.user = user;
        });

        this.language = this.translateService.currentLang;

        this.languageSubscription = this.translateService.onLangChange.subscribe(() => {
            this.language = this.translateService.currentLang;
        });
    }

    updateReservations(reset = false): void {
        // We don't need to unsubscribe Http-based observables.
        this.authenticationService.getLocationReservations().pipe(
            map((reservations: LocationReservation[]) =>
                reservations.filter((reservation: LocationReservation) =>
                    reservation.timeslot.locationId === this.location.locationId
                )
            )
        ).subscribe((reservations: LocationReservation[]) => {
            this.allReservations = reservations.sort((a, b) =>
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
        this.timeslotsService.getTimeslotsOfLocation(this.location.locationId).subscribe((timeslots: Timeslot[]) => {
            this.events = timeslots.map(timeslot => this.timeslotCalendarEventService.timeslotToCalendarEvent(
                timeslot, this.language, [
                    ...this.newReservations, ...this.allReservations.filter(res =>
                        !this.removedReservations.some(res2 =>
                            timeslotEquals(res.timeslot, res2.timeslot)
                        )
                    )
                ]
            ));
        });
    }

    timeslotPicked(event): void {
        const currentUser: User = this.user;
        const currentTimeslot: Timeslot = event.timeslot;

        // Only logged-in users can select timeslots.
        if (!currentUser || !currentTimeslot) {
            return;
        }

        // The timeslot should be reservable.
        if (!currentTimeslot.reservableFrom) {
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
            timeslotEquals(currentReservation.timeslot, other.timeslot) && !other.isCanceled() && !other.timeslot.isInPast()
        );

        // Only continue if there are free seats or when the slot already was selected.
        if (currentTimeslot.amountOfReservations < currentTimeslot.seatCount || selected) {
            // Try to find an existing reservation for this timeslot.
            const oldReservation = this.allReservations.find(
                reservation => reservation.timeslot.timeslotSequenceNumber === currentTimeslot.timeslotSequenceNumber
            );

            if (!selected || oldReservation?.state !== LocationReservationState.PRESENT) {
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
        this.modalService.open(template, {
            panelClass: ['cs--cyan', 'bigmodal']
        });
    }

    commitReservations(template: TemplateRef<unknown>): void {
        this.modalService.closeAll();

        this.modalService.open(template, {
            panelClass: ['cs--cyan', 'bigmodal']
        });

        this.newReservationsCreator = combineLatest([
            this.locationReservationsService.postLocationReservations(
                this.newReservations
            ),
            this.locationReservationsService.deleteLocationReservations(
                this.removedReservations
            )
        ]).pipe(
            tap(() => {
                this.updateReservations(true);
            }),
            map(([res]) => res)
        );
    }

    toggleSubscription(): void {
        let request: Observable<void>;

        // Only toggle subscription in case of a logged-in user.
        if (this.user) {
            this.location.subscribed = !this.location.subscribed;

            if (this.location.subscribed) {
                request = this.locationService.subscribeToLocation(
                    this.location.locationId
                );
            } else {
                request = this.locationService.unsubscribeFromLocation(
                    this.location.locationId
                );
            }

            request.subscribe();
        }
    }

    ngOnDestroy(): void {
        this.locationSubscription.unsubscribe();
        this.languageSubscription.unsubscribe();
        this.eventsSubscription.unsubscribe();
        this.userSubscription.unsubscribe();
    }
}
