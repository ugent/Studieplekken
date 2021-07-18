import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import * as moment from 'moment';
import { Observable } from 'rxjs';
import { map } from 'rxjs/internal/operators/map';
import { filter } from 'rxjs/operators';
import { TimeslotsService } from 'src/app/services/api/calendar-periods/calendar-periods.service';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { CalendarPeriod } from 'src/app/shared/model/CalendarPeriod';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';
import {
  Timeslot,
} from 'src/app/shared/model/Timeslot';
import { LocationService } from '../../../../services/api/locations/location.service';
import { Location } from '../../../../shared/model/Location';

@Component({
  selector: 'app-timeslot-table',
  templateUrl: './timeslot-table.component.html',
  styleUrls: ['./timeslot-table.component.css'],
})
export class TimeslotTableComponent implements OnInit {
  timeslot: Timeslot;
  locationReservations: Observable<LocationReservation[]>;
  locationId: number;

  constructor(
    private route: ActivatedRoute,
    private locationReservationsService: LocationReservationsService,
    private locationService: LocationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.locationId = Number(this.route.snapshot.paramMap.get('locationId'));
    const date = moment(this.route.snapshot.paramMap.get('date'), 'YYYY-MM-DD');
    const seqnr = Number(this.route.snapshot.paramMap.get('seqnr'));

    // Check if locationId, calendarId and seqnr are a Number before proceeding. If NaN, redirect to management locations.
    if (isNaN(this.locationId) || isNaN(seqnr)) {
      this.router.navigate(['/management/locations']).catch(console.log);
      return;
    }

    this.timeslot = new Timeslot(seqnr, date, null, 0, null, null, null, null, null);
    this.locationReservations = this.locationReservationsService.getLocationReservationsOfTimeslot(
      this.timeslot
    );
  }

  print(): void {
    window.print();
  }

  timestring(timeslot: Timeslot): string {
    return `${timeslot.getStartMoment().format(
      'DD/MM/YYYY\tHH:mm'
    )}-${timeslot.getEndMoment().format('HH:mm')}`;
  }

  getLocation(locationId: number): Observable<Location> {
    return this.locationService.getLocation(locationId);
  }


}
