import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  TemplateRef,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import * as moment from 'moment';
import { BarcodeService } from 'src/app/services/barcode.service';
import { User } from 'src/app/shared/model/User';
import { TableDataService } from 'src/app/stad-gent-components/atoms/table/data-service/table-data-service.service';
import { TabularData } from 'src/app/stad-gent-components/atoms/table/tabular-data';
import { LocationReservationsService } from '../../../../../../services/api/location-reservations/location-reservations.service';
import {
  LocationReservation,
  LocationReservationState,
} from '../../../../../../shared/model/LocationReservation';
import { Timeslot } from '../../../../../../shared/model/Timeslot';

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

  @Output()
  reservationChange: EventEmitter<unknown> = new EventEmitter<unknown>();

  locationReservationToDelete: LocationReservation = undefined;
  successDeletingLocationReservation: boolean = undefined;

  filteredLocationReservations: LocationReservation[] = [];
  currentTableData: TabularData<LocationReservation> = null;

  searchTerm = '';
  noSuchUserFoundWarning = false;
  selectionTimeout: number;
  penaltyManagerUser: User;

  constructor(
    private locationReservationService: LocationReservationsService,
    private modalService: MatDialog,
    private barcodeService: BarcodeService,
    private tabularDataService: TableDataService
  ) {
  }

  ngOnInit() {
    this.currentTableData = this.tabularDataService.reservationsToScanningTable(this.filteredLocationReservations, this.isManagement);

  }

  ngOnChanges(changes: SimpleChanges): void {
      if(changes.locationReservations)
        this.setCurrentReservations(this.locationReservations)

      if(this.lastScanned)
        this.setLastScanned(this.lastScanned)
  }

  // /***************
  // *   SCANNING   *
  // ****************/

  scanLocationReservation(
    reservation: LocationReservation,
    attended: boolean,
    errorTemplate: TemplateRef<unknown>
  ): void {
    const olds = reservation.state;
    const newS = attended
      ? LocationReservationState.PRESENT
      : LocationReservationState.ABSENT;
    reservation.state = newS;
    this.setLastScanned(reservation);

    this.locationReservationService
      .postLocationReservationAttendance(reservation, attended)
      .subscribe(
        () => {},
        (err) => {
          reservation.state = olds;
          this.setLastScanned(reservation);
          console.error(err);
          this.modalService.open(errorTemplate);
        }
      );
  }

  onFinishScanningClick(modalTemplate: TemplateRef<unknown>): void {
    this.modalService.open(modalTemplate, {
      panelClass: ['cs--cyan', 'bigmodal'],
    });
  }

  setAllNotScannedToUnattended(errorTemplate: TemplateRef<unknown>): void {
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
          this.modalService.open(errorTemplate, {
            panelClass: ['cs--cyan', 'bigmodal'],
          });
        }
      );
  }

  // /*************
  // *   DELETE   *
  // **************/

  prepareToDeleteLocationReservation(
    locationReservation: LocationReservation,
    template: TemplateRef<unknown>
  ): void {
    this.successDeletingLocationReservation = undefined;
    this.locationReservationToDelete = locationReservation;
    this.modalService.open(template, { panelClass: ['cs--cyan', 'bigmodal'] });
  }

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
    u.lastName.toLowerCase().includes(this.searchTerm.toLowerCase());

  updateSearchTerm(errorTemplate: TemplateRef<unknown>): void {
    this.noSuchUserFoundWarning =
      this.searchTerm.length > 0 &&
      this.locationReservations.every((lr) => !this.userHasSearchTerm(lr.user));
    if (this.noSuchUserFoundWarning) this.delayedSelectInputBox();

    const fullyMatchedUser = this.barcodeService.getReservation(
      this.filteredLocationReservations,
      this.searchTerm
    );

    if (this.searchTerm.length > 0) this.setLastScanned(null);

    if (fullyMatchedUser) {
      this.setLastScanned(fullyMatchedUser);
      this.scanLocationReservation(fullyMatchedUser, true, errorTemplate);
      setTimeout(() => {
        this.searchTerm = '';
        this.updateSearchTerm(errorTemplate);
      }, 10);
    }
  }

  filter(locationReservations: LocationReservation[]): LocationReservation[] {
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
    { columnIndex, data }: { columnIndex: number; data: LocationReservation },
    errorTemplate: TemplateRef<unknown>,
    penaltyManager: TemplateRef<unknown>
  ) {
    if (this.isManagement) {
      console.log(columnIndex);
      if (columnIndex == 3) {
        this.lastScanned = data;
        return this.scanLocationReservation(data, true, errorTemplate);
      } else if (columnIndex == 4)
        return this.scanLocationReservation(data, false, errorTemplate);
      else if (columnIndex == 2) {
        return this.openPenaltyBox(data, penaltyManager);
      }
    }

    if (columnIndex == 2) {
      this.lastScanned = data;
      return this.scanLocationReservation(data, true, errorTemplate);
    } else if (columnIndex == 3)
      return this.scanLocationReservation(data, false, errorTemplate);
  }

  isTimeslotStartInFuture() {
    return this.currentTimeSlot.getStartMoment().isAfter(moment())
  }

  setCurrentReservations(locationReservations: LocationReservation[]) {
    this.filteredLocationReservations = this.filter(locationReservations);
    this.currentTableData = {
      ...this.currentTableData,
      data: this.filteredLocationReservations,
    };
  }

  setLastScanned(lastScanned: LocationReservation) {
    this.lastScanned = lastScanned;
    this.filteredLocationReservations = this.filter(this.locationReservations)
    this.currentTableData = {
      ...this.currentTableData,
      data: this.filteredLocationReservations,
    };
  }

  selectInputBox() {
    const el = document.getElementById('search') as HTMLInputElement;
    el.focus();
    el.select();
  }

  delayedSelectInputBox() {
    if (this.selectionTimeout) clearTimeout(this.selectionTimeout);

    this.selectionTimeout = setTimeout(() => this.selectInputBox(), 800);
  }

  openPenaltyBox(locres: LocationReservation, modal: TemplateRef<unknown>) {
    this.penaltyManagerUser = locres.user;
    this.modalService.open(modal, { panelClass: ['cs--cyan', 'bigmodal'] });
  }
}
