import { Component, OnInit, TemplateRef } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  Validators
} from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Building } from 'src/app/shared/model/Building';
import { AuthoritiesService } from '../../services/api/authorities/authorities.service';
import { BuildingService } from '../../services/api/buildings/buildings.service';
import { LocationService } from '../../services/api/locations/location.service';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { Authority } from '../../shared/model/Authority';
import { Location } from '../../shared/model/Location';
import { map } from 'rxjs/internal/operators/map';
import { Timeslot } from 'src/app/shared/model/Timeslot';
import { TimeslotsService } from 'src/app/services/api/calendar-periods/timeslot.service';
import * as moment from 'moment';

@Component({
  selector: 'app-locations-management',
  templateUrl: './locations-management.component.html',
  styleUrls: ['./locations-management.component.scss'],
})
export class LocationsManagementComponent implements OnInit {
  locations: Observable<Location[]>;
  unapprovedLocations: Observable<Location[]>;

  addLocationFormGroup: FormGroup;
  addingWasSuccess: boolean = undefined;

  currentLocationNameToDelete: string;
  currentLocationIdToDelete: number;
  currentTimeslotsToDelete: Timeslot[];
  currentReservationCount: number = undefined;
  deletionWasSuccess: boolean = undefined;

  authoritiesObs: Observable<Authority[]>;
  authoritiesMap: Map<number, Authority>;

  buildingsObs: Observable<Building[]>;
  buildingsMap: Map<number, Building>;
  editMode: boolean;
  showAddWarning = false;

  constructor(
    private locationService: LocationService,
    private authoritiesService: AuthoritiesService,
    private authenticationService: AuthenticationService,
    private buildingsService: BuildingService,
    private dialog: MatDialog,
    private calendarPeriodService: TimeslotsService
  ) { }

  ngOnInit(): void {
    this.setupForm();

    this.authenticationService.user.subscribe((next) => {
      // only set the locations and authorities if the user is authenticated
      if (next.userId !== '') {
        this.setupLocationsAndAuthorities();
        this.setupBuildings();
      }
    });
  }

  setupForm(edit: boolean = false): void {
    this.addLocationFormGroup = new FormGroup({
      locationId: new FormControl(-1, Validators.required.bind(this)),
      name: new FormControl(
        { value: '', disabled: edit },
        Validators.required.bind(this)
      ),
      authority: new FormControl('', Validators.required.bind(this)),
      building: new FormControl('', Validators.required.bind(this)),
      numberOfSeats: new FormControl('', Validators.required.bind(this)),
      forGroup: new FormControl(false, Validators.required.bind(this)),
      imageUrl: new FormControl(''),
      usesPenaltyPoints: new FormControl(false),
    });
  }

  // ********************
  // *   CRUD: Create   *
  // ********************/

  prepareToAddLocation(template: TemplateRef<unknown>): void {
    this.addingWasSuccess = undefined;
    this.dialog.open(template, { panelClass: "cs--cyan" });
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
    location.usesPenaltyPoints = Boolean(this.usesPenaltyPoints.value);
    // location.numberOfLockers = 0;

    this.addingWasSuccess = null;
    if (this.addLocationFormGroup.valid) {
      this.locationService.addLocation(location).subscribe(
        () => {
          this.successHandler();
          this.dialog.closeAll();
        },
        () => {
          this.errorHandler();
        }
      );
    }
  }

  // ********************
  // *   CRUD: Delete   *
  // ********************/

  prepareToDeleteLocation(
    location: Location,
    template: TemplateRef<unknown>
  ): void {
    this.deletionWasSuccess = undefined;
    this.currentLocationNameToDelete = location.name;
    this.currentLocationIdToDelete = location.locationId;
    this.calendarPeriodService
      .getTimeslotsOfLocation(location.locationId)
      .subscribe((next) => {
        this.currentTimeslotsToDelete = next;
        this.currentTimeslotsToDelete.sort((timeslota: Timeslot, timeslotb: Timeslot) => timeslota.getStartMoment().diff(timeslotb.getStartMoment()));
        this.dialog.open(template);
      });
  }

