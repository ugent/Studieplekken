import {Component, OnInit} from '@angular/core';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../shared/animations/RowAnimation';
import {Observable} from 'rxjs';
import {Location} from '../../shared/model/Location';
import {LocationService} from '../../services/api/locations/location.service';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';
import {Authority} from '../../shared/model/Authority';
import {AuthoritiesService} from '../../services/api/authorities/authorities.service';
import {tap} from 'rxjs/operators';

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

  authoritiesObs: Observable<Authority[]>;
  authoritiesMap: Map<number, Authority>;

  constructor(private locationService: LocationService,
              private authoritiesService: AuthoritiesService) { }

  ngOnInit(): void {
    this.locations = this.locationService.getLocations();
    this.setupForm();

    this.authoritiesObs = this.authoritiesService.getAllAuthorities().pipe(tap(
      next => {
        this.authoritiesMap = new Map<number, Authority>();
        next.forEach(value => {
          this.authoritiesMap.set(value.authorityId, value);
        });
      }
    ));
  }

  setupForm(): void {
    this.addLocationFormGroup = new FormGroup({
      name: new FormControl('', Validators.required),
      authority: new FormControl('', Validators.required),
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
    location.authority = this.authoritiesMap.get(Number(this.authority.value));

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
  get authority(): AbstractControl { return this.addLocationFormGroup.get('authority'); }
  get address(): AbstractControl { return this.addLocationFormGroup.get('address'); }
  get numberOfSeats(): AbstractControl { return this.addLocationFormGroup.get('numberOfSeats'); }
  get numberOfLockers(): AbstractControl { return this.addLocationFormGroup.get('numberOfLockers'); }
  get imageUrl(): AbstractControl { return this.addLocationFormGroup.get('imageUrl'); }
}
