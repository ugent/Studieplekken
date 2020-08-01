import { Injectable } from '@angular/core';
import {TranslateService} from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class LanguageService {
  supportedLanguages = ['nl', 'en'];
  currentLanguageIndex: number;

  constructor(private translate: TranslateService) {
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
}
