import {Component, OnDestroy, OnInit, TemplateRef} from '@angular/core';
import {Location} from '../../shared/model/Location';
import {ActivatedRoute} from '@angular/router';
import {BehaviorSubject, combineLatest, Observable, Subscription} from 'rxjs';
import {LocationService} from '../../services/api/locations/location.service';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {CalendarEvent} from 'angular-calendar';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import {TranslateService} from '@ngx-translate/core';
import {LocationTag} from '../../shared/model/LocationTag';
import {CalendarPeriodsService} from '../../services/api/calendar-periods/calendar-periods.service';
import {includesTimeslot, Timeslot, timeslotEquals} from 'src/app/shared/model/Timeslot';
import {LocationReservationsService} from 'src/app/services/api/location-reservations/location-reservations.service';
import {AuthenticationService} from 'src/app/services/authentication/authentication.service';
import {LocationReservation} from 'src/app/shared/model/LocationReservation';
import {CalendarPeriod, mapCalendarPeriodsToCalendarEvents} from '../../shared/model/CalendarPeriod';
import {defaultLocationImage, LocationStatus, msToShowFeedback} from '../../app.constants';
import {BsModalRef, BsModalService} from 'ngx-bootstrap/modal';
import * as moment from 'moment';
import {DatePipe} from '@angular/common';
import {Pair} from '../../shared/model/helpers/Pair';
import {
  ApplicationTypeFunctionalityService
} from 'src/app/services/functionality/application-type/application-type-functionality.service';

@Component({
  selector: 'app-location-details',
  templateUrl: './location-details.component.html',
  styleUrls: ['./location-details.component.css', '../location.css'],
  providers: [DatePipe]
})
export class LocationDetailsComponent implements OnInit, OnDestroy {
  location: Observable<Location>;
  locationName: string;
  tags: LocationTag[];

  events: CalendarEvent[] = [];

  selectedSubject: BehaviorSubject<LocationReservation[]> = new BehaviorSubject([]);
  originalList: LocationReservation[];
  subscription: Subscription;

  showSuccess = false;
  showError = false;

  currentTimeslot: Timeslot;
  isModified = false;

  editor = ClassicEditor;
  description = {
    show: '',
    english: '',
    dutch: ''
  };

  altImageUrl = defaultLocationImage;
  imageUrlErrorOccurred = false;

  status: Pair<LocationStatus, string>;
  statusInCurrentLang: string;

  currentLang: string;

  modalRef: BsModalRef;
  newReservations: LocationReservation[];
  removedReservations: LocationReservation[];

  calendarMap: Map<number, CalendarPeriod> = new Map();
  locationReservations: Observable<LocationReservation[]>;
  showAdmin: boolean;
  showLockersManagement: boolean;
  capacity: number;

  locationSub: Subscription;
  calendarSub: Subscription;

  constructor(private locationService: LocationService,
              private route: ActivatedRoute,
              private sanitizer: DomSanitizer,
              private translate: TranslateService,
              private calendarPeriodsService: CalendarPeriodsService,
              private datepipe: DatePipe,
              private authenticationService: AuthenticationService,
              private locationReservationService: LocationReservationsService,
              private modalService: BsModalService,
              private functionalityService: ApplicationTypeFunctionalityService) {
  }

  ngOnInit(): void {
    this.locationName = this.route.snapshot.paramMap.get('locationName');
    this.location = this.locationService.getLocation(this.locationName);
    this.showAdmin = this.authenticationService.isAdmin();
    this.currentLang = this.translate.currentLang;

    // when the location is loaded, setup the descriptions
    this.locationSub = this.location.subscribe(next => {
      this.description.dutch = next.descriptionDutch;
      this.description.english = next.descriptionEnglish;
      this.capacity = next.numberOfSeats;
      this.setDescriptionToShow();

      this.tags = next.assignedTags;

      this.updateCalendar();
    });

    // if the browser language would change, the description needs to change
    this.translate.onLangChange.subscribe(
      () => {
        this.setDescriptionToShow();
        this.currentLang = this.translate.currentLang;
        this.updateCalendar();
      }
    );

    setInterval(() => {
      this.updateCalendar();
    }, 300000); // 5 minutes

    this.showLockersManagement = this.functionalityService.showLockersManagementFunctionality();
  }

  ngOnDestroy(): void {
    this.locationSub.unsubscribe();
    this.calendarSub.unsubscribe();
  }

  locationStatusColorClass(): string {
    return (this.status && this.status.first === LocationStatus.OPEN) ? 'open' : 'closed';
  }

