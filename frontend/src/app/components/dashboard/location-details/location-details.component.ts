import {DatePipe} from '@angular/common';
import {AfterViewInit, Component, OnDestroy, OnInit, TemplateRef} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {DomSanitizer} from '@angular/platform-browser';
import {ActivatedRoute, Router} from '@angular/router';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {
    BehaviorSubject,
    combineLatest, forkJoin,
    merge,
    Observable,
    of,
    ReplaySubject,
    Subject,
    Subscription,
    throwError
} from 'rxjs';
import {map} from 'rxjs/internal/operators/map';
import {LocationReservationsService} from 'src/app/extensions/services/api/location-reservations/location-reservations.service';
import {AuthenticationService} from 'src/app/extensions/services/authentication/authentication.service';
import {TimeslotCalendarEventService} from 'src/app/extensions/services/timeslots/timeslot-calendar-event/timeslot-calendar-event.service';
import {LocationReservation, LocationReservationState} from 'src/app/extensions/model/LocationReservation';
import {Timeslot, timeslotEquals} from 'src/app/extensions/model/Timeslot';
import {BreadcrumbService} from 'src/app/stad-gent-components/header/breadcrumbs/breadcrumb.service';
import {defaultTeaserImages, LocationStatus} from '../../../app.constants';
import {TimeslotsService} from '../../../extensions/services/api/calendar-periods/timeslot.service';
import {LocationService} from '../../../extensions/services/api/locations/location.service';
import {Pair} from '../../../extensions/model/helpers/Pair';
import {Location} from '../../../extensions/model/Location';
import {LocationTag} from '../../../extensions/model/LocationTag';
import * as Leaf from 'leaflet';
import {LoginRedirectService} from 'src/app/extensions/services/authentication/login-redirect.service';
import {catchError, tap} from 'rxjs/operators';
import {CalendarEvent} from 'angular-calendar';

// Leaflet stuff.
const iconRetinaUrl = './assets/marker-icon-2x.png';
const iconUrl = './assets/marker-icon.png';
const shadowUrl = './assets/marker-shadow.png';
const iconDefault = Leaf.icon({
    iconRetinaUrl,
    iconUrl,
    shadowUrl,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    tooltipAnchor: [16, -28],
    shadowSize: [41, 41]
});
Leaf.Marker.prototype.options.icon = iconDefault;

@Component({
    selector: 'app-location-details',
    templateUrl: './location-details.component.html',
    styleUrls: ['./location-details.component.scss', '../location.scss'],
    providers: [DatePipe],
})
export class LocationDetailsComponent implements OnInit, AfterViewInit, OnDestroy {
    location: Observable<Location>;
    locationId: number;
    tags: LocationTag[];

    events: CalendarEvent<{ timeslot: Timeslot }>[] = [];

    editor: unknown = ClassicEditor;

    selectedSubject: BehaviorSubject<LocationReservation[]> = new BehaviorSubject<LocationReservation[]>([]);
    originalList: LocationReservation[];
    subscription: Subscription;

    showError = false;
    showSuccessPendingLong = false;
    showSuccessPendingShort = false;
    showSuccessDeletion = false;

    isModified = false;
    isFirst = true;

    description = {
        show: '',
        english: '',
        dutch: '',
    };

    altImageUrl = defaultTeaserImages[Math.floor(Math.random() * defaultTeaserImages.length)];
    imageUrlErrorOccurred = false;

    status: Pair<LocationStatus, string>;
    currentLang: string;

    modalRef: MatDialogRef<unknown, unknown>;
    newReservations: LocationReservation[];
    removedReservations: LocationReservation[];

    private timeouts = [];
    locationReservations: LocationReservation[];
    showAdmin: boolean;
    capacity: number;

    locationSub: Subscription;
    calendarSub: Subscription;

    leafletMap: Leaf.Map;

    pendingReservations: LocationReservation[] = [];
    newReservationCreator: Observable<moment.Moment[]>;

    ownReservations: Subject<LocationReservation[]> = new ReplaySubject();

    locationSubscribed = false;

