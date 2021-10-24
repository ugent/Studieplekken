import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Observable, of, Subject } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LocationService } from 'src/app/services/api/locations/location.service';
import { UserService } from 'src/app/services/api/users/user.service';
import { objectExists } from 'src/app/shared/GeneralFunctions';
import { Location } from 'src/app/shared/model/Location';
import { User } from 'src/app/shared/model/User';

@Component({
  selector: 'app-volunteer-management-panel',
  templateUrl: './volunteer-management-panel.component.html',
  styleUrls: ['./volunteer-management-panel.component.scss'],
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

  private modalRef: MatDialogRef<unknown>;
  collapsed = true;

  constructor(
    private locationService: LocationService,
    private modalService: MatDialog,
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
    this.modalRef = this.modalService.open(template, {panelClass: ["cs--cyan" ,"bigmodal"]});
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

    this.modalRef.close();
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
