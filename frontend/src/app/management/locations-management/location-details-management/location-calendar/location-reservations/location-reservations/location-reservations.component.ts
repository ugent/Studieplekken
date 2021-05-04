import {
  Component,
  EventEmitter,
  Input,
  Output,
  TemplateRef,
} from '@angular/core';
import { LocationReservation } from '../../../../../../shared/model/LocationReservation';
import {
  Timeslot,
  timeslotEndHour,
  timeslotStartHour,
} from '../../../../../../shared/model/Timeslot';
import * as moment from 'moment';
import { CalendarPeriod } from '../../../../../../shared/model/CalendarPeriod';
import { LocationReservationsService } from '../../../../../../services/api/location-reservations/location-reservations.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { User } from 'src/app/shared/model/User';
import { BarcodeService } from 'src/app/services/barcode.service';

@Component({
  selector: 'app-location-reservations',
  templateUrl: './location-reservations.component.html',
  styleUrls: ['./location-reservations.component.css'],
})
export class LocationReservationsComponent {
  @Input() locationReservations: LocationReservation[];
  @Input() currentCalendarPeriod?: CalendarPeriod;
  @Input() currentTimeSlot: Timeslot;
  @Input() lastScanned?: LocationReservation;

  @Input() isManagement = true; // enable some functionality that should not be enabled for volunteers in the Scan page

  @Output()
  reservationChange: EventEmitter<unknown> = new EventEmitter<unknown>();

  locationReservationToDelete: LocationReservation = undefined;

  successDeletingLocationReservation: boolean = undefined;

  searchTerm = '';

  scannedLocationReservations: LocationReservation[] = [];
  noSuchUserFoundWarning = false;
  waitingForServer = false;

  userHasSearchTerm: (u: User) => boolean = (u: User) =>
    u.augentID.includes(this.searchTerm) ||
    u.firstName.includes(this.searchTerm) ||
    u.lastName.includes(this.searchTerm);

  constructor(
    private locationReservationService: LocationReservationsService,
    private modalService: BsModalService,
    private barcodeService: BarcodeService
  ) {}

  // /***************
  // *   SCANNING   *
  // ****************/

  scanLocationReservation(
    reservation: LocationReservation,
    attended: boolean,
    errorTemplate: TemplateRef<unknown>
  ): void {
    const idx = this.scannedLocationReservations.findIndex((r) => {
      return (
        r.timeslot.timeslotSeqnr === reservation.timeslot.timeslotSeqnr &&
        r.user === reservation.user
      );
    });

    // only perform API call if the attendance/absence changes
    if (
      idx >= 0 &&
      attended === this.scannedLocationReservations[idx].attended
    ) {
      return;
    }

    this.waitingForServer = true;
    this.locationReservationService
      .postLocationReservationAttendance(reservation, attended)
      .subscribe(
        () => {
          this.waitingForServer = false;
          reservation.attended = attended;

          if (idx < 0) {
            this.scannedLocationReservations.push(reservation);
          } else {
            this.scannedLocationReservations[idx].attended = attended;
          }
        },
        (err) => {
          this.waitingForServer = false;
          console.error(err);
          this.modalService.show(errorTemplate);
        }
      );
  }

  onFinishScanningClick(modalTemplate: TemplateRef<unknown>): void {
    this.modalService.show(modalTemplate);
  }

  setAllNotScannedToUnattended(errorTemplate: TemplateRef<unknown>): void {
    // hide finishScanningModal
    this.modalService.hide();

    // if the update is not successful, rollback UI changes
    const rollback: LocationReservation[] = [];

    // set all reservations where attended is null to false
    this.locationReservations.forEach((reservation) => {
      if (reservation.attended !== true) {
        reservation.attended = false;
        rollback.push(reservation);
      }
    });

    // update server side
    this.locationReservationService
      .setAllNotScannedAsUnattended(this.currentTimeSlot)
      .subscribe(
        () => {},
        () => {
          // on error, rollback UI changes
          rollback.forEach((reservation) => {
            reservation.attended = null;
          });
          this.modalService.show(errorTemplate);
        }
      );
  }

