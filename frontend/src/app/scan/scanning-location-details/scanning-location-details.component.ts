import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {LocationService} from '../../services/api/locations/location.service';
import {combineLatest, Observable, Subject} from 'rxjs';
import {Location} from '../../shared/model/Location';
import {User} from '../../shared/model/User';
import {ScanningService} from '../../services/api/scan/scanning.service';
import {catchError, map, switchMap} from 'rxjs/operators';
import {of} from 'rxjs/internal/observable/of';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { BarcodeService } from 'src/app/services/barcode.service';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';

@Component({
  selector: 'app-scanning-location-details',
  templateUrl: './scanning-location-details.component.html',
  styleUrls: ['./scanning-location-details.component.css']
})
export class ScanningLocationDetailsComponent implements OnInit {

  locationObs: Observable<Location>;
  locationReservationObs: Observable<LocationReservation[]>;

  loadingError = new Subject<boolean>();
  reservation?: LocationReservation;
  scanningfield: string;
  error: string;

  lastScanned?: LocationReservation;

  constructor(private route: ActivatedRoute,
              private locationService: LocationService,
              private scanningService: ScanningService,
              private reservationService: LocationReservationsService,
              private barcodeService: BarcodeService) { }

  ngOnInit(): void {
    const locationId = Number(this.route.snapshot.paramMap.get('locationId'));
    // thanks to the caching that was implemented, the locationService will just return the cached location
    this.locationObs = this.locationService.getLocation(locationId);

    this.locationReservationObs = this.locationObs
                  .pipe(
                    switchMap(l => this.reservationService.getLocationReservationsOfTimeslot(l.currentTimeslot)),
                    catchError(err => {
                      console.error('Error while loading the users you could scan.', err);
                      this.loadingError.next(true);
                      return of(null);
                    }))
  }

  getValidator(reservations: LocationReservation[]): (a: string) => boolean {
    return (code) => code.length > 6; // filter out the most egregious examples
  }


  scanUser(reservations: LocationReservation[], code: string): void {
    this.error = "";
    console.log(code)
    const res = this.barcodeService.getReservation(reservations, code)
    if(res == null)
      this.error = "scan.maybe";
    else
      this.reservation = res;
  }

  confirm(location: Location): void {
    this.reservationService.postLocationReservationAttendance(this.reservation, true).subscribe(() => {}, e => this.error = "scan.error");
    this.reservation.attended = true;
    this.lastScanned = this.reservation;
    this.reservation = null;
  }

  cancel(): void {
    this.reservation = null;
  }

}
