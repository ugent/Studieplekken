import {Component, OnDestroy, OnInit} from '@angular/core';
import {BreadcrumbService} from '../stad-gent-components/header/breadcrumbs/breadcrumb.service';
import {User} from '../../model/User';
import {ReplaySubject, Subject, Subscription} from 'rxjs';
import {AuthenticationService} from '../../extensions/services/authentication/authentication.service';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss'],
})
export class ProfileComponent implements OnInit, OnDestroy {

    protected userSub$: Subject<User>;

    protected subscription: Subscription;

    constructor(
        private breadcrumbService: BreadcrumbService,
        private authenticationService: AuthenticationService
    ) {
        this.userSub$ = new ReplaySubject();
        this.subscription = new Subscription();
    }

    ngOnInit(): void {
        this.subscription.add(
            this.authenticationService.user.subscribe(user =>
                this.userSub$.next(user)
            )
        );

        this.breadcrumbService.setCurrentBreadcrumbs([{
            pageName: 'Profile', url: '/profile/overview'
        }]);
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }
}
