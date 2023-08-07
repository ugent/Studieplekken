import {Component, OnInit} from '@angular/core';
import {combineLatest, Observable, ReplaySubject, Subject} from 'rxjs';
import {User} from '../../../../../model/User';
import {ActivatedRoute} from '@angular/router';
import {AuthenticationService} from '../../../../../extensions/services/authentication/authentication.service';
import {UserService} from '../../../../../extensions/services/api/users/user.service';
import {filter, map, switchMap, tap} from 'rxjs/operators';
import {Authority} from '../../../../../model/Authority';
import {AuthoritiesService} from '../../../../../extensions/services/api/authorities/authorities.service';

@Component({
    selector: 'app-user-details-management',
    templateUrl: './user-details-management.component.html',
    styleUrls: ['./user-details-management.component.scss'],
})
export class UserDetailsManagementComponent implements OnInit {

    protected currentUserObs$: Observable<User>;
    protected userObs$: Observable<User>;
    protected addedAuthoritiesObs$: Observable<Authority[]>;
    protected addableAuthoritiesObs$: Observable<Authority[]>;

    protected showRolesManagement: boolean;
    protected userId: string;

    constructor(
        private userService: UserService,
        private route: ActivatedRoute,
        private authenticationService: AuthenticationService,
        private authoritiesService: AuthoritiesService
    ) {
    }

    ngOnInit(): void {
        this.userId = this.route.snapshot.paramMap.get('id');

        this.currentUserObs$ = this.userService.getUserByAUGentId(this.userId);
        this.userObs$ = this.authenticationService.getUserObs().pipe(
            tap((user: User) =>
                this.showRolesManagement = user.isAdmin()
            )
        );
        this.addedAuthoritiesObs$ = this.currentUserObs$.pipe(
            switchMap(user =>
                this.authoritiesService.getAuthoritiesOfUser(user.userId).pipe()
            )
        );
        this.addableAuthoritiesObs$ = combineLatest([this.userObs$, this.addedAuthoritiesObs$]).pipe(
            switchMap(([user, addedAuthorities]) =>
                (user.isAdmin() ?
                    this.authoritiesService.getAllAuthorities() :
                    this.authoritiesService.getAuthoritiesOfUser(user.userId)
                ).pipe(
                    map(allAuthorities =>
                        allAuthorities.filter(authority =>
                            !addedAuthorities.some(addedAuthority =>
                                authority.authorityId === addedAuthority.authorityId
                            )
                        )
                    )
                )
            )
        );
    }
}
