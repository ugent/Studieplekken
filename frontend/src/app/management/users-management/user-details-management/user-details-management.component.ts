import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from '../../../shared/model/User';
import {UserDetailsService} from '../../../services/single-point-of-truth/user-details/user-details.service';
import {ActivatedRoute} from '@angular/router';
import {ApplicationTypeFunctionalityService} from '../../../services/functionality/application-type/application-type-functionality.service';

@Component({
  selector: 'app-user-details-management',
  templateUrl: './user-details-management.component.html',
  styleUrls: ['./user-details-management.component.css']
})
export class UserDetailsManagementComponent implements OnInit {
  userObs: Observable<User> = this.userDetailsService.userObs;

  userQueryingError: boolean = undefined;
  userId: string;

  showPenaltyManagement: boolean;

  constructor(private userDetailsService: UserDetailsService,
              private route: ActivatedRoute,
              private functionalityService: ApplicationTypeFunctionalityService) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.userId = id;
    this.userDetailsService.loadUser(id);

    this.userObs.subscribe(
      (next) => {
        this.userQueryingError = false;
      }, (error) => {
        this.userQueryingError = true;
      }
    );

    this.showPenaltyManagement = this.functionalityService.showPenaltyFunctionality();
  }
}
