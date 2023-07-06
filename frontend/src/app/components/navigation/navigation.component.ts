import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { UserService } from '../../extensions/services/api/users/user.service';
import { AuthenticationService } from '../../extensions/services/authentication/authentication.service';
import { BreadcrumbService } from '../../stad-gent-components/header/breadcrumbs/breadcrumb.service';
import { LoginRedirectService } from '../../extensions/services/authentication/login-redirect.service';

@Component({
  selector: 'app-information',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.scss'],
})
export class NavigationComponent implements OnInit {
  showManagement = false;
  showAdmin = false;
  showSupervisors = false;
  showLoggedIn = false;

  constructor(
    private translate: TranslateService,
    private authenticationService: AuthenticationService,
    private userService: UserService,
    private breadcrumbService: BreadcrumbService,
    private loginRedirectService: LoginRedirectService
  ) {}

  ngOnInit(): void {

    this.loginRedirectService.registerUrl("/navigation");
    // subscribe to the user observable to make sure that the correct information
    // is shown in the application.
    this.authenticationService.user.subscribe((next) => {
      // first, check if the user is logged in
      if (this.authenticationService.isLoggedIn()) {
        this.showLoggedIn = true;
        if (next.userVolunteer.length > 0){
          this.showSupervisors = true;
        }else{
          this.showSupervisors = false;
        }
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

    this.breadcrumbService.setCurrentBreadcrumbs([{pageName: "Navigation", url:"/navigation"}])
  }

  currentLanguage(): string {
    return this.translate.currentLang;
  }
}
