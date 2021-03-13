import { Component, OnInit } from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {LocationService} from '../../services/api/locations/location.service';
import {Observable, Subject} from 'rxjs';
import {Location} from '../../shared/model/Location';
import {User} from '../../shared/model/User';
import {ScanningService} from '../../services/api/scan/scanning.service';
import {catchError} from 'rxjs/operators';
import {of} from 'rxjs/internal/observable/of';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';

@Component({
  selector: 'app-scanning-location-details',
  templateUrl: './scanning-location-details.component.html',
  styleUrls: ['./scanning-location-details.component.css']
})
export class ScanningLocationDetailsComponent implements OnInit {

  locationObs: Observable<Location>;
  usersObs: Observable<User[]>;
  loadingError = new Subject<boolean>();
  user?: User;
  scanningfield: string;
  error: string;

  constructor(private route: ActivatedRoute,
              private locationService: LocationService,
              private scanningService: ScanningService,
              private reservationService: LocationReservationsService) { }

  ngOnInit(): void {
    const locationId = Number(this.route.snapshot.paramMap.get('locationId'));
    // thanks to the caching that was implemented, the locationService will just return the cached location
    this.locationObs = this.locationService.getLocation(locationId);

    this.usersObs = this.scanningService.getUsersForLocationToScan(locationId).pipe(
      catchError(err => {
        console.error('Error while loading the users you could scan.', err);
        this.loadingError.next(true);
        return of(null);
      })
    );
  }

  getValidator(users: User[]): (a: string) => boolean {
    return (code) => {console.log(code, users); return users.some(u => u.augentID === this.upcEncoded(code)); };
  }

  private upcEncoded(code: string): string {
    return '0' + code.substr(0, code.length - 1);
  }

  scanUser(users: User[], code: string): void {
    this.error = "";
    const user = users.find(u => u.augentID === this.upcEncoded(code));
    this.user = user;
  }

  confirm(location: Location): void {
    const reservation = new LocationReservation(this.user, location.currentTimeslot);
    this.reservationService.postLocationReservationAttendance(reservation, true).subscribe(() => {}, e => this.error = "scan.error");
    this.user = null;
  }

  cancel(): void {
    this.user = null;
  }

  scannedInput(users: User[], location: Location): void {
    const user = users.find(u => u.augentID === this.scanningfield);
    if (!user) {
      if(this.scanningfield.length >= 12) {
        this.error = "scan.unregistered"
      } else {
        this.error = "";
      }

      return;
    }
    const reservation = new LocationReservation(user, location.currentTimeslot);
    this.reservationService.postLocationReservationAttendance(reservation, true).subscribe(() => {}, e => this.error = "scan.error");
    this.user = user;
    setTimeout(() => this.user = null, 1500);
    this.scanningfield = "";
    }
}
