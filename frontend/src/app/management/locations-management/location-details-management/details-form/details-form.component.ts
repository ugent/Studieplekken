import {Component, Input, OnInit} from '@angular/core';
import {Location, LocationConstructor} from '../../../../shared/model/Location';
import {AbstractControl, FormControl, FormGroup} from '@angular/forms';
import {LocationService} from '../../../../services/api/locations/location.service';
import {Observable} from 'rxjs';
import {
  LocationDetailsService
} from '../../../../services/single-point-of-truth/location-details/location-details.service';
import {Authority} from '../../../../shared/model/Authority';
import {AuthoritiesService} from '../../../../services/api/authorities/authorities.service';
import {tap} from 'rxjs/operators';
import {Building} from 'src/app/shared/model/Building';
import {BuildingService} from 'src/app/services/api/buildings/buildings.service';
import {msToShowFeedback} from '../../../../app.constants';
import {
  ApplicationTypeFunctionalityService
} from 'src/app/services/functionality/application-type/application-type-functionality.service';
import {AuthenticationService} from '../../../../services/authentication/authentication.service';

@Component({
  selector: 'app-details-form',
  templateUrl: './details-form.component.html',
  styleUrls: ['./details-form.component.css']
})
export class DetailsFormComponent implements OnInit {
  @Input() location: Observable<Location>;
  locationObj: Location;

  authoritiesObs: Observable<Authority[]>;
  authoritiesMap: Map<number, Authority>; // map the authorityId to the Authority object

  buildingsObs: Observable<Building[]>;
  buildingsMap: Map<number, Building>; // map the buildingId to the Building object

  locationForm = new FormGroup({
    name: new FormControl({value: '', disabled: true}),
    authority: new FormControl({value: '', disabled: true}),
    building: new FormControl({value: '', disabled: true}),
    numberOfSeats: new FormControl({value: '', disabled: true}),
    numberOfLockers: new FormControl({value: '', disabled: true}),
    forGroup: new FormControl({value: '', disabled: true}),
    imageUrl: new FormControl({value: '', disabled: true})
  });

  disableEditLocationButton = false;
  disableCancelLocationButton = true;
  disablePersistLocationButton = true;

  successUpdatingLocation: boolean = undefined;
  showLockersManagement: boolean;

  canEditLocationSeats = this.authenticationService.isAdmin();

  constructor(private locationService: LocationService,
              private locationDetailsService: LocationDetailsService,
              private authoritiesService: AuthoritiesService,
              private buildingsService: BuildingService,
              private functionalityService: ApplicationTypeFunctionalityService,
              private authenticationService: AuthenticationService) {
  }

  get authorityInLocationForm(): Authority {
    return this.authoritiesMap.get(Number(this.locationForm.get('authority').value));
  }

  get buildingInLocationForm(): Building {
    return this.buildingsMap.get(Number(this.locationForm.get('building').value));
  }

  ngOnInit(): void {
    // if the location has been retrieved, populate the form group
    this.location.subscribe(next => {
      this.updateFormGroup(next);
      this.locationObj = next;
    });

    // make sure that the correct authorities are retrieved
    if (this.authenticationService.isAdmin()) {
      this.authoritiesObs = this.authoritiesService.getAllAuthorities();
    } else {
      this.authoritiesObs = this.authoritiesService.getAuthoritiesOfUser(this.authenticationService.userValue().augentID);
    }

    // the authoritiesObs is used in the form, asynchronously
    // the authoritiesMap is used to set the authority object
    // when the user wants to change the authority
    this.authoritiesObs.subscribe(
      next => {
        this.authoritiesMap = new Map<number, Authority>();
        next.forEach(value => {
          this.authoritiesMap.set(value.authorityId, value);
        });
      }
    );

    this.buildingsObs = this.buildingsService.getAllBuildings().pipe(tap(
      next => {
        this.buildingsMap = new Map<number, Building>();
        next.forEach(value => {
          this.buildingsMap.set(value.buildingId, value);
        });
      }
    ));

    this.showLockersManagement = this.functionalityService.showLockersManagementFunctionality();
  }

