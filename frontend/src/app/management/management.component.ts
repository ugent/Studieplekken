import { Component, OnInit } from '@angular/core';
import {ApplicationTypeFunctionalityService} from '../services/functionality/application-type/application-type-functionality.service';
import {AuthenticationService} from '../services/authentication/authentication.service';

@Component({
  selector: 'app-management',
  templateUrl: './management.component.html',
  styleUrls: ['./management.component.css']
})
export class ManagementComponent implements OnInit {
  showReservations: boolean;
  showPenalties: boolean;
  showTagManagement: boolean;

  constructor(private functionalityService: ApplicationTypeFunctionalityService,
              private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
    // Show certain functionality depending on type of application
    this.showReservations = this.functionalityService.showReservationsFunctionality();
    this.showPenalties = this.functionalityService.showPenaltyFunctionality();

    // Show certain functionality depending on the role of the user
    this.authenticationService.user.subscribe(
      (next) => {
        this.showTagManagement = next.admin;
      }
    );
  }

}
