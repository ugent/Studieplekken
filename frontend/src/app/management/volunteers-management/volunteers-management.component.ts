import { Component, OnInit } from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {Location} from '../../shared/model/Location';
import {LocationService} from '../../services/api/locations/location.service';
import {UserService} from '../../services/api/users/user.service';
import {AuthenticationService} from '../../services/authentication/authentication.service';
import {catchError} from 'rxjs/operators';
import {of} from 'rxjs/internal/observable/of';

@Component({
  selector: 'app-volunteers-management',
  templateUrl: './volunteers-management.component.html',
  styleUrls: ['./volunteers-management.component.css']
})
export class VolunteersManagementComponent implements OnInit {

  manageableLocationsObs: Observable<Location[]>;
  errorSubject: Subject<boolean> = new Subject();

  constructor(private locationService: LocationService,
              private userService: UserService,
              private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
    const authenticatedUserId = this.authenticationService.userValue().augentID;
    this.manageableLocationsObs = this.userService.getManageableLocations(authenticatedUserId)
      .pipe(
        catchError(err => {
          console.error(err);
          this.errorSubject.next(true);
          return of(null);
        })
      );
  }

}


