import { Component, OnInit } from '@angular/core';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../../shared/animations/RowAnimation';
import {FormControl, FormGroup} from '@angular/forms';
import {Observable} from 'rxjs';
import {Location} from '../../../shared/model/Location';
import {LockerReservation, LockerReservationConstructor} from '../../../shared/model/LockerReservation';
import {LocationService} from '../../../services/api/locations/location.service';
import {LockerReservationService} from '../../../services/api/locker-reservations/locker-reservation.service';
import {CustomDate, customDateToTypeScriptDate} from '../../../shared/model/helpers/CustomDate';

@Component({
  selector: 'app-locker-reservations-management',
  templateUrl: './locker-reservations-management.component.html',
  styleUrls: ['./locker-reservations-management.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class LockerReservationsManagementComponent implements OnInit {
  formGroup = new FormGroup({
    location: new FormControl(''),
    includePastReservations: new FormControl(false)
  });

  locations: Observable<Location[]>;
  lockerReservationsObs: Observable<LockerReservation[]>;

  neverSearched = true;
  errorOnRetrievingReservations = false;
  lastFormGroupValue: {location: string, includePastReservations: boolean};

  currentLockerReservationToDelete: LockerReservation = LockerReservationConstructor.new();
  deletionWasSuccess: boolean = undefined;

  constructor(private locationService: LocationService,
              private lockerReservationsService: LockerReservationService) { }

  ngOnInit(): void {
    this.locations = this.locationService.getLocations();
  }

  submitSearch(value: {location: string, includePastReservations: boolean}): void {
    this.neverSearched = false;

    // save this value, this will be used to re-invoke this submitSearch if a location reservation
    // would be deleted at a certain moment
    this.lastFormGroupValue = {
      location: value.location,
      includePastReservations: value.includePastReservations
    };

    this.lockerReservationsObs = this.lockerReservationsService
      .getLockerReservationsOfLocation(value.location, value.includePastReservations);

    this.lockerReservationsObs.subscribe(
      () => {
        this.errorOnRetrievingReservations = false;
      }, () => {
        this.errorOnRetrievingReservations = true;
      }
    );
  }

  enableSearchButton(value: {location: string, includePastReservations: boolean}): boolean {
    return value.location !== '';
  }

  enableClearButton(value: {location: string, includePastReservations: boolean}): boolean {
    return value.location !== '' || value.includePastReservations;
  }

  clearFormInput(): void {
    this.formGroup.setValue({
      location: '',
      includePastReservations: false
    });
  }

  prepareToDeleteLockerReservation(lockerReservation: LockerReservation): void {
    this.deletionWasSuccess = undefined;
    this.currentLockerReservationToDelete = lockerReservation;
  }

  deleteLockerReservationLinkedToCurrentLockerToDelete(): void {
    this.deletionWasSuccess = null;
    this.lockerReservationsService.deleteLockerReservation(this.currentLockerReservationToDelete).subscribe(
      () => {
        this.deletionWasSuccess = true;
        this.submitSearch(this.lastFormGroupValue);
      }, () => {
        this.deletionWasSuccess = false;
      }
    );
  }

  toDateString(date: CustomDate): string {
    return customDateToTypeScriptDate(date).toLocaleDateString();
  }
}
