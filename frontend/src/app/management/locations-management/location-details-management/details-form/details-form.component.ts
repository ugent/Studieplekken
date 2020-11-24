import {Component, Input, OnInit} from '@angular/core';
import {Location} from '../../../../shared/model/Location';
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
    });

    // the authoritiesObs is used in the form, asynchronously
    // the authoritiesMap is used to set the authority object
    // when the user wants to change the authority
    this.authoritiesObs = this.authoritiesService.getAllAuthorities().pipe(tap(
      next => {
        this.authoritiesMap = new Map<number, Authority>();
        next.forEach(value => {
          this.authoritiesMap.set(value.authorityId, value);
        });
      }
    ));

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
    if (!this.authenticationService.isAdmin()) {
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

  persistLocationDetailsButtonClick(from: Location, to: Location): void {
    this.successUpdatingLocation = null; // show 'loading' message

    // The management of the descriptions is done in separate panel-groups.
    // Therefore, we copy these attributes here.
    to.descriptionDutch = from.descriptionDutch;
    to.descriptionEnglish = from.descriptionEnglish;

    // set the authority object based on the authorityId that is selected in the form
    to.authority = this.authorityInLocationForm;
    to.building = this.buildingInLocationForm;

    this.locationService.updateLocation(from.name, to).subscribe(
      () => {
        this.successHandler();

        // update the location attribute: 'loadLocation' will perform a next()
        // on the subject behavior, which will trigger a next() on the underlying
        // observable, to which the HTML is implicitly subscribed through the
        // *ngIf="location | async as location" in the outer div of the template.
        this.locationDetailsService.loadLocation(to.name);
      }, () => {
        this.errorHandler();
        // reload the location to be sure
        this.locationDetailsService.loadLocation(from.name);
      }
    );

    this.disableFormGroup();
    this.changeEnableDisableLocationDetailsFormButtons();
  }

  // /******************
  // *   AUXILIARIES   *
  // *******************/

  get numberOfSeats(): AbstractControl { return this.locationForm.get('numberOfSeats'); }

  successHandler(): void {
    this.successUpdatingLocation = true;
    setTimeout(() => this.successUpdatingLocation = undefined, msToShowFeedback);
  }

  errorHandler(): void {
    this.successUpdatingLocation = false;
    setTimeout(() => this.successUpdatingLocation = undefined, msToShowFeedback);
  }
}
