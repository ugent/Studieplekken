import { Component, OnInit } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Location } from '../../../extensions/model/Location';
import { UserService } from '../../../extensions/services/api/users/user.service';
import { AuthenticationService } from '../../../extensions/services/authentication/authentication.service';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs/internal/observable/of';

@Component({
  selector: 'app-volunteers-management',
  templateUrl: './volunteers-management.component.html',
  styleUrls: ['./volunteers-management.component.scss'],
})
export class VolunteersManagementComponent implements OnInit {
  manageableLocationsObs: Observable<Location[]>;
  errorSubject: Subject<boolean> = new Subject();

  constructor(
    private userService: UserService,
    private authenticationService: AuthenticationService
  ) {}

  ngOnInit(): void {
    const authenticatedUserId = this.authenticationService.userValue().userId;
    this.manageableLocationsObs = this.userService
      .getManageableLocations(authenticatedUserId)
      .pipe(
        catchError((err) => {
          console.error(err);
          this.errorSubject.next(true);
          return of<Location[]>(null);
        })
      );
  }
}