  updateFormGroup(location: Location): void {
    this.locationForm.setValue({
      name: location.name,
      authority: location.authority.authorityId,
      building: location.building.buildingId,
      numberOfSeats: location.numberOfSeats,
      numberOfLockers: 0,
      forGroup: location.forGroup,
      imageUrl: location.imageUrl
    });
  }

  disableFormGroup(): void {
    this.locationForm.disable();
  }

  enableFormGroup(): void {
    this.locationForm.enable();
  }

  changeEnableDisableLocationDetailsFormButtons(): void {
    this.disableEditLocationButton = !this.disableEditLocationButton;
    this.disableCancelLocationButton = !this.disableCancelLocationButton;
    this.disablePersistLocationButton = !this.disablePersistLocationButton;
  }

  editLocationDetailsButtonClick(): void {
    this.enableFormGroup();

    // only the admin can change the number of seats of a location
    if (!this.canEditLocationSeats) {
      this.numberOfSeats.disable();
    }

    this.changeEnableDisableLocationDetailsFormButtons();
  }

  cancelLocationDetailsButtonClick(location: Location): void {
    this.disableFormGroup();
    this.updateFormGroup(location);
    this.changeEnableDisableLocationDetailsFormButtons();
    this.successUpdatingLocation = undefined;
  }

  persistLocationDetailsButtonClick(): void {
    this.successUpdatingLocation = null; // show 'loading' message

    const from: Location = this.locationObj;
    const to: Location = this.locationInForm;

    this.locationService.updateLocation(from.locationId, to).subscribe(
      () => {
        this.successHandler();

        // update the location attribute: 'loadLocation' will perform a next()
        // on the subject behavior, which will trigger a next() on the underlying
        // observable, to which the HTML is implicitly subscribed through the
        // *ngIf="location | async as location" in the outer div of the template.
        this.locationDetailsService.loadLocation(to.locationId);
      }, () => {
        this.errorHandler();
        // reload the location to be sure
        this.locationDetailsService.loadLocation(from.locationId);
      }
    );

    this.disableFormGroup();
    this.changeEnableDisableLocationDetailsFormButtons();
  }

  // /******************
  // *   AUXILIARIES   *
  // *******************/

  get name(): AbstractControl { return this.locationForm.get('name'); }
  get authority(): AbstractControl { return this.locationForm.get('authority'); }
  get building(): AbstractControl { return this.locationForm.get('building'); }
  get numberOfSeats(): AbstractControl { return this.locationForm.get('numberOfSeats'); }
  get numberOfLockers(): AbstractControl { return this.locationForm.get('numberOfLockers'); }
  get forGroup(): AbstractControl { return this.locationForm.get('forGroup'); }
  get imageUrl(): AbstractControl { return this.locationForm.get('imageUrl'); }

  get locationInForm(): Location {
    const location: Location = LocationConstructor.newFromObj(this.locationObj);

    location.name = String(this.name.value);
    location.authority = this.authorityInLocationForm;
    location.building = this.buildingInLocationForm;

    // seats must be enabled to read data
    this.numberOfSeats.enable();
    location.numberOfSeats = Number(this.numberOfSeats.value);
    // disable the numberOfSeats again if user is not admin
    if (!this.canEditLocationSeats) {
      this.numberOfSeats.disable();
    }

    location.numberOfLockers = Number(this.numberOfLockers.value);
    location.forGroup = Boolean(this.forGroup.value);
    location.imageUrl = String(this.imageUrl.value);

    return location;
  }

  successHandler(): void {
    this.successUpdatingLocation = true;
    setTimeout(() => this.successUpdatingLocation = undefined, msToShowFeedback);
  }

  errorHandler(): void {
    this.successUpdatingLocation = false;
    setTimeout(() => this.successUpdatingLocation = undefined, msToShowFeedback);
  }
}
