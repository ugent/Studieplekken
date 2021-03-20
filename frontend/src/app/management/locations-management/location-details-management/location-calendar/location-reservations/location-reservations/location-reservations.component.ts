import {Component, EventEmitter, Input, OnChanges, OnInit, Output, TemplateRef} from '@angular/core';
import {LocationReservation} from '../../../../../../shared/model/LocationReservation';
import {Timeslot, timeslotEndHour, timeslotStartHour} from '../../../../../../shared/model/Timeslot';
import * as moment from 'moment';
import {CalendarPeriod} from '../../../../../../shared/model/CalendarPeriod';
import {LocationReservationsService} from '../../../../../../services/api/location-reservations/location-reservations.service';
import { BsModalService } from 'ngx-bootstrap/modal';
import { User } from 'src/app/shared/model/User';

@Component({
  selector: 'app-location-reservations',
  templateUrl: './location-reservations.component.html',
  styleUrls: ['./location-reservations.component.css']
})
export class LocationReservationsComponent implements OnInit, OnChanges {
  @Input() locationReservations: LocationReservation[];
  @Input() currentCalendarPeriod?: CalendarPeriod;
  @Input() currentTimeSlot: Timeslot;
  @Input() lastScanned?: LocationReservation;

  @Input() hideDeleteIcon = false; // used by ScanningLocationDetails to disable the "delete" icon for volunteers

  @Output() reservationChange: EventEmitter<any> = new EventEmitter<any>();


  locationReservationToDelete: LocationReservation = undefined;

  successDeletingLocationReservation: boolean = undefined;
  successFinishingScan: boolean = undefined;

  startedScanning = true;
  searchTerm = '';

  scannedLocationReservations: LocationReservation[] = [];
  warning = false;

  private static upcEncoded(code: string): string {
    return '0' + code.substr(0, code.length - 1);
  }

  userHasSearchTerm = (u: User) => u.augentID.includes(this.searchTerm) ||
    u.firstName.includes(this.searchTerm) || u.lastName.includes(this.searchTerm)

  constructor(private locationReservationService: LocationReservationsService,
              private modalService: BsModalService) { }

  ngOnInit(): void {
  }

  ngOnChanges(): void {
  }

  // /***************
  // *   SCANNING   *
  // ****************/

  startScanning(): void {
    this.scannedLocationReservations = [];
    this.startedScanning = true;
  }

  prepareFinishScanning(): void {
    this.successFinishingScan = undefined;
  }

  finishScanning(): void {
    this.successFinishingScan = null;
    // this.locationReservationService.scanLocationReservations(this.scannedLocationReservations)
    //   .subscribe(
    //     () => {
    //       this.successFinishingScan = true;
    //       this.updateLocationReservations();
    //     }, () => {
    //       this.successFinishingScan = false;
    //     }
    //   );
    this.startedScanning = false;
  }

  scanLocationReservation(reservation: LocationReservation, attended: boolean): void {
    const idx = this.scannedLocationReservations.findIndex(
      r => {
        return r.timeslot.timeslotSeqnr === reservation.timeslot.timeslotSeqnr &&
          r.user === reservation.user;
      });

    if (idx < 0) {
      reservation.attended = attended;
      this.scannedLocationReservations.push(reservation);
      this.locationReservationService.postLocationReservationAttendance(reservation, attended).subscribe();
    } else {
      this.scannedLocationReservations[idx].attended = attended;
      reservation.attended = attended;
      this.locationReservationService.postLocationReservationAttendance(reservation, attended).subscribe();
    }
  }

  // /*************
  // *   DELETE   *
  // **************/

  prepareToDeleteLocationReservation(locationReservation: LocationReservation, template: TemplateRef<any>): void {
    console.log(locationReservation, template);
    this.successDeletingLocationReservation = undefined;
    this.locationReservationToDelete = locationReservation;
    this.modalService.show(template);
  }

  deleteLocationReservation(): void {
    this.successDeletingLocationReservation = null;
    this.locationReservationService.deleteLocationReservation(this.locationReservationToDelete).subscribe(
      () => {
        this.successDeletingLocationReservation = true;
        if (this.reservationChange) {
          this.reservationChange.emit(null);
        }
        this.modalService.hide();
      }
    );
  }

  // /******************
  // *   AUXILIARIES   *
  // *******************/

  getCorrectI18NObject(reservation: LocationReservation): string {
    if (this.isTimeslotStartInPast()) {
      if (reservation.attended === null) {
        return 'management.locationDetails.calendar.reservations.table.notScanned';
      } else {
        return reservation.attended ? 'general.yes' : 'general.no';
      }
    } else if (this.isTimeslotEndInPast()) {
      if (reservation.attended === null) {
        return 'management.locationDetails.calendar.reservations.table.yetToScan';
      } else {
        return reservation.attended ? 'general.yes' : 'general.no';
      }
    } else {
      return 'general.notAvailableAbbreviation';
    }
  }

  isTimeslotEndInPast(): boolean {
    if (!this.currentCalendarPeriod) {
      return false; // Assume current
    }
    return timeslotEndHour(this.currentCalendarPeriod, this.currentTimeSlot).isBefore(moment());
  }

  isTimeslotStartInPast(): boolean {
    if (!this.currentCalendarPeriod) {
      return true; // Assume current
    }

    const start = timeslotStartHour(this.currentCalendarPeriod, this.currentTimeSlot);
    return start.isBefore(moment());
  }

  isButtonDisabled(reservation: LocationReservation): boolean {
    return reservation.attended;
  }

  /**
   * Update this.locationReservations based on this.scannedLocationReservations
   */
  updateLocationReservations(): void {
    this.scannedLocationReservations.forEach(
      slr => {
        const idx = this.locationReservations.findIndex(lr => lr.user === slr.user
          && lr.timeslot.timeslotSeqnr === slr.timeslot.timeslotSeqnr);

        if (idx >= 0) {
          this.locationReservations[idx].attended = slr.attended;
        }
      }
    );
  }

  closeModal(): void {
    this.modalService.hide();
  }

  updateSearchTerm(): void {
    this.warning = this.locationReservations.every(lr => !this.userHasSearchTerm(lr.user)) && this.searchTerm.length > 0;

    const fullyMatchedUser = this.locationReservations
      .find(lr => lr.user.augentID === LocationReservationsComponent.upcEncoded(this.searchTerm));

    if (fullyMatchedUser) {
      this.scanLocationReservation(fullyMatchedUser, true);
      setTimeout(
        () => {
          this.searchTerm = '';
          this.lastScanned = fullyMatchedUser;
          this.updateSearchTerm();
        }, 10);
    }

    this.lastScanned = null;
  }

  filter(locationReservations: LocationReservation[]): LocationReservation[] {

    // Sorting the searchterm hits first. After that, fallback on name sorting (createdAt is not available here)
    locationReservations.sort((a, b) => {

      if (a === this.lastScanned || b === this.lastScanned) {
        return a === this.lastScanned ? -1 : 1;
      }

      if (this.userHasSearchTerm(a.user) !== this.userHasSearchTerm(b.user)) {
        return this.userHasSearchTerm(a.user) ? -1 : 1;
      }

      if (b.attended !== a.attended){
        return a.attended ? 1 : -1;
      }

      return a.user.lastName.localeCompare(b.user.lastName);
    });


    return locationReservations;
  }
}
