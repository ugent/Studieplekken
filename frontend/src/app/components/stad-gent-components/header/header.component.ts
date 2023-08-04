import {Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {filter, map} from 'rxjs/operators';
import {AuthenticationService} from 'src/app/extensions/services/authentication/authentication.service';
import {Breadcrumb, BreadcrumbService} from './breadcrumbs/breadcrumb.service';
import {User, UserConstructor} from '../../../model/User';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
    protected userSubject: BehaviorSubject<User>;
    protected accordionSubject: Subject<boolean>;
    protected languageSubject: Subject<boolean>;

    protected showAdmin = false;
    protected showManagement = false;
    protected showLoggedIn = false;
    protected showVolunteer = false;

    constructor(
        private breadcrumbService: BreadcrumbService,
        private authenticationService: AuthenticationService,
        private translationService: TranslateService
    ) {
        this.userSubject = new BehaviorSubject(
            UserConstructor.new()
        );
        this.accordionSubject = new Subject();
        this.languageSubject = new Subject();
    }

    ngOnInit(): void {
        // subscribe to the user observable to make sure that the correct information
        // is shown in the application.
        this.authenticationService.user.subscribe((user) => {
            this.userSubject.next(user);

            this.showLoggedIn = user.isLoggedIn();
            this.showAdmin = user.isAdmin();
            this.showManagement = user.isAuthority();
            this.showVolunteer = user.isScanner();
        });
    }

    getLinkedBreadcrumbs(): Observable<Breadcrumb[]> {
        return this.breadcrumbService.getCurrentBreadcrumbs().pipe(
            map(v =>
                v.slice(0, -1)
            )
        );
    }

    getUnlinkedBreadcrumbs(): Observable<Breadcrumb[]> {
        return this.breadcrumbService.getCurrentBreadcrumbs().pipe(
            map(v =>
                v.slice(-1)
            )
        );
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
