import {Component, Input, OnInit} from '@angular/core';
import {Location, LocationConstructor} from '../../../../shared/model/Location';
import {FormControl, FormGroup} from '@angular/forms';
import {LocationService} from '../../../../services/api/locations/location.service';
import {Observable} from 'rxjs';
import {Router} from '@angular/router';
import {msToShowFeedback} from "../../../../../environments/environment";

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
              private router: Router) { }

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

  persistLocationDetailsButtonClick(from: Location, to: {name: string, address: string, numberOfSeats: number,
    numberOfLockers: number, imageUrl: string}): void {
    const updatedLocation: Location = LocationConstructor.new();
    updatedLocation.name = to.name;
    updatedLocation.address = to.address;
    updatedLocation.numberOfSeats = to.numberOfSeats;
    updatedLocation.numberOfLockers = to.numberOfLockers;
    updatedLocation.imageUrl = to.imageUrl;

    this.successUpdatingLocation = null; // show 'loading' message
    this.locationService.updateLocation(from.name, updatedLocation).subscribe(
      () => {
        this.successHandler();

        if (from.name !== to.name) {
          this.router.navigate(['/management/locations/' + to.name]).catch();
        }

        // update the location attribute
        this.location = this.locationService.getLocation(updatedLocation.name);
      }, () => {
        this.errorHandler();
      }
    );

    this.disableFormGroup();
    this.updateFormGroup(updatedLocation);
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
