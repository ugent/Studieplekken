import {Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {AuthenticationService} from './services/authentication/authentication.service';
import {Language} from './app.constants';

import * as moment from 'moment';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
    // The default language of the application.
    private defaultLang = Language.DUTCH;

    constructor(
        private authenticationService: AuthenticationService,
        private translateService: TranslateService
    ) {
        // tries to set the language to the default browser language of the user if 'en' or 'nl' (else en)
        const browserLang = translateService.getBrowserLang();
        const selectedLang = localStorage.getItem('selectedLanguage');

        // If you think about supporting another language, you must change the exported variable 'appLanguages'
        // in environments.ts accordingly. This variable is used in PenaltiesComponent to show the correct description
        translateService.setDefaultLang(
            this.defaultLang
        );

        if (selectedLang !== null) {
            this.setLanguage(selectedLang);
        } else {
            this.setLanguage(
                browserLang
            );
        }
    }

    /**
     * Initializes the application.
     */
    ngOnInit(): void {
        // Upon successful login, the backend redirects the user to /dashboard.
        // Since the user was redirected to the cas-login website, the AppComponent
        // will be recreated. After the recreation, we try to log-in in the frontend.
        this.authenticationService.login();
    }

    /**
     * Sets the language of the application.
     *
     * @param language
     */
    setLanguage(language: string): void {
        this.translateService.use(
            language
        );
        moment.locale(
            language
        );

        localStorage.setItem('selectedLanguage', language);
    }

    /**
     * Shows a message to the user.
     *
     * @param message
     * @returns {boolean}
     */
    shouldShowAlert(message: string): boolean {
        return false && localStorage.getItem(message) === null;
    }

    /**
     * Hides the alert message.
     *
     * @param message
     */
    hideAlert(message: string): void {
        localStorage.setItem(message, true.toString());
    }
}
