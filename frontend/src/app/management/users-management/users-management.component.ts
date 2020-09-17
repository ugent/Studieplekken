import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {objectExists} from '../../shared/GeneralFunctions';
import {Observable} from 'rxjs';
import {User} from '../../shared/model/User';
import {UserService} from '../../services/api/users/user.service';
import {HttpErrorResponse} from '@angular/common/http';
import {map} from 'rxjs/operators';
import {ApplicationTypeFunctionalityService} from '../../services/functionality/application-type/application-type-functionality.service';

@Component({
  selector: 'app-users-management',
  templateUrl: './users-management.component.html',
  styleUrls: ['./users-management.component.css']
})
export class UsersManagementComponent implements OnInit {
  formGroup = new FormGroup({
    firstName: new FormControl(''),
    lastName: new FormControl(''),
    barcode: new FormControl('')
  });

  neverSearched = true;
  filteredUsers: Observable<User[]>;

  // the 'booleanId' is used in this.handler(number)
  errorOnRetrievingFilteredUsers = false; // booleanId = 0
  noUserWithBarcodeHasBeenFound = false;  // booleanId = 1

  showPenaltyPoints: boolean;

  constructor(private userService: UserService,
              private functionalityService: ApplicationTypeFunctionalityService) { }

  ngOnInit(): void {
    this.showPenaltyPoints = this.functionalityService.showPenaltyFunctionality();
  }

  submitSearch(value: {firstName: string, lastName: string, barcode: string}): void {
    this.neverSearched = null;

    if ((objectExists(value.firstName) && value.firstName.trim().length > 0) &&
      (objectExists(value.lastName) && value.lastName.trim().length > 0)) {
      // search by first and last name
      this.filteredUsers = this.userService.getUsersByFirstAndLastName(value.firstName, value.lastName);
    } else if (objectExists(value.firstName) && value.firstName.trim().length > 0) {
      // search by first name
      this.filteredUsers = this.userService.getUsersByFirstName(value.firstName);
    } else if (objectExists(value.lastName) && value.lastName.trim().length > 0) {
      // search by last name
      this.filteredUsers = this.userService.getUsersByLastName(value.lastName);
    } else {
      // else, the only option left is searching by the barcode
      this.filteredUsers = this.userService.getUserByBarcode(value.barcode)
        .pipe(map<User, User[]>(((next) => Array.of(next))));
    }

    this.filteredUsers.subscribe(
      () => {
        this.successHandler();
      }, (error) => {
        this.errorHandler(error);
      }
    );
  }

  filterFormChanged(value: {firstName: string, lastName: string, barcode: string}): void {
    const nameFieldsEnabled = this.enableFirstAndLastNameInputFields(value);
    nameFieldsEnabled ? this.formGroup.get('firstName').enable() : this.formGroup.get('firstName').disable();
    nameFieldsEnabled ? this.formGroup.get('lastName').enable() : this.formGroup.get('lastName').disable();

    const barcodeFieldEnabled = this.enableBarcodeInputField(value);
    barcodeFieldEnabled ? this.formGroup.get('barcode').enable() : this.formGroup.get('barcode').disable();
  }

  /*
   * If there is any value in the barcode input field, disable the first and lastname input fields
   */
  enableFirstAndLastNameInputFields(value: {firstName: string, lastName: string, barcode: string}): boolean {
    return !objectExists(value.barcode) || value.barcode.trim().length === 0;
  }

  enableBarcodeInputField(value: {firstName: string, lastName: string, barcode: string}): boolean {
    return (!objectExists(value.firstName) || value.firstName.trim().length === 0) &&
      (!objectExists(value.lastName) || value.lastName.trim().length === 0);
  }

  /*
   * If any of the input fields are not empty after trimming, enable the 'search' button
   */
  enableSearchButton(value: {firstName: string, lastName: string, barcode: string}): boolean {
    return (objectExists(value.firstName) && value.firstName.trim().length > 0) ||
      (objectExists(value.lastName) && value.lastName.trim().length > 0) ||
      (objectExists(value.barcode) && value.barcode.trim().length > 0);
  }

  /*
   * If any of the input fields are not empty without trimming, enable the 'search' button
   */
  enableClearButton(value: {firstName: string, lastName: string, barcode: string}): boolean {
    return (objectExists(value.firstName) && value.firstName.length > 0) ||
      (objectExists(value.lastName) && value.lastName.length > 0) ||
      (objectExists(value.barcode) && value.barcode.length > 0);
  }

  clearFilterInput(): void {
    this.formGroup.setValue({
      firstName: '',
      lastName: '',
      barcode: ''
    });
    this.formGroup.enable();
  }

  successHandler(): void {
    this.handler(-1);
  }

  errorHandler(error: HttpErrorResponse): void {
    // 417 = HTTP EXPECTATION FAILED -> no user with given barcode has been found
    if (error.status === 417) {
      this.handler(1);
    } else {
      this.handler(0);
    }
  }

  handler(booleanId: number): void {
    this.neverSearched = false;
    this.errorOnRetrievingFilteredUsers = false;
    this.noUserWithBarcodeHasBeenFound = false;

    if (booleanId === 0) {
      this.errorOnRetrievingFilteredUsers = true;
    } else if (booleanId === 1) {
      this.noUserWithBarcodeHasBeenFound = true;
    }
  }
}
