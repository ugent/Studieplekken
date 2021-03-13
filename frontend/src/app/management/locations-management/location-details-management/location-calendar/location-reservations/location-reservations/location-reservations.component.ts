import {Component, EventEmitter, Input, OnInit, Output, TemplateRef} from '@angular/core';
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
export class LocationReservationsComponent implements OnInit {
  @Input() locationReservations: LocationReservation[];
  @Input() currentCalendarPeriod?: CalendarPeriod;
  @Input() currentTimeSlot: Timeslot;

  @Output() reservationChange: EventEmitter<any> = new EventEmitter<any>();

  locationReservationToDelete: LocationReservation = undefined;

  successDeletingLocationReservation: boolean = undefined;
  successFinishingScan: boolean = undefined;

  startedScanning = true;
  searchTerm: string = "";

  scannedLocationReservations: LocationReservation[] = [];

  constructor(private locationReservationService: LocationReservationsService,
              private modalService: BsModalService) { }

  ngOnInit(): void {
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
    console.log(reservation);
    const idx = this.scannedLocationReservations.findIndex(
      r => {
        return r.timeslot.timeslotSeqnr === reservation.timeslot.timeslotSeqnr &&
          r.user === reservation.user;
      });

    if (idx < 0) {
      const lr = LocationReservation.fromJSON(reservation);
      lr.attended = attended;
      this.scannedLocationReservations.push(lr);
      this.locationReservationService.postLocationReservationAttendance(reservation, attended).subscribe();
    } else {
      this.scannedLocationReservations[idx].attended = attended;
      this.locationReservationService.postLocationReservationAttendance(reservation, attended).subscribe();
    }
  }

  // /*************
  // *   DELETE   *
  // **************/

  prepareToDeleteLocationReservation(locationReservation: LocationReservation, template: TemplateRef<any>): void {
    console.log(locationReservation, template)
    this.successDeletingLocationReservation = undefined;
    this.locationReservationToDelete = locationReservation;
    this.modalService.show(template);
  }

  deleteLocationReservation(): void {
    this.successDeletingLocationReservation = null;
    this.locationReservationService.deleteLocationReservation(this.locationReservationToDelete).subscribe(
      () => {
        this.successDeletingLocationReservation = true;
        if(this.reservationChange)
          this.reservationChange.emit(null);
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
    if(!this.currentCalendarPeriod)
      return false; // Assume current
    return timeslotEndHour(this.currentCalendarPeriod, this.currentTimeSlot).isBefore(moment());
  }

  isTimeslotStartInPast(): boolean {
    if(!this.currentCalendarPeriod)
      return true; // Assume current

    const start = timeslotStartHour(this.currentCalendarPeriod, this.currentTimeSlot);
    return start.isBefore(moment());
  }

  isButtonDisabled(reservation: LocationReservation, attended: boolean): boolean {
    for (const l of this.scannedLocationReservations) {
      if (l.user === reservation.user && l.timeslot.timeslotSeqnr === reservation.timeslot.timeslotSeqnr &&
          l.attended === attended) {
        return true;
      }
    }
    return false;
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

  filter(locationReservations: LocationReservation[]): LocationReservation[] {
    const userHasSearchTerm = (u: User) => u.augentID.includes(this.searchTerm) || u.firstName.includes(this.searchTerm) || u.lastName.includes(this.searchTerm)

    // Sorting the searchterm hits first. After that, fallback on name sorting (createdAt is not available here)
    locationReservations.sort((a, b) => {

      if(userHasSearchTerm(a.user) && !userHasSearchTerm(b.user)) {
        return -1;
      }
      if(userHasSearchTerm(b.user) && !userHasSearchTerm(a.user)){
        return 1
      }

      return a.user.lastName.localeCompare(b.user.lastName)
    })

    console.log(locationReservations)

    return locationReservations
  }
}
