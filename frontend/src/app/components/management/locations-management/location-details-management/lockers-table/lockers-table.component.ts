import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Location } from '../../../../../extensions/model/Location';
import { LockersService } from '../../../../../extensions/services/api/lockers/lockers.service';
import { LockerReservation } from '../../../../../extensions/model/LockerReservation';
import { tap } from 'rxjs/operators';
import * as moment from 'moment';
import { msToShowFeedback } from '../../../../../app.constants';

export enum LockerStatus {
  AVAILABLE,
  OCCUPIED,
}

@Component({
  selector: 'app-lockers-table',
  templateUrl: './lockers-table.component.html',
  styleUrls: ['./lockers-table.component.scss'],
})
export class LockersTableComponent implements OnInit {
  @Input() location: Observable<Location>;
  lockerStatuses: Observable<LockerReservation[]>;
  LockerStatus = LockerStatus;

  successOnUpdatingLockerReservation = undefined;

  numberOfLinesOnPage = 15;
  currentLowerIndexOfSlice = 0;
  currentUpperIndexOfSlice = 15;
  pageIndices: number[];

  floor: (x: number) => number = (x: number) => Math.floor(x);

  constructor(private lockersService: LockersService) {}

  ngOnInit(): void {
    this.location.subscribe((next) => {
      this.lockerStatuses = this.lockersService
        .getLockersStatusesOfLocation(next.locationId)
        // fill the the array this.pageIndices with all possible pages, based on the length of
        // the resulted lockersStatusesOfLocation
        .pipe(
          tap((next2) => {
            this.pageIndices = Array(
              Math.ceil(next2.length / this.numberOfLinesOnPage)
            )
              .fill(1)
              .map((ignore, i) => i + 1);
          })
        );
    });
  }

  getStatusOfLocker(lockerReservation: LockerReservation): LockerStatus {
    return lockerReservation.owner === null
      ? LockerStatus.AVAILABLE
      : LockerStatus.OCCUPIED;
  }

  pickupKeyButtonClick(lockerReservation: LockerReservation): void {
    this.successOnUpdatingLockerReservation = null;

    lockerReservation.keyPickupDate = moment();
    this.lockersService.updateLockerReservation(lockerReservation).subscribe(
      () => {
        this.successHandler();
      },
      () => {
        // undo pickup
        lockerReservation.keyPickupDate = null;
        this.errorHandler();
      }
    );
  }

  returnKeyButtonClick(lockerReservation: LockerReservation): void {
    this.successOnUpdatingLockerReservation = null;

    lockerReservation.keyReturnedDate = moment();
    this.lockersService.updateLockerReservation(lockerReservation).subscribe(
      () => {
        this.successHandler();
        lockerReservation.owner = null;
        lockerReservation.keyPickupDate = null;
        lockerReservation.keyReturnedDate = null;
      },
      () => {
        lockerReservation.keyReturnedDate = null;
        this.errorHandler();
      }
    );
  }

  successHandler(): void {
    this.successOnUpdatingLockerReservation = true;
    setTimeout(
      () => (this.successOnUpdatingLockerReservation = undefined),
      msToShowFeedback
    );
  }

  errorHandler(): void {
    this.successOnUpdatingLockerReservation = false;
    setTimeout(
      () => (this.successOnUpdatingLockerReservation = undefined),
      msToShowFeedback
    );
  }

  isActivePage(pageNumber: number): boolean {
    return (
      pageNumber ===
      Math.floor(this.currentLowerIndexOfSlice / this.numberOfLinesOnPage) + 1
    );
  }

  showPage(pageNumber: number): void {
    this.currentLowerIndexOfSlice = (pageNumber - 1) * this.numberOfLinesOnPage;
    this.currentUpperIndexOfSlice = pageNumber * this.numberOfLinesOnPage;
  }
}