    constructor(
        private locationService: LocationService,
        private route: ActivatedRoute,
        private sanitizer: DomSanitizer,
        private translate: TranslateService,
        private timeslotsService: TimeslotsService,
        private datepipe: DatePipe,
        private authenticationService: AuthenticationService,
        private locationReservationService: LocationReservationsService,
        private modalService: MatDialog,
        private router: Router,
        private breadcrumbs: BreadcrumbService,
        private timeslotCalendarEventService: TimeslotCalendarEventService,
        private loginRedirect: LoginRedirectService
    ) {
    }

    ngOnInit(): void {
        this.locationId = Number(
            this.route.snapshot.paramMap.get('locationId')
        );

        this.breadcrumbs.setCurrentBreadcrumbs([{
            pageName: 'Details', url: `/dashboard/${this.locationId}`
        }]);
        this.loginRedirect.registerUrl(`/dashboard/${this.locationId}`);

        // Check if locationId is a Number before proceeding. If NaN, redirect to dashboard.
        if (isNaN(this.locationId)) {
            this.router.navigate(['/dashboard']).catch(console.log);

            return;
        }

        this.location = this.locationService.getLocation(this.locationId);

        combineLatest([this.authenticationService.user, this.location]).subscribe(([user, location]) => {
            if (user != null) {
                this.showAdmin = this.authenticationService.isAdmin() || user.userAuthorities.map(
                    a => a.authorityId
                ).includes(location.authority.authorityId);
                this.updateOwnReservations();
            }
        });

        this.ownReservations.subscribe(v =>
            this.selectedSubject.next(
                v.filter(f => f.timeslot.locationId === this.locationId)
            )
        );

        this.currentLang = this.translate.currentLang;

        // when the location is loaded, set up the descriptions
        this.locationSub = this.location.subscribe((next) => {
            this.description.dutch = next.descriptionDutch;
            this.description.english = next.descriptionEnglish;
            this.capacity = next.numberOfSeats;
            this.setDescriptionToShow();

            this.tags = next.assignedTags;

            this.updateCalendar();
        });

        // if the browser language would change, the description needs to change
        this.translate.onLangChange.subscribe(() => {
            this.setDescriptionToShow();
            this.currentLang = this.translate.currentLang;
            this.updateCalendar();
        });

        setInterval(() => {
            this.updateCalendar();
            this.updateOwnReservations();
        }, 60 * 1000); // 1 minute

        this.location.pipe().subscribe(location => {
            this.locationSubscribed = location.subscribed;
        });
    }

    ngAfterViewInit(): void {
        if (this.leafletMap) {
            this.leafletMap.off();
            this.leafletMap.remove();
        }

        // when the location is loaded, set up the Leaflet map
        this.locationSub = this.location.subscribe((next) => {
            if (next) {
                this.setupLeafletMap(next);
            }
        });
    }

    ngOnDestroy(): void {
        this.locationSub?.unsubscribe();
        this.calendarSub?.unsubscribe();

        if (this.leafletMap) {
            this.leafletMap.off();
            this.leafletMap.remove();
        }
    }

    timeslotPicked(event: Event): void {
        const currentTimeslot: Timeslot = event['timeslot'];

        if (!this.loggedIn()) {
            // When not logged in, calendar periods are unclickable
            return;
        }

        if (!currentTimeslot) {
            // the calendar period is not reservable
            return;
        }

        // If the selected timeslot is not yet reservable, don't do anything
        if (currentTimeslot.reservableFrom && moment().isBefore(currentTimeslot.reservableFrom)) {
            return;
        }

        // If the timeslot is in the past, reservation can no longer be changed.
        if (currentTimeslot.isInPast()) {
            return;
        }

        const reservation: LocationReservation = new LocationReservation(
            this.authenticationService.userValue(), currentTimeslot, null
        );

        const timeslotIsSelected = this.selectedSubject.value.some((r) =>
            timeslotEquals(r.timeslot, reservation.timeslot) && r.state !== LocationReservationState.DELETED &&
            r.state !== LocationReservationState.REJECTED
        );

        // if it is full, and you don't have this reservation yet, unselect
        if (
            currentTimeslot.amountOfReservations >=
            currentTimeslot.seatCount &&
            !timeslotIsSelected
        ) {
            return;
        }

        const oldReservation = this.originalList.find(
            t => t.timeslot.timeslotSequenceNumber === currentTimeslot.timeslotSequenceNumber
        );

        if (timeslotIsSelected && oldReservation?.state === LocationReservationState.PRESENT) {
            // If user is already scanned as present, do not allow to unselect.
            return;
        }

        this.isModified = true;

        if (!timeslotIsSelected && oldReservation && (oldReservation.state === LocationReservationState.REJECTED
            || oldReservation.state === LocationReservationState.DELETED)) { // If it was rejected, allow to try again
            const nextval = [...this.selectedSubject.value.filter(o => o !== oldReservation
                && o.timeslot.timeslotSequenceNumber !== oldReservation.timeslot.timeslotSequenceNumber), reservation];
            this.selectedSubject.next(nextval);
        } else if (timeslotIsSelected) { // If it's already selected, unselect
            const nextval = this.selectedSubject.value.filter(
                (r) => !timeslotEquals(r.timeslot, reservation.timeslot)
            );
            this.selectedSubject.next(nextval);
            // If it's not yet selected, add to selection
        } else {
            const nextval = [...this.selectedSubject.value, reservation];
            this.selectedSubject.next(nextval);
        }
    }

