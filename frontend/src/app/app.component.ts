import { Component } from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Router} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  supportedLanguages = ['nl', 'en'];
  currentLanguageIndex: number;

  // every navigationItem should have an entry in assets/i18n/*.json
  navigationItems: string[] = ['dashboard', 'profile', 'scan', 'management', 'information'];

  constructor(private translate: TranslateService,
              private router: Router) {
    // setup translation
    this.setupLanguage();
  }

  setupLanguage(): void {
    this.currentLanguageIndex = this.supportedLanguages.indexOf(this.translate.getBrowserLang());
    if (this.currentLanguageIndex < 0) {
      this.currentLanguageIndex = 0;
    }
    this.translate.use(this.supportedLanguages[this.currentLanguageIndex]);
  }

  currentLanguage(): string {
    return this.supportedLanguages[this.currentLanguageIndex];
  }

  otherSupportedLanguage(): string {
    return this.supportedLanguages[(this.currentLanguageIndex + 1) % this.supportedLanguages.length];
  }

  changeLanguage(): void {
    this.currentLanguageIndex = (this.currentLanguageIndex + 1) % this.supportedLanguages.length;
    this.translate.use(this.supportedLanguages[this.currentLanguageIndex]);
  }

  isActive(path: string): boolean {
    return this.router.url === path;
  }

  // TODO
  isLoggedIn(): boolean {
    return false;
  }
}
