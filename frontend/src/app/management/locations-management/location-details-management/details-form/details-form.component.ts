import {Component, Input, OnInit} from '@angular/core';
import {Location} from '../../../../shared/model/Location';
import {FormControl, FormGroup} from '@angular/forms';
import {LocationService} from '../../../../services/api/locations/location.service';
import {Observable} from 'rxjs';
import {msToShowFeedback} from '../../../../../environments/environment';
// @ts-ignore
import {LocationDetailsService} from '../../../../services/single-point-of-truth/location-details/location-details.service';

@Component({
  selector: 'app-details-form',
  templateUrl: './details-form.component.html',
  styleUrls: ['./details-form.component.css']
})
export class DetailsFormComponent implements OnInit {
  @Input() location: Observable<Location>;

  locationForm = new FormGroup({
    name: new FormControl({value: '', disabled: true}),
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
              private locationDetailsService: LocationDetailsService) { }

  ngOnInit(): void {
    // if the location has been retrieved, populate the form group
    this.location.subscribe(next => {
      this.updateFormGroup(next);
    });
  }

  updateFormGroup(location: Location): void {
    this.locationForm.setValue({
      name: location.name,
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
  }

  persistLocationDetailsButtonClick(from: Location, to: Location): void {
    this.successUpdatingLocation = null; // show 'loading' message

    // The management of the authorities and descriptions are done in
    // separate panel-groups. Therefore, we copy these attributes.
    to.authority = from.authority;
    to.descriptionDutch = from.descriptionDutch;
    to.descriptionEnglish = from.descriptionEnglish;

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

  successHandler(): void {
    this.successUpdatingLocation = true;
    setTimeout(() => this.successUpdatingLocation = undefined, msToShowFeedback);
  }

  errorHandler(): void {
    this.successUpdatingLocation = false;
    setTimeout(() => this.successUpdatingLocation = undefined, msToShowFeedback);
  }
}
