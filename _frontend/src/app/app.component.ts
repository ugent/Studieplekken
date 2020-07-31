import {Component} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {AuthenticationService} from '../services/authentication.service';
import {IRoles} from "../interfaces/IRoles";
import {roles, urls} from "../environments/environment";


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'blokAtUGent';
  roles: IRoles;

  iso = 'en';
  urls = urls;

  constructor(private translate: TranslateService, public authenticationService: AuthenticationService) {
    // if you think about supporting another language, you must change the exported variable 'appLanguages'
    // in environments.ts accordingly. This variable is used in PenaltiesComponent to show the correct description
    translate.setDefaultLang('en');
    // tries to set the language to the default browserlanguage of the user if 'en' or 'nl' (else en)
    const browserLang = translate.getBrowserLang();
    // add another language? -> add language to regex and read comments at the beginning of this constructor!
    translate.use(browserLang.match(/en|nl/) ? browserLang : 'en');
    this.iso = translate.currentLang;

    this.roles = roles;
  }

  useLanguage(e: Event) {
    e.preventDefault();
    if (this.translate.currentLang === 'nl') {
      this.translate.use('en');
      this.iso = 'en';
    } else {
      this.translate.use('nl');
      this.iso = 'nl';
    }
  }

}
