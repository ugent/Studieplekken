import {Component, Input, OnInit} from '@angular/core';
import {Location} from '../../../../shared/model/Location';
import {FormControl, FormGroup} from '@angular/forms';
import {LocationService} from '../../../../services/api/locations/location.service';
import {Observable} from 'rxjs';
import {msToShowFeedback} from '../../../../../environments/environment';
import {LocationDetailsService} from '../../../../services/single-point-of-truth/location-details/location-details.service';
import {Authority} from '../../../../shared/model/Authority';
import {AuthoritiesService} from '../../../../services/api/authorities/authorities.service';
import {tap} from 'rxjs/operators';

@Component({
  selector: 'app-details-form',
  templateUrl: './details-form.component.html',
  styleUrls: ['./details-form.component.css']
})
export class DetailsFormComponent implements OnInit {
  @Input() location: Observable<Location>;

  authoritiesObs: Observable<Authority[]>;
  authoritiesMap: Map<number, Authority>; // map the authorityId to the Authority object

  locationForm = new FormGroup({
    name: new FormControl({value: '', disabled: true}),
    authority: new FormControl({value: '', disabled: true}),
    address: new FormControl({value: '', disabled: true}),
    numberOfSeats: new FormControl({value: '', disabled: true}),
    numberOfLockers: new FormControl({value: '', disabled: true}),
    imageUrl: new FormControl({value: '', disabled: true})
  });

  disableEditLocationButton = false;
  disableCancelLocationButton = true;
  disablePersistLocationButton = true;

  successUpdatingLocation: boolean = undefined;

  constructor(private locationService: LocationService,
              private locationDetailsService: LocationDetailsService,
              private authoritiesService: AuthoritiesService) { }

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
  }

  updateFormGroup(location: Location): void {
    this.locationForm.setValue({
      name: location.name,
      authority: location.authority.authorityId,
      address: location.address,
      numberOfSeats: location.numberOfSeats,
      numberOfLockers: location.numberOfLockers,
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

    // set the authority object based on the authorityId that is selected in the form
    to.authority = this.authorityInLocationForm;

    this.locationService.updateLocation(from.name, to).subscribe(
      () => {
        this.successHandler();

        // update the location attribute
        // this, 'loadLocation' function will perform a next() on the
        // subject behavior, which will trigger a next() on the underlying
        // observable, to which the HTML is implicitly subscribed
        this.locationDetailsService.loadLocation(to.name);
      }, () => {
        this.errorHandler();
        // load to be sure
        this.locationDetailsService.loadLocation(from.name);
      }
    );

    this.disableFormGroup();
    this.changeEnableDisableLocationDetailsFormButtons();
  }

  successHandler(): void {
    this.successUpdatingLocation = true;
    setTimeout(() => this.successUpdatingLocation = undefined, msToShowFeedback);
  }

  errorHandler(): void {
    this.successUpdatingLocation = false;
    setTimeout(() => this.successUpdatingLocation = undefined, msToShowFeedback);
  }

  get authorityInLocationForm(): Authority {
    return this.authoritiesMap.get(Number(this.locationForm.get('authority').value));
  }
}
