import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LocationReservation} from '../../../../../../shared/model/LocationReservation';
import {Timeslot, timeslotStartHour} from '../../../../../../shared/model/Timeslot';
import * as moment from 'moment';
import {CalendarPeriod} from '../../../../../../shared/model/CalendarPeriod';
import {tap} from 'rxjs/operators';
import {LocationReservationsService} from '../../../../../../services/api/location-reservations/location-reservations.service';

@Component({
  selector: 'app-location-reservations',
  templateUrl: './location-reservations.component.html',
  styleUrls: ['./location-reservations.component.css']
})
export class LocationReservationsComponent implements OnInit {
  @Input() locationReservations: LocationReservation[];
  @Input() currentCalendarPeriod: CalendarPeriod;
  @Input() currentTimeSlot: Timeslot;

  @Output() reservationChange: EventEmitter<any> = new EventEmitter<any>();

  locationReservationToDelete: LocationReservation = undefined;

  successDeletingLocationReservation: boolean = undefined;

  constructor(private locationReservationService: LocationReservationsService) { }

  ngOnInit(): void {
  }

  // /*************
  // *   DELETE   *
  // **************/

  prepareToDeleteLocationReservation(locationReservation: LocationReservation): void {
    this.successDeletingLocationReservation = undefined;
    this.locationReservationToDelete = locationReservation;
  }

  deleteLocationReservation(): void {
    this.successDeletingLocationReservation = null;
    this.locationReservationService.deleteLocationReservation(this.locationReservationToDelete).subscribe(
      () => {
        this.successDeletingLocationReservation = true;
        this.reservationChange.emit(null);
      }
    );
  }

  // /******************
  // *   AUXILIARIES   *
  // *******************/

  onCheckboxToggle(reservation, checked): void {
    this.locationReservationService.postLocationReservationAttendance(reservation, checked)
      .pipe(tap(() => this.reservationChange.emit(null)))
      .subscribe();
  }

  showCheckbox(): boolean {
    return this.currentCalendarPeriod && this.currentTimeSlot
      && timeslotStartHour(this.currentCalendarPeriod, this.currentTimeSlot).isBefore(moment());
  }
}