  // /*************
  // *   DELETE   *
  // **************/

  prepareToDeleteLocationReservation(
    locationReservation: LocationReservation,
    template: TemplateRef<unknown>
  ): void {
    console.log(locationReservation, template);
    this.successDeletingLocationReservation = undefined;
    this.locationReservationToDelete = locationReservation;
    this.modalService.show(template);
  }

  deleteLocationReservation(): void {
    this.successDeletingLocationReservation = null;
    this.locationReservationService
      .deleteLocationReservation(this.locationReservationToDelete)
      .subscribe(() => {
        this.successDeletingLocationReservation = true;
        if (this.reservationChange) {
          this.reservationChange.emit(null);
        }
        this.modalService.hide();
      });
  }

  // /******************
  // *   AUXILIARIES   *
  // *******************/

  getCorrectI18NObject(reservation: LocationReservation): string {
    if (reservation.attended === null) {
      if (this.isTimeslotStartInFuture()) {
        return 'general.notAvailableAbbreviation'
      } else {
        return 'management.locationDetails.calendar.reservations.table.notScanned';
      }
    } else {
      return reservation.attended ? 'general.yes' : 'general.no';
    }
  }

  isTimeslotEndInPast(): boolean {
    if (!this.currentCalendarPeriod) {
      return false; // Assume current
    }
    return timeslotEndHour(
      this.currentCalendarPeriod,
      this.currentTimeSlot
    ).isBefore(moment());
  }

  isTimeslotStartInPast(): boolean {
    if (!this.currentCalendarPeriod) {
      return true; // Assume current
    }

    const start = timeslotStartHour(
      this.currentCalendarPeriod,
      this.currentTimeSlot
    );
    return start.isBefore(moment());
  }

  isTimeslotStartInFuture(): boolean {
    return !this.isTimeslotStartInPast();
  }

  disableYesButton(reservation: LocationReservation): boolean {
    return reservation.attended !== null && reservation.attended === true;
  }

  disableNoButton(reservation: LocationReservation): boolean {
    return reservation.attended !== null && reservation.attended === false;
  }

  /**
   * Update this.locationReservations based on this.scannedLocationReservations
   */
  updateLocationReservations(): void {
    this.scannedLocationReservations.forEach((slr) => {
      const idx = this.locationReservations.findIndex(
        (lr) =>
          lr.user === slr.user &&
          lr.timeslot.timeslotSeqnr === slr.timeslot.timeslotSeqnr
      );

      if (idx >= 0) {
        this.locationReservations[idx].attended = slr.attended;
      }
    });
  }

  closeModal(): void {
    this.modalService.hide();
  }

  updateSearchTerm(errorTemplate: TemplateRef<unknown>): void {
    this.noSuchUserFoundWarning =
      this.searchTerm.length > 0 &&
      this.locationReservations.every((lr) => !this.userHasSearchTerm(lr.user));

    const fullyMatchedUser = this.barcodeService.getReservation(
      this.locationReservations,
      this.searchTerm
    );

    if (fullyMatchedUser) {
      this.scanLocationReservation(fullyMatchedUser, true, errorTemplate);
      setTimeout(() => {
        this.searchTerm = '';
        this.lastScanned = fullyMatchedUser;
        this.updateSearchTerm(errorTemplate);
      }, 10);
    }

    this.lastScanned = null;
  }

  filter(locationReservations: LocationReservation[]): LocationReservation[] {
    // Sorting the searchTerm hits first. After that, fallback on name sorting (createdAt is not available here)
    locationReservations.sort((a, b) => {
      if (a === this.lastScanned || b === this.lastScanned) {
        return a === this.lastScanned ? -1 : 1;
      }

      if (this.userHasSearchTerm(a.user) !== this.userHasSearchTerm(b.user)) {
        return this.userHasSearchTerm(a.user) ? -1 : 1;
      }

      if (b.attended !== a.attended) {
        return a.attended ? 1 : -1;
      }

      return a.user.lastName.localeCompare(b.user.lastName);
    });

    return locationReservations;
  }
}