    handleImageError(): void {
        this.imageUrlErrorOccurred = true;
    }

    setDescriptionToShow(): void {
        const lang = this.translate.currentLang;

        // Depending on the browser language, return the description of the language.
        // Show the dutch description if the browser language is 'nl'.
        // Otherwise, show the english description.
        this.description.show =
            lang === 'nl' ? this.description.dutch : this.description.english;
    }

    updateCalendar(updateSelection = false): void {
        // retrieve the calendar periods and map them to calendar events used by Angular Calendar
        if (this.subscription) {
            this.subscription.unsubscribe();
        }

        this.calendarSub = combineLatest([
            this.timeslotsService.getTimeslotsOfLocation(this.locationId),
            this.ownReservations,
            this.selectedSubject,
        ])
            .subscribe(([timeslots, reservations, proposedReservations]) => {
                this.originalList = [...reservations.filter(r => r.timeslot.locationId === this.locationId)];
                this.pendingReservations = this.originalList.filter(locres => locres.state === LocationReservationState.PENDING);
                this.timeouts.forEach(t => clearTimeout(t));

                // Only do this once, when selectedSubject isn't initialized yet.
                // TODO: Why only the first time? If there is a faulty reservation it'll keep trying (and failing) to make that reservation.
                // If this line can be removed then this scenario
                if (this.isFirst || updateSelection) {
                    this.isFirst = false;
                    this.selectedSubject.next([...this.originalList]);
                    return;
                }

                this.timeouts = timeslots
                    .filter(t => t.reservable)
                    .map(e => (e.reservableFrom.valueOf() - moment().valueOf()))
                    .filter(d => d > 0)
                    .filter(d => d < 1000 * 60 * 60 * 24 * 2) // don't set more than two days in advance (weird bugs if you do)
                    .map(d => setTimeout(() => this.draw(timeslots, proposedReservations), d));
                this.draw(timeslots, [...this.pendingReservations, ...proposedReservations]);
            });
    }

    draw(timeslots: Timeslot[], proposedReservations: LocationReservation[]): void {
        this.events = timeslots.map(t => this.timeslotCalendarEventService.timeslotToCalendarEvent(
            t, this.currentLang, [...proposedReservations]
        ));
    }

    updateReservationIsPossible(): boolean {
        return !(this.isModified && this.authenticationService.isLoggedIn());
    }

    commitReservations(template: TemplateRef<unknown>): void {
        // We need to find out which of the selected boxes need to be removed, and which need to be added.
        // Therefore, we calculate selected \ previous.
        this.newReservations = this.selectedSubject.value.filter(
            (selected) => {
                if (selected.state === LocationReservationState.DELETED || selected.state === LocationReservationState.REJECTED) {
                    return false;
                }
                const tempList: LocationReservation[] = this.originalList.filter(
                    (reservation) => {
                        return reservation.user.userId === selected.user.userId &&
                            reservation.timeslot.timeslotSequenceNumber === selected.timeslot.timeslotSequenceNumber;
                    }
                );
                const oldReservation = tempList.length > 0 ? tempList[0] : undefined;
                if (!oldReservation) {
                    return true;
                }

                return oldReservation && (
                    oldReservation.state === LocationReservationState.DELETED || oldReservation.state === LocationReservationState.REJECTED
                );
            }
        );

        // And we calculate previous \ selected
        this.removedReservations = this.originalList.filter(
            (selected) => {
                if (selected.state === LocationReservationState.DELETED || selected.state === LocationReservationState.REJECTED) {
                    return false;
                }
                const tempList: LocationReservation[] = this.selectedSubject.value.filter(
                    (res) => res.timeslot.timeslotSequenceNumber === selected.timeslot.timeslotSequenceNumber
                );
                const reservation = tempList.length > 0 ? tempList[0] : undefined;

                return !reservation;
            });

        this.modalRef = this.modalService.open(template, {panelClass: ['cs--cyan', 'bigmodal']});
    }

