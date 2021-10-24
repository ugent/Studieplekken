import { AfterViewChecked, AfterViewInit, Component, ElementRef, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { delay, distinctUntilChanged, map, tap } from 'rxjs/operators';
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

  constructor(private breadcrumbService: BreadcrumbService, private authenticationService: AuthenticationService,
    private translationService: TranslateService) { }

  ngOnInit(): void {

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
    return this.authenticationService.user
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
