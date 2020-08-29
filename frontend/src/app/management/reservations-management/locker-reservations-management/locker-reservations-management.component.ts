import { Component, OnInit } from '@angular/core';
import {transition, trigger, useAnimation} from "@angular/animations";
import {rowsAnimation} from "../../../shared/animations/RowAnimation";
import {FormControl, FormGroup} from "@angular/forms";
import {Observable} from "rxjs";
import {Location} from "../../../shared/model/Location";
import {LockerReservation, LockerReservationConstructor} from "../../../shared/model/LockerReservation";
import {LocationService} from "../../../services/api/locations/location.service";
import {LockerReservationService} from "../../../services/api/locker-reservations/locker-reservation.service";
import {CustomDate, customDateToTypeScriptDate} from "../../../shared/model/helpers/CustomDate";

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
    start: new FormControl(''),
    end: new FormControl(''),
    includePastReservations: new FormControl(false)
  });

  locations: Observable<Location[]>;
  lockerReservationsObs: Observable<LockerReservation[]>;

  neverSearched = true;
  errorOnRetrievingReservations = false;
  lastFormGroupValue: {location: string, start: string, end: string, includePastReservations: boolean};

  currentLockerReservationToDelete: LockerReservation = LockerReservationConstructor.new();
  deletionWasSuccess: boolean = undefined;

  constructor(private locationService: LocationService,
              private lockerReservationsService: LockerReservationService) { }

  ngOnInit(): void {
    this.locations = this.locationService.getLocations();
  }

  submitSearch(value: {location: string, start: string, end: string, includePastReservations: boolean}): void {
    this.neverSearched = false;

    // save this value, this will be used to re-invoke this submitSearch if a location reservation
    // would be deleted at a certain moment
    this.lastFormGroupValue = {
      location: value.location,
      start: value.start,
      end: value.end,
      includePastReservations: value.includePastReservations
    };

    // Depending on different input tags filled or not filled, make the correct HTTP call
    if (value.start === '' && value.end === '') {
      this.lockerReservationsObs = this.lockerReservationsService
        .getLockerReservationsOfLocation(value.location, value.includePastReservations);
    } else if (value.end === '') {
      this.lockerReservationsObs = this.lockerReservationsService
        .getLockerReservationsOfLocationFrom(value.location, new Date(value.start), value.includePastReservations);
    } else if (value.start === '') {
      this.lockerReservationsObs = this.lockerReservationsService
        .getLockerReservationsOfLocationUntil(value.location, new Date(value.end), value.includePastReservations);
    } else {
      this.lockerReservationsObs = this.lockerReservationsService
        .getLockerReservationsOfLocationFromAndUntil(value.location, new Date(value.start), new Date(value.end),
          value.includePastReservations);
    }

    this.lockerReservationsObs.subscribe(
      () => {
        this.errorOnRetrievingReservations = false;
      }, () => {
        this.errorOnRetrievingReservations = true;
      }
    );
  }

  enableSearchButton(value: {location: string, start: string, end: string, includePastReservations: boolean}): boolean {
    return value.location !== '';
  }

  enableClearButton(value: {location: string, start: string, end: string, includePastReservations: boolean}): boolean {
    return value.location !== '' || value.start !== '' || value.end !== '' || value.includePastReservations;
  }

  clearFormInput(): void {
    this.formGroup.setValue({
      location: '',
      start: '',
      end: '',
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
