import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { UserService } from '../services/api/users/user.service';
import { AuthenticationService } from '../services/authentication/authentication.service';
import { BreadcrumbService } from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';

@Component({
  selector: 'app-information',
  templateUrl: './information.component.html',
  styleUrls: ['./information.component.scss'],
})
export class InformationComponent implements OnInit {
  showManagement = false;
  showAdmin = false;
  showSupervisors = false;

  constructor(
    private translate: TranslateService,
    private authenticationService: AuthenticationService,
    private userService: UserService,
    private breadcrumbService: BreadcrumbService
  ) {}

  ngOnInit(): void {
    // subscribe to the user observable to make sure that the correct information
    // is shown in the application.
    this.authenticationService.user.subscribe((next) => {
      // first, check if the user is logged in
      if (this.authenticationService.isLoggedIn()) {
        this.authenticationService.hasVolunteeredObs.subscribe(o => this.showSupervisors = o);

        if (next.admin) {
          this.showAdmin = true;
        } else {
          this.userService
            .hasUserAuthorities(next.userId)
            .subscribe((next2) => {
              this.showManagement = next2;
            });
        }
      } else {
        this.showManagement = false;
      }
    });

    this.breadcrumbService.setCurrentBreadcrumbs([{pageName: "Information", url:"/information"}])
  }

  currentLanguage(): string {
    return this.translate.currentLang;
  }
}
