import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationService} from '../../extensions/services/authentication/authentication.service';
import {Subscription} from 'rxjs';
import {BreadcrumbService, managementBreadcrumb} from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';
import {environment} from 'src/environments/environment';
import {User} from '../../extensions/model/User';

@Component({
    selector: 'app-management',
    templateUrl: './management.component.html',
    styleUrls: ['./management.component.scss']
})
export class ManagementComponent implements OnInit, OnDestroy {
    protected showTagManagement: boolean;
    protected showAdmin: boolean;
    protected showVolunteersManagement: boolean;
    protected showActionlog: boolean;
    protected showStats: boolean;

    protected showStagingWarning =
        environment.showStagingWarning;

    protected subscription: Subscription =
        new Subscription();

    constructor(
        private authenticationService: AuthenticationService,
        private breadcrumbsService: BreadcrumbService
    ) {
    }

    ngOnInit(): void {
        // Show certain functionality depending on the role of the user.
        this.subscription.add(
            this.authenticationService.user.subscribe((authenticatedUser: User) => {
                    this.showAdmin = authenticatedUser.isAdmin();
                    this.showTagManagement = authenticatedUser.isAdmin();
                    this.showVolunteersManagement = authenticatedUser.isAdmin() || authenticatedUser.isAuthority();
                    this.showActionlog = authenticatedUser.isAdmin();
                    this.showStats = authenticatedUser.isAdmin();
                }
            )
        );

        this.breadcrumbsService.setCurrentBreadcrumbs([
            managementBreadcrumb
        ]);
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }
}
