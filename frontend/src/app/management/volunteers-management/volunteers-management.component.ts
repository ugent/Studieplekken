import { Component, OnInit } from '@angular/core';
import {forkJoin, Observable} from 'rxjs';
import {Location} from '../../shared/model/Location';
import {LocationService} from '../../services/api/locations/location.service';
import {UserService} from '../../services/api/users/user.service';
import {AuthenticationService} from '../../services/authentication/authentication.service';
import {map, switchMap} from 'rxjs/operators';
import {User} from '../../shared/model/User';

@Component({
  selector: 'app-volunteers-management',
  templateUrl: './volunteers-management.component.html',
  styleUrls: ['./volunteers-management.component.css']
})
export class VolunteersManagementComponent implements OnInit {

  manageableLocationsObs: Observable<Location[]>;
  volunteersObs: Observable<User[][]>;

  constructor(private locationService: LocationService,
              private userService: UserService,
              private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
    const getVolunteersForLocation = (location: Location) => this.locationService.getVolunteers(location.locationId)
    const authenticatedUserId = this.authenticationService.userValue().augentID;
    this.manageableLocationsObs = this.userService.getManageableLocations(authenticatedUserId);
    this.volunteersObs = this.manageableLocationsObs.pipe(map(vl => vl.map(getVolunteersForLocation)), switchMap(v => forkJoin(v)) )
  }

}