    confirmReservationChange(template: TemplateRef<unknown>): void {
        this.modalService.open(template, {panelClass: ['cs--cyan', 'bigmodal']});
        this.newReservationCreator = combineLatest([
            this.locationReservationService.postLocationReservations(
                this.newReservations
            ),
            this.locationReservationService.deleteLocationReservations(
                this.removedReservations
            ),
        ]).pipe(
            catchError((err) => {
                this.updateCalendar(true);
                return throwError(err);
            }),
            tap(
                () => this.updateOwnReservations()
            ),
            map(([res]) => res)
        );

        this.isModified = false;
        this.modalRef.close();
    }

    declineReservationChange(): void {
        this.modalRef.close();
    }

    formatReservation(reservation: LocationReservation): Observable<string> {
        return this.locationService.getLocation(reservation.timeslot.locationId)
            .pipe(map(location => {
                    const date = reservation.timeslot.timeslotDate.format('DD/MM/YYYY');
                    const hour = reservation.timeslot.openingHour.format('HH:mm');

                    return location.name + ' (' + date + ' ' + hour + ')';
                }
            ));
    }

    private updateOwnReservations(): void {
        this.authenticationService.getLocationReservations().subscribe(next =>
            this.ownReservations.next(next.filter(f => f.timeslot.locationId === this.locationId))
        );
    }

    loggedIn(): boolean {
        return this.authenticationService.isLoggedIn();
    }

    showDescription(description: string): boolean {
        return description !== '';
    }

    setupLeafletMap(location: Location): void {
        const originalTile = Leaf.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19
        });

        const coordinates = new Leaf.LatLng(location.building.latitude, location.building.longitude);

        this.leafletMap = new Leaf.Map('leafletMap', {
            center: coordinates,
            zoom: 18,
            layers: [originalTile],
            crs: Leaf.CRS.EPSG3857
        });

        new Leaf.Marker(coordinates).addTo(this.leafletMap);
    }

    getStateI18NObject(state: LocationReservationState): string {
        return 'profile.reservations.locations.table.attended.' + state;
    }

    needsTooltip(state: LocationReservationState): boolean {
        return state === LocationReservationState.REJECTED;
    }

    currentLanguage(): Observable<string> {
        return merge<LangChangeEvent, LangChangeEvent>(
            of<LangChangeEvent>({
                lang: this.translate.currentLang,
            } as LangChangeEvent),
            this.translate.onLangChange
        ).pipe(map((s) => s.lang));
    }

    showUgentWarning(location: Location): boolean {
        return location.institution === 'UGent';
    }

    futureReservations(reservations: LocationReservation[]): LocationReservation[] {
        return reservations.filter((res) => !res.timeslot.isInPast());
    }

    nonDeletedReservation(reservations: LocationReservation[]): LocationReservation[] {
        return reservations.filter((res) => res.state !== LocationReservationState.DELETED);
    }

    sortedLocalReservations(reservations: LocationReservation[]): LocationReservation[] {
        return Array.from(reservations).sort((a, b) => a.timeslot.getStartMoment().isBefore(b.timeslot.getStartMoment()) ? 1 : -1);
    }

    penaltyPointsOk(location: Location): Observable<boolean> {
        return location.usesPenaltyPoints ?
            this.authenticationService.penaltyObservable.pipe(map(p => p.points < 100)) : of(true);
    }

    toggleSubscription(): void {
        this.locationSubscribed = !this.locationSubscribed;

        // Check if the user is logged in
        if (!this.authenticationService.isLoggedIn()) {
            return;
        }

        if (this.locationSubscribed) {
            this.locationService.subscribeToLocation(this.locationId).subscribe();
        } else {
            this.locationService.unsubscribeFromLocation(this.locationId).subscribe();
        }
    }

}
