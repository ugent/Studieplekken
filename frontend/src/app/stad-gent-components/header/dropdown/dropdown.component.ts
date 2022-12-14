import { Component, Input, OnInit } from '@angular/core';
import { isThisISOWeek } from 'date-fns';
import { Subject } from 'rxjs';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import { User } from 'src/app/shared/model/User';
import { UserService } from 'src/app/services/api/users/user.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-header-dropdown',
  templateUrl: './dropdown.component.html',
  styleUrls: ['./dropdown.component.scss']
})
export class DropdownComponent implements OnInit {
  //@Input() user: User;
  @Input() accordion: Subject<boolean>;
  @Input() isProfile: Subject<boolean>;

  showAdmin = false;
  showManagement = false;
  showLoggedIn=false;
  showVolunteer=false;
  user = null;

  constructor(private authenticationService: AuthenticationService, private userService: UserService, private translationService: TranslateService) { }

  ngOnInit(): void {
    this.authenticationService.user.subscribe((next) => {
      this.user = next
      // first, check if the user is logged in
      if (this.authenticationService.isLoggedIn()) {
        this.showLoggedIn = true;
        if (next.admin) {
          this.showAdmin = true;
        } else {
          this.showManagement = this.user.userAuthorities.length > 0;
          this.showVolunteer = this.user.userVolunteer.length > 0;
        }
      } else {
        this.showManagement = false;
        this.showLoggedIn = false;
        this.showVolunteer = false;
        this.showAdmin = false;
      }
    });
  }


  logout() {
    return this.authenticationService.logout();
  }

  close() {
    this.accordion.next(false);
  }

}