  isFuture(cp: Timeslot): boolean {
    return cp.getEndMoment().isAfter(moment(new Date()));
  }

  deleteLocation(): void {
    this.deletionWasSuccess = null;
    this.locationService
      .deleteLocation(this.currentLocationIdToDelete)
      .subscribe(
        () => {
          this.successDeletionHandler();
          this.currentTimeslotsToDelete = [];
          this.dialog.closeAll();
        },
        () => {
          this.deletionWasSuccess = false;
        }
      );
  }

  closeModal(): void {
    this.dialog.closeAll();
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

  // *******************
  // *   Auxiliaries   *
  // *******************

  get name(): AbstractControl {
    return this.addLocationFormGroup.get('name');
  }

  get authority(): AbstractControl {
    return this.addLocationFormGroup.get('authority');
  }

  get building(): AbstractControl {
    return this.addLocationFormGroup.get('building');
  }

  get numberOfSeats(): AbstractControl {
    return this.addLocationFormGroup.get('numberOfSeats');
  }

  get forGroup(): AbstractControl {
    return this.addLocationFormGroup.get('forGroup');
  }

  get imageUrl(): AbstractControl {
    return this.addLocationFormGroup.get('imageUrl');
  }

  get usesPenaltyPoints(): AbstractControl {
    return this.addLocationFormGroup.get('usesPenaltyPoints');

  }

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
    const allBuildings = this.buildingsService.getAllBuildings();

    // Only show the buildings the user has access to.
    this.buildingsObs = allBuildings.pipe(
      tap((next) => {
        this.buildingsMap = new Map<number, Building>();
        next.forEach((value) => {
          this.buildingsMap.set(value.buildingId, value);
        });
      })
    );
  }

  /**
   * The user may manage all the locations, and create new locations for all authorities.
   */
  setupLocationsAndAuthoritiesAsAdmin(): void {
    this.locations = this.locationService.getLocations();
    this.unapprovedLocations = this.locationService.getUnapprovedLocations();
    this.authoritiesObs = this.authoritiesService.getAllAuthorities().pipe(
      tap((next) => {
        this.authoritiesMap = new Map<number, Authority>();
        next.forEach((value) => {
          this.authoritiesMap.set(value.authorityId, value);
        });
      })
    );
  }

  /**
   * If the user is not admin, he may only manage or add locations that are in his own authorities.
   */
  setupLocationsAndAuthoritiesAsEmployee(): void {
    this.locations = this.authoritiesService.getLocationsInAuthoritiesOfUser(
      this.authenticationService.userValue().userId
    );
    this.authoritiesObs = this.authoritiesService
      .getAuthoritiesOfUser(this.authenticationService.userValue().userId)
      .pipe(
        tap((next) => {
          this.authoritiesMap = new Map<number, Authority>();
          next.forEach((value) => {
            this.authoritiesMap.set(value.authorityId, value);
          });
        })
      );
  }

  prepareToApproveLocation(
    location: Location,
    template: TemplateRef<unknown>
  ): void {
    this.setupForm();
    this.editMode = true;
    this.addLocationFormGroup.get('locationId').setValue(location.locationId);
    this.addLocationFormGroup.get('name').setValue(location.name);
    this.addLocationFormGroup
      .get('building')
      .setValue(location.building.buildingId);
    this.addLocationFormGroup
      .get('authority')
      .setValue(location.authority.authorityId);
    this.addLocationFormGroup
      .get('numberOfSeats')
      .setValue(location.numberOfSeats);
    this.addLocationFormGroup.get('forGroup').setValue(location.forGroup);
    this.addLocationFormGroup.get('imageUrl').setValue(location.imageUrl);
    this.dialog.open(template);
  }

  approveLocation(location: Location): void {
    location.authority = this.authoritiesMap.get(Number(this.authority.value));
    location.building = this.buildingsMap.get(Number(this.building.value));

    this.addingWasSuccess = null;
    if (this.addLocationFormGroup.valid) {
      this.locationService.approveLocation(location, true).subscribe(
        () => {
          this.successHandler();
          this.dialog.closeAll();
        },
        () => {
          this.errorHandler();
        }
      );
    }
  }
}
