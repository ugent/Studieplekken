import {Component, TemplateRef} from '@angular/core';
import { AuthenticationService } from '../../../services/authentication/authentication.service';
import { LocationReservation } from '../../../shared/model/LocationReservation';
import { CalendarPeriod } from 'src/app/shared/model/CalendarPeriod';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { Timeslot } from 'src/app/shared/model/Timeslot';
import { Pair } from '../../../shared/model/helpers/Pair';
import * as moment from 'moment';
import { BsModalService } from 'ngx-bootstrap/modal';
import { LocationService } from 'src/app/services/api/locations/location.service';
import { Observable } from 'rxjs';
import { Location } from 'src/app/shared/model/Location'

@Component({
  selector: 'app-profile-location-reservations',
  templateUrl: './profile-location-reservations.component.html',
  styleUrls: ['./profile-location-reservations.component.css'],
})
export class ProfileLocationReservationsComponent {
  locationReservations: LocationReservation[] = [];

  locationReservationToDelete: LocationReservation = undefined;
  calendarPeriodForLocationReservationToDelete: CalendarPeriod = undefined;

  successGettingLocationReservations: boolean = undefined;
  successDeletingLocationReservation: boolean = undefined;

  constructor(
    private authenticationService: AuthenticationService,
    private locationReservationService: LocationReservationsService,
    private modalService: BsModalService,
    private locationService: LocationService
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
    template: TemplateRef<any>
  ): void {
    this.successDeletingLocationReservation = undefined;
    this.locationReservationToDelete = locationReservation;
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
      .add(timeslot.timeslotSequenceNumber * calendarPeriod.timeslotLength, 'minutes');
    return openingTime.format('HH:mm');
  }

  needTooltip(
    reservation: LocationReservation,
  ): boolean {
    return (
      this.isTimeslotInPast(reservation.timeslot) &&
      reservation.attended === null
    );
  }

  getCorrectI18NObject(
    reservation: LocationReservation,
  ): string {
    if (this.isTimeslotInPast(reservation.timeslot)) {
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
  ): boolean {
    return timeslot.getEndMoment().isBefore(moment());
  }

  formatDate(date: unknown): string {
    return moment(date).format('DD/MM/YYYY');
  }

  closeModal(): void {
    this.modalService.hide();
  }

  getLocation(id: number): Observable<Location> {
    return this.locationService.getLocation(id)
  }
}
