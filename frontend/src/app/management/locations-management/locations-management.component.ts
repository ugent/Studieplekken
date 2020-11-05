import {Component, OnInit} from '@angular/core';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../shared/animations/RowAnimation';
import {Observable} from 'rxjs';
import {Location} from '../../shared/model/Location';
import {LocationService} from '../../services/api/locations/location.service';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';
import {Authority} from '../../shared/model/Authority';
import {AuthoritiesService} from '../../services/api/authorities/authorities.service';
import {BuildingService} from '../../services/api/buildings/buildings.service';
import {AuthenticationService} from '../../services/authentication/authentication.service';
import {tap} from 'rxjs/operators';
import { Building } from 'src/app/shared/model/Building';

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

  buildingsObs: Observable<Building[]>;
  buildingsMap: Map<number, Building>;

  constructor(private locationService: LocationService,
              private authoritiesService: AuthoritiesService,
              private authenticationService: AuthenticationService,
              private buildingsService: BuildingService) { }

  ngOnInit(): void {
    this.setupForm();

    this.authenticationService.user.subscribe(
      (next) => {
        // only set the locations and authorities if the user is authenticated
        if (next.augentID !== '') {
          this.setupLocationsAndAuthorities();
          this.setupBuildings();
        }
      }
    );
  }

  setupForm(): void {
    this.addLocationFormGroup = new FormGroup({
      name: new FormControl('', Validators.required),
      authority: new FormControl('', Validators.required),
      building: new FormControl('', Validators.required),
      numberOfSeats: new FormControl('', Validators.required),
      numberOfLockers: new FormControl('', Validators.required),
      forGroup: new FormControl(false, Validators.required),
      imageUrl: new FormControl('')
    });
  }

  prepareToAddLocation(): void {
    this.addingWasSuccess = undefined;
  }

  addNewLocation(location: Location): void {
    location.authority = this.authoritiesMap.get(Number(this.authority.value));
    location.building = this.buildingsMap.get(Number(this.building.value));

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
    this.setupLocationsAndAuthorities();
    this.setupForm();
  }

  successDeletionHandler(): void {
    this.deletionWasSuccess = true;
    this.setupLocationsAndAuthorities();
  }

  errorHandler(): void {
    this.addingWasSuccess = false;
  }

  setCurrentLocationNameToDelete(locationName: string): void {
    this.currentLocationNameToDelete = locationName;
  }

  get name(): AbstractControl { return this.addLocationFormGroup.get('name'); }
  get authority(): AbstractControl { return this.addLocationFormGroup.get('authority'); }
  get building(): AbstractControl { return this.addLocationFormGroup.get('building'); }
  get numberOfSeats(): AbstractControl { return this.addLocationFormGroup.get('numberOfSeats'); }
  get numberOfLockers(): AbstractControl { return this.addLocationFormGroup.get('numberOfLockers'); }
  get forGroup(): AbstractControl { return this.addLocationFormGroup.get('forGroup'); }
  get imageUrl(): AbstractControl { return this.addLocationFormGroup.get('imageUrl'); }

  // *******************
  // *   Auxiliaries   *
  // *******************

  /**
   * Setup the locations and authorities depending on whether or not the user is admin
   */
  setupLocationsAndAuthorities(): void {
    if (this.authenticationService.isAdmin()) {
      this.setupLocationsAndAuthoritiesAsAdmin();
    } else {
      this.setupLocationsAndAuthoritiesAsEmployee();
    }
  }

  /**
   * The user can always see all buildings.
   */
  setupBuildings(): void {
    this.buildingsObs = this.buildingsService.getAllBuildings().pipe(tap(
      next => {
        this.buildingsMap = new Map<number, Building>();
        next.forEach(value => {
          this.buildingsMap.set(value.buildingId, value);
        });
      }
    ));
  }

  /**
   * The user may manage all the locations, and create new locations for all authorities.
   */
  setupLocationsAndAuthoritiesAsAdmin(): void {
    this.locations = this.locationService.getLocations();
    this.authoritiesObs = this.authoritiesService.getAllAuthorities().pipe(tap(
      next => {
        this.authoritiesMap = new Map<number, Authority>();
        next.forEach(value => {
          this.authoritiesMap.set(value.authorityId, value);
        });
      }
    ));
  }

  /**
   * If the user is not admin, he may only manage or add locations that are in his own authorities.
   */
  setupLocationsAndAuthoritiesAsEmployee(): void {
    this.locations = this.authoritiesService
      .getLocationsInAuthoritiesOfUser(this.authenticationService.userValue().augentID);
    this.authoritiesObs = this.authoritiesService
      .getAuthoritiesOfUser(this.authenticationService.userValue().augentID).pipe(tap(
      next => {
        this.authoritiesMap = new Map<number, Authority>();
        next.forEach(value => {
          this.authoritiesMap.set(value.authorityId, value);
        });
      }
    ));
  }
}
