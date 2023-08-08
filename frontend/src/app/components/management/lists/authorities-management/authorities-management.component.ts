import {Component, OnInit, TemplateRef} from '@angular/core';
import {
    AbstractControl, FormControl, FormGroup,
    UntypedFormControl,
    UntypedFormGroup,
    Validators
} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {EMPTY, Observable} from 'rxjs';
import {AuthoritiesService} from '../../../../extensions/services/api/authorities/authorities.service';
import {
    AuthorityToManageService
} from '../../../../extensions/services/single-point-of-truth/authority-to-manage/authority-to-manage.service';
import {Authority, AuthorityConstructor} from '../../../../model/Authority';
import {BaseManagementComponent} from '../base-management.component';
import {AuthenticationService} from '../../../../extensions/services/authentication/authentication.service';
import {startWith, switchMap} from 'rxjs/operators';
import {TableAction, TableMapper} from '../../../../model/Table';

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
