import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../../../shared/model/User';
import { UserDetailsService } from '../../../services/single-point-of-truth/user-details/user-details.service';
import { ActivatedRoute } from '@angular/router';
import { ApplicationTypeFunctionalityService } from '../../../services/functionality/application-type/application-type-functionality.service';
import { AuthenticationService } from '../../../services/authentication/authentication.service';

@Component({
  selector: 'app-user-details-management',
  templateUrl: './user-details-management.component.html',
  styleUrls: ['./user-details-management.component.css'],
})
export class UserDetailsManagementComponent implements OnInit {
  userObs: Observable<User> = this.userDetailsService.userObs;

  userQueryingError: boolean = undefined;
  userId: string;

  showPenaltyManagement: boolean;
  showRolesManagement: boolean;

  constructor(
    private userDetailsService: UserDetailsService,
    private route: ActivatedRoute,
    private functionalityService: ApplicationTypeFunctionalityService,
    private authenticationService: AuthenticationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.userId = id;
    this.userDetailsService.loadUser(id);

    this.userObs.subscribe(
      () => {
        this.userQueryingError = false;
      },
      () => {
        this.userQueryingError = true;
      }
    );

    // set show-variables based on functionality of application
    this.showPenaltyManagement = this.functionalityService.showPenaltyFunctionality();

    // set show-variables based on authorization
    this.authenticationService.user.subscribe((next) => {
      this.showRolesManagement = next.admin;
    });
  }
}
