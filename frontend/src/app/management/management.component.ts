import {Component, OnInit} from '@angular/core';
import {
    ApplicationTypeFunctionalityService
} from '../services/functionality/application-type/application-type-functionality.service';
import {AuthenticationService} from '../services/authentication/authentication.service';
import {combineLatest} from 'rxjs';
import {BreadcrumbService, managementBreadcrumb} from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';
import {LoginRedirectService} from '../services/authentication/login-redirect.service';
import {environment} from 'src/environments/environment';

@Component({
    selector: 'app-management',
    templateUrl: './management.component.html',
    styleUrls: ['./management.component.scss'],
})
export class ManagementComponent implements OnInit {
    showTagManagement: boolean;
    showAdmin: boolean;
    showVolunteersManagement: boolean;
    showActionlog: boolean;
    showStats: boolean;
    showStagingWarning = environment.showStagingWarning;

    constructor(
        private authenticationService: AuthenticationService,
        private breadcrumbsService: BreadcrumbService
    ) {}

    ngOnInit(): void {
        // Show certain functionality depending on the role of the user
        const authenticatedUserObs = this.authenticationService.user;
        const hasAuthoritiesObs = this.authenticationService.hasAuthoritiesObs;

        combineLatest([authenticatedUserObs, hasAuthoritiesObs]).subscribe(
            ([authenticatedUser, hasAuthorities]) => {
                this.showAdmin = authenticatedUser.admin;
                this.showTagManagement = authenticatedUser.admin;
                this.showVolunteersManagement = authenticatedUser.admin || hasAuthorities;
                this.showActionlog = authenticatedUser.admin;
                this.showStats = authenticatedUser.admin;
            }
        );

        this.breadcrumbsService.setCurrentBreadcrumbs([managementBreadcrumb]);
    }
}
