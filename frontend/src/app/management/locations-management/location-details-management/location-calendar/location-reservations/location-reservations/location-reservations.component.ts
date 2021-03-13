import {Component, EventEmitter, Input, OnInit, Output, TemplateRef} from '@angular/core';
import {LocationReservation} from '../../../../../../shared/model/LocationReservation';
import {Timeslot, timeslotEndHour, timeslotStartHour} from '../../../../../../shared/model/Timeslot';
import * as moment from 'moment';
import {CalendarPeriod} from '../../../../../../shared/model/CalendarPeriod';
import {LocationReservationsService} from '../../../../../../services/api/location-reservations/location-reservations.service';
import { BsModalService } from 'ngx-bootstrap/modal';

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

  startedScanning = false;

  scannedLocationReservations: LocationReservation[];

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
    } else {
      this.scannedLocationReservations[idx].attended = attended;
    }
  }

  // /*************
  // *   DELETE   *
  // **************/

  prepareToDeleteLocationReservation(locationReservation: LocationReservation, template: TemplateRef<any>): void {
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
}
