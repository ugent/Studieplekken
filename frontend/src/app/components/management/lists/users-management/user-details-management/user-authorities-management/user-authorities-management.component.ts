import {Component, Input} from '@angular/core';
import {combineLatest, Observable, ReplaySubject, Subject} from 'rxjs';
import {Authority, AuthorityConstructor} from '../../../../../../model/Authority';
import {User} from '../../../../../../model/User';
import {BaseManagementComponent} from '../../../base-management.component';
import {DeleteAction, TableAction, TableMapper} from '../../../../../../model/Table';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {AuthoritiesService} from '../../../../../../extensions/services/api/authorities/authorities.service';
import {filter, first, switchMap, tap} from 'rxjs/operators';
import {AuthenticationService} from '../../../../../../extensions/services/authentication/authentication.service';

@Component({
    selector: 'app-user-authorities-management',
    templateUrl: './user-authorities-management.component.html',
    styleUrls: ['./user-authorities-management.component.scss'],
})
export class UserAuthoritiesManagementComponent extends BaseManagementComponent<Authority> {

    @Input() userObs: Observable<User>;

    protected addableAuthorities$: Subject<Authority[]>;

    constructor(
        private authoritiesService: AuthoritiesService,
        private authenticationService: AuthenticationService
    ) {
        super();

        this.addableAuthorities$ = new ReplaySubject();
    }

    ngOnInit(): void {
        super.ngOnInit();
    }

    setupForm(item: Authority = AuthorityConstructor.new()): void {
        this.formGroup = new FormGroup({
            authority: new FormControl(item.authorityId, Validators.required)
        });
    }

    setupItems(): void {
        combineLatest([this.userObs, this.authenticationService.user]).pipe(
            switchMap(([user, loggedInUser]) =>
                combineLatest([
                    this.authoritiesService.getAuthoritiesOfUser(user.userId),
                    loggedInUser.isAdmin()
                        ? this.authoritiesService.getAllAuthorities()
                        : this.authoritiesService.getAuthoritiesOfUser(loggedInUser.userId)
                ])
            ), first()
        ).subscribe(([userAuthorities, allAuthorities]) => {
            this.itemsSub.next(userAuthorities);
            this.addableAuthorities$.next(allAuthorities.filter(authority =>
                !userAuthorities.some(uAuthority => uAuthority.authorityId === authority.authorityId)
            ));
        });
    }

    storeAdd(body: any): void {
        this.sendBackendRequest(
            this.userObs.pipe(
                switchMap(user =>
                    this.authoritiesService.addUserToAuthority(user.userId, body.authority)
                )
            )
        );
    }

    storeDelete(item: Authority): void {
        this.sendBackendRequest(
            this.userObs.pipe(
                switchMap(user =>
                    this.authoritiesService.deleteUserFromAuthority(user.userId, item.authorityId)
                )
            )
        );
    }

    getTableActions(): TableAction<Authority>[] {
        return [
            new DeleteAction((authority: Authority) => {
                this.prepareDelete(authority);
            })
        ];
    }

    getTableMapper(): TableMapper<Authority> {
        return (authority: Authority) => ({
            'management.authorities.table.authorityName': authority.authorityName,
            'management.authorities.table.description': authority.description
        });
    }
}
