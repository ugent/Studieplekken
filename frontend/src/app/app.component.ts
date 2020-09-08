import { Component } from '@angular/core';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {ApplicationTypeFunctionalityService} from './services/functionality/application-type/application-type-functionality.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  // every navigationItem should have an entry in assets/i18n/*.json
  navigationItems: string[] = ['dashboard', 'profile', 'scan', 'management', 'information'];

  constructor(private translate: TranslateService,
              private router: Router,
              private functionalityService: ApplicationTypeFunctionalityService) {
    // if you think about supporting another language, you must change the exported variable 'appLanguages'
    // in environments.ts accordingly. This variable is used in PenaltiesComponent to show the correct description
    translate.setDefaultLang('en');
    // tries to set the language to the default browserlanguage of the user if 'en' or 'nl' (else en)
    const browserLang = translate.getBrowserLang();
    // add another language? -> add language to regex and read comments at the beginning of this constructor!
    translate.use(browserLang.match(/en|nl/) ? browserLang : 'en');

    const showScan = this.functionalityService.showScanningFunctionality();
    if (!showScan) {
      const idx = this.navigationItems.findIndex(v => v === 'scan');
      this.navigationItems.splice(idx, 1);
    }
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

  // TODO
  isLoggedIn(): boolean {
    return false;
  }
}
