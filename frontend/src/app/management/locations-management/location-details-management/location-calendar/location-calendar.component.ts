import { Component, OnInit, TemplateRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { CalendarEvent } from 'angular-calendar';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Observable, Subject, timer } from 'rxjs';
import { of } from 'rxjs/internal/observable/of';
import { catchError, first, switchMap, tap } from 'rxjs/operators';
import { TimeslotsService } from 'src/app/services/api/calendar-periods/calendar-periods.service';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { LocationService } from 'src/app/services/api/locations/location.service';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import {
  ApplicationTypeFunctionalityService
} from 'src/app/services/functionality/application-type/application-type-functionality.service';
import { Location } from 'src/app/shared/model/Location';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';
import { Timeslot, timeslotToCalendarEvent } from 'src/app/shared/model/Timeslot';
import { DefaultMap } from "src/app/shared/default-map/defaultMap"
import { Moment } from 'moment';
import * as moment from 'moment';

type TypeOption = {
  date: string;
  openingHour: string;
  closingHour: string;
  reservable: boolean;
  reservableFrom: string;
  seatCount: number;
  timeslots: Timeslot[];
};

@Component({
  selector: 'app-location-calendar',
  templateUrl: './location-calendar.component.html',
  styleUrls: ['./location-calendar.component.css']
})
export class LocationCalendarComponent implements OnInit {
  locationId: number; // will be set based on the url

  timeslotObs: Observable<Timeslot[]>;
  errorSubject = new Subject<boolean>();

  locationReservations: LocationReservation[];
  currentTimeSlot: Timeslot;
  toUpdateTimeslot: Timeslot;

  refresh: Subject<unknown> = new Subject<unknown>();

  jumpToDate: Moment = null;


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

  /**
   * Depending on what the ApplicationTypeFunctionalityService returns
   * for the functionality of reservations, 'showReservationInformation'
   * will be set.
   */
  showReservationInformation: boolean;

  isAdmin: boolean = this.authenticationService.isAdmin();

  currentLang: string;

  constructor(
    private timeslotService: TimeslotsService,
    private functionalityService: ApplicationTypeFunctionalityService,
    private locationReservationService: LocationReservationsService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private translate: TranslateService,
    private route: ActivatedRoute,
    private router: Router,
    private locationService: LocationService
  ) { }

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
    this.timeslotObs = this.timeslotService
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
          return of<Timeslot[]>([]);
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

    if (!event.timeslot.reservable) {
      this.showReservations = false;
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


  closeModal(): void {
    this.modalService.hide();
  }

  open(modal: TemplateRef<any>) {
    this.modalService.show(modal)
  }

  getLocation(): Observable<Location> {
    return this.locationService.getLocation(this.locationId)
  }

  newTimeslot(timeslot: Timeslot) {
    this.timeslotService.addTimeslot(timeslot).subscribe(() => this.setup())
    this.modalService.hide();
  }


  prepareUpdate(timeslot: Timeslot, modal: TemplateRef<any>) {
    this.toUpdateTimeslot = timeslot;
    this.open(modal);
  }

  prepareAdd(modal: TemplateRef<any>) {
    this.toUpdateTimeslot = null;
    this.open(modal);
  }

  updateTimeslot(timeslot: Timeslot) {
    this.timeslotService.updateTimeslot(timeslot).subscribe(() => this.setup())
    this.modalService.hide();
  }

  prepareDelete(timeslot: Timeslot, modal: TemplateRef<any>) {
    this.toUpdateTimeslot = timeslot;
    this.open(modal);
  }

  prepareCopy(timeslot: Timeslot, modal: TemplateRef<any>) {
    this.toUpdateTimeslot = timeslot;
    this.open(modal);
  }

  delete(timeslot: Timeslot) {
    this.timeslotService.deleteTimeslot(timeslot).subscribe(() => this.setup())
    this.modalService.hide();
  }

  copy(timeslot: Timeslot, weekOffset: number) {
    const date = moment(timeslot.timeslotDate).add(weekOffset+1, "weeks");
    const reservationDiff = date.diff(timeslot.timeslotDate, "minutes");

    this.timeslotService.addTimeslot(new Timeslot(null, date, null, null, timeslot.reservable, timeslot.reservableFrom?.subtract(reservationDiff), timeslot.locationId, timeslot.openingHour, timeslot.closingHour, timeslot.timeslotGroup))
    .subscribe(() => this.setup())
    this.modalService.hide();
  }

  groupTimeslots(timeslot: Timeslot[]) {
    if (!timeslot)
      return []

    const perGroup = new DefaultMap<number, Timeslot>();
    timeslot.forEach(t => perGroup.addValueAsList(t.timeslotGroup, t))


    return [...perGroup.values()].map(t => this.getGroupDetails(t)).reverse();
  }

  /**
   * This function gets the details for the table.
   * Usually, a timeslot group will be on one day, in one time category.
   * However, with migrated timeslots, this might not be the case. We'll group all these together.
   * @param timeslot 
   */
  private getGroupDetails(timeslot: Timeslot[]): TypeOption {
    const days = timeslot.map(t => t.timeslotDate.day())
    const allOnSameWeekDay = days.every((a) => a == days[0])
    if (!allOnSameWeekDay) {
      // This shouldn't happen.
      // Only happens with migrated timeslots.
      return {
        "date": "Varies",
        "openingHour": "Varies",
        "closingHour": "Varies",
        "reservable": true,
        "reservableFrom": "Varies",
        "seatCount": timeslot[0].seatCount,
        "timeslots":timeslot
      }
    }

    const allOnSameDay = timeslot.every(t => t.timeslotDate.isSame(timeslot[0].timeslotDate, "day"))

    return {
      "date": allOnSameDay ? timeslot[0].timeslotDate.format("DD/MM/YYYY") : timeslot[0].timeslotDate.format("dddd"),
      "openingHour": timeslot[0].openingHour.format("HH:mm"),
      "closingHour": timeslot[0].closingHour.format("HH:mm"),
      "reservable": timeslot[0].reservable,
      "reservableFrom": allOnSameDay ? timeslot[0].reservableFrom.format("DD/MM/YYYY HH:mm") : timeslot[0].reservableFrom.format("dddd HH:mm"),
      "seatCount": timeslot[0].seatCount,
      "timeslots": timeslot
    }

  }

  jumpTo(type: TypeOption) {
    const lastTimeslot = type.timeslots.reduce((a, b) => a.timeslotDate.isAfter(b.timeslotDate) ? a:b)
    this.jumpToDate = lastTimeslot.timeslotDate;
    this.currentTimeSlot = lastTimeslot;
  }

  hourPickedHandler(date: Moment, modal: TemplateRef<any>) {
    this.toUpdateTimeslot = new Timeslot(null, date, null, null, null, null, this.locationId, date, null, null);
    this.modalService.show(modal)
  }

  generateWeeksOptions(amountOfWeeks: number, startDate: Moment) {
    const options = []
    let today = moment(startDate);
    for(let i = 0; i < amountOfWeeks; i++) {
      today = today.add(7, "days");

      const firstDayOfWeek = moment(today).startOf("isoWeek")
      const lastDayOfWeek = moment(today).endOf("isoWeek")

      options.push({id: i, date: moment(today), range: `${firstDayOfWeek.format("DD/MM/YYYY")} - ${lastDayOfWeek.format("DD/MM/YYYY")}` })
    }
    return options;
  }

  trackWeekOption(w: {id: number}) {
    return w.id;
  }

}
