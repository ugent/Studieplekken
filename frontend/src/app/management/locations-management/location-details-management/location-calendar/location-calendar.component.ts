import { trigger, transition, useAnimation } from '@angular/animations';
import { Component, OnInit, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CalendarEvent } from 'angular-calendar';
import * as moment from 'moment';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { CalendarPeriodsService } from 'src/app/services/api/calendar-periods/calendar-periods.service';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
// tslint:disable-next-line:max-line-length
import { ApplicationTypeFunctionalityService } from 'src/app/services/functionality/application-type/application-type-functionality.service';
import { rowsAnimation } from 'src/app/shared/animations/RowAnimation';
import { CalendarPeriod, mapCalendarPeriodsToCalendarEvents } from 'src/app/shared/model/CalendarPeriod';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';
import { Timeslot } from 'src/app/shared/model/Timeslot';
import { LocationOpeningperiodDialogComponent } from './location-openingperiod-dialog/location-openingperiod-dialog.component';
import { Location } from 'src/app/shared/model/Location';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import { Moment } from 'moment';

@Component({
  selector: 'app-location-calendar',
  templateUrl: './location-calendar.component.html',
  styleUrls: ['./location-calendar.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class LocationCalendarComponent implements OnInit {
  @Input() location: Observable<Location>;

  locationName: string;
  locationFlat: Location;

  locationReservations: LocationReservation[];
  currentTimeSlot: Timeslot;

  refresh: Subject<any> = new Subject();

  prepareToUpdatePeriod: CalendarPeriod = null;
  currentCalendarPeriod: CalendarPeriod = null;

  calendarPeriodModel: BehaviorSubject<CalendarPeriod> =
                                new BehaviorSubject(new CalendarPeriod(null, null, null, null, null, null, false, null, 0, [], null));

  /**
   * 'calendarPeriods' is the list of CalendarPeriods that the user
   * can modify using the form in the template
   */
  calendarPeriods: CalendarPeriod[] = [];

  /**
   * 'events' is the object that is used by the angular-calendar module
   * to show the events in the calendar.
   */
  events: CalendarEvent<CalendarPeriod>[] = [];

  /**
   * 'eventsInDataLayer' is an object that keeps track of the opening
   * periods, that are stored in the data layer. This object is used
   * to be able to determine whether or not 'periods' has changed
   */
  calendarPeriodsInDataLayer: CalendarPeriod[] = [];

  disableFootButtons = true;

  showReservations = false;

  errorOnRetrievingReservations = false;

  successAddingLocationReservation: boolean = undefined;
  successUpdatingLocationReservation: boolean = undefined;
  successDeletingLocationReservation: boolean = undefined;

  /**
   * Depending on what the ApplicationTypeFunctionalityService returns
   * for the functionality of reservations, 'showReservationInformation'
   * will be set.
   */
  showReservationInformation: boolean;

  constructor(private calendarPeriodsService: CalendarPeriodsService,
              private functionalityService: ApplicationTypeFunctionalityService,
              private locationReservationService: LocationReservationsService,
              private authorizationService: AuthenticationService,
              private dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.location.subscribe(next => {
      this.locationName = next.name;
      this.locationFlat = next;
      this.calendarPeriodModel.subscribe(console.log);
      this.setup();
    });
    this.showReservationInformation = this.functionalityService.showReservationsFunctionality();
  }

  // /**********
  // *   ADD   *
  // ***********/

  prepareAdd(): void {
    this.calendarPeriodModel.next(new CalendarPeriod(null, this.locationFlat, null, null, null, null, false, null, 0, [], null));
    this.prepareToUpdatePeriod = null;
    this.successAddingLocationReservation = undefined;
  }

  add(): void {
    this.update(true);
  }

  // /*************
  // *   UPDATE   *
  // **************/

  prepareUpdate(calendarPeriod: CalendarPeriod): void {
    this.successUpdatingLocationReservation = undefined;
    this.prepareToUpdatePeriod = calendarPeriod;
    // Copy
    this.calendarPeriodModel.next(CalendarPeriod.fromJSON(calendarPeriod));
  }

  update(add = false): void {
    this.successAddingLocationReservation = null;
    this.successUpdatingLocationReservation = null;

    this.calendarPeriods = this.calendarPeriods.filter(c => !this.prepareToUpdatePeriod || c.id !== this.prepareToUpdatePeriod.id);
    if (this.calendarPeriodModel) {
      this.calendarPeriods = [...this.calendarPeriods, this.calendarPeriodModel.value];
    }

    // Check if the closing time - opening time is divisible by timeslot_size.
    this.checkForWarning();

    // this.calendarPeriods is not empty, and all values are valid: persist update(s)
    this.calendarPeriodsService.updateCalendarPeriod(
      this.locationName,
      this.calendarPeriodsInDataLayer,
      this.calendarPeriodModel.value
    ).subscribe(
      () => {
        add ? this.successAddingLocationReservation = true : this.successUpdatingLocationReservation = true;
        this.setup();
      }, () => {
        add ? this.successAddingLocationReservation = false : this.successUpdatingLocationReservation = false;
        this.rollback();
      }
    );
  }

  // /*************
  // *   DELETE   *
  // **************/

  prepareDelete(calendarPeriod: CalendarPeriod): void {
    this.prepareToUpdatePeriod = calendarPeriod;
    this.successDeletingLocationReservation = undefined;
  }

  delete(): void {
    this.successDeletingLocationReservation = null;
    this.calendarPeriods = this.calendarPeriods.filter(c => c.id !== this.prepareToUpdatePeriod.id);
    this.calendarPeriodsService.deleteCalendarPeriods([this.calendarPeriodsInDataLayer.find(c => c.id !== this.prepareToUpdatePeriod.id)])
    .subscribe(
      () => {
        this.successDeletingLocationReservation = true;
        this.setup();
      }, () => {
        this.successDeletingLocationReservation = false;
        this.rollback();
      }
    );
  }

  // /******************
  // *   AUXILIARIES   *
  // *******************/

  setup(): void {
    // retrieve all calendar periods for this location
    this.calendarPeriodsService.getCalendarPeriodsOfLocation(this.locationName).subscribe(next => {
      if (next === null) {
        return;
      }

      next.forEach(n => n.openingTime = moment(n.openingTime, 'HH:mm:ss'));
      next.forEach(n => n.closingTime = moment(n.closingTime, 'HH:mm:ss'));
      this.calendarPeriods = next;

      // make a deep copy to make sure that can be calculated whether any period has changed
      this.calendarPeriodsInDataLayer = [];
      next.forEach(n => {
        this.calendarPeriodsInDataLayer.push(CalendarPeriod.fromJSON(n));
      });

      // fill the events based on the calendar periods
      this.events = mapCalendarPeriodsToCalendarEvents(next);

      // and update the calendar
      this.refresh.next(null);
    });
  }

  rollback(): void {
    this.calendarPeriods = [];
    this.calendarPeriodsInDataLayer.forEach(
      next => {
        this.calendarPeriods.push(CalendarPeriod.fromJSON(next));
      }
    );
  }

  checkForWarning(): void {
    let showWarning = false;

    this.calendarPeriods.forEach(element => {
      // if the element is not reservable, no timeslot size is assigned to the
      // calendar period and thus the divisibility does not need to be checked
      if (!element.reservable) {
        return;
      }

      // if the difference between closing time and opening time in minutes is
      // not divisible by the timeslot size (in minutes), then show the warning
      if ((element.openingTime.diff(element.closingTime, 'minutes') % element.reservableTimeslotSize) !== 0) {
        showWarning = true;
      }
    });

    // if necessary, show the warning
    if (showWarning) {
      this.dialog.open(LocationOpeningperiodDialogComponent);
    }
  }

  timeslotPickedHandler(event: any): void {
    // event is a non-reservable calendar period.
    if (!event.hasOwnProperty('timeslot')) {
      this.showReservations = false;
      this.errorOnRetrievingReservations = false;
      return;
    }

    this.currentTimeSlot = event.timeslot;
    this.currentCalendarPeriod = event.calendarPeriod;

    this.loadReservations();
  }

  loadReservations(): void {
    this.showReservations = null;
    this.locationReservationService.getLocationReservationsOfTimeslot(this.currentTimeSlot).subscribe((next) => {
      this.locationReservations = next;
      this.showReservations = true;
      this.errorOnRetrievingReservations = false;
    }, () => {
      this.showReservations = false;
      this.errorOnRetrievingReservations = true;
    });
  }

  getMinStartDate(): Moment {
    if (!this.authorizationService.isAdmin()) {
      return null;
    } else {
      return moment().add(3, 'weeks').day(8);
    }
  }
}
