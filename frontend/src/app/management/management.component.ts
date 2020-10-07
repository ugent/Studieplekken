import { Component, OnInit } from '@angular/core';
import {ApplicationTypeFunctionalityService} from '../services/functionality/application-type/application-type-functionality.service';
import {AuthenticationService} from '../services/authentication/authentication.service';
import {Role} from '../../environments/environment';

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
    this.showReservations = this.functionalityService.showReservationsFunctionality();
    this.showPenalties = this.functionalityService.showPenaltyFunctionality();
    this.authenticationService.user.subscribe(
      (next) => {
        this.showTagManagement = next.roles.includes(Role.ADMIN);
      }
    );
  }

}
