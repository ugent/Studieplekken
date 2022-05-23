import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { LocationReservationsService } from '../../services/api/location-reservations/location-reservations.service';
import { LocationReservation, LocationReservationState } from '../../shared/model/LocationReservation';
import { Timeslot } from '../../shared/model/Timeslot';
import * as moment from 'moment';
import { User } from '../../shared/model/User';
import { Observable } from 'rxjs';
import { LocationService } from 'src/app/services/api/locations/location.service';
import { map } from 'rxjs/operators';
import { MatDialog } from '@angular/material/dialog';
import { TableDataService } from 'src/app/stad-gent-components/atoms/table/data-service/table-data-service.service';
import { TabularData } from 'src/app/stad-gent-components/atoms/table/tabular-data';

@Component({
  selector: 'app-profile-reservations',
  templateUrl: './profile-reservations.component.html',
  styleUrls: ['./profile-reservations.component.scss'],
})
export class ProfileReservationsComponent implements OnInit {
  @Input() userObj?: User;

  locationReservations: LocationReservation[] = [];

  locationReservationToDelete: LocationReservation = undefined;

  successGettingLocationReservations: boolean = undefined;
  successDeletingLocationReservation: boolean = undefined;

  constructor(
    private authenticationService: AuthenticationService,
    private locationReservationService: LocationReservationsService,
    private modalService: MatDialog,
    private locationService: LocationService,
    private tableDataService: TableDataService
  ) {
  }

  ngOnInit(): void {
    this.setup();
  }

  setup(): void {
    // don't setup if user is not logged in (or logged in user isn't loaded yet)
    if (!this.authenticationService.isLoggedIn()) {
      return;
    }

    // let the user know that the location reservations are loading
    this.successGettingLocationReservations = null;

    // load the location reservations
    this.locationReservationsAndCalendarPeriodsObservable()
      .subscribe(
        (next) => {
          this.successGettingLocationReservations = true;
          this.locationReservations = next;
        },
        () => {
          this.successGettingLocationReservations = false;
        });
  }

  prepareToDeleteLocationReservation(
    locationReservation: LocationReservation,
    template: TemplateRef<any>
  ): void {
    this.successDeletingLocationReservation = undefined;
    this.locationReservationToDelete = locationReservation;
    this.modalService.open(template, { panelClass: ['cs--cyan', 'bigmodal'] });
  }

  deleteLocationReservation(): void {
    this.successDeletingLocationReservation = null;
    this.locationReservationService
      .deleteLocationReservation(this.locationReservationToDelete)
      .subscribe(
        () => {
          this.successDeletingLocationReservation = true;
          this.setup();
          this.modalService.closeAll();
        },
        () => {
          this.successDeletingLocationReservation = false;
        }
      );
  }

  // /******************
  // *   AUXILIARIES   *
  // *******************/

  getBeginHour(timeslot: Timeslot): string {
    return timeslot.openingHour.format('HH:mm');
  }

  // TODO(ydndonck): What is this? Should be double checked.
  needTooltip(
    reservation: LocationReservation,
  ): boolean {
    return reservation.timeslot.isInPast() &&
      reservation.timeslot.reservable &&
      reservation.state === LocationReservationState.APPROVED;
  }

  canDeleteReservation(
    reservation: LocationReservation
  ): boolean {
    const ts = reservation.timeslot;
    const hours = moment.duration(ts.closingHour.hours(), 'hours');
    const minutes = moment.duration(ts.closingHour.minutes(), 'minutes');
    const closingDateTime = moment(ts.timeslotDate).add(hours).add(minutes);
    const now = moment();
    if (closingDateTime.isBefore(now)) {
      return false;
    }
    switch (reservation.state) {
      case LocationReservationState.PENDING:
      case LocationReservationState.REJECTED:
      case LocationReservationState.APPROVED: {
        return true;
      }
      default: {
        return false;
      }
    }
  }

  getCorrectI18NObject(
    reservation: LocationReservation,
  ): string {
    return 'profile.reservations.locations.table.attended.' + reservation.state;
    if (reservation.timeslot.getStartMoment().isBefore(moment())) {
      if (reservation.state === LocationReservationState.APPROVED) {
        return 'profile.reservations.locations.table.attended.notScanned';
      } else {
        return (reservation.state === LocationReservationState.PRESENT)
          ? 'profile.reservations.locations.table.attended.yes'
          : 'profile.reservations.locations.table.attended.no';
      }
    }
    return 'general.notAvailableAbbreviation';
  }

  formatDate(date: unknown): string {
    return moment(date).format('DD/MM/YYYY');
  }

  closeModal(): void {
    this.modalService.closeAll();
  }

  locationReservationsAndCalendarPeriodsObservable(): Observable<LocationReservation[]> {
    if (this.userObj === undefined) {
      return this.authenticationService
        .getLocationReservations();
    } else {
      return this.locationReservationService
        .getLocationReservationsOfUser(this.userObj.userId);
    }
  }

  getLocation(locationReservation: LocationReservation): Observable<string> {
    return this.locationService.getLocation(locationReservation.timeslot.locationId).pipe(map(l => l.name));
  }

  sortedLocationReservations(lres: LocationReservation[]): Array<LocationReservation> {
    return Array.from(lres).sort((a, b) => a.timeslot.getStartMoment().isBefore(b.timeslot.getStartMoment()) ? 1 : -1);
  }

  getTabularData(locationReservations: LocationReservation[]): Observable<TabularData<LocationReservation>> {
    return this.tableDataService.reservationsToProfileTable(this.sortedLocationReservations(locationReservations));
  }

}
