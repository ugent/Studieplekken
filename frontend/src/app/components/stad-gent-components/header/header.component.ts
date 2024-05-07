import {Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {AuthenticationService} from 'src/app/services/authentication/authentication.service';
import {Breadcrumb, BreadcrumbService} from './breadcrumbs/breadcrumb.service';
import {User, UserConstructor} from '../../../model/User';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
    protected userSubject: Observable<User>;
    protected accordionSubject: Subject<boolean>;
    protected languageSubject: Subject<boolean>;

    constructor(
        private breadcrumbService: BreadcrumbService,
        private authenticationService: AuthenticationService,
        private translationService: TranslateService
    ) {
        this.userSubject = this.authenticationService.user;
        this.accordionSubject = new Subject();
        this.languageSubject = new Subject();
    }

    /**
     * Get the breadcrumbs that are linked to the current page
     *
     * @returns Observable<Breadcrumb[]>
     */
    getLinkedBreadcrumbs(): Observable<Breadcrumb[]> {
        return this.breadcrumbService.getCurrentBreadcrumbs().pipe(
            map(v =>
                v.slice(0, -1)
            )
        );
    }

    /**
     * Get the breadcrumbs that are not linked to the current page
     *
     * @returns Observable<Breadcrumb[]>
     */
    getUnlinkedBreadcrumbs(): Observable<Breadcrumb[]> {
        return this.breadcrumbService.getCurrentBreadcrumbs().pipe(
            map(v =>
                v.slice(-1)
            )
        );
    }

    /**
     * Get the current language
     *
     * @returns string
     */
    currentLanguage(): string {
        return localStorage.getItem('selectedLanguage');
    }

    /**
     * Get the other supported language
     *
     * @returns string
     */
    otherSupportedLanguage(): string {
        return localStorage.getItem('selectedLanguage') === 'nl' ? 'en' : 'nl';
    }

    /**
     * Change the language of the application
     *
     * @param event
     */
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
