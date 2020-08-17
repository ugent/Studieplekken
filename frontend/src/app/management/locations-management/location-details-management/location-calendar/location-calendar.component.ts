import {Component, Input, OnInit} from '@angular/core';
import {CalendarEvent} from 'angular-calendar';
import {CalendarPeriodConstructor, CalendarPeriod, isCalendarPeriodValid} from '../../../../shared/model/CalendarDay';
import {Observable, Subject} from 'rxjs';
import {Location} from '../../../../shared/model/Location';
import {CalendarPeriodsService} from '../../../../services/calendar-periods/calendar-periods.service';
import {equalCalendarPeriods} from '../../../../shared/comparators/ModelComparators';

@Component({
  selector: 'app-location-calendar',
  templateUrl: './location-calendar.component.html',
  styleUrls: ['./location-calendar.component.css']
})
export class LocationCalendarComponent implements OnInit {
  @Input() location: Observable<Location>;
  locationObj: Location;

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
  eventsInDataLayer: CalendarPeriod[] = [];

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
      this.locationObj = next;
      this.setupEvents(next.name);
    });
  }

  setupEvents(locationName: string): void {
    // retrieve all calendar periods for this location
    this.calendarPeriodsService.getCalendarPeriodsOfLocation(locationName).subscribe(next => {
      // make a deep copy to make sure that can be calculated whether any period has changed
      this.eventsInDataLayer = [];
      next.forEach(n => {
        this.eventsInDataLayer.push(CalendarPeriodConstructor.newFromObj(n));
      });

      // fill the events based on the calendar periods
      this.events = this.mapCalendarPeriodsToCalendarEvents(next);
    });
  }

  mapCalendarPeriodsToCalendarEvents(periods: CalendarPeriod[]): CalendarEvent[] {
    const events: CalendarEvent[] = [];

    periods.forEach(period => {
      events.push({
        title: period.openingTime + ' - ' + period.closingTime,
        start: new Date(period.startsAt),
        end: new Date(period.endsAt),
        meta: period
      });
    });

    return events;
  }

  isPeriodInEvents(period: CalendarPeriod): boolean {
    return this.events.findIndex(next => equalCalendarPeriods(period, next.meta)) < 0;
  }

  isPeriodInBackend(event: CalendarEvent): boolean {
    return this.eventsInDataLayer.findIndex(next => equalCalendarPeriods(next, event.meta)) < 0;
  }

  hasAnyPeriodChanged(): boolean {
    // if the lengths do not match, there must have changed something
    if (this.events.length !== this.eventsInDataLayer.length) {
      return true;
    }

    // if the lengths do match, try if all values in this.eventsInDataLayer
    // have a matching value in this.events
    this.eventsInDataLayer.forEach(n => {
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

    this.events[idx] = {
      title: period.openingTime + ' - ' + period.closingTime,
      start: new Date(period.startsAt),
      end: new Date(period.endsAt),
      meta: period
    };
  }

  addOpeningPeriodButtonClick(): void {
    this.addOpeningPeriod();
    this.disableFootButtons = true;
  }

  addOpeningPeriod(): void {
    const period: CalendarPeriod = CalendarPeriodConstructor.new();
    period.location = this.locationObj;

    this.events = [
      ...this.events,
      {
        title: period.openingTime + ' - ' + period.closingTime,
        start: new Date(period.startsAt),
        end: new Date(period.endsAt),
        meta: period
      }
    ];
  }

  updateOpeningPeriodButtonClick(): void {
    this.updateOpeningPeriod();
    this.disableFootButtons = true;
  }

  /**
   * This is the method that does all the CUD-work of
   * the CRUD operations available for CALENDAR_PERIODS
   */
  updateOpeningPeriod(): void {
    this.disableFootButtons = true;

    if (this.hasAnyPeriodChanged()) {
      // if this.events.length === 0, delete everything instead of updating
      if (this.events.length === 0) {
        this.calendarPeriodsService.deleteCalendarPeriods(this.eventsInDataLayer).subscribe(() => {
          this.showSuccess = true;
          setTimeout(() => this.showSuccess = false, this.msToShowFeedback);
        }, () => {
          this.showError = true;
          setTimeout(() => this.showError = false, this.msToShowFeedback);
        });
        return;
      }

      // if this.eventsInDataLayer.length === 0, add all events instead of updating
      if (this.eventsInDataLayer.length === 0) {
        this.calendarPeriodsService.addCalendarPeriods(this.events.map<CalendarPeriod>(n => n.meta)).subscribe( () => {
          this.showSuccess = true;
          setTimeout(() => this.showSuccess = false, this.msToShowFeedback);
        }, () => {
          this.showError = true;
          setTimeout(() => this.showError = false, this.msToShowFeedback);
        });
        return;
      }

      // check if all periods are valid
      this.events.forEach(n => {
        if (!isCalendarPeriodValid(n.meta)) {
          this.handleWrongCalendarPeriodFormatOnUpdate();
        }
      });

      // this.events is not empty, and all values are valid: persist update(s)
      this.calendarPeriodsService.updateCalendarPeriods(this.eventsInDataLayer,
        this.events.map<CalendarPeriod>(n => n.meta)).subscribe(() => {
          this.showSuccess = true;
          setTimeout(() => this.showSuccess = false, this.msToShowFeedback);
      }, (() => {
        this.showError = true;
        setTimeout(() => this.showError = false, this.msToShowFeedback);
      }));
    } else {
      this.handleNothingHasChangedOnUpdate();
    }
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
    this.events = this.mapCalendarPeriodsToCalendarEvents(this.eventsInDataLayer);
    this.disableFootButtons = true;
  }
}
