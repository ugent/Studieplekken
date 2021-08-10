import { Template } from '@angular/compiler/src/render3/r3_ast';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { CalendarEvent } from 'angular-calendar';
import { BsModalService } from 'ngx-bootstrap/modal';
import { Observable, Subject, timer } from 'rxjs';
import { of } from 'rxjs/internal/observable/of';
import { catchError, switchMap, tap } from 'rxjs/operators';
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
    this.timeslotObs = this.timeslotService
      .getTimeslotsOfLocation(this.locationId)
      .pipe(
        tap((next) => {
          // fill the events based on the calendar periods
          this.events = next.map(t => timeslotToCalendarEvent(t, this.translate.currentLang))
          console.log(this.events)

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
    if (!event.timeslot || !event.timeslot.reservable) {
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

  updateTimeslot(timeslot: Timeslot) {
    this.timeslotService.updateTimeslot(timeslot).subscribe(() => this.setup())
    this.modalService.hide();
  }

  prepareDelete(timeslot: Timeslot, modal: TemplateRef<any>) {
    this.toUpdateTimeslot = timeslot;
    this.open(modal);
  }

  delete(timeslot: Timeslot) {
    this.timeslotService.deleteTimeslot(timeslot).subscribe(() => this.setup())
    this.modalService.hide();
  }
}
