import { Component, OnInit } from '@angular/core';
import { objectExists } from '../../shared/GeneralFunctions';
import { Observable, of } from 'rxjs';
import { User } from '../../shared/model/User';
import { UserService } from '../../services/api/users/user.service';
import { catchError, tap } from 'rxjs/operators';
import { ApplicationTypeFunctionalityService } from '../../services/functionality/application-type/application-type-functionality.service';

@Component({
  selector: 'app-admins-management',
  templateUrl: './admins-management.component.html',
  styleUrls: ['./admins-management.component.css'],
})
export class AdminsManagementComponent implements OnInit {
  loading = true;
  filteredUsers: Observable<User[]>;

  errorOnRetrievingAdmins = false; // booleanId = 0

  showPenaltyPoints: boolean;

  constructor(
    private userService: UserService,
    private functionalityService: ApplicationTypeFunctionalityService
  ) {}

  ngOnInit(): void {
    this.showPenaltyPoints = this.functionalityService.showPenaltyFunctionality();
    this.filteredUsers = this.userService.getAdmins().pipe(
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
}
