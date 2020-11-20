import {Component, OnInit, TemplateRef} from '@angular/core';
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
import { BsModalService } from 'ngx-bootstrap/modal';
import { CalendarPeriodsService } from 'src/app/services/api/calendar-periods/calendar-periods.service';
import { CalendarPeriod } from 'src/app/shared/model/CalendarPeriod';
import { LocationReservationsService } from 'src/app/services/api/location-reservations/location-reservations.service';

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
  unapprovedLocations: Observable<Location[]>;

  addLocationFormGroup: FormGroup;
  addingWasSuccess: boolean = undefined;

  currentLocationNameToDelete: string;
  currentCalendarPeriodsToDelete: CalendarPeriod[];
  currentReservationCount: number = undefined;
  deletionWasSuccess: boolean = undefined;

  authoritiesObs: Observable<Authority[]>;
  authoritiesMap: Map<number, Authority>;

  buildingsObs: Observable<Building[]>;
  buildingsMap: Map<number, Building>;
  editMode: boolean;
  showAddWarning: boolean = false;
  locationToAdd: Location;

  constructor(private locationService: LocationService,
              private authoritiesService: AuthoritiesService,
              private authenticationService: AuthenticationService,
              private buildingsService: BuildingService,
              private modalService: BsModalService,
              private calendarPeriodService: CalendarPeriodsService) { }

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

  setupForm(edit: boolean = false): void {
    this.addLocationFormGroup = new FormGroup({
      name: new FormControl({value: '', disabled: edit}, Validators.required),
      authority: new FormControl('', Validators.required),
      building: new FormControl('', Validators.required),
      numberOfSeats: new FormControl('', Validators.required),
      forGroup: new FormControl(false, Validators.required),
      imageUrl: new FormControl('')
    });
  }

  // ********************
  // *   CRUD: Create   *
  // ********************/

  prepareToAddLocation(template: TemplateRef<any>): void {
    this.addingWasSuccess = undefined;
    this.modalService.show(template);
    this.editMode = false;
  }

  addNewLocation(location: Location): void {
    if (!this.showAddWarning) {
      this.showAddWarning = true;
      return;
    }
    this.showAddWarning = false;


    location.authority = this.authoritiesMap.get(Number(this.authority.value));
    location.building = this.buildingsMap.get(Number(this.building.value));
    location.numberOfLockers = 0;

    this.addingWasSuccess = null;
    if (this.addLocationFormGroup.valid) {
      this.locationService.addLocation(location).subscribe(
        () => {
          this.successHandler();
          this.modalService.hide();
        }, () => {
          this.errorHandler();
        }
      );
    }
  }

  // ********************
  // *   CRUD: Delete   *
  // ********************/

  prepareToDeleteLocation(location: Location, template: TemplateRef<any>): void {
    this.deletionWasSuccess = undefined;
    this.currentLocationNameToDelete = location.name;
    this.calendarPeriodService.getCalendarPeriodsOfLocation(location.name).subscribe((next) => {
      this.currentCalendarPeriodsToDelete = next;
      this.modalService.show(template);
    });
  }

  deleteLocation(): void {
    this.deletionWasSuccess = null;
    this.locationService.deleteLocation(this.currentLocationNameToDelete).subscribe(
      () => {
        this.successDeletionHandler();
        this.currentCalendarPeriodsToDelete = [];
        this.modalService.hide();
      }, () => {
        this.deletionWasSuccess = false;
      }
    );
  }

  clearForm(): void {
    this.setupForm();
  }

  validForm(): boolean {
    return this.addLocationFormGroup.valid;
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

  // *******************
  // *   Auxiliaries   *
  // *******************

  get name(): AbstractControl { return this.addLocationFormGroup.get('name'); }
  get authority(): AbstractControl { return this.addLocationFormGroup.get('authority'); }
  get building(): AbstractControl { return this.addLocationFormGroup.get('building'); }
  get numberOfSeats(): AbstractControl { return this.addLocationFormGroup.get('numberOfSeats'); }
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
    this.unapprovedLocations = this.locationService.getUnapprovedLocations();
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

  closeModal(): void {
    this.modalService.hide();
  }

  prepareToApproveLocation(location: Location, template: TemplateRef<any>): void {
    this.setupForm();
    this.editMode = true;
    this.addLocationFormGroup.get('name').setValue(location.name);
    this.addLocationFormGroup.get('building').setValue(location.building.buildingId);
    this.addLocationFormGroup.get('authority').setValue(location.authority.authorityId);
    this.addLocationFormGroup.get('numberOfSeats').setValue(location.numberOfSeats);
    this.addLocationFormGroup.get('forGroup').setValue(location.forGroup);
    this.addLocationFormGroup.get('imageUrl').setValue(location.imageUrl);
    this.modalService.show(template);
  }

  approveLocation(location: Location): void {
    location.authority = this.authoritiesMap.get(Number(this.authority.value));
    location.building = this.buildingsMap.get(Number(this.building.value));

    this.addingWasSuccess = null;
    if (this.addLocationFormGroup.valid) {
      this.locationService.approveLocation(location, true).subscribe(
        () => {
          this.successHandler();
          this.modalService.hide();
        }, () => {
          this.errorHandler();
        }
      );
    }
  }
}
