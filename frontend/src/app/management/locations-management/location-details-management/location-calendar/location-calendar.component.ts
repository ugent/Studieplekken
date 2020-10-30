import {Component, Input, OnInit} from '@angular/core';
import {CalendarEvent} from 'angular-calendar';
import {
  CalendarPeriodConstructor,
  CalendarPeriod,
  isCalendarPeriodValid, mapCalendarPeriodsToCalendarEvents
} from '../../../../shared/model/CalendarPeriod';
import {Observable, Subject} from 'rxjs';
import {Location} from '../../../../shared/model/Location';
import {CalendarPeriodsService} from '../../../../services/api/calendar-periods/calendar-periods.service';
import {ApplicationTypeFunctionalityService} from '../../../../services/functionality/application-type/application-type-functionality.service';
import {toDateTimeString, typeScriptDateToCustomDate} from '../../../../shared/model/helpers/CustomDate';
import {msToShowFeedback} from '../../../../../environments/environment';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { LocationReservation, LocationReservationConstructor } from 'src/app/shared/model/LocationReservation';
import { transition, trigger, useAnimation } from '@angular/animations';
import { rowsAnimation } from 'src/app/shared/animations/RowAnimation';
import { Timeslot } from 'src/app/shared/model/Timeslot';

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

  locationReservations: LocationReservation[];
  currentTimeSlot: Timeslot;

  currentLocationReservationToDelete: LocationReservation = LocationReservationConstructor.new();

  refresh: Subject<any> = new Subject();

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

  /**
   * The boolean-attributes below are used to give feedback to the
   * user when he/she has pressed the "Update" button in different
   * scenarios.
   */
  showWrongCalendarPeriodFormat = false;
  showSuccessButNoChanges = false;
  showSuccess = false;
  showError = false;
  showReservations = false;

  errorOnRetrievingReservations = false;
  deletionWasSuccess: boolean = undefined;

  /**
   * Depending on what the ApplicationTypeFunctionalityService returns
   * for the functionality of reservations, 'showReservationInformation'
   * will be set.
   */
  showReservationInformation: boolean;

  constructor(private calendarPeriodsService: CalendarPeriodsService,
              private functionalityService: ApplicationTypeFunctionalityService,
              private locationReservationService: LocationReservationsService) { }

  ngOnInit(): void {
    this.location.subscribe(next => {
      this.locationName = next.name;
      this.setupEvents();
    });
    this.showReservationInformation = this.functionalityService.showReservationsFunctionality();
  }

  setupEvents(): void {
    // retrieve all calendar periods for this location
    this.calendarPeriodsService.getCalendarPeriodsOfLocation(this.locationName).subscribe(next => {
      if (next === null) {
        return;
      }

      this.calendarPeriods = next;

      // make a deep copy to make sure that can be calculated whether any period has changed
      this.calendarPeriodsInDataLayer = [];
      next.forEach(n => {
        this.calendarPeriodsInDataLayer.push(CalendarPeriodConstructor.newFromObj(n));
      });

      // fill the events based on the calendar periods
      this.events = mapCalendarPeriodsToCalendarEvents(next);
    });
  }

  hasAnyPeriodChanged(): boolean {
    // if the lengths do not match, there must have changed something
    if (this.calendarPeriods.length !== this.calendarPeriodsInDataLayer.length) {
      return true;
    }

    // if the lengths do match, check if all values in this.calendarPeriodsInDataLayer
    // have a matching value in this.calendarPeriods
    for (const period of this.calendarPeriodsInDataLayer) {
      if (!this.calendarPeriods.includes(period)) {
        return true;
      }
    }

    return false;
  }

  refreshCalendar(period: CalendarPeriod): void {
    // only refresh the calendar if the period that has changed is valid
    if (!isCalendarPeriodValid(period)) {
      return;
    }

    // set the events
    this.events = mapCalendarPeriodsToCalendarEvents(this.calendarPeriods);

    // refresh the calendar
    this.refresh.next();

    // make sure that the user can update changes
    this.disableFootButtons = !this.hasAnyPeriodChanged();
  }

  addOpeningPeriodButtonClick(location: Location): void {
    this.addOpeningPeriod(location);
    this.disableFootButtons = false;
    console.log(this.disableFootButtons)

  }

  addOpeningPeriod(location: Location): void {
    const period: CalendarPeriod = CalendarPeriodConstructor.new();
    period.location = location;

    // If the information about reservations may not be shown (configured in environments.ts),
    // then we need to programmatically provide a valid value for 'reservableFrom' because
    // the user will not be able to set the value manually.
    // If not set, the period will not be addable. Therefore, we just provide the current date-time.
    if (true || !this.showReservationInformation) {
      let dateTime = toDateTimeString(typeScriptDateToCustomDate(new Date()));
      // remove the trailing ':ss' and replace 'T' with ' ' to make a valid
      // dateTimeStr for the database: 'YYYY-MM-DD HH:MI'
      dateTime = dateTime.substr(0, dateTime.length - 3).replace('T', ' ');
      period.reservableFrom = dateTime;
    }

    this.calendarPeriods.push(period);
  }

  updateOpeningPeriodButtonClick(): void {
    this.disableFootButtons = true;
    this.updateOpeningPeriod();
  }

  /**
   * This is the method that does all the CUD-work of
   * the CRUD operations available for CALENDAR_PERIODS
   */
  updateOpeningPeriod(): void {
    if (this.hasAnyPeriodChanged()) {
      // if this.events.length === 0, delete everything instead of updating
      if (this.calendarPeriods.length === 0) {
        this.deleteAllPeriodsInDataLayer();
        return;
      }

      // before updating or adding anything, check if all periods are valid
      // Note: do not do this.calendarPeriods.forEach(handler), because the return
      //   will return from the lambda, but not from the outer function and thus, a
      //   in the 'handler' request will be sent to the backend, which is not wat we
      //   want if not all the periods are validly filled in
      for (const n of this.calendarPeriods) {
        n.reservableFrom = n.reservableFrom && n.reservableFrom.toString() + ' 00:00';
        console.log(n.reservableFrom)
        if (!isCalendarPeriodValid(n)) {
          this.handleWrongCalendarPeriodFormatOnUpdate();
          return;
        }
      }

      // if this.eventsInDataLayer.length === 0, add all events instead of updating
      if (this.calendarPeriodsInDataLayer.length === 0) {
        this.addAllCalendarPeriods();
        return;
      }

      // this.calendarPeriods is not empty, and all values are valid: persist update(s)
      this.calendarPeriodsService.updateCalendarPeriods(
        this.locationName,
        this.calendarPeriodsInDataLayer,
        this.calendarPeriods
      ).subscribe(() => {
        this.successHandler();
      }, () => this.errorHandler());
    } else {
      this.handleNothingHasChangedOnUpdate();
    }
  }

  deleteAllPeriodsInDataLayer(): void {
    this.calendarPeriodsService.deleteCalendarPeriods(this.calendarPeriodsInDataLayer)
      .subscribe(() => this.successHandler(), () => this.errorHandler());
  }

  addAllCalendarPeriods(): void{
    this.calendarPeriodsService.addCalendarPeriods(this.calendarPeriods)
      .subscribe(() => this.successHandler(), () => this.errorHandler());
  }

  handleWrongCalendarPeriodFormatOnUpdate(): void {
    this.showWrongCalendarPeriodFormat = true;
    setTimeout(() => this.showWrongCalendarPeriodFormat = false, msToShowFeedback);
  }

  handleNothingHasChangedOnUpdate(): void {
    this.showSuccessButNoChanges = true;
    setTimeout(() => this.showSuccessButNoChanges = false, msToShowFeedback);
  }

  deleteOpeningPeriodButtonClick(period: CalendarPeriod): void {
    this.deleteOpeningPeriod(period);
    this.disableFootButtons = false;
  }

  deleteOpeningPeriod(period: CalendarPeriod): void {
    this.calendarPeriods = this.calendarPeriods.filter((next) => next !== period);
    this.events = mapCalendarPeriodsToCalendarEvents(this.calendarPeriods);
  }

  cancelChangesButtonClick(): void {
    this.events = mapCalendarPeriodsToCalendarEvents(this.calendarPeriodsInDataLayer);

    // deep copy of this.calendarPeriodsInDataLayer to this.calendarPeriods
    this.calendarPeriods = [];
    this.calendarPeriodsInDataLayer
      .forEach(value => this.calendarPeriods.push(CalendarPeriodConstructor.newFromObj(value)));

    this.disableFootButtons = true;
  }

  successHandler(): void {
    this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, msToShowFeedback);
    this.setupEvents();
  }

  errorHandler(): void {
    this.showError = true;
    setTimeout(() => this.showError = false, msToShowFeedback);
  }

  timeslotPickedHandler(event: any): void{
    // event is a non-reservable calendar period.
    if (!event.hasOwnProperty('timeslotSeqnr')) {
      this.showReservations = false;
      this.errorOnRetrievingReservations = false;
      return;
    }

    this.currentTimeSlot = event;
    this.loadReservations();
  }

  private loadReservations(): void {
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

  prepareToDeleteLocationReservation(locationReservation: LocationReservation): void {
    this.deletionWasSuccess = undefined;
    this.currentLocationReservationToDelete = locationReservation;
  }

  deleteLocationReservation(): void {
    console.log('Removing reservation');
    this.locationReservationService.deleteLocationReservation(this.currentLocationReservationToDelete).subscribe(
      () => {
        this.deletionWasSuccess = true;
        this.loadReservations();
      }, () => {
        this.deletionWasSuccess = false;
        this.loadReservations();
      }
    )
  }
}
