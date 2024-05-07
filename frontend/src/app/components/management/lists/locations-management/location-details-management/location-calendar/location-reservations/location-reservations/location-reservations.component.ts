import {
    Component,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
    TemplateRef,
} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import * as moment from 'moment';
import {BarcodeService} from 'src/app/services/barcode.service';
import {User} from 'src/app/model/User';
import {TableDataService} from 'src/app/components/stad-gent-components/atoms/table/data-service/table-data-service.service';
import {TabularData} from 'src/app/components/stad-gent-components/atoms/table/tabular-data';
import {
    LocationReservationsService
} from '../../../../../../../../services/api/location-reservations/location-reservations.service';
import {
    LocationReservation,
    LocationReservationState,
} from '../../../../../../../../model/LocationReservation';
import {Timeslot} from '../../../../../../../../model/Timeslot';
import {ModalComponent} from '../../../../../../../stad-gent-components/molecules/modal/modal.component';

@Component({
    selector: 'app-location-reservations',
    templateUrl: './location-reservations.component.html',
    styleUrls: ['./location-reservations.component.scss'],
})
export class LocationReservationsComponent implements OnChanges {

    @Input() locationReservations: LocationReservation[];
    @Input() currentTimeSlot: Timeslot;
    @Input() lastScanned?: LocationReservation;

    @Input() isManagement = true; // enable some functionality that should not be enabled for volunteers in the Scan page

    @Output() reservationChange: EventEmitter<unknown> = new EventEmitter<unknown>();

    locationReservationToDelete: LocationReservation = undefined;
    successDeletingLocationReservation: boolean = undefined;

    filteredLocationReservations: LocationReservation[] = [];
    currentTableData: TabularData<LocationReservation> = null;

    searchTerm = '';
    noSuchUserFoundWarning = false;
    userAlreadyPresentWarning: User[] = [];
    selectionTimeout;
    penaltyManagerUser: User;

    constructor(
        private locationReservationService: LocationReservationsService,
        private modalService: MatDialog,
        private barcodeService: BarcodeService,
        private tabularDataService: TableDataService
    ) {
    }

    protected readonly undefined = undefined;

    ngOnInit(): void {
        this.currentTableData = this.tabularDataService.reservationsToScanningTable(
            this.filteredLocationReservations, this.isManagement
        );
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.locationReservations) {
            this.setCurrentReservations(this.locationReservations);
        }

