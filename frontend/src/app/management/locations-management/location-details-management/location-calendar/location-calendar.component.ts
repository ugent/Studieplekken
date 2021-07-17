import { Component, OnInit, Input, TemplateRef } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CalendarEvent } from 'angular-calendar';
import * as moment from 'moment';
import { Observable, Subject, BehaviorSubject } from 'rxjs';
import { TimeslotsService } from 'src/app/services/api/calendar-periods/calendar-periods.service';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { ApplicationTypeFunctionalityService } from 'src/app/services/functionality/application-type/application-type-functionality.service';
import {
  CalendarPeriod,
} from 'src/app/shared/model/CalendarPeriod';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';
import { Timeslot, timeslotToCalendarEvent } from 'src/app/shared/model/Timeslot';
import { LocationOpeningperiodDialogComponent } from './location-openingperiod-dialog/location-openingperiod-dialog.component';
import { Location } from 'src/app/shared/model/Location';
import { BsModalService } from 'ngx-bootstrap/modal';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import { Moment } from 'moment';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRoute } from '@angular/router';
import { catchError, tap } from 'rxjs/operators';
import { of } from 'rxjs/internal/observable/of';

@Component({
  selector: 'app-location-calendar',
  templateUrl: './location-calendar.component.html',
  styleUrls: ['./location-calendar.component.css'],
})
export class LocationCalendarComponent implements OnInit {
  @Input() location: Location; // only use this for creating a CalendarPeriod
  locationId: number; // will be set based on the url

  timeslotObs: Observable<Timeslot[]>;
  errorSubject = new Subject<boolean>();

  locationReservations: LocationReservation[];
  currentTimeSlot: Timeslot;

  refresh: Subject<unknown> = new Subject<unknown>();

  /**
   * 'calendarPeriods' is the list of CalendarPeriods that the user
   * can modify using the form in the template
   */
  calendarPeriods: CalendarPeriod[] = [];

  /**
   * 'events' is the object that is used by the angular-calendar module
   * to show the events in the calendar.
   */
  events: CalendarEvent<{
    timeslot?: Timeslot;
  }>[] = [];

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

  isAdmin: boolean = this.authenticationService.isAdmin();

  currentLang: string;

  constructor(
    private calendarPeriodsService: TimeslotsService,
    private functionalityService: ApplicationTypeFunctionalityService,
    private locationReservationService: LocationReservationsService,
    private dialog: MatDialog,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private translate: TranslateService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.locationId = Number(this.route.snapshot.paramMap.get('locationId'));

    this.currentLang = this.translate.currentLang;
    this.translate.onLangChange.subscribe(() => {
      this.currentLang = this.translate.currentLang;
      this.setup();
    });

    this.setup();

    this.showReservationInformation = this.functionalityService.showReservationsFunctionality();
  }


  // /******************
  // *   AUXILIARIES   *
  // *******************/

  setup(): void {
    // retrieve all calendar periods for this location
    this.timeslotObs = this.calendarPeriodsService
      .getTimeslotsOfLocation(this.locationId)
      .pipe(
        tap((next) => {
          // fill the events based on the calendar periods
          this.events = next.map(t => timeslotToCalendarEvent(t, this.translate.currentLang))

          // and update the calendar
          this.refresh.next(null);
        }),
        catchError((err) => {
          console.error(err);
          this.errorSubject.next(true);
          return of<Timeslot[]>(null);
        })
      );
  }

  rollback(): void {
    this.calendarPeriods = [];
    this.calendarPeriodsInDataLayer.forEach((next) => {
      this.calendarPeriods.push(CalendarPeriod.fromJSON(next));
    });
  }

  checkForWarning(calendarPeriod: CalendarPeriod): void {
    let showWarning = false;

    const element = calendarPeriod;
    if (!element.reservable) {
      return;
    }

    // if the difference between closing time and opening time in minutes is
    // not divisible by the timeslot size (in minutes), then show the warning
    if (
      element.openingTime.diff(element.closingTime, 'minutes') %
        element.timeslotLength !==
      0
    ) {
      showWarning = true;
    }

    // if necessary, show the warning
    if (showWarning) {
      this.dialog.open(LocationOpeningperiodDialogComponent);
    }
  }

  timeslotPickedHandler(event: Event): void {
    // event is a non-reservable calendar period.
    if (!event['timeslot']) {
      this.showReservations = false;
      this.errorOnRetrievingReservations = false;
      return;
    }

    this.currentTimeSlot = event['timeslot'] as Timeslot;

    this.loadReservations();
  }

  loadReservations(): void {
    this.showReservations = null;
    this.locationReservationService
      .getLocationReservationsOfTimeslot(this.currentTimeSlot)
      .subscribe(
        (next) => {
          this.locationReservations = next;
          this.showReservations = true;
          this.errorOnRetrievingReservations = false;
        },
        () => {
          this.showReservations = false;
          this.errorOnRetrievingReservations = true;
        }
      );
  }

  getMinStartDate(): Moment {
    if (this.authenticationService.isAdmin()) {
      return null;
    } else {
      return moment().add(3, 'weeks').day(8);
    }
  }

  getMinReservableFrom(model: { startsAt: moment.MomentInput }): Moment {
    if (!model.startsAt) {
      return null;
    } else {
      return moment(model.startsAt).subtract(3, 'weeks').day(2);
    }
  }

  // If the admin is executing a change on own authority, show warning.
  showAdminWarnMessage(model: CalendarPeriod): boolean {
    if (!this.authenticationService.isAdmin()) {
      return false;
    }

    if (
      model.startsAt &&
      model.startsAt.isBefore(moment().add(3, 'weeks').day(8))
    ) {
      return true;
    }

    return (
     true
    );
  }

  closeModal(): void {
    this.modalService.hide();
  }

  public getCalendarPeriodTimeInMinutes(
    calendarPeriod: CalendarPeriod
  ): number {
    if (!calendarPeriod.closingTime) return null;

    return calendarPeriod.openingTime?.diff(
      calendarPeriod.closingTime,
      'minutes'
    );
  }
}
