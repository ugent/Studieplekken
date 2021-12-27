import { DatePipe } from '@angular/common';
import { AfterViewInit, Component, OnDestroy, OnInit, TemplateRef } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import * as moment from 'moment';
import { BehaviorSubject, combineLatest, merge, Observable, of, Subscription, throwError } from 'rxjs';
import { map } from 'rxjs/internal/operators/map';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import {
  ApplicationTypeFunctionalityService
} from 'src/app/services/functionality/application-type/application-type-functionality.service';
import { TimeslotCalendarEventService } from 'src/app/services/timeslots/timeslot-calendar-event/timeslot-calendar-event.service';
import { LocationReservation, LocationReservationState } from 'src/app/shared/model/LocationReservation';
import {
  includesTimeslot,
  Timeslot,
  timeslotEquals,
} from 'src/app/shared/model/Timeslot';
import { BreadcrumbService } from 'src/app/stad-gent-components/header/breadcrumbs/breadcrumb.service';
import {
  defaultTeaserImages,
  LocationStatus,
  msToShowFeedback
} from '../../app.constants';
import { TimeslotsService } from '../../services/api/calendar-periods/timeslot.service';
import { LocationService } from '../../services/api/locations/location.service';
import { Pair } from '../../shared/model/helpers/Pair';
import { Location } from '../../shared/model/Location';
import { LocationTag } from '../../shared/model/LocationTag';
import { Moment } from 'moment';
import * as Leaf from 'leaflet';
import { LoginRedirectService } from 'src/app/services/authentication/login-redirect.service';
import { catchError, tap } from 'rxjs/operators';
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

  events: CalendarEvent<{timeslot: Timeslot}>[] = [];

  editor: unknown = ClassicEditor;

  selectedSubject: BehaviorSubject<LocationReservation[]> = new BehaviorSubject<
    LocationReservation[]
  >([]);
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

  altImageUrl = defaultTeaserImages[Math.floor(Math.random()*defaultTeaserImages.length)];
  imageUrlErrorOccurred = false;

  status: Pair<LocationStatus, string>;
  statusInCurrentLang: string;

  currentLang: string;

  modalRef: MatDialogRef<unknown, unknown>;
  newReservations: LocationReservation[];
  removedReservations: LocationReservation[];

  private timeouts: number[] = [];
  locationReservations: LocationReservation[];
  showAdmin: boolean;
  showLockersManagement: boolean;
  capacity: number;

  locationSub: Subscription;
  calendarSub: Subscription;

  leafletMap: Leaf.Map;

  pendingReservations: LocationReservation[] = [];
  rejectedReservations: LocationReservation[] = [];
  acceptedReservations: LocationReservation[] = [];
  newReservationCreator: Observable<moment.Moment[]>;

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
    private functionalityService: ApplicationTypeFunctionalityService,
    private router: Router,
    private breadcrumbs: BreadcrumbService,
    private timeslotCalendarEventService: TimeslotCalendarEventService,
    private loginRedirect: LoginRedirectService

  ) { }

  ngOnInit(): void {
    this.locationId = Number(this.route.snapshot.paramMap.get('locationId'));

    this.breadcrumbs.setCurrentBreadcrumbs([{ pageName: "Details", url: `/dashboard/${this.locationId}` }])
    this.loginRedirect.registerUrl(`/dashboard/${this.locationId}`);

    // Check if locationId is a Number before proceeding. If NaN, redirect to dashboard.
    if (isNaN(this.locationId)) {
      this.router.navigate(['/dashboard']).catch(console.log);
      return;
    }

    this.location = this.locationService.getLocation(this.locationId);

    this.showAdmin = this.authenticationService.isAdmin();
    combineLatest([this.authenticationService.user, this.location]).subscribe(([user, location]) => {
      this.showAdmin = this.authenticationService.isAdmin() ||
       user.userAuthorities.map(a => a.authorityId).includes(location.authority.authorityId);
    })

    this.authenticationService
      .getLocationReservations()
      .subscribe((next) => (this.locationReservations = next));
    this.currentLang = this.translate.currentLang;

    // when the location is loaded, setup the descriptions
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
    }, 60 * 1000); // 1 minute

    this.showLockersManagement = this.functionalityService.showLockersManagementFunctionality();
  }

  ngAfterViewInit(): void {
    if (this.leafletMap) {
      this.leafletMap.off();
      this.leafletMap.remove();
    }
    // when the location is loaded, setup the Leaflet map
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

  locationStatusColorClass(): string {
    return this.status && this.status.first === LocationStatus.OPEN
      ? 'open'
      : 'closed';
  }

  timeslotPicked(event: Event): void {
    if (!event['timeslot']) {
      // the calendar period is not reservable
      return;
    }
    const currentTimeslot = event['timeslot'] as Timeslot;

    if (!this.loggedIn()) {
      // When not logged in, calendar periods are unclickable
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


    
    let reservation: LocationReservation = {
      state: null, // TODO(ydndonck): Should this be approved? Something else? Currently using database default.
      user: this.authenticationService.userValue(),
      timeslot: currentTimeslot,
    };
    // Check if reservation doesn't already exist in deleted state.
    /*for (const res of this.selectedSubject.value) {
      if (res.timeslot.timeslotSequenceNumber == currentTimeslot.timeslotSequenceNumber) {
        reservation = res;
        reservation.state = R
        break;
      }
    }*/

    const timeslotIsSelected = this.selectedSubject.value.some((r) =>
      timeslotEquals(r.timeslot, reservation.timeslot) && r.state != LocationReservationState.DELETED
    );

    // if it is full and you don't have this reservation yet, unselect
    if (
      currentTimeslot.amountOfReservations >=
      currentTimeslot.seatCount &&
      !timeslotIsSelected
    ) {
      return;
    }

    this.isModified = true;

    const oldReservation = this.originalList.find(t => t.timeslot.timeslotSequenceNumber === currentTimeslot.timeslotSequenceNumber);
    
    if (!timeslotIsSelected && oldReservation && (oldReservation.state === LocationReservationState.REJECTED || oldReservation.state == LocationReservationState.DELETED)) { // If it was rejected, allow to try again
      const nextval = [...this.selectedSubject.value.filter(o => o !== oldReservation && o.timeslot.timeslotSequenceNumber !== oldReservation.timeslot.timeslotSequenceNumber), reservation];
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

  updateCalendar(update_selection = false): void {
    // retrieve the calendar periods and map them to calendar events used by Angular Calendar
    if (this.subscription) {
      this.subscription.unsubscribe();
    }

    this.calendarSub = combineLatest([
      this.timeslotsService.getTimeslotsOfLocation(this.locationId),
      this.authenticationService.getLocationReservations(),
      this.selectedSubject,
    ])
      .subscribe(([timeslots, reservations, proposedReservations]) => {
        this.originalList = [...reservations.filter(r => r.timeslot.locationId == this.locationId)];
        this.pendingReservations = this.originalList.filter(locres => locres.state === LocationReservationState.PENDING);
        this.timeouts.forEach(t => clearTimeout(t));

        // Only do this once, when selectedSubject isn't initialized yet.
        // TODO: Why only the first time? If there is a faulty reservation it'll keep trying (and failing) to make that reservation.
        // If this line can be removed then this scenario
        if (this.isFirst || update_selection) {
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

  draw(timeslots: Timeslot[], proposedReservations : LocationReservation[]): void {
    this.events = timeslots.map(t => this.timeslotCalendarEventService.timeslotToCalendarEvent(
      t, this.currentLang, [...proposedReservations]
    ));
  }

  updateReservationIsPossible(): boolean {
    return !(this.isModified && this.authenticationService.isLoggedIn());
  }

  commitReservations(template: TemplateRef<unknown>): void {
    // We need to find out which of the selected boxes need to be removed, and which need to be added.
    // Therefore, we calculate selected \ previous
    this.newReservations = this.selectedSubject.value.filter(
      (selected) => {
        if (selected.state === LocationReservationState.DELETED) {
          return false;
        }
        const tempList: LocationReservation[] = this.originalList.filter(
          (reservation) => {
            return  reservation.user.userId == selected.user.userId &&
                    reservation.timeslot.timeslotSequenceNumber == selected.timeslot.timeslotSequenceNumber;
          }
        );
        const oldReservation = tempList.length > 0? tempList[0] : undefined;
        if (!oldReservation) {
          return true;
        }
        if (oldReservation && (oldReservation.state === LocationReservationState.DELETED || oldReservation.state == LocationReservationState.REJECTED)) {
          return true;
        }
        return false;
      }
        /*!includesTimeslot(
          this.originalList.map((l) => l.timeslot),
          selected.timeslot
        ) ||
         this.originalList.find(l => l.timeslot.timeslotSequenceNumber === selected.timeslot.timeslotSequenceNumber)?.state === LocationReservationState.REJECTED*/
    );

    // And we calculate previous \ selected
    this.removedReservations = this.originalList.filter(
      (selected) => {
        if (selected.state === LocationReservationState.DELETED) {
          return false;
        }
        const tempList: LocationReservation[] = this.selectedSubject.value.filter(
          (res) =>  res.timeslot.timeslotSequenceNumber === selected.timeslot.timeslotSequenceNumber
        );
        const reservation = tempList.length > 0 ? tempList[0] : undefined;
        if (!reservation) {
          return true;
        }
        return false;
      });
      /*!includesTimeslot(
        this.selectedSubject.value.map((l) => l.timeslot),
        selected.timeslot
      ));*/
      /*{
        const tempList: LocationReservation[] = this.selectedSubject.value.filter(
          (reservation) => reservation.user.userId == selected.user.userId && reservation.timeslot.timeslotSequenceNumber == selected.timeslot.timeslotSequenceNumber
        );
        const selectedReservation = tempList.length > 0? tempList[0] : undefined;
        if (selectedReservation && selectedReservation.state !== selected.state && selected.state === LocationReservationState.DELETED) {
          return true;
        }
        return false;
      }
        )*/
    // );

    this.modalRef = this.modalService.open(template, { panelClass: ["cs--cyan", "bigmodal"] });
  }

  confirmReservationChange(template: TemplateRef<unknown>): void {
    this.modalService.open(template, { panelClass: ["cs--cyan", "bigmodal"] });
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
        () => this.updateCalendar()
      ),
      map(([res]) => res)
      );

    // .subscribe(
    //   (res) => {
    //     this.showError = false;
    //     this.showSuccessDeletion = false;
    //     this.showSuccessPendingShort = false;
    //     this.showSuccessPendingLong = false;

    //     const reservationProcessingStart : Moment[] = res[0];

    //     this.updateCalendar();
    //     if (reservationProcessingStart.length !== 0) {
    //       const now : Moment = moment();
    //       const maxMoment : Moment = moment.max(reservationProcessingStart);
    //       const hasDelayedReservation: boolean = now.isBefore(maxMoment);
    //       if (hasDelayedReservation) {
    //         this.showSuccessPendingLong = true;
    //         const msDelayUntilReservationsStart = moment.duration(maxMoment.diff(now)).asMilliseconds();
    //         setTimeout(() => (document.querySelector("#location_reservations").scrollIntoView()), 300);
    //         setTimeout(() => (this.showSuccessPendingLong = false), msDelayUntilReservationsStart);
    //       } else {
    //         this.showSuccessPendingShort = true;
    //         setTimeout(() => (document.querySelector("#location_reservations").scrollIntoView({'behavior': 'smooth'})), 300);
    //         setTimeout(() => (this.showSuccessPendingShort = false), msToShowFeedback);
    //       }
    //     } else {
    //       this.showSuccessDeletion = true;
    //       setTimeout(() => (this.showSuccessDeletion = false), msToShowFeedback);
    //     }
    //   },
    //   () => {
    //     this.showError = true;
    //     this.showSuccessDeletion = false;
    //     this.showSuccessPendingShort = false;
    //     this.showSuccessPendingLong = false;
    //     this.isModified = true;
    //     setTimeout(() => (this.showError = false), msToShowFeedback);
    //   }
    // );
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

  loggedIn(): boolean {
    return this.authenticationService.isLoggedIn();
  }

  showDescription(description: string): boolean {
    return description !== '';
  }

  setupLeafletMap(location: Location): void {

    const originalTile = Leaf.tileLayer('https://geo.gent.be/geoserver/gwc/service/wmts?layer=SG-E-Stadsplan%3AStadsplan&style=default&tilematrixset=SG-WEB%20MERCATOR&Service=WMTS&Request=GetTile&Version=1.0.0&Format=image%2Fpng&TileMatrix=SG-WEB%20MERCATOR%3A{z}&TileCol={x}&TileRow={y}', {
      tileSize: 512,
      zoomOffset: -1,
      maxZoom: 25,
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

  getStateI18NObject(state : LocationReservationState): string {
    return 'profile.reservations.locations.table.attended.' + state;
  }

  getLinkToElement(id : string): string {
    return document.location.href.replace(document.location.hash, '') + id;
  }
  currentLanguage(): Observable<string> {
    return merge<LangChangeEvent, LangChangeEvent>(
      of<LangChangeEvent>({
        lang: this.translate.currentLang,
      } as LangChangeEvent),
      this.translate.onLangChange
    ).pipe(map((s) => s.lang));
  }

  showUgentWarning(location: Location) {
    return location.institution == 'UGent';
  }

  futureReservations(reservations: LocationReservation[]) {
    return reservations.filter((res) => !res.timeslot.isInPast());
  }

  nonDeletedReservation(reservations: LocationReservation[]) {
    return reservations.filter((res) => res.state !== LocationReservationState.DELETED);
  }

}
