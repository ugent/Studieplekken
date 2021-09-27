import { Component, OnDestroy, OnInit, TemplateRef } from '@angular/core';
import { Location } from '../../shared/model/Location';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable, Subscription } from 'rxjs';
import { LocationService } from '../../services/api/locations/location.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { CalendarEvent } from 'angular-calendar';
import { TranslateService } from '@ngx-translate/core';
import { LocationTag } from '../../shared/model/LocationTag';
import { TimeslotsService } from '../../services/api/calendar-periods/timeslot.service';
import {
  includesTimeslot,
  Timeslot,
  timeslotEquals,
  timeslotToCalendarEvent,
} from 'src/app/shared/model/Timeslot';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';
import {
  CalendarPeriod
} from '../../shared/model/CalendarPeriod';
import {
  defaultLocationImage,
  LocationStatus,
  msToShowFeedback,
} from '../../app.constants';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import * as moment from 'moment';
import { DatePipe } from '@angular/common';
import { Pair } from '../../shared/model/helpers/Pair';
import {
  ApplicationTypeFunctionalityService
} from 'src/app/services/functionality/application-type/application-type-functionality.service';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import { map } from 'rxjs/internal/operators/map';
import {
  ConversionToCalendarEventService
} from '../../services/styling/CalendarEvent/conversion-to-calendar-event.service';
import { distinctUntilChanged, distinctUntilKeyChanged } from 'rxjs/operators';

@Component({
  selector: 'app-location-details',
  templateUrl: './location-details.component.html',
  styleUrls: ['./location-details.component.css', '../location.css'],
  providers: [DatePipe],
})
export class LocationDetailsComponent implements OnInit, OnDestroy {
  location: Observable<Location>;
  locationId: number;
  tags: LocationTag[];

  events: Timeslot[] = [];

  editor: unknown = ClassicEditor;

  selectedSubject: BehaviorSubject<LocationReservation[]> = new BehaviorSubject<
    LocationReservation[]
  >([]);
  originalList: LocationReservation[];
  subscription: Subscription;

  showSuccess = false;
  showError = false;

  isModified = false;

  isFirst = true;

  description = {
    show: '',
    english: '',
    dutch: '',
  };

  altImageUrl = defaultLocationImage;
  imageUrlErrorOccurred = false;

  status: Pair<LocationStatus, string>;
  statusInCurrentLang: string;

  currentLang: string;

  modalRef: BsModalRef;
  newReservations: LocationReservation[];
  removedReservations: LocationReservation[];

  private timeouts: number[] = [];
  locationReservations: LocationReservation[];
  showAdmin: boolean;
  showLockersManagement: boolean;
  capacity: number;

  locationSub: Subscription;
  calendarSub: Subscription;