        if (this.lastScanned) {
            this.setLastScanned(this.lastScanned);
        }
    }

    // /***************
    // *   SCANNING   *
    // ****************/

    scanLocationReservation(
        reservation: LocationReservation,
        attended: boolean,
        errorTemplate: ModalComponent,
        previousScanned: LocationReservation
    ): void {
        const olds = reservation.state;
        const newS = attended
            ? LocationReservationState.PRESENT
            : LocationReservationState.ABSENT;
        if (olds === LocationReservationState.PRESENT && newS === LocationReservationState.PRESENT) {
            // Trying to set a user as present who was already present.
            // Check if it was the last scanned user. (Maybe request was sent twice)
            if (!previousScanned || previousScanned.user.userId !== reservation.user.userId) {
                // Not the last scanned user. Show warning.
                console.log('Not the last scanned user. Show warning.', previousScanned);
                this.userAlreadyPresentWarning.push(reservation.user);
                return;
            } else {
                console.log('lastScanned', previousScanned);
            }
        }

        reservation.state = newS;
        this.setLastScanned(reservation);

        this.locationReservationService
            .postLocationReservationAttendance(reservation, attended)
            .subscribe(
                () => {
                },
                (err) => {
                    reservation.state = olds;
                    this.setLastScanned(reservation);
                    console.error(err);
                    errorTemplate.open();
                }
            );
    }

    setAllNotScannedToUnattended(errorTemplate: ModalComponent): void {
        // hide finishScanningModal
        this.modalService.closeAll();

        // if the update is not successful, rollback UI changes
        const newLocationReservations: LocationReservation[] = [];

        // set all reservations where attended is null to false
        this.locationReservations.forEach((reservation) => {
            if (reservation.state !== LocationReservationState.PRESENT) {
                newLocationReservations.push(
                    new LocationReservation(
                        reservation.user,
                        reservation.timeslot,
                        LocationReservationState.ABSENT,
                        reservation.createdAt
                    )
                );
            } else {
                newLocationReservations.push(reservation);
            }
        });

        // update server side
        this.locationReservationService
            .setAllNotScannedAsUnattended(this.currentTimeSlot)
            .subscribe(
                () => {
                    this.locationReservations = newLocationReservations;
                },
                () => {
                    errorTemplate.open();
                }
            );
    }

    // /*************
    // *   DELETE   *
    // **************/
    deleteLocationReservation(): void {
        this.successDeletingLocationReservation = null;
        this.locationReservationService
            .deleteLocationReservation(this.locationReservationToDelete)
            .subscribe(() => {
                this.successDeletingLocationReservation = true;
                if (this.reservationChange) {
                    this.reservationChange.emit(null);
                }
                this.modalService.closeAll();
            });
    }

    // /******************
    // *   AUXILIARIES   *
    // *******************/
    closeModal(): void {
        this.modalService.closeAll();
    }

    userHasSearchTerm: (u: User) => boolean = (u: User) =>
        u.userId.includes(this.searchTerm) ||
        u.firstName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        u.lastName.toLowerCase().includes(this.searchTerm.toLowerCase())

    updateSearchTerm(errorTemplate: ModalComponent): void {
        this.noSuchUserFoundWarning =
            this.searchTerm.length > 0 &&
            this.locationReservations.every((lr) => !this.userHasSearchTerm(lr.user));
        if (this.noSuchUserFoundWarning) { this.delayedSelectInputBox(); }

        const fullyMatchedUser = this.barcodeService.getReservation(
            this.filteredLocationReservations,
            this.searchTerm
        );

        const previousScanned = this.lastScanned;
        if (this.searchTerm.length > 0) { this.setLastScanned(null); }

        if (fullyMatchedUser) {
            this.setLastScanned(fullyMatchedUser);
            this.scanLocationReservation(fullyMatchedUser, true, errorTemplate, previousScanned);
            setTimeout(() => {
                this.searchTerm = '';
                this.updateSearchTerm(errorTemplate);
            }, 10);
        }
    }

    filter(locationReservations: LocationReservation[]): LocationReservation[] {
        if (!locationReservations) {
            return [];
        }
        console.log('Start of filter: ' + performance.now());

        locationReservations = locationReservations.filter(
            (r) => r.state !== LocationReservationState.DELETED && r.state !== LocationReservationState.REJECTED
        );

        // Sorting the searchTerm hits first. After that, fallback on name sorting (createdAt is not available here)
        locationReservations.sort((a, b) => {
            if (a === this.lastScanned || b === this.lastScanned) {
                return a === this.lastScanned ? -1 : 1;
            }

            if (this.userHasSearchTerm(a.user) !== this.userHasSearchTerm(b.user)) {
                return this.userHasSearchTerm(a.user) ? -1 : 1;
            }

            if (b.state !== a.state) {
                const order = [
                    LocationReservationState.APPROVED, // Not scanned first.
                    LocationReservationState.ABSENT,
                    LocationReservationState.PRESENT,
                    LocationReservationState.REJECTED,
                    LocationReservationState.PENDING,
                ];
                for (const state of order) {
                    if (a.state === state) {
                        return -1;
                    }
                    if (b.state === state) {
                        return 1;
                    }
                }
            }

            // If a.user.firstName equals b.user.firstName, the first localeCompare returns 0 (= false)
            // and thus the second localeCompare is executed. If they are not equal, the first localeCompare
            // returns either -1 or 1 (both equivalent to 'true' in a boolean expression) and thus the second
            // localeCompare is not executed.
            return (
                a.user.firstName.localeCompare(b.user.firstName) ||
                a.user.lastName.localeCompare(b.user.lastName)
            );
        });

        console.log('End of filter: ' + performance.now());

        return locationReservations;
    }

    onAction(
        {columnIndex, data}: { columnIndex: number; data: LocationReservation },
        errorTemplate: ModalComponent,
        penaltyManager: TemplateRef<unknown>
    ): void {
        const previousScanned = this.lastScanned;
        if (this.isManagement) {
            if (columnIndex === 3) {
                this.lastScanned = data;
                return this.scanLocationReservation(data, true, errorTemplate, previousScanned);
            } else if (columnIndex === 4) {
                return this.scanLocationReservation(data, false, errorTemplate, previousScanned);
            } else if (columnIndex === 2) {
                return this.openPenaltyBox(data, penaltyManager);
            }
        }

        if (columnIndex === 2) {
            this.lastScanned = data;
            return this.scanLocationReservation(data, true, errorTemplate, previousScanned);
        } else if (columnIndex === 3) {
            return this.scanLocationReservation(data, false, errorTemplate, previousScanned);
        }
    }

    isTimeslotStartInFuture(): boolean {
        return this.currentTimeSlot.getStartMoment().isAfter(moment());
    }

    setCurrentReservations(locationReservations: LocationReservation[]): void {
        this.filteredLocationReservations = this.filter(locationReservations);
        this.currentTableData = {
            ...this.currentTableData,
            data: this.filteredLocationReservations,
        };
    }

    setLastScanned(lastScanned: LocationReservation): void {
        this.lastScanned = lastScanned;
        this.filteredLocationReservations = this.filter(this.locationReservations);
        this.currentTableData = {
            ...this.currentTableData,
            data: this.filteredLocationReservations,
        };
    }

    selectInputBox(): void {
        const el = document.getElementById('search') as HTMLInputElement;
        el.focus();
        el.select();
    }

    delayedSelectInputBox(): void {
        if (this.selectionTimeout) {
            clearTimeout(this.selectionTimeout);
        }

        this.selectionTimeout = setTimeout(() => this.selectInputBox(), 800);
    }

    openPenaltyBox(locres: LocationReservation, modal: TemplateRef<unknown>): void {
        this.penaltyManagerUser = locres.user;
        this.modalService.open(modal, {panelClass: ['cs--cyan', 'bigmodal']});
    }
}
