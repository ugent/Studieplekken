import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { Observable } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { UserService } from 'src/app/services/api/users/user.service';
import { objectExists } from '@/util/GeneralFunctions';
import { User } from '@/model/User';

@Component({
  selector: 'app-search-user-form',
  templateUrl: './search-user-form.component.html',
  styleUrls: ['./search-user-form.component.scss']
})
export class SearchUserFormComponent implements OnInit {
  formGroup = new UntypedFormGroup({
    firstName: new UntypedFormControl(''),
    lastName: new UntypedFormControl(''),
    barcode: new UntypedFormControl(''),
  });

  public neverSearched: boolean = true;
  public error: any;

  @Output() filteredUsers = new EventEmitter<User[]>();

  constructor(private userService: UserService) { }

  ngOnInit(): void {
  }

  filterFormChanged(value: {
    firstName: string;
    lastName: string;
    barcode: string;
  }): void {
    const nameFieldsEnabled = this.enableFirstAndLastNameInputFields(value);
    nameFieldsEnabled
      ? this.formGroup.get('firstName').enable()
      : this.formGroup.get('firstName').disable();
    nameFieldsEnabled
      ? this.formGroup.get('lastName').enable()
      : this.formGroup.get('lastName').disable();

    const barcodeFieldEnabled = this.enableBarcodeInputField(value);
    barcodeFieldEnabled
      ? this.formGroup.get('barcode').enable()
      : this.formGroup.get('barcode').disable();
  }

  /*
 * If there is any value in the barcode input field, disable the first and lastname input fields
 */
  enableFirstAndLastNameInputFields(value: {
    firstName: string;
    lastName: string;
    barcode: string;
  }): boolean {
    return !objectExists(value.barcode) || value.barcode.trim().length === 0;
  }

  enableBarcodeInputField(value: {
    firstName: string;
    lastName: string;
    barcode: string;
  }): boolean {
    return (
      (!objectExists(value.firstName) || value.firstName.trim().length === 0) &&
      (!objectExists(value.lastName) || value.lastName.trim().length === 0)
    );
  }

  /*
   * If any of the input fields are not empty after trimming, enable the 'search' button
   */
  enableSearchButton(value: {
    firstName: string;
    lastName: string;
    barcode: string;
  }): boolean {
    return (
      (objectExists(value.firstName) && value.firstName.trim().length > 0) ||
      (objectExists(value.lastName) && value.lastName.trim().length > 0) ||
      (objectExists(value.barcode) && value.barcode.trim().length > 0)
    );
  }

  clearFilterInput(): void {
    this.formGroup.setValue({
      firstName: '',
      lastName: '',
      barcode: '',
    });
    this.formGroup.enable();
  }

  submitSearch(value: {
    firstName: string;
    lastName: string;
    barcode: string;
  }): void {
    this.neverSearched = null;

    let filteredUsers: Observable<User[]>;

    if (
      objectExists(value.firstName) &&
      value.firstName.trim().length > 0 &&
      objectExists(value.lastName) &&
      value.lastName.trim().length > 0
    ) {
      // search by first and last name
      filteredUsers = this.userService.getUsersByFirstAndLastName(
        value.firstName,
        value.lastName
      );
    } else if (
      objectExists(value.firstName) &&
      value.firstName.trim().length > 0
    ) {
      // search by first name
      filteredUsers = this.userService.getUsersByFirstName(
        value.firstName
      );
    } else if (
      objectExists(value.lastName) &&
      value.lastName.trim().length > 0
    ) {
      // search by last name
      filteredUsers = this.userService.getUsersByLastName(value.lastName);
    } else {
      // else, the only option left is searching by the barcode
      filteredUsers = this.userService
        .getUserByBarcode(value.barcode)
        .pipe(
          map<User, User[]>((next) => Array.of(next))
        );
    }

    filteredUsers
    .pipe(
      tap(_ => this.neverSearched = false),
      catchError(e => {this.error = e; throw e})
      )
    .subscribe(u => this.filteredUsers.next(u));
  }



}
