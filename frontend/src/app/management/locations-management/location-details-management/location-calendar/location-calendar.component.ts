import { Component, OnInit, TemplateRef } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { CalendarEvent, CalendarView } from 'angular-calendar';
import * as moment from 'moment';
import { Moment } from 'moment';
import { combineLatest, Observable, Subject, timer } from 'rxjs';
import { of } from 'rxjs/internal/observable/of';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { TimeslotsService } from 'src/app/services/api/calendar-periods/timeslot.service';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { LocationService } from 'src/app/services/api/locations/location.service';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import {
  ApplicationTypeFunctionalityService
} from 'src/app/services/functionality/application-type/application-type-functionality.service';
import { TimeslotGroupService } from "src/app/services/timeslots/timeslot-group/timeslot-group.service";
import { TimeslotCalendarEventService } from "src/app/services/timeslots/timeslot-calendar-event/timeslot-calendar-event.service";

import { Location } from 'src/app/shared/model/Location';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';
import { Timeslot } from 'src/app/shared/model/Timeslot';
import {booleanSorter} from 'src/app/shared/util/Util'

type TypeOption = {
  date: string;
  openingHour: string;
  closingHour: string;
  reservable: boolean;
  reservableFrom: string;
  seatCount: number;
  timeslots: Timeslot[];
  repeatable: boolean;
};

@Component({
  selector: 'app-location-calendar',
  templateUrl: './location-calendar.component.html',
  styleUrls: ['./location-calendar.component.scss']
})
export class LocationCalendarComponent implements OnInit {
  locationId: number; // will be set based on the url

  timeslotObs: Observable<Timeslot[]>;
  errorSubject = new Subject<boolean>();

  locationReservations: LocationReservation[];
  currentTimeSlot: Timeslot;
  toUpdateTimeslot: Timeslot;

  refresh: Subject<unknown> = new Subject<unknown>();

  jumpToDate: Moment = moment();


  /**
   * 'events' is the object that is used by the angular-calendar module
   * to show the events in the calendar.
   */
  events: CalendarEvent<{
    timeslot?: Timeslot;
  }>[] = [];

  suggestions: {model: Timeslot, copy: Timeslot}[] = [];


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

  calendarViewStyle: CalendarView = CalendarView.Week;

  constructor(
    private timeslotService: TimeslotsService,
    private functionalityService: ApplicationTypeFunctionalityService,
    private locationReservationService: LocationReservationsService,
    private dialog: MatDialog,
    private modalService: MatDialog,
    private authenticationService: AuthenticationService,
    private translate: TranslateService,
    private route: ActivatedRoute,
    private router: Router,
    private locationService: LocationService,
    private timeslotGroupService: TimeslotGroupService,
    private timeslotCalendarEventService: TimeslotCalendarEventService
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
    this.timeslotObs =
    combineLatest([
      this.timeslotService.getTimeslotsOfLocation(this.locationId),
      this.locationService.getLocation(this.locationId)
    ])
      .pipe(
        tap(([next, location]) => {
          // fill the events based on the calendar periods
          this.events = next.map(t => this.timeslotCalendarEventService.timeslotToCalendarEvent(t, this.translate.currentLang))


          this.suggestions = this.timeslotGroupService.getSuggestions(next, location)
          const suggestionEvents = this.suggestions.map(t => this.timeslotCalendarEventService.suggestedTimeslotToCalendarEvent(t.copy, this.translate.currentLang));


          this.events = this.events.concat(suggestionEvents)
          // and update the calendar
          this.refresh.next(null);
        }),

        map(([next]) => next),

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
    const timeslot: Timeslot = event['timeslot'] as Timeslot;

    if (timeslot.reservable) {
      this.showReservations = false;
    }

    this.currentTimeSlot = timeslot;

    if (!timeslot.timeslotSequenceNumber) {
      return;
    }
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
    this.modalService.closeAll();
  }

  open(modal: TemplateRef<any>) {
    this.modalService.open(modal, { panelClass: ["cs--cyan", "bigmodal"] });
  }

  getLocation(): Observable<Location> {
    return this.locationService.getLocation(this.locationId)
  }

  newTimeslot(timeslot: Timeslot) {
    this.timeslotService.addTimeslot(timeslot).subscribe(() => this.setup())
    this.modalService.closeAll();
  }


  prepareUpdate(timeslot: Timeslot, modal: TemplateRef<any>) {
    this.toUpdateTimeslot = timeslot;
    this.open(modal);
  }

  prepareAdd(modal: TemplateRef<any>) {
    this.toUpdateTimeslot = null;
    this.open(modal);
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
    this.modalService.closeAll();
  }


  copy(timeslot: Timeslot, weekOffset: string, location: Location) {
    const newTimeslot = this.timeslotGroupService.copy(timeslot, moment(weekOffset), location);
    this.timeslotService.addTimeslot(newTimeslot).subscribe(() => this.setup());
    this.modalService.closeAll();
  }

  timeslotGroupData(timeslots: Timeslot[]) {
    if(!timeslots)
      return []

    const perGroup = this.timeslotGroupService.groupTimeslots(timeslots);
    const bestPerGroup = this.timeslotGroupService.getOldestTimeslotPerGroup(timeslots)

    const toSort = Array.from(perGroup);

    // Sort first on weekday, then on repeatability.
    // The repeatable (=> currently active, used) periods will come first, in a week overview (sorted by day)
    // The non-repeable, old periods are second.
    toSort.sort((a, b) => bestPerGroup.get(a[0]).timeslotDate.isoWeekday() - bestPerGroup.get(b[0]).timeslotDate.isoWeekday())
    toSort.sort(booleanSorter(t => bestPerGroup.get(t[0]).repeatable))

    return toSort.map(([g, t]) => this.getGroupDetails(t, bestPerGroup.get(g)));
  }

  /**
   * This function gets the details for the table.
   * Usually, a timeslot group will be on one day, in one time category.
   * However, with migrated timeslots, this might not be the case. We'll group all these together.
   * @param timeslot
   */
  private getGroupDetails(timeslot: Timeslot[], oldestTimeslot: Timeslot): TypeOption {
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
        "repeatable": false,
        "seatCount": timeslot[0].seatCount,
        "timeslots":timeslot
      }
    }

