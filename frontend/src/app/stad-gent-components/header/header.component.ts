import { AfterViewChecked, AfterViewInit, Component, ElementRef, OnInit } from '@angular/core';
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
  showLoggedIn=false;

  constructor(private breadcrumbService: BreadcrumbService, private authenticationService: AuthenticationService,
    private translationService: TranslateService, private userService: UserService) { }

  ngOnInit(): void {
    // subscribe to the user observable to make sure that the correct information
    // is shown in the application.
    this.authenticationService.user.subscribe((next) => {
      // first, check if the user is logged in
      if (this.authenticationService.isLoggedIn()) {
        this.showLoggedIn = true;
        if (this.authenticationService.hasVolunteeredValue()){
          this.showSupervisors = true;
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
