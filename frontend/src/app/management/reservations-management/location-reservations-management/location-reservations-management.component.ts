import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {Observable} from 'rxjs';
import {Location} from '../../../shared/model/Location';
import {LocationService} from '../../../services/api/locations/location.service';
import {LocationReservation, LocationReservationConstructor} from '../../../shared/model/LocationReservation';
import {LocationReservationsService} from '../../../services/api/location-reservations/location-reservations.service';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../../shared/animations/RowAnimation';
import {CustomDate, customDateToTypeScriptDate} from '../../../shared/model/helpers/CustomDate';

@Component({
  selector: 'app-location-reservations-management',
  templateUrl: './location-reservations-management.component.html',
  styleUrls: ['./location-reservations-management.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class LocationReservationsManagementComponent implements OnInit {
  formGroup = new FormGroup({
    location: new FormControl(''),
    start: new FormControl(''),
    end: new FormControl(''),
    includePastReservations: new FormControl(false)
  });

  locations: Observable<Location[]>;
  locationReservationsObs: Observable<LocationReservation[]>;

  neverSearched = true;
  errorOnRetrievingReservations = false;
  lastFormGroupValue: {location: string, start: string, end: string, includePastReservations: boolean};

  currentLocationReservationToDelete: LocationReservation = LocationReservationConstructor.new();
  deletionWasSuccess: boolean = undefined;

  constructor(private locationService: LocationService,
              private locationReservationsService: LocationReservationsService) { }

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
    /*if (value.start === '' && value.end === '') {
      this.locationReservationsObs = this.locationReservationsService
        .getLocationReservationsOfLocation(value.location, value.includePastReservations);
    } else if (value.end === '') {
      this.locationReservationsObs = this.locationReservationsService
        .getLocationReservationsOfLocationFrom(value.location, new Date(value.start), value.includePastReservations);
    } else if (value.start === '') {
      this.locationReservationsObs = this.locationReservationsService
        .getLocationReservationsOfLocationUntil(value.location, new Date(value.end), value.includePastReservations);
    } else {
      this.locationReservationsObs = this.locationReservationsService
        .getLocationReservationsOfLocationFromAndUntil(value.location, new Date(value.start), new Date(value.end),
          value.includePastReservations);
    }*/

    this.locationReservationsObs.subscribe(
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

  prepareToDeleteLocationReservation(locationReservation: LocationReservation): void {
    this.deletionWasSuccess = undefined;
    this.currentLocationReservationToDelete = locationReservation;
  }

  deleteLocationReservationLinkedToCurrentLocationNameToDelete(): void {
    this.deletionWasSuccess = null;
    this.locationReservationsService.deleteLocationReservation(this.currentLocationReservationToDelete).subscribe(
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
