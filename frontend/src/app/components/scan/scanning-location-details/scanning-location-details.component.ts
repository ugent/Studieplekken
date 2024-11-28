import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {LocationService} from '@/services/api/locations/location.service';
import {Observable, Subject} from 'rxjs';
import {Location} from '@/model/Location';
import {catchError, switchMap, tap} from 'rxjs/operators';
import {of} from 'rxjs/internal/observable/of';
import {
    LocationReservationsService
} from 'src/app/services/api/location-reservations/location-reservations.service';
import {BarcodeService} from 'src/app/services/barcode.service';
import {LocationReservation, LocationReservationState} from 'src/app/model/LocationReservation';
import {timer} from 'rxjs';
import { ScannerComponent } from '@/components/shared/scanner/scanner.component';

@Component({
    selector: 'app-scanning-location-details',
    templateUrl: './scanning-location-details.component.html',
    styleUrls: ['./scanning-location-details.component.scss'],
})
export class ScanningLocationDetailsComponent implements OnInit {
    @ViewChild(ScannerComponent)
    public scannerComponent: ScannerComponent;
    @ViewChild('scannerModal') 
    public scannerModal: TemplateRef<any>;

    protected locationObs$: Observable<Location>;
    protected locationReservationObs$: Observable<LocationReservation[]>;

    protected selectedReservation: LocationReservation;
    protected scannedReservation: LocationReservation;

    protected isLoading: boolean;
    protected hasLoadingError: boolean = false;
    protected scanningError: string;

    constructor(
        private route: ActivatedRoute,
        private locationService: LocationService,
        private reservationService: LocationReservationsService,
        private barcodeService: BarcodeService,
        private router: Router
    ) {
    }

    public ngOnInit(): void {
        const locationId = Number(this.route.snapshot.paramMap.get('locationId'));

        // Check if locationId is a Number before proceeding. 
        // If NaN, redirect to scan locations.
        if (isNaN(locationId)) {
            void this.router.navigate(['/scan/locations']);
            return;
        }
    
        this.locationObs$ = this.locationService.getLocation(locationId).pipe(
            catchError((err) => {
                console.error('Error while fetching the location: ', err);
                this.isLoading = false;
                return of<Location>(null);
            })
        );

        this.locationReservationObs$ = timer(0, 60 * 1000).pipe(
            switchMap(() =>
                this.locationObs$.pipe(
                    switchMap((location: Location) => 
                        this.reservationService.getLocationReservationsOfTimeslot(
                            location.currentTimeslot.timeslotSequenceNumber
                        )
                    )
                )
            ),
            catchError((err) => {
                console.error('Error while loading the users you could scan.', err);
                this.hasLoadingError = true;
                return of<LocationReservation[]>(null);
            })
        );
    }

    /**
     * Scans a user's reservation based on the provided barcode.
     * 
     * @param reservations - An array of `LocationReservation` objects to search through.
     * @param code - The barcode string to match against the reservations.
     * 
     * @returns void
     */
    public scanUser(reservations: LocationReservation[], code: string): void {
        this.resetScanningError();

        const reservation = this.barcodeService.getReservation(
            reservations,
            code
        );

        if (reservation === null) {
            this.setScanningError('scan.maybe');
        } else {
            this.selectedReservation = reservation;
        }
    }

    /**
     * Confirms the reservation by posting the location reservation attendance,
     * updating the reservation state to PRESENT, and setting the last scanned reservation.
     * After confirmation, the current reservation is set to null.
     *
     * @returns {void}
     */
    public confirmReservation(): void {
        this.reservationService.postLocationReservationAttendance(this.selectedReservation, true).subscribe();
        this.selectedReservation.state = LocationReservationState.PRESENT;
        this.scannedReservation = this.selectedReservation;
        this.selectedReservation = null;
    }

    /**
     * Resets the last scanned reservation.
     *
     * @returns {void}
     */
    protected resetScanningError(): void {
        this.scanningError = '';
    }

    /**
     * Sets the scanning error message.
     *
     * @param error - The error message to be set.
     */
    protected setScanningError(error: string): void {
        this.scanningError = error;
    }
}
