import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { Observable, of, Subject } from 'rxjs';
import { LocationService } from 'src/app/services/api/locations/location.service';
import { User } from 'src/app/shared/model/User';
import { Location } from 'src/app/shared/model/Location';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { FormGroup, FormControl } from '@angular/forms';
import { objectExists } from 'src/app/shared/GeneralFunctions';
import { UserService } from 'src/app/services/api/users/user.service';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-volunteer-management-panel',
  templateUrl: './volunteer-management-panel.component.html',
  styleUrls: ['./volunteer-management-panel.component.css'],
})
export class VolunteerManagementPanelComponent implements OnInit {
  @Input() location: Location;

  volunteerObs: Observable<User[]>;
  errorSubject: Subject<boolean> = new Subject<boolean>();

  formGroup = new FormGroup({
    firstName: new FormControl(''),
    lastName: new FormControl(''),
  });
  neverSearched = true;
  filteredUsers: Observable<User[]>;

  private modalRef: BsModalRef;
  collapsed = true;

  constructor(
    private locationService: LocationService,
    private modalService: BsModalService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.volunteerObs = this.locationService
      .getVolunteers(this.location.locationId)
      .pipe(
        catchError((err) => {
          console.error(err);
          this.errorSubject.next(true);
          return of<User[]>(null);
        })
      );
  }

  showEmpty(volunteers: User[]): boolean {
    return volunteers.length <= 0;
  }

  showAdd(template: TemplateRef<unknown>): void {
    this.modalRef = this.modalService.show(
      template,
      Object.assign({}, { class: 'modal-lg' })
    );
  }

  enableSearchButton(value: { firstName: string; lastName: string }): boolean {
    return (
      (objectExists(value.firstName) && value.firstName.trim().length > 0) ||
      (objectExists(value.lastName) && value.lastName.trim().length > 0)
    );
  }

  /*
   * If any of the input fields are not empty without trimming, enable the 'search' button
   */
  enableClearButton(value: { firstName: string; lastName: string }): boolean {
    return (
      (objectExists(value.firstName) && value.firstName.length > 0) ||
      (objectExists(value.lastName) && value.lastName.length > 0)
    );
  }

  clearFilterInput(): void {
    this.formGroup.setValue({
      firstName: '',
      lastName: '',
    });
    this.formGroup.enable();

    this.filteredUsers = of<User[]>([]);
  }

  submitSearch(value: {
    firstName: string;
    lastName: string;
    barcode: string;
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

  addVolunteer(user: User): void {
    this.locationService
      .addVolunteer(this.location.locationId, user.userId)
      .subscribe(
        () =>
          (this.volunteerObs = this.locationService.getVolunteers(
            this.location.locationId
          ))
      );

    this.modalRef.hide();
    this.collapsed = false;
  }

  deleteVolunteer(user: User): void {
    this.locationService
      .deleteVolunteer(this.location.locationId, user.userId)
      .subscribe(
        () =>
          (this.volunteerObs = this.locationService.getVolunteers(
            this.location.locationId
          ))
      );

    this.collapsed = false;
  }
}
