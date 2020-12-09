import {Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import { UserService } from '../services/api/users/user.service';
import { AuthenticationService } from '../services/authentication/authentication.service';

@Component({
  selector: 'app-information',
  templateUrl: './information.component.html',
  styleUrls: ['./information.component.css']
})
export class InformationComponent implements OnInit {

  showManagement = false;

  constructor(private translate: TranslateService,
              private authenticationService: AuthenticationService,
              private userService: UserService) {
  }

  ngOnInit(): void {
    // subscribe to the user observable to make sure that the correct information
    // is shown in the application.
    this.authenticationService.user.subscribe(
      next => {

        // first, check if the user is logged in
        if (this.authenticationService.isLoggedIn()) {
          if (next.admin) {
            this.showManagement = true;
          } else {
            this.userService.hasUserAuthorities(next.augentID).subscribe(
              next2 => {
                this.showManagement = next2;
              }
            );
          }

        } else {
          this.showManagement = false;
        }
      }
    );
  }

  currentLanguage(): string {
    return this.translate.currentLang;
  }
}
