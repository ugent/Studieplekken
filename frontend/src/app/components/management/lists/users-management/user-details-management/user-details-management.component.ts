import {Component, OnInit} from '@angular/core';
import {Observable, ReplaySubject, Subject} from 'rxjs';
import {User} from '../../../../../model/User';
import {UserDetailsService} from '../../../../../extensions/services/single-point-of-truth/user-details/user-details.service';
import {ActivatedRoute} from '@angular/router';
import {AuthenticationService} from '../../../../../extensions/services/authentication/authentication.service';
import {UserService} from '../../../../../extensions/services/api/users/user.service';

@Component({
    selector: 'app-user-details-management',
    templateUrl: './user-details-management.component.html',
    styleUrls: ['./user-details-management.component.scss'],
})
export class UserDetailsManagementComponent implements OnInit {

    protected userSub$: Subject<User>;

    protected showRolesManagement$: Subject<boolean>;
    protected userId$: Subject<string>;

    constructor(
        private userService: UserService,
        private route: ActivatedRoute,
        private authenticationService: AuthenticationService,
    ) {
        this.userSub$ = new ReplaySubject();
        this.showRolesManagement$ = new ReplaySubject();
        this.userId$ = new ReplaySubject();
    }

    ngOnInit(): void {
        const userId = this.route.snapshot.paramMap.get('id');

        this.userService.getUserByAUGentId(
            userId
        ).subscribe(user =>
            this.userSub$.next(user)
        );

        this.userId$.next(userId);

        // set show-variables based on authorization
        this.authenticationService.user.subscribe((next) => {
            this.showRolesManagement$.next(
                next.isAdmin()
            );
        });
    }
}
