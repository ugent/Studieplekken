import {Component, OnInit} from '@angular/core';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../shared/animations/RowAnimation';
import {Observable} from 'rxjs';
import {Location} from '../../shared/model/Location';
import {LocationService} from '../../services/api/locations/location.service';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';

@Component({
  selector: 'app-locations-management',
  templateUrl: './locations-management.component.html',
  styleUrls: ['./locations-management.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class LocationsManagementComponent implements OnInit {
  locations: Observable<Location[]>;

  addLocationFormGroup: FormGroup;
  addingWasSuccess: boolean = undefined;

  currentLocationNameToDelete: string;
  deletionWasSuccess: boolean = undefined;

  constructor(private locationService: LocationService) { }

  ngOnInit(): void {
    this.locations = this.locationService.getLocations();
    this.setupForm();
  }

  setupForm(): void {
    this.addLocationFormGroup = new FormGroup({
      name: new FormControl('', Validators.required),
      address: new FormControl('', Validators.required),
      numberOfSeats: new FormControl('', Validators.required),
      numberOfLockers: new FormControl('', Validators.required),
      imageUrl: new FormControl('')
    });
  }

  prepareToAddLocation(): void {
    this.addingWasSuccess = undefined;
  }

  addNewLocation(location: Location): void {
    this.addingWasSuccess = null;
    if (this.addLocationFormGroup.valid) {
      this.locationService.addLocation(location).subscribe(
        () => {
          this.successHandler();
        }, () => {
          this.errorHandler();
        }
      );
    }
  }

  clearForm(): void {
    this.setupForm();
  }

  validForm(): boolean {
    return this.addLocationFormGroup.valid;
  }

  prepareToDeleteLocation(locationName: string): void {
    this.deletionWasSuccess = undefined;
    this.currentLocationNameToDelete = locationName;
  }

  deleteLocationLinkedToCurrentLocationNameToDelete(): void {
    this.deletionWasSuccess = null;
    this.locationService.deleteLocation(this.currentLocationNameToDelete).subscribe(
      () => {
        this.successDeletionHandler();
      }, () => {
        this.deletionWasSuccess = false;
      }
    );
  }

  successHandler(): void {
    this.addingWasSuccess = true;
    this.locations = this.locationService.getLocations();
    this.setupForm();
  }

  successDeletionHandler(): void {
    this.deletionWasSuccess = true;
    this.locations = this.locationService.getLocations();
  }

  errorHandler(): void {
    this.addingWasSuccess = false;
  }

  setCurrentLocationNameToDelete(locationName: string): void {
    this.currentLocationNameToDelete = locationName;
  }

  get name(): AbstractControl { return this.addLocationFormGroup.get('name'); }
  get address(): AbstractControl { return this.addLocationFormGroup.get('address'); }
  get numberOfSeats(): AbstractControl { return this.addLocationFormGroup.get('numberOfSeats'); }
  get numberOfLockers(): AbstractControl { return this.addLocationFormGroup.get('numberOfLockers'); }
  get imageUrl(): AbstractControl { return this.addLocationFormGroup.get('imageUrl'); }
}
