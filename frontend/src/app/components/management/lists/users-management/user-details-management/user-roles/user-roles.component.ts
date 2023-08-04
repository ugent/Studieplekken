import {Component, Input, OnInit, TemplateRef} from '@angular/core';
import {Observable} from 'rxjs';
import {User, UserConstructor} from '../../../../../../model/User';
import {AbstractControl, UntypedFormControl, UntypedFormGroup} from '@angular/forms';
import {UserDetailsService} from '../../../../../../extensions/services/single-point-of-truth/user-details/user-details.service';
import {UserService} from '../../../../../../extensions/services/api/users/user.service';
import {msToShowFeedback} from '../../../../../../app.constants';
import {MatDialog} from '@angular/material/dialog';
import {MatDialogRef} from '@angular/material/dialog/dialog-ref';

@Component({
    selector: 'app-user-roles',
    templateUrl: './user-roles.component.html',
    styleUrls: ['./user-roles.component.scss'],
})
export class UserRolesComponent implements OnInit {
    @Input() userObs: Observable<User>;
    user: User;

    userUpdatingSuccess: boolean = undefined;

    roleFormGroup = new UntypedFormGroup({
        admin: new UntypedFormControl(''),
    });

    modalRef: MatDialogRef<unknown>;

    constructor(
        private userDetailsService: UserDetailsService,
        private userService: UserService,
        private modalService: MatDialog
    ) {
    }

    ngOnInit(): void {
        this.userObs.subscribe((next) => {
            this.user = next;
            this.admin.setValue(this.user.admin);
        });
    }

    submitUpdateUser(): void {
        const clone = UserConstructor.newFromObj(this.user);
        clone.admin = this.admin.value as boolean;
        this.userService.updateUser(this.user.userId, clone).subscribe(
            () => {
                this.successUpdatingUserHandler();
            },
            () => {
                this.errorUpdatingUserHandler();
            }
        );
    }

    successUpdatingUserHandler(): void {
        this.userDetailsService.loadUser(this.user.userId);
        this.userUpdatingSuccess = true;
        setTimeout(() => (this.userUpdatingSuccess = undefined), msToShowFeedback);
    }

    errorUpdatingUserHandler(): void {
        this.userUpdatingSuccess = false;
        setTimeout(() => (this.userUpdatingSuccess = undefined), msToShowFeedback);
    }

    disableRoleUpdateButton(): boolean {
        return this.admin.value === this.user.admin;
    }

    resetRolesFormArrayButtonClick(): void {
        this.admin.setValue(this.user.admin);
    }

    onAdminClick(event: Event, templateAdd: TemplateRef<unknown>, templateRemove: TemplateRef<unknown>, checkboxValue: boolean): void {
        event.preventDefault();
        if (checkboxValue) {
            this.modalRef = this.modalService.open(templateRemove);
        } else {
            this.modalRef = this.modalService.open(templateAdd);
        }
    }

    confirmAdminChange(): void {
        this.admin.setValue(!this.admin.value);
        this.modalRef.close();
    }

    declineAdminChange(): void {
        this.modalRef.close();
    }

    // ********************************************
    // *   Getters for roleFormGroup's controls   *
    // ********************************************
    get admin(): AbstractControl {
        return this.roleFormGroup.get('admin');
    }
}
