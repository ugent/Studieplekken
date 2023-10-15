import {Component, EventEmitter, Input, Output} from '@angular/core';
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

    @Input() user: User;
    @Input() addableAuthorities: Authority[];
    @Input() addedAuthorities: Authority[];

    @Output() updatedAuthorities: EventEmitter<void>;

    constructor(
        private authoritiesService: AuthoritiesService
    ) {
        super();

        this.updatedAuthorities = new EventEmitter();
    }

    ngOnInit(): void {
        super.ngOnInit();

        this.refresh$.subscribe(() =>
            this.updatedAuthorities.emit()
        );
    }

    setupForm(item: Authority = AuthorityConstructor.new()): void {
        this.formGroup = new FormGroup({
            authority: new FormControl(item.authorityId, Validators.required)
        });
    }

    storeAdd(body: any): void {
        this.sendBackendRequest(
            this.authoritiesService.addUserToAuthority(this.user.userId, body.authority)
        );
    }

    storeDelete(item: Authority): void {
        this.sendBackendRequest(
            this.authoritiesService.deleteUserFromAuthority(this.user.userId, item.authorityId)
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
