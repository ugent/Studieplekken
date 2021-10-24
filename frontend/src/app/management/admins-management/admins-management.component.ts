import { Component, OnInit, TemplateRef } from '@angular/core';
import { objectExists } from '../../shared/GeneralFunctions';
import { Observable, of } from 'rxjs';
import { User, UserConstructor } from '../../shared/model/User';
import { UserService } from '../../services/api/users/user.service';
import { catchError, tap } from 'rxjs/operators';
import { ApplicationTypeFunctionalityService } from '../../services/functionality/application-type/application-type-functionality.service';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-admins-management',
  templateUrl: './admins-management.component.html',
  styleUrls: ['./admins-management.component.scss'],
})
export class AdminsManagementComponent implements OnInit {
  loading = true;
  adminsObs: Observable<User[]>;

  formGroup = new FormGroup({
    firstName: new FormControl(''),
    lastName: new FormControl(''),
  });
  neverSearched = true;
  filteredUsers: Observable<User[]>;

  errorOnRetrievingAdmins = false; // booleanId = 0

  showPenaltyPoints: boolean;

  constructor(
    private userService: UserService,
    private functionalityService: ApplicationTypeFunctionalityService,
    private modalService: MatDialog,
  ) {
  }

  ngOnInit(): void {
    this.showPenaltyPoints = this.functionalityService.showPenaltyFunctionality();
    this.adminsObs = this.userService.getAdmins().pipe(
      tap(() => (this.loading = false)),
      catchError((e) => {
        this.errorOnRetrievingAdmins = !!e;
        return of<User[]>([]);
      })
    );
  }

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

  /*
   * If any of the input fields are not empty without trimming, enable the 'search' button
   */
  enableClearButton(value: {
    firstName: string;
    lastName: string;
    barcode: string;
  }): boolean {
    return (
      (objectExists(value.firstName) && value.firstName.length > 0) ||
      (objectExists(value.lastName) && value.lastName.length > 0) ||
      (objectExists(value.barcode) && value.barcode.length > 0)
    );
  }

  showAdd(template: TemplateRef<unknown>): void {
    this.modalService.open(template, {panelClass: ["cs--cyan" ,"bigmodal"]});
  }

  submitSearch(value: {
    firstName: string;
    lastName: string;
  }): void {
    this.neverSearched = null;
    if (
      objectExists(value.firstName) &&
      value.firstName.trim().length > 0 &&
      objectExists(value.lastName) &&
      value.lastName.trim().length > 0
    ) {
      // search by first and last name
      this.filteredUsers = this.userService.getUsersByFirstAndLastName(
        value.firstName,
        value.lastName
      );
    } else if (
      objectExists(value.firstName) &&
      value.firstName.trim().length > 0
    ) {
      // search by first name
      this.filteredUsers = this.userService.getUsersByFirstName(
        value.firstName
      );
    } else if (
      objectExists(value.lastName) &&
      value.lastName.trim().length > 0
    ) {
      // search by last name
      this.filteredUsers = this.userService.getUsersByLastName(value.lastName);
    }
  }

  clearFilterInput(): void {
    this.formGroup.setValue({
      firstName: '',
      lastName: '',
    });
    this.formGroup.enable();

    this.filteredUsers = of<User[]>([]);
  }

  addAdmin(user: User): void {
    const clone = UserConstructor.newFromObj(user);
    clone.admin = true;
    this.userService
      .updateUser(user.userId, clone)
      .subscribe(
        () =>
          (this.adminsObs = this.userService.getAdmins().pipe(
            tap(() => (this.loading = false)),
            catchError((e) => {
              this.errorOnRetrievingAdmins = !!e;
              return of<User[]>([]);
            })
          ))
      );

    this.modalService.closeAll();
  }
}