    const allOnSameDay = timeslot.every(t => t.timeslotDate.isSame(timeslot[0].timeslotDate, "day"))

    return {
      "date": allOnSameDay ? oldestTimeslot.timeslotDate.format("DD/MM/YYYY") : oldestTimeslot.timeslotDate.format("dddd"),
      "openingHour": oldestTimeslot.openingHour.format("HH:mm"),
      "closingHour": oldestTimeslot.closingHour.format("HH:mm"),
      "reservable": oldestTimeslot.reservable,
      "reservableFrom": allOnSameDay ? oldestTimeslot.reservableFrom.format("DD/MM/YYYY HH:mm") : oldestTimeslot.reservableFrom.format("dddd HH:mm"),
      "seatCount": oldestTimeslot.seatCount,
      "timeslots": timeslot,
      "repeatable": oldestTimeslot.repeatable
    }

  }

  hourPickedHandler(date: Moment, modal: TemplateRef<any>) {
    console.log(date);
    const openingHour = moment(date.format("HH:mm"), "HH:mm")
    this.toUpdateTimeslot = new Timeslot(null, date, null, null, null, null, this.locationId, openingHour, null, null, false);
    this.modalService.open(modal)
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

  showApproveAll() {
    return this.getCurrentSuggestions().length > 0
  }

  approveAll() {
    const suggestions = this.getCurrentSuggestions();
    combineLatest(suggestions.map(t => this.timeslotService.addTimeslot(t.copy))).subscribe(() => this.setup())
  }

  approve(timeslot: Timeslot) {
    this.timeslotService.addTimeslot(timeslot).subscribe(() => this.setup())
    this.currentTimeSlot = null;
  }

  rejectAll() {
    const suggestions = this.getCurrentSuggestions();
    combineLatest(suggestions.map(t => this.timeslotService.setRepeatable(t.model, false))).subscribe(() => this.setup())
  }

  reject(timeslot: Timeslot) {
    const suggestion = this.getCurrentSuggestions().find(s => s.copy === timeslot);
    this.timeslotService.setRepeatable(suggestion.model, false).subscribe(() => this.setup())
    this.currentTimeSlot = null;
  }

  private getCurrentSuggestions() {
    if(this.calendarViewStyle == CalendarView.Day) {
      return this.timeslotGroupService.filterSuggestionsByMoment(this.suggestions, this.jumpToDate, "day")
    }

    if(this.calendarViewStyle == CalendarView.Week) {
      return this.timeslotGroupService.filterSuggestionsByMoment(this.suggestions, this.jumpToDate, "isoWeek")
    }

    if(this.calendarViewStyle == CalendarView.Month) {
      return this.timeslotGroupService.filterSuggestionsByMoment(this.suggestions, this.jumpToDate, "month")
    }

    return [];
  }

  public isSuggestion(timeslot: Timeslot) {
    return this.suggestions.map(s => s.copy).includes(timeslot);
  }

}
