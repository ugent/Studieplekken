import {Component, OnInit} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {UserService} from '../services/api/users/user.service';
import {AuthenticationService} from '../services/authentication/authentication.service';
import {BreadcrumbService} from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';

@Component({
    selector: 'app-information',
    templateUrl: './information.component.html',
    styleUrls: ['./information.component.scss'],
})
export class InformationComponent implements OnInit {
    public currentLang: string;

    public showManagement = false;
    public showAdmin = false;
    public showSupervisors = false;

    constructor(
        private translate: TranslateService,
        private authenticationService: AuthenticationService,
        private userService: UserService,
        private breadcrumbService: BreadcrumbService
    ) {
    }

    /**
     * Determine what manuals are shown to the user,
     * depending on their role.
     */
    ngOnInit(): void {
        // subscribe to the user observable to make sure that the correct information
        // is shown in the application.
        this.authenticationService.user.subscribe((_) => {
            // first, check if the user is logged in
            if (this.authenticationService.isLoggedIn()) {
                this.showManagement = this.authenticationService.isAuthority();
                this.showAdmin = this.authenticationService.isAdmin();
                this.showSupervisors = this.authenticationService.isScanner();
            }
        });

        this.translate.onLangChange.subscribe(() => {
            this.currentLang = this.translate.currentLang;
        });

        this.breadcrumbService.setCurrentBreadcrumbs([{pageName: 'Information', url: '/information'}]);
    }
}
