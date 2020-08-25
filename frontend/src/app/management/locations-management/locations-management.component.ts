import {Component, OnInit} from '@angular/core';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../shared/animations/RowAnimation';
import {Observable} from 'rxjs';
import {Location} from '../../shared/model/Location';
import {LocationService} from '../../services/api/locations/location.service';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';
import {msToShowFeedback} from "../../../environments/environment";

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
  showSuccess = false;
  showError = false;
  showCloseButton = false;

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

  submitNewLocation(location: Location) {
    if (this.addLocationFormGroup.valid) {
      this.locationService.addLocation(location).subscribe(
        () => {
          this.successHandler();
        }, () => {
          this.errorHandler();
        }
      )
    }
  }

  clearForm(): void {
    this.setupForm();
  }

  validForm(): boolean {
    return this.addLocationFormGroup.valid;
  }

  successHandler(): void {
    this.showSuccess = true;
    setTimeout(() => this.showSuccess = false, msToShowFeedback);
    this.locations = this.locationService.getLocations();
    this.setupForm();
  }

  errorHandler(): void {
    this.showError = true;
    setTimeout(() => this.showError = false, msToShowFeedback);
  }

  get name(): AbstractControl { return this.addLocationFormGroup.get('name'); }
  get address(): AbstractControl { return this.addLocationFormGroup.get('address'); }
  get numberOfSeats(): AbstractControl { return this.addLocationFormGroup.get('numberOfSeats'); }
  get numberOfLockers(): AbstractControl { return this.addLocationFormGroup.get('numberOfLockers'); }
  get imageUrl(): AbstractControl { return this.addLocationFormGroup.get('imageUrl'); }
}
