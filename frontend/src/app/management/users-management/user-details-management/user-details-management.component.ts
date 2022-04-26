import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../../../shared/model/User';
import { UserDetailsService } from '../../../services/single-point-of-truth/user-details/user-details.service';
import { ActivatedRoute } from '@angular/router';
import { ApplicationTypeFunctionalityService } from '../../../services/functionality/application-type/application-type-functionality.service';
import { AuthenticationService } from '../../../services/authentication/authentication.service';
import { PenaltyList, PenaltyService } from '../../../services/api/penalties/penalty.service';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-user-details-management',
  templateUrl: './user-details-management.component.html',
  styleUrls: ['./user-details-management.component.scss'],
})
export class UserDetailsManagementComponent implements OnInit {
  userObs: Observable<User> = this.userDetailsService.userObs;
  penaltyObservable: Observable<{ penalties: PenaltyList; points: number }>;

  userQueryingError: boolean = undefined;
  userId: string;

  showPenaltyManagement: boolean;
  showRolesManagement: boolean;

  constructor(
    private userDetailsService: UserDetailsService,
    private route: ActivatedRoute,
    private functionalityService: ApplicationTypeFunctionalityService,
    private authenticationService: AuthenticationService,
    private penaltiesService: PenaltyService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.userId = id;
    this.userDetailsService.loadUser(id);

    this.penaltyObservable = this.penaltiesService.getPenaltiesOfUserById(this.userId).pipe(map(p => ({points: 1, penalties: p})));

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

  onPenaltyDelete(): void {
    this.penaltyObservable = this.penaltiesService.getPenaltiesOfUserById(this.userId).pipe(map(p => ({points: 1, penalties: p})));
  }
}
