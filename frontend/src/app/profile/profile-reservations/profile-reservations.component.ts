import {Component, TemplateRef} from '@angular/core';
import {AuthenticationService} from '../../services/authentication/authentication.service';
import {LocationReservationsService} from '../../services/api/location-reservations/location-reservations.service';
import {BsModalService} from 'ngx-bootstrap/modal';
import {Pair} from '../../shared/model/helpers/Pair';
import {LocationReservation} from '../../shared/model/LocationReservation';
import {CalendarPeriod} from '../../shared/model/CalendarPeriod';
import {Timeslot, timeslotEndHour, timeslotStartHour} from '../../shared/model/Timeslot';
import * as moment from 'moment';

@Component({
  selector: 'app-profile-reservations',
  templateUrl: './profile-reservations.component.html',
  styleUrls: ['./profile-reservations.component.css'],
})
export class ProfileReservationsComponent {
  locationReservations: Pair<LocationReservation, CalendarPeriod>[] = [];

  locationReservationToDelete: LocationReservation = undefined;
  calendarPeriodForLocationReservationToDelete: CalendarPeriod = undefined;

  successGettingLocationReservations: boolean = undefined;
  successDeletingLocationReservation: boolean = undefined;

  constructor(
    private authenticationService: AuthenticationService,
    private locationReservationService: LocationReservationsService,
    private modalService: BsModalService
  ) {
    authenticationService.user.subscribe(() => {
      this.setup();
    });
  }

  setup(): void {
    // don't setup if user is not logged in (or logged in user isn't loaded yet)
    if (!this.authenticationService.isLoggedIn()) {
      return;
    }

    // let the user know that his/hers location reservations are loading
    this.successGettingLocationReservations = null;

    // load the location reservations
    this.authenticationService
      .getLocationReservationsAndCalendarPeriods()
      .subscribe(
        (next) => {
          this.successGettingLocationReservations = true;
          this.locationReservations = next;
        },
        () => {
          this.successGettingLocationReservations = false;
        }
      );
  }

  prepareToDeleteLocationReservation(
    locationReservation: LocationReservation,
    calendarPeriod: CalendarPeriod,
    template: TemplateRef<any>
  ): void {
    this.successDeletingLocationReservation = undefined;
    this.locationReservationToDelete = locationReservation;
    this.calendarPeriodForLocationReservationToDelete = calendarPeriod;
    this.modalService.show(template);
  }

  deleteLocationReservation(): void {
    this.successDeletingLocationReservation = null;
    this.locationReservationService
      .deleteLocationReservation(this.locationReservationToDelete)
      .subscribe(
        () => {
          this.successDeletingLocationReservation = true;
          this.setup();
          this.modalService.hide();
        },
        () => {
          this.successDeletingLocationReservation = false;
        }
      );
  }

  // /******************
  // *   AUXILIARIES   *
  // *******************/

  getBeginHour(timeslot: Timeslot, calendarPeriod: CalendarPeriod): string {
    const openingTime = calendarPeriod.openingTime
      .clone()
      .add(timeslot.timeslotSeqnr * calendarPeriod.timeslotLength, 'minutes');
    return openingTime.format('HH:mm');
  }

  needTooltip(
    reservation: LocationReservation,
    calendarPeriod: CalendarPeriod
  ): boolean {
    return (
      this.isTimeslotInPast(reservation.timeslot, calendarPeriod) &&
      reservation.attended === null
    );
  }

  getCorrectI18NObject(
    reservation: LocationReservation,
    calendarPeriod: CalendarPeriod
  ): string {
    if (timeslotStartHour(calendarPeriod, reservation.timeslot).isBefore(moment())) {
      if (reservation.attended === null) {
        return 'profile.reservations.locations.table.attended.notScanned';
      } else {
        return reservation.attended
          ? 'profile.reservations.locations.table.attended.yes'
          : 'profile.reservations.locations.table.attended.no';
      }
    }
    return 'general.notAvailableAbbreviation';
  }

  isTimeslotInPast(
    timeslot: Timeslot,
    calendarPeriod: CalendarPeriod
  ): boolean {
    return timeslotEndHour(calendarPeriod, timeslot).isBefore(moment());
  }

  formatDate(date: unknown): string {
    return moment(date).format('DD/MM/YYYY');
  }

  closeModal(): void {
    this.modalService.hide();
  }

}
