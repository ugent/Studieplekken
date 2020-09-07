import {Component, Input, OnInit} from '@angular/core';
import {CalendarEvent} from 'angular-calendar';
import {
  CalendarPeriodConstructor,
  CalendarPeriod,
  isCalendarPeriodValid,
  calendarPeriodToCalendarEvent
} from '../../../../shared/model/CalendarPeriod';
import {Observable, Subject} from 'rxjs';
import {Location} from '../../../../shared/model/Location';
import {CalendarPeriodsService} from '../../../../services/api/calendar-periods/calendar-periods.service';
import {equalCalendarPeriods} from '../../../../shared/comparators/ModelComparators';

@Component({
  selector: 'app-location-calendar',
  templateUrl: './location-calendar.component.html',
  styleUrls: ['./location-calendar.component.css']
})
export class LocationCalendarComponent implements OnInit {
  @Input() location: Observable<Location>;

  refresh: Subject<any> = new Subject();

  /*
   * 'events' is the object that is used by the frontend
   * to update/add periods.
   */
  events: CalendarEvent<CalendarPeriod>[] = [];

  /*
   * 'eventsInDataLayer' is an object that keeps track of the opening
   * periods, that are stored in the data layer. This object is used
   * to be able to get the updated/added periods, so that we can inform
   * the data layer which objects to update
   */
  calendarPeriodsInDataLayer: CalendarPeriod[] = [];

  disableFootButtons = true;

  /*
   * The boolean-attributes below are used to give feedback to the
   * user when he/she has pressed the "Update" button in different
   * scenarios.
   */
  msToShowFeedback = 10000; // 10 sec
  showWrongCalendarPeriodFormat = false;
  showSuccessButNoChanges = false;
  showSuccess = false;
  showError = false;

  constructor(private calendarPeriodsService: CalendarPeriodsService) { }

  ngOnInit(): void {
    this.location.subscribe(next => {
      this.setupEvents(next.name);
    });
  }

  setupEvents(locationName: string): void {
    // retrieve all calendar periods for this location
    this.calendarPeriodsService.getCalendarPeriodsOfLocation(locationName).subscribe(next => {
      if (next === null) {
        return;
      }

      // make a deep copy to make sure that can be calculated whether any period has changed
      this.calendarPeriodsInDataLayer = [];
      next.forEach(n => {
        this.calendarPeriodsInDataLayer.push(CalendarPeriodConstructor.newFromObj(n));
      });

      // fill the events based on the calendar periods
      this.events = this.mapCalendarPeriodsToCalendarEvents(next);
    });
  }

  mapCalendarPeriodsToCalendarEvents(periods: CalendarPeriod[]): CalendarEvent[] {
    return periods.map<CalendarEvent>(n => {
      return calendarPeriodToCalendarEvent(n);
    });
  }

  isPeriodInEvents(period: CalendarPeriod): boolean {
    return this.events.findIndex(next => equalCalendarPeriods(period, next.meta)) < 0;
  }

  isPeriodInBackend(event: CalendarEvent): boolean {
    return this.calendarPeriodsInDataLayer.findIndex(next => equalCalendarPeriods(next, event.meta)) < 0;
  }

  hasAnyPeriodChanged(): boolean {
    // if the lengths do not match, there must have changed something
    if (this.events.length !== this.calendarPeriodsInDataLayer.length) {
      return true;
    }

    // if the lengths do match, try if all values in this.eventsInDataLayer
    // have a matching value in this.events
    this.calendarPeriodsInDataLayer.forEach(n => {
      if (!this.isPeriodInEvents(n)) {
        return false;
      }
    });

    return true;
  }

  refreshCalendar(event: CalendarEvent): void {
    // find index corresponding to the given event
    const idx = this.events.findIndex(next => event === next);

    if (idx < 0) {
      return;
    }

    this.prepareCalendarEventBasedOnMeta(idx);
    this.refresh.next();

    // make sure that the user can update changes
    this.disableFootButtons = !this.isPeriodInBackend(event);
  }

