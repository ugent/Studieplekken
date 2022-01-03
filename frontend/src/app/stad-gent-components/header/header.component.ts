import { AfterViewChecked, AfterViewInit, Component, ElementRef, OnInit, HostListener } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { delay, distinctUntilChanged, map, tap } from 'rxjs/operators';
import { UserService } from 'src/app/services/api/users/user.service';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import {BreadcrumbService} from "./breadcrumbs/breadcrumb.service"

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, AfterViewInit {

  accordionSubject = new Subject<boolean>();
  languageSubject = new Subject<boolean>();
  showSupervisors = false;
  showAdmin = false;
  showManagement = false;
  showLoggedIn = false;
  showVolunteer = false;
  MOBILE_SIZE = 885;

  constructor(private breadcrumbService: BreadcrumbService, private authenticationService: AuthenticationService,
    private translationService: TranslateService, private userService: UserService) { }

  mobile: boolean;

  ngOnInit(): void {
    this.mobile = window.innerWidth < this.MOBILE_SIZE;
    // subscribe to the user observable to make sure that the correct information
    // is shown in the application.
    this.authenticationService.user.subscribe((user) => {
      // first, check if the user is logged in
      if (this.authenticationService.isLoggedIn()) {
        this.showLoggedIn = true;
        if (this.authenticationService.hasVolunteeredValue()){
          this.showSupervisors = true;
        }
        if (user.admin) {
          this.showAdmin = true;
        } else {
          this.showManagement = user.userAuthorities.length > 0;
          this.showVolunteer = user.userVolunteer.length > 0;
        }
      } else {
        this.showManagement = false;
        this.showLoggedIn = false;
        this.showVolunteer = false;
        this.showAdmin = false;
        this.showSupervisors = false;
      }
    });
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    this.mobile = window.innerWidth < this.MOBILE_SIZE;
  }

  ngAfterViewInit() {
  }

  getLinkedBreadcrumbs() {
    return this.breadcrumbService.getCurrentBreadcrumbs().pipe(map(v => v.slice(0,-1)))
  }

  getUnlinkedBreadcrumbs() {
    return this.breadcrumbService.getCurrentBreadcrumbs().pipe(map(v => v.slice(-1)))
  }

  getUser() {
    return this.authenticationService.user;
  }

  logout(): void {
    this.authenticationService.logout();
  }

  currentLanguage(): string {
    return localStorage.getItem('selectedLanguage');
  }

  otherSupportedLanguage(): string {
    return localStorage.getItem('selectedLanguage') === 'nl' ? 'en' : 'nl';
  }

  changeLanguage(event: Event): void {
    event.preventDefault();
    if (localStorage.getItem('selectedLanguage') === 'nl') {
      localStorage.setItem('selectedLanguage', 'en');
      this.translationService.use('en');
    } else {
      localStorage.setItem('selectedLanguage', 'nl');
      this.translationService.use('nl');
    }

    this.languageSubject.next(false);
  }
}