  timeslotPicked(event: any): void {
    if (!event.hasOwnProperty('timeslot')) {
      // the calendar period is not reservable
      return;
    }

    if (!this.loggedIn()) {
      // When not logged in, calendar periods are unclickable
      return;
    }


    // If the selected timeslot is not yet reservable, don't do anything
    const calendarPeriod: CalendarPeriod = event.calendarPeriod;
    if (moment().isBefore(calendarPeriod.reservableFrom)) {
      return;
    }

    this.isModified = true;

    this.currentTimeslot = event.timeslot;

    const reservation: LocationReservation = {user: this.authenticationService.userValue(), timeslot: this.currentTimeslot};
    const timeslotIsSelected = this.selectedSubject.value.some(r => timeslotEquals(r.timeslot, reservation.timeslot));

    if (this.currentTimeslot.amountOfReservations >= this.capacity && !timeslotIsSelected) {
      return;
    }
    this.isModified = true;


    // If it's already selected, unselect
    if (timeslotIsSelected) {
      const nextval = this.selectedSubject.value.filter(r => !timeslotEquals(r.timeslot, reservation.timeslot));
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
    const url = 'https://www.google.com/maps?q=' + location.building.address + '&output=embed';
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

  setDescriptionToShow(): void {
    const lang = this.translate.currentLang;

    // Depending on the browser language, return the description of the language.
    // Show the dutch description if the browser language is 'nl'.
    // Otherwise, show the english description.
    this.description.show = lang === 'nl' ? this.description.dutch : this.description.english;
  }

  updateCalendar(): void {
    // retrieve the calendar periods and map them to calendar events used by Angular Calendar
    if (this.subscription) {
      this.subscription.unsubscribe();
    }

    this.calendarSub = combineLatest([
      this.calendarPeriodsService.getCalendarPeriodsOfLocation(this.locationName),
      this.authenticationService.getLocationReservations(),
    ])
      .subscribe(([periods, reservations]) => {
        this.originalList = [...reservations];
        this.selectedSubject.next(reservations);

        periods.forEach(element => {
          this.calendarMap.set(element.id, element);

          // @Reviewers: I have no idea what the code below actually does. It breaks the highlight of your reserved slots, though.

          // const duration = element.reservableFrom.valueOf() - moment().valueOf();
          // if (duration > 0) {
          //   setTimeout(() => {
          //     this.events = mapCalendarPeriodsToCalendarEvents([...this.calendarMap.values()], this.currentLang, []);
          //   }, duration);
          // }
        });

        this.subscription = this.selectedSubject.asObservable().subscribe(proposedReservations =>
          this.events = mapCalendarPeriodsToCalendarEvents(periods, this.currentLang, [...proposedReservations]));
      });
  }

  updateReservationIsPossible(): boolean {
    return !(this.isModified && this.authenticationService.isLoggedIn());
  }

  commitReservations(template: TemplateRef<any>): void {
    // We need to find out which of the selected boxes need to be removed, and which need to be added.
    // Therefore, we calculate selected \ previous
    this.newReservations = this.selectedSubject.value
      .filter(selected => !includesTimeslot(this.originalList.map(l => l.timeslot), selected.timeslot));

    // And we calculate previous \ selected
    this.removedReservations = this.originalList
      .filter(selected => !includesTimeslot(this.selectedSubject.value.map(l => l.timeslot), selected.timeslot));

    this.modalRef = this.modalService.show(template);
  }

  confirmReservationChange(): void {
    combineLatest([
      this.locationReservationService.postLocationReservations(this.newReservations),
      this.locationReservationService.deleteLocationReservations(this.removedReservations)
    ]).subscribe(() => {
      this.updateCalendar();
      this.showSuccess = true;
      this.showError = false;
      setTimeout(() => this.showSuccess = false, msToShowFeedback);
    }, () => {
      this.isModified = true;
      this.showSuccess = false;
      this.showError = true;
      setTimeout(() => this.showError = false, msToShowFeedback);
    });
    this.isModified = false;
    this.modalRef.hide();
  }

  declineReservationChange(): void {
    this.modalRef.hide();
  }

  getBeginHour(calendarPeriod: CalendarPeriod, timeslot: Timeslot): moment.Moment {
    const d = moment(timeslot.timeslotDate.format('DD-MM-YYYY') + 'T' + calendarPeriod.openingTime.format('HH:mm'), 'DD-MM-YYYYTHH:mm');
    d.add(timeslot.timeslotSeqnr * calendarPeriod.timeslotLength, 'minutes');
    return d;
  }

  formatReservation(reservation: LocationReservation): string {
    const name = this.calendarMap.get(reservation.timeslot.calendarId).location.name;
    const date = reservation.timeslot.timeslotDate.format('DD/MM/YYYY');
    const hour = this.getBeginHour(this.calendarMap.get(reservation.timeslot.calendarId), reservation.timeslot).format('HH:mm');

    return name + ' (' + date + ' ' + hour + ')';
  }

  loggedIn(): boolean {
    return this.authenticationService.isLoggedIn();
  }

  showDescription(description: string): boolean {
    return description !== '';
  }
}