  prepareCalendarEventBasedOnMeta(idx: number): void {
    const period = this.events[idx].meta;
    this.events[idx] = calendarPeriodToCalendarEvent(period);
  }

  addOpeningPeriodButtonClick(location: Location): void {
    this.addOpeningPeriod(location);
    this.disableFootButtons = false;
  }

  addOpeningPeriod(location: Location): void {
    const period: CalendarPeriod = CalendarPeriodConstructor.new();
    period.location = location;

    this.events = [
      ...this.events, calendarPeriodToCalendarEvent(period)
    ];
  }

  updateOpeningPeriodButtonClick(locationName: string): void {
    this.disableFootButtons = true;
    this.updateOpeningPeriod(locationName);
  }

  /**
   * This is the method that does all the CUD-work of
   * the CRUD operations available for CALENDAR_PERIODS
   */
  updateOpeningPeriod(locationName: string): void {
    if (this.hasAnyPeriodChanged()) {
      // if this.events.length === 0, delete everything instead of updating
      if (this.events.length === 0) {
        this.deleteAllPeriodsInDataLayer(locationName);
        return;
      }

      // before updating or adding anything, check if all periods are valid
      // Note: do not do this.events.forEach(handler), because the return in the 'handler'
      //   will return from the lambda, but not from the outer function and thus, a
      //   request will be sent to the backend, which is not wat we want if not all
      //   the periods are validly filled in
      for (const n of this.events) {
        if (!isCalendarPeriodValid(n.meta)) {
          this.handleWrongCalendarPeriodFormatOnUpdate();
          return;
        }
      }

      // if this.eventsInDataLayer.length === 0, add all events instead of updating
      if (this.calendarPeriodsInDataLayer.length === 0) {
        this.addAllPeriodsInEvents(locationName);
        return;
      }

      // this.events is not empty, and all values are valid: persist update(s)
      this.calendarPeriodsService.updateCalendarPeriods(
        locationName,
        this.calendarPeriodsInDataLayer,
        this.events.map<CalendarPeriod>(n => n.meta)
      ).subscribe(() => {
        this.successHandler(locationName);
      }, () => this.errorHandler());
    } else {
      this.handleNothingHasChangedOnUpdate();
    }
  }

  deleteAllPeriodsInDataLayer(locationName: string): void {
    this.calendarPeriodsService.deleteCalendarPeriods(this.calendarPeriodsInDataLayer)
      .subscribe(() => this.successHandler(locationName), () => this.errorHandler());
  }

  addAllPeriodsInEvents(locationName: string): void{
    this.calendarPeriodsService.addCalendarPeriods(this.events.map<CalendarPeriod>(n => n.meta))
      .subscribe(() => this.successHandler(locationName), () => this.errorHandler());
  }

  handleWrongCalendarPeriodFormatOnUpdate(): void {
    this.showWrongCalendarPeriodFormat = true;
    setTimeout(() => this.showWrongCalendarPeriodFormat = false, this.msToShowFeedback);
  }

  handleNothingHasChangedOnUpdate(): void {
    this.showSuccessButNoChanges = true;
    setTimeout(() => this.showSuccessButNoChanges = false, this.msToShowFeedback);
  }

  deleteOpeningPeriodButtonClick(event: CalendarEvent): void {
    this.deleteOpeningPeriod(event);
    this.disableFootButtons = false;
  }

  deleteOpeningPeriod(event: CalendarEvent): void {
    this.events = this.events.filter((next) => next !== event);
  }

  cancelChangesButtonClick(): void {
    this.events = this.mapCalendarPeriodsToCalendarEvents(this.calendarPeriodsInDataLayer);
    this.disableFootButtons = true;
  }

  successHandler(locationName: string): void {
    this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, this.msToShowFeedback);
    this.setupEvents(locationName);
  }

  errorHandler(): void {
    this.showError = true;
    setTimeout(() => this.showError = false, this.msToShowFeedback);
  }
}
