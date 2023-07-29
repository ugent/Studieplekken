import {AfterViewInit, Component, OnInit, HostListener} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Observable, Subject} from 'rxjs';
import {map} from 'rxjs/operators';
import {UserService} from 'src/app/extensions/services/api/users/user.service';
import {AuthenticationService} from 'src/app/extensions/services/authentication/authentication.service';
import {Breadcrumb, BreadcrumbService} from './breadcrumbs/breadcrumb.service'
import {User} from '../../extensions/model/User';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, AfterViewInit {

    accordionSubject = new Subject<boolean>();
    languageSubject = new Subject<boolean>();
    showAdmin = false;
    showManagement = false;
    showLoggedIn = false;
    showVolunteer = false;
    MOBILE_SIZE = 800;

    constructor(
        private breadcrumbService: BreadcrumbService, private authenticationService: AuthenticationService,
        private translationService: TranslateService, private userService: UserService
    ) {
    }

    mobile: boolean;

    ngOnInit(): void {
        this.mobile = window.innerWidth < this.MOBILE_SIZE;
        // subscribe to the user observable to make sure that the correct information
        // is shown in the application.
        this.authenticationService.user.subscribe((user) => {
            // first, check if the user is logged in
            if (user.isLoggedIn()) {
                this.showLoggedIn = true;
                if (user.admin) {
                    this.showAdmin = true;
                } else {
                    this.showManagement = user.userAuthorities.length > 0;
                    this.showVolunteer = user.userVolunteer.length > 0;
                }
            } else {
                this.showManagement = false;
                this.showLoggedIn = false;
                this.showVolunteer = false;
                this.showAdmin = false;
            }
        });
    }

    @HostListener('window:resize', ['$event'])
    onResize(): void {
        this.mobile = window.innerWidth < this.MOBILE_SIZE;
    }

    ngAfterViewInit(): void {
    }

    getLinkedBreadcrumbs(): Observable<Breadcrumb[]> {
        return this.breadcrumbService.getCurrentBreadcrumbs().pipe(map(v => v.slice(0, -1)))
    }

    getUnlinkedBreadcrumbs(): Observable<Breadcrumb[]> {
        return this.breadcrumbService.getCurrentBreadcrumbs().pipe(map(v => v.slice(-1)))
    }

    getUser(): Observable<User> {
        return this.authenticationService.user;
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
