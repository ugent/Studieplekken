import { trigger, transition, useAnimation } from '@angular/animations';
import { Component, OnInit, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CalendarEvent } from 'angular-calendar';
import * as moment from 'moment';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { CalendarPeriodsService } from 'src/app/services/api/calendar-periods/calendar-periods.service';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { ApplicationTypeFunctionalityService } from 'src/app/services/functionality/application-type/application-type-functionality.service';
import { rowsAnimation } from 'src/app/shared/animations/RowAnimation';
import { CalendarPeriod, mapCalendarPeriodsToCalendarEvents, isCalendarPeriodValid } from 'src/app/shared/model/CalendarPeriod';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';
import { Timeslot, timeslotStartHour } from 'src/app/shared/model/Timeslot';
import { UserConstructor } from 'src/app/shared/model/User';
import { LocationOpeningperiodDialogComponent } from './location-openingperiod-dialog/location-openingperiod-dialog.component';
import { Location } from 'src/app/shared/model/Location';
import { msToShowFeedback } from 'src/app/app.constants';
import { tap } from 'rxjs/operators';
import { AuthorizationGuardService } from 'src/app/services/guard/authentication/authorization-guard/authorization-guard.service';
import { UserService } from 'src/app/services/api/users/user.service';
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

  currentLocationReservationToDelete: LocationReservation = new LocationReservation(UserConstructor.new(), null)

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
              private locationReservationService: LocationReservationsService,
              private authorizationService: AuthenticationService,
              private dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.location.subscribe(next => {
      this.locationName = next.name;
      this.locationFlat = next;
      this.calendarPeriodModel.subscribe(console.log)
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



  checkForWarning(): void {
    let showWarning = false;
    this.calendarPeriods.forEach(element => {
      if (!element.reservable) {
        return;
      }
      const begin = new Date(element.startsAt + ' ' + element.openingTime);
      const end = new Date(element.startsAt + ' ' + element.closingTime);
      const diffMs = Math.round((end.getTime() - begin.getTime()) / 60000);
      if ((element.openingTime.diff(element.closingTime, 'minutes') % element.reservableTimeslotSize) !== 0) {
        showWarning = true;
      }
    });

    if (showWarning) {
      this.dialog.open(LocationOpeningperiodDialogComponent);
    }
  }

  deleteAllPeriodsInDataLayer(): void {
    this.calendarPeriodsService.deleteCalendarPeriods(this.calendarPeriodsInDataLayer)
      .subscribe(() => this.successHandler(), () => this.errorHandler());
  }

  addAllCalendarPeriods(): void {
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
      .forEach(value => this.calendarPeriods.push(CalendarPeriod.fromJSON(value)));

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
    this.locationReservationService.deleteLocationReservation(this.currentLocationReservationToDelete).subscribe(
      () => {
        this.deletionWasSuccess = true;
        this.loadReservations();
      }, () => {
        this.deletionWasSuccess = false;
        this.loadReservations();
      }
    );
  }

  prepareUpdate(calendarPeriod: CalendarPeriod): void {
    this.prepareToUpdatePeriod = calendarPeriod;
    // Copy
    this.calendarPeriodModel.next(CalendarPeriod.fromJSON(calendarPeriod));
  }

  prepareDelete(calendarPeriod: CalendarPeriod): void {
    this.prepareToUpdatePeriod = calendarPeriod;
  }

  prepareAdd(): void {
    this.calendarPeriodModel.next(new CalendarPeriod(null, this.locationFlat, null, null, null, null, false, null, 0, [], null));
    this.prepareToUpdatePeriod = null;
  }

  update(): void {
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
    ).subscribe(() => {
      this.successHandler();
    }, () => this.errorHandler());
  }

  delete(): void {
    this.calendarPeriods = this.calendarPeriods.filter(c => c.id !== this.prepareToUpdatePeriod.id);
    this.calendarPeriodsService.deleteCalendarPeriods([this.calendarPeriodsInDataLayer.find(c => c.id !== this.prepareToUpdatePeriod.id)])
    .subscribe(() => {
      this.successHandler();
    }, () => this.errorHandler());
  }

  onCheckboxToggle(reservation, checked): void {
    this.locationReservationService.postLocationReservationAttendance(reservation, checked)
                    .pipe(tap(() => this.loadReservations()))
                    .subscribe();
  }

  showCheckbox(): boolean {
    return this.currentCalendarPeriod && this.currentTimeSlot
           && timeslotStartHour(this.currentCalendarPeriod, this.currentTimeSlot).isBefore(moment());
  }

  getMinStartDate(): Moment {
    if (this.authorizationService.isAdmin()) {
      return null;
    } else {
      return moment().add(3, 'weeks').day(8);
    }
  }

  // If the admin is executing a change on own authority, show warning.
  showAdminWarnMessage(model): boolean {
    if (!this.authorizationService.isAdmin()) {
      return false;
    }

    if (model.startsAt && model.startsAt.isBefore(moment().add(3, 'weeks').day(8))) {
      return true;
    }

    if (this.prepareToUpdatePeriod && this.prepareToUpdatePeriod.startsAt.isBefore(moment().add(3, 'weeks').day(8))) {
      return true;
    }
    
    return false;
  }
}
