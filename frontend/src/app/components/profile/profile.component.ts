import {Component, OnInit} from '@angular/core';
import {BreadcrumbService} from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';
import {User} from '../../model/User';
import {Observable} from 'rxjs';
import {AuthenticationService} from '../../extensions/services/authentication/authentication.service';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss'],
})
export class ProfileComponent implements OnInit {

    protected userObs$: Observable<User>;

    constructor(
        private breadcrumbService: BreadcrumbService,
        private authenticationService: AuthenticationService
    ) {
    }

    ngOnInit(): void {
        this.userObs$ = this.authenticationService.getUserObs();

        this.breadcrumbService.setCurrentBreadcrumbs([{
            pageName: 'Profile', url: '/profile/overview'
        }]);
    }
}
