import { Component } from '@angular/core';
import {Router} from '@angular/router';
import {LanguageService} from './services/language/language.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  // every navigationItem should have an entry in assets/i18n/*.json
  navigationItems: string[] = ['dashboard', 'profile', 'scan', 'management', 'information'];

  constructor(private languageService: LanguageService,
              private router: Router) {
  }

  currentLanguage(): string {
    return this.languageService.currentLanguage();
  }

  otherSupportedLanguage(): string {
    return this.languageService.otherSupportedLanguage();
  }

  changeLanguage(): void {
    this.languageService.changeLanguage();
  }

  isActive(path: string): boolean {
    return this.router.url === path;
  }

  // TODO
  isLoggedIn(): boolean {
    return false;
  }
}
