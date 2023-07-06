import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';
import { TimeslotsService } from 'src/app/extensions/services/api/calendar-periods/timeslot.service';
import { LocationReservationsService } from 'src/app/extensions/services/api/location-reservations/location-reservations.service';
import { LocationReservation } from 'src/app/extensions/model/LocationReservation';
import {
  Timeslot
} from 'src/app/extensions/model/Timeslot';
import { LocationService } from '../../../../../extensions/services/api/locations/location.service';
import { Location } from '../../../../../extensions/model/Location';

@Component({
  selector: 'app-timeslot-table',
  templateUrl: './timeslot-table.component.html',
  styleUrls: ['./timeslot-table.component.scss'],
})
export class TimeslotTableComponent implements OnInit {
  timeslot: Observable<Timeslot>;
  locationReservations: Observable<LocationReservation[]>;

  constructor(
    private route: ActivatedRoute,
    private locationReservationsService: LocationReservationsService,
    private locationService: LocationService,
    private router: Router,
    private timeslotService: TimeslotsService
  ) { }

  ngOnInit(): void {
    const seqnr = Number(this.route.snapshot.paramMap.get('seqnr'));

    // Check if locationId, calendarId and seqnr are a Number before proceeding. If NaN, redirect to management locations.
    if (isNaN(seqnr)) {
      this.router.navigate(['/management/locations']).catch(console.log);
      return;
    }

    this.timeslot = this.timeslotService.getById(seqnr);
    this.locationReservations = this.locationReservationsService.getLocationReservationsOfTimeslot(
      seqnr
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

  sortReservations(lrs: LocationReservation[]) {
    return lrs.sort((a, b) => a.user.lastName < b.user.lastName ? -1:1);
  }

}
