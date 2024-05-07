import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationService} from '../../services/authentication/authentication.service';
import {Observable, Subscription} from 'rxjs';
import {BreadcrumbService, managementBreadcrumb} from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';
import {environment} from 'src/environments/environment';
import {User} from '../../model/User';

@Component({
    selector: 'app-management',
    templateUrl: './management.component.html',
    styleUrls: ['./management.component.scss']
})
export class ManagementComponent implements OnInit {

    protected userObs$: Observable<User>;

    protected showStagingWarning: boolean;

    constructor(
        private authenticationService: AuthenticationService,
        private breadcrumbsService: BreadcrumbService
    ) {
        this.showStagingWarning = environment.showStagingWarning;
    }

    ngOnInit(): void {
        // Show certain functionality depending on the role of the user.
        this.userObs$ = this.authenticationService.user;

        this.breadcrumbsService.setCurrentBreadcrumbs([
            managementBreadcrumb
        ]);
    }
}
