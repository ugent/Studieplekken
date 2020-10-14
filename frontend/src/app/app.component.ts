import {Component} from '@angular/core';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {ApplicationTypeFunctionalityService} from './services/functionality/application-type/application-type-functionality.service';
import {AuthenticationService} from './services/authentication/authentication.service';
import {Role} from '../environments/environment';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  showLogin = true;
  showDashboard = true;
  showProfile = false;
  showScan = false;
  showManagement = false;
  showInformation = true;

  loggedIn = false;

  constructor(private translate: TranslateService,
              private router: Router,
              private functionalityService: ApplicationTypeFunctionalityService,
              private authenticationService: AuthenticationService) {
    /******************************
     *   Language support setup   *
     ******************************/
    // if you think about supporting another language, you must change the exported variable 'appLanguages'
    // in environments.ts accordingly. This variable is used in PenaltiesComponent to show the correct description
    translate.setDefaultLang('en');
    // tries to set the language to the default browserlanguage of the user if 'en' or 'nl' (else en)
    const browserLang = translate.getBrowserLang();
    // add another language? -> add language to regex and read comments at the beginning of this constructor!
    translate.use(browserLang.match(/en|nl/) ? browserLang : 'en');

    /******************
     *   Show setup   *
     ******************/
    this.authenticationService.user.subscribe(
      (next) => {
        const scanFunc = this.functionalityService.showScanningFunctionality();

        // first, check if the user is logged in
        if (this.authenticationService.isLoggedIn()) {
          this.loggedIn = true;

          this.showLogin = false;
          this.showDashboard = true;
          this.showProfile = true;
          this.showScan = scanFunc && (next.roles.includes(Role.ADMIN) || next.roles.includes(Role.EMPLOYEE));
          this.showManagement = next.roles.includes(Role.ADMIN) || next.roles.includes(Role.EMPLOYEE);
          this.showInformation = true;

        } else {
          this.loggedIn = false;

          this.showLogin = true;
          this.showDashboard = true;
          this.showProfile = false;
          this.showScan = false;
          this.showManagement = false;
          this.showInformation = true;
        }

        // possibly overrule the shows above due to limitations in functionality
        this.showScan = this.functionalityService.showScanningFunctionality();
      }
    );
  }

  currentLanguage(): string {
    return this.translate.currentLang;
  }

  otherSupportedLanguage(): string {
    return this.translate.currentLang === 'nl' ? 'en' : 'nl';
  }

  changeLanguage(event: Event): void {
    event.preventDefault();
    if (this.translate.currentLang === 'nl') {
      this.translate.use('en');
    } else {
      this.translate.use('nl');
    }
  }

  isActive(path: string): boolean {
    return this.router.url === path;
  }

  logout(): void {
    this.authenticationService.logout();
  }
}
