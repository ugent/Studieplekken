import {Component} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {EMPTY, Observable} from 'rxjs';
import {AuthoritiesService} from '@/services/api/authorities/authorities.service';
import {Authority, AuthorityConstructor} from '@/model/Authority';
import {BaseManagementComponent} from '../base-management.component';
import {AuthenticationService} from '@/services/authentication/authentication.service';
import {startWith, switchMap} from 'rxjs/operators';
import {TableMapper} from '@/model/Table';

@Component({
    selector: 'app-authorities-management',
    templateUrl: './authorities-management.component.html',
    styleUrls: ['./authorities-management.component.scss'],
})
export class AuthoritiesManagementComponent extends BaseManagementComponent<Authority> {

    protected authoritiesObs$: Observable<Authority[]>;

    constructor(
        private authoritiesService: AuthoritiesService,
        private authenticationService: AuthenticationService
    ) {
        super();
    }

    ngOnInit(): void {
        super.ngOnInit();

        this.authoritiesObs$ = this.refresh$.pipe(
            startWith(EMPTY), switchMap(() =>
                this.authenticationService.getUserObs().pipe(
                    switchMap(user =>
                        user.isAdmin() ? this.authoritiesService.getAllAuthorities() : this.authoritiesService.getAuthoritiesOfUser(
                            user.userId
                        )
                    )
                )
            )
        );
    }

    setupForm(authority: Authority = AuthorityConstructor.new()): void {
        this.formGroup = new FormGroup({
            authorityId: new FormControl(authority.authorityId),
            authorityName: new FormControl(authority.authorityName),
            description: new FormControl(authority.description)
        });
    }

    storeAdd(): void {
        this.sendBackendRequest(
            this.authoritiesService.addAuthority(
                this.formGroup.value
            )
        );
    }

    storeUpdate(authority: Authority): void {
        this.sendBackendRequest(
            this.authoritiesService.updateAuthority(
                authority.authorityId, this.formGroup.value
            )
        );
    }

    storeDelete(authority: Authority): void {
        this.sendBackendRequest(
            this.authoritiesService.deleteAuthority(authority.authorityId)
        );
    }

    getTableMapper(): TableMapper<Authority> {
        return (authority: Authority) => ({
            'management.authorities.table.authorityName': authority.authorityName,
            'management.authorities.table.description': authority.description
        });
    }
}
