import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import * as moment from 'moment';
import { Observable } from 'rxjs';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';
import { Timeslot } from 'src/app/shared/model/Timeslot';

@Component({
  selector: 'app-timeslot-table',
  templateUrl: './timeslot-table.component.html',
  styleUrls: ['./timeslot-table.component.css']
})
export class TimeslotTableComponent implements OnInit {
  timeslot: Timeslot;
  locationReservations: Observable<LocationReservation[]>;

  constructor(private route: ActivatedRoute,
              private locationReservationsService: LocationReservationsService) { }

  ngOnInit(): void {
    const locationName = this.route.snapshot.paramMap.get('locationName');
    const calendarId = Number(this.route.snapshot.paramMap.get('calendarId'));
    const date = moment(this.route.snapshot.paramMap.get('date'), 'YYYY-MM-dd');
    const seqnr = Number(this.route.snapshot.paramMap.get('seqnr'));

    this.timeslot = new Timeslot(seqnr, date, calendarId, null);
    this.locationReservations = this.locationReservationsService.getLocationReservationsOfTimeslot(this.timeslot);
  }

}
