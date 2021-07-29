import { Component, OnDestroy, OnInit, TemplateRef } from '@angular/core';
import { Location } from '../../shared/model/Location';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, combineLatest, Observable, Subscription } from 'rxjs';
import { LocationService } from '../../services/api/locations/location.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { CalendarEvent } from 'angular-calendar';
import { TranslateService } from '@ngx-translate/core';
import { LocationTag } from '../../shared/model/LocationTag';
import { CalendarPeriodsService } from '../../services/api/calendar-periods/calendar-periods.service';
import {
  includesTimeslot,
  Timeslot,
  timeslotEquals,
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
import {
  ConversionToCalendarEventService
} from '../../services/styling/CalendarEvent/conversion-to-calendar-event.service';

@Component({
  selector: 'app-location-details',
  templateUrl: './location-details.component.html',
  styleUrls: ['./location-details.component.scss', '../location.css'],
  providers: [DatePipe],
})
export class LocationDetailsComponent implements OnInit, OnDestroy {
  location: Observable<Location>;
  locationId: number;
  tags: LocationTag[];

  events: CalendarEvent[] = [];

  editor: unknown = ClassicEditor;

  selectedSubject: BehaviorSubject<LocationReservation[]> = new BehaviorSubject<
    LocationReservation[]
  >([]);
  originalList: LocationReservation[];
  subscription: Subscription;

  showSuccess = false;
  showError = false;

  currentTimeslot: Timeslot;
  isModified = false;

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

  calendarMap: Map<number, CalendarPeriod> = new Map<number, CalendarPeriod>();
  locationReservations: LocationReservation[] = [];
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
    private calendarPeriodsService: CalendarPeriodsService,
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

  timeslotPicked(event: {
    timeslot: Timeslot;
    calendarPeriod: CalendarPeriod;
  }): void {
    if (
      !event.timeslot || // the calendar period is not reservable
      !this.loggedIn() || // when not logged in, calendar periods are unclickable
      !this.conversionService.clickableBasedOnTime(
        event,
        this.locationReservations
      )
    ) {
      return;
    }

    // If the selected timeslot is not yet reservable, don't do anything
    const calendarPeriod: CalendarPeriod = event.calendarPeriod;
    if (moment().isBefore(calendarPeriod.reservableFrom)) {
      return;
    }

    this.isModified = true;
    this.currentTimeslot = event.timeslot;

    const reservation: LocationReservation = {
      user: this.authenticationService.userValue(),
      timeslot: this.currentTimeslot,
    };
    const timeslotIsSelected = this.selectedSubject.value.some((r) =>
      timeslotEquals(r.timeslot, reservation.timeslot)
    );

    if (
      this.currentTimeslot.amountOfReservations >=
        this.currentTimeslot.seatCount &&
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
      this.calendarPeriodsService.getCalendarPeriodsOfLocation(this.locationId),
      this.authenticationService.getLocationReservations(),
    ]).subscribe(([periods, reservations]) => {
      this.originalList = [...reservations];
      this.selectedSubject.next(reservations);

      periods.forEach((element) => {
        this.calendarMap.set(element.id, element);

        // To avoid that users all refresh the page together when the calendar
        // period is open for reservation, a timeout is set that refreshes the
        // timeslots in the calendar. This makes them clickable without the
        // need for refreshing the page which saves a lot of requests to the server.
        // But, to avoid that a lot of timers are initialized, only those calendar
        // periods that are reservable within one day are actually initialized.
        // Another pitfall is that if the duration is too big for an uint32, the
        // timer is triggered immediately: https://catonmat.net/settimeout-setinterval.
        const duration = element.reservableFrom.valueOf() - moment().valueOf();
        const oneDay = 24 * 3600 * 1000;
        if (duration > 0 && duration < oneDay) {
          setTimeout(() => {
            this.events = this.conversionService.mapCalendarPeriodsToCalendarEvents(
              [...this.calendarMap.values()],
              this.currentLang,
              [...this.selectedSubject.value]
            );
          }, duration);
        }
      });

      this.subscription = this.selectedSubject
        .asObservable()
        .subscribe(
          (proposedReservations) =>
            (this.events = this.conversionService.mapCalendarPeriodsToCalendarEvents(
              periods,
              this.currentLang,
              [...proposedReservations]
            ))
        );
    });
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

  getBeginHour(
    calendarPeriod: CalendarPeriod,
    timeslot: Timeslot
  ): moment.Moment {
    const d = moment(
      timeslot.timeslotDate.format('DD-MM-YYYY') +
        'T' +
        calendarPeriod.openingTime.format('HH:mm'),
      'DD-MM-YYYYTHH:mm'
    );
    d.add(timeslot.timeslotSeqnr * calendarPeriod.timeslotLength, 'minutes');
    return d;
  }

  formatReservation(reservation: LocationReservation): string {
    const name = this.calendarMap.get(reservation.timeslot.calendarId).location
      .name;
    const date = reservation.timeslot.timeslotDate.format('DD/MM/YYYY');
    const hour = this.getBeginHour(
      this.calendarMap.get(reservation.timeslot.calendarId),
      reservation.timeslot
    ).format('HH:mm');

    return name + ' (' + date + ' ' + hour + ')';
  }

  loggedIn(): boolean {
    return this.authenticationService.isLoggedIn();
  }

  showDescription(description: string): boolean {
    return description !== '';
  }
}
