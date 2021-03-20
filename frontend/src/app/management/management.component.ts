import { Component, OnInit } from '@angular/core';
import {ApplicationTypeFunctionalityService} from '../services/functionality/application-type/application-type-functionality.service';
import {AuthenticationService} from '../services/authentication/authentication.service';
import {combineLatest} from 'rxjs';

@Component({
  selector: 'app-management',
  templateUrl: './management.component.html',
  styleUrls: ['./management.component.css']
})
export class ManagementComponent implements OnInit {
  showReservations: boolean;
  showPenalties: boolean;
  showTagManagement: boolean;
  showAdmin: boolean = this.authenticationService.isAdmin();
  showVolunteersManagement: boolean;

  constructor(private functionalityService: ApplicationTypeFunctionalityService,
              private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
    // Show certain functionality depending on type of application
    this.showReservations = this.functionalityService.showReservationsFunctionality();
    this.showPenalties = this.functionalityService.showPenaltyFunctionality();

    // Show certain functionality depending on the role of the user
    const authenticatedUserObs = this.authenticationService.user;
    const hasAuthoritiesObs = this.authenticationService.hasAuthoritiesObs;

    combineLatest([authenticatedUserObs, hasAuthoritiesObs])
      .subscribe(result => {
        const authenticatedUser = result[0];
        const hasAuthorities = result[1];

        this.showTagManagement = authenticatedUser.admin;
        this.showVolunteersManagement = authenticatedUser.admin || hasAuthorities;
      });
  }

}
