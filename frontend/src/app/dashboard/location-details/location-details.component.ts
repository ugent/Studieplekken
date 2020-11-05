import {Component, OnInit} from '@angular/core';
import {Location} from '../../shared/model/Location';
import {ActivatedRoute} from '@angular/router';
import {BehaviorSubject, combineLatest, Observable, Subscription} from 'rxjs';
import {LocationService} from '../../services/api/locations/location.service';
import {vars, msToShowFeedback} from '../../../environments/environment';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {CalendarEvent} from 'angular-calendar';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import {TranslateService} from '@ngx-translate/core';
import {LocationTag} from '../../shared/model/LocationTag';
import {TagsService} from '../../services/api/tags/tags.service';
import {CalendarPeriodsService} from '../../services/api/calendar-periods/calendar-periods.service';
import { mapCalendarPeriodsToCalendarEvents } from 'src/app/shared/model/CalendarPeriod';
import { includesTimeslot, Timeslot, timeslotEquals } from 'src/app/shared/model/Timeslot';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';
import { Subject } from 'rxjs/internal/Subject';

@Component({
  selector: 'app-location-details',
  templateUrl: './location-details.component.html',
  styleUrls: ['./location-details.component.css']
})
export class LocationDetailsComponent implements OnInit {
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

  altImageUrl = vars.defaultLocationImage;

  currentLang: string;

  constructor(private locationService: LocationService,
              private tagsService: TagsService,
              private route: ActivatedRoute,
              private sanitizer: DomSanitizer,
              private translate: TranslateService,
              private calendarPeriodsService: CalendarPeriodsService,
              private authenticationService: AuthenticationService,
              private locationReservationService: LocationReservationsService) { }

  ngOnInit(): void {
    this.locationName = this.route.snapshot.paramMap.get('locationName');
    this.location = this.locationService.getLocation(this.locationName);
    this.currentLang = this.translate.currentLang;

    // when the location is loaded, setup the descriptions
    this.location.subscribe(next => {
      this.description.dutch = next.descriptionDutch;
      this.description.english = next.descriptionEnglish;
      this.setDescriptionToShow();

      this.tags = next.assignedTags;

      this.updateCalendar();
    });

    // if the browser language would change, the description needs to change
    this.translate.onLangChange.subscribe(
      () => {
        this.setDescriptionToShow();
        this.currentLang = this.translate.currentLang;
      }
    );
  }

  timeslotPicked(event: Timeslot): void {
    if(!event.hasOwnProperty('timeslotSeqnr')) {
      return;
    }

    this.isModified = true;
    this.currentTimeslot = event;
    const reservation: LocationReservation = {user: this.authenticationService.userValue(), timeslot: event};

    // If it's already selected, unselect
    if (this.selectedSubject.value.some(r => timeslotEquals(r.timeslot, reservation.timeslot))) {
      const nextval = this.selectedSubject.value.filter(r => !timeslotEquals(r.timeslot, reservation.timeslot));
      this.selectedSubject.next(nextval);
      // If it's not yet selected, add to selection
    } else {
      const nextval = [...this.selectedSubject.value, reservation];
      this.selectedSubject.next(nextval);
    }
  }

  handleImageError(location: Location): void {
    location.imageUrl = vars.defaultLocationImage;
  }

  getGoogleMapsUrl(location: Location): SafeResourceUrl {
    const url = 'https://www.google.com/maps?q=' + location.address + '&output=embed';
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

    combineLatest([
      this.calendarPeriodsService.getCalendarPeriodsOfLocation(this.locationName),
        this.authenticationService.getLocationReservations(),
        ])
      .subscribe(([periods, reservations]) => {
        this.originalList = [...reservations];
        this.selectedSubject.next(reservations);

        this.subscription = this.selectedSubject.asObservable().subscribe(proposedReservations =>
                  this.events = mapCalendarPeriodsToCalendarEvents(periods, [...proposedReservations]));
      });
  }

  updateReservationIsPossible(): boolean {
    return !this.isModified;
  }

  commitReservations(): void {
    this.isModified = false;

    // We need to find out which of the selected boxes need to be removed, and which need to be added.
    // Therefore, we calculate selected \ previous
    const newReservations = this.selectedSubject.value
                    .filter(selected => !includesTimeslot(this.originalList.map(l => l.timeslot), selected.timeslot));

    // And we calculate previous \ selected
    const removedReservations = this.originalList
                    .filter(selected => !includesTimeslot(this.selectedSubject.value.map(l => l.timeslot), selected.timeslot));


    combineLatest([
          this.locationReservationService.postLocationReservations(newReservations),
          this.locationReservationService.deleteLocationReservations(removedReservations)
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
  }
}