  constructor(
    private locationService: LocationService,
    private route: ActivatedRoute,
    private sanitizer: DomSanitizer,
    private translate: TranslateService,
    private timeslotsService: TimeslotsService,
    private datepipe: DatePipe,
    private authenticationService: AuthenticationService,
    private locationReservationService: LocationReservationsService,
    private modalService: BsModalService,
    private functionalityService: ApplicationTypeFunctionalityService,
    private conversionService: ConversionToCalendarEventService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.locationId = Number(this.route.snapshot.paramMap.get('locationId'));

    // Check if locationId is a Number before proceeding. If NaN, redirect to dashboard.
    if (isNaN(this.locationId)) {
      this.router.navigate(['/dashboard']).catch(console.log);
      return;
    }

    this.location = this.locationService.getLocation(this.locationId);
    this.showAdmin = this.authenticationService.isAdmin();
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

  ngOnDestroy(): void {
    this.locationSub?.unsubscribe();
    this.calendarSub?.unsubscribe();
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
    if (moment().isBefore(currentTimeslot.reservableFrom)) {
      return;
    }

    
    const reservation: LocationReservation = {
      user: this.authenticationService.userValue(),
      timeslot: currentTimeslot,
    };

    const timeslotIsSelected = this.selectedSubject.value.some((r) =>
      timeslotEquals(r.timeslot, reservation.timeslot)
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



    // If it's already selected, unselect
    if (timeslotIsSelected) {
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

  getGoogleMapsUrl(location: Location): SafeResourceUrl {
    const url =
      'https://www.google.com/maps?q=' +
      location.building.address +
      '&output=embed';
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

  setDescriptionToShow(): void {
    const lang = this.translate.currentLang;

    // Depending on the browser language, return the description of the language.
    // Show the dutch description if the browser language is 'nl'.
    // Otherwise, show the english description.
    this.description.show =
      lang === 'nl' ? this.description.dutch : this.description.english;
  }

  updateCalendar(): void {
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
      this.originalList = [...reservations.filter(r => r.timeslot.locationId === this.locationId)];
      this.timeouts.forEach(t => clearTimeout(t))

      // Only do this once, when selectedSubject isn't initialized yet.
      if(this.isFirst) {
        this.isFirst = false;
        this.selectedSubject.next([...this.originalList])
        return;
      }

      this.timeouts = timeslots
        .map(e => (e.reservableFrom.valueOf() - moment().valueOf()))
        .filter(d => d > 0)
        .filter(d => d < 1000 * 60 * 60 * 25 *2) // don't set more than two days in advance (weird bugs if you do)
        .map(d => setTimeout(() => this.draw(timeslots, proposedReservations), d));
      this.draw(timeslots, proposedReservations);
    })
  }

  draw(timeslots, proposedReservations): void {
    this.events = timeslots.map(t => timeslotToCalendarEvent(
      t, this.currentLang, [...proposedReservations]
    ))
    console.log(proposedReservations)
  }

  updateReservationIsPossible(): boolean {
    return !(this.isModified && this.authenticationService.isLoggedIn());
  }

  commitReservations(template: TemplateRef<unknown>): void {
    // We need to find out which of the selected boxes need to be removed, and which need to be added.
    // Therefore, we calculate selected \ previous
    this.newReservations = this.selectedSubject.value.filter(
      (selected) =>
        !includesTimeslot(
          this.originalList.map((l) => l.timeslot),
          selected.timeslot
        )
    );

    // And we calculate previous \ selected
    this.removedReservations = this.originalList.filter(
      (selected) =>
        !includesTimeslot(
          this.selectedSubject.value.map((l) => l.timeslot),
          selected.timeslot
        )
    );

    this.modalRef = this.modalService.show(template);
  }

  confirmReservationChange(): void {
    combineLatest([
      this.locationReservationService.postLocationReservations(
        this.newReservations
      ),
      this.locationReservationService.deleteLocationReservations(
        this.removedReservations
      ),
    ]).subscribe(
      () => {
        this.updateCalendar();
        this.showSuccess = true;
        this.showError = false;
        setTimeout(() => (this.showSuccess = false), msToShowFeedback);
      },
      () => {
        this.isModified = true;
        this.showSuccess = false;
        this.showError = true;
        setTimeout(() => (this.showError = false), msToShowFeedback);
      }
    );
    this.isModified = false;
    this.modalRef.hide();
  }

  declineReservationChange(): void {
    this.modalRef.hide();
  }

  formatReservation(reservation: LocationReservation): Observable<string> {
    return this.locationService.getLocation(reservation.timeslot.locationId)
      .pipe(map(location => {
        const date = reservation.timeslot.timeslotDate.format('DD/MM/YYYY');
        const hour = reservation.timeslot.openingHour.format('HH:mm');

        return location.name + ' (' + date + ' ' + hour + ')';
      }
      ))
  }

  loggedIn(): boolean {
    return this.authenticationService.isLoggedIn();
  }

  showDescription(description: string): boolean {
    return description !== '';
  }
}
