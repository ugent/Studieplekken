import { Component, OnInit, Input, TemplateRef } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { CalendarEvent } from 'angular-calendar';
import * as moment from 'moment';
import { TimeslotsService } from 'src/app/services/api/calendar-periods/calendar-periods.service';
import { Observable, Subject, BehaviorSubject, timer } from 'rxjs';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import {
  ApplicationTypeFunctionalityService
} from 'src/app/services/functionality/application-type/application-type-functionality.service';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';
import { Timeslot, timeslotToCalendarEvent } from 'src/app/shared/model/Timeslot';
import { LocationOpeningperiodDialogComponent } from './location-openingperiod-dialog/location-openingperiod-dialog.component';
import { Location } from 'src/app/shared/model/Location';
import { BsModalService } from 'ngx-bootstrap/modal';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import { Moment } from 'moment';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { of } from 'rxjs/internal/observable/of';
import {
  ConversionToCalendarEventService
} from '../../../../services/styling/CalendarEvent/conversion-to-calendar-event.service';

@Component({
  selector: 'app-location-calendar',
  templateUrl: './location-calendar.component.html',
  styleUrls: ['./location-calendar.component.css']
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
   * 'events' is the object that is used by the angular-calendar module
   * to show the events in the calendar.
   */
  events: CalendarEvent<{
    timeslot?: Timeslot;
  }>[] = [];


  disableFootButtons = true;

  showReservations = false;

  errorOnRetrievingReservations = false;

  successAddingLocationReservation: boolean = undefined;
  successUpdatingLocationReservation: boolean = undefined;
  successDeletingLocationReservation: boolean = undefined;

  calendarPeriods = [];

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
    private route: ActivatedRoute,
    private conversionService: ConversionToCalendarEventService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.locationId = Number(this.route.snapshot.paramMap.get('locationId'));

    // Check if locationId is a Number before proceeding. If NaN, redirect to management locations.
    if (isNaN(this.locationId)) {
      this.router.navigate(['/management/locations']).catch(console.log);
      return;
    }

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


  timeslotPickedHandler(event: any): void {
    // event is a non-reservable calendar period.
    if (!event.timeslot) {
      this.showReservations = false;
      this.errorOnRetrievingReservations = false;
      return;
    }

    this.currentTimeSlot = event['timeslot'] as Timeslot;

    this.loadReservations();
  }

  loadReservations(): void {
    this.showReservations = null;
    timer(0, 60 * 1000)
      .pipe(
        switchMap(() =>
          this.locationReservationService.getLocationReservationsOfTimeslot(
            this.currentTimeSlot
          )
        )
      )
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


  closeModal(): void {
    this.modalService.hide();
  }
}
