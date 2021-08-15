import { Component, OnInit } from '@angular/core';
import { objectExists } from '../../shared/GeneralFunctions';
import { Observable } from 'rxjs';
import { User } from '../../shared/model/User';
import { UserService } from '../../services/api/users/user.service';
import { HttpErrorResponse } from '@angular/common/http';
import { ApplicationTypeFunctionalityService } from '../../services/functionality/application-type/application-type-functionality.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-users-management',
  templateUrl: './users-management.component.html',
  styleUrls: ['./users-management.component.scss'],
})
export class UsersManagementComponent implements OnInit {

  neverSearched = true;
  filteredUsers: Observable<User[]>;

  // the 'booleanId' is used in this.handler(number)
  errorOnRetrievingFilteredUsers = false; // booleanId = 0
  noUserWithBarcodeHasBeenFound = false; // booleanId = 1

  showPenaltyPoints: boolean;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private functionalityService: ApplicationTypeFunctionalityService
  ) {}

  ngOnInit(): void {
    this.showPenaltyPoints = this.functionalityService.showPenaltyFunctionality();
  }

  toProfile(user: User) {
    this.router.navigate([user.userId], {relativeTo: this.route})
  }
}
