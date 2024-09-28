import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {LocationService} from '../../../services/api/locations/location.service';
import {Observable, Subject} from 'rxjs';
import {Location} from '../../../model/Location';
import {ScanningService} from '../../../services/api/scan/scanning.service';
import {catchError, switchMap} from 'rxjs/operators';
import {of} from 'rxjs/internal/observable/of';
import {
    LocationReservationsService
} from 'src/app/services/api/location-reservations/location-reservations.service';
import {BarcodeService} from 'src/app/services/barcode.service';
import {LocationReservation, LocationReservationState} from 'src/app/model/LocationReservation';
import {timer} from 'rxjs';

@Component({
    selector: 'app-scanning-location-details',
    templateUrl: './scanning-location-details.component.html',
    styleUrls: ['./scanning-location-details.component.scss'],
})
export class ScanningLocationDetailsComponent implements OnInit {
    locationObs: Observable<Location>;
    locationReservationObs: Observable<LocationReservation[]>;
    locationLoadingSubject: Subject<boolean> = new Subject();

    loadingError = new Subject<boolean>();
    reservation?: LocationReservation;
    error: string;

    lastScanned?: LocationReservation;

    @ViewChild('scanner') scannerModal: TemplateRef<any>;

    constructor(
        private route: ActivatedRoute,
        private locationService: LocationService,
        private reservationService: LocationReservationsService,
        private barcodeService: BarcodeService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        const locationId = Number(this.route.snapshot.paramMap.get('locationId'));

        // Check if locationId is a Number before proceeding. If NaN, redirect to scan locations.
        if (isNaN(locationId)) {
            void this.router.navigate(['/scan/locations']);
        } else {
            // thanks to the caching that was implemented, the locationService will just return the cached location
            this.locationObs = this.locationService.getLocation(locationId).pipe(
                catchError((err) => {
                    console.error('Error while fetching the location: ', err);
                    this.locationLoadingSubject.next(true);
                    return of<Location>(null);
                })
            );

            this.locationReservationObs = timer(0, 60 * 1000).pipe(
                switchMap(() => this.locationObs),
                switchMap((l) =>
                    this.reservationService.getLocationReservationsOfTimeslot(
                        l.currentTimeslot.timeslotSequenceNumber
                    )
                ),
                catchError((err) => {
                    console.error('Error while loading the users you could scan.', err);
                    this.loadingError.next(true);
                    return of<LocationReservation[]>(null);
                })
            );
        }
    }

    scanUser(reservations: LocationReservation[], code: string): void {
        this.error = '';
        const res = this.barcodeService.getReservation(reservations, code);

        if (res == null) {
            this.error = 'scan.maybe';
        } else {
            this.reservation = res;
        }
    }

    confirm(): void {
        this.reservationService.postLocationReservationAttendance(this.reservation, true);
        this.reservation.state = LocationReservationState.PRESENT;
        this.lastScanned = this.reservation;
        this.reservation = null;
    }
}
