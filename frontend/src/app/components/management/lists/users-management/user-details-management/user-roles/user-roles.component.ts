import {Component, Input, OnInit} from '@angular/core';
import {User, UserConstructor} from '../../../../../../model/User';
import {FormControl, FormGroup, UntypedFormControl, UntypedFormGroup} from '@angular/forms';
import {UserDetailsService} from '../../../../../../extensions/services/single-point-of-truth/user-details/user-details.service';
import {UserService} from '../../../../../../extensions/services/api/users/user.service';
import {msToShowFeedback} from '../../../../../../app.constants';
import {ModalComponent} from '../../../../../stad-gent-components/molecules/modal/modal.component';

@Component({
    selector: 'app-user-roles',
    templateUrl: './user-roles.component.html',
    styleUrls: ['./user-roles.component.scss'],
})
export class UserRolesComponent implements OnInit {
    @Input() user: User;

    userUpdatingSuccess: boolean = undefined;

    roleFormGroup: FormGroup;

    modalRef: ModalComponent;

    constructor(
        private userDetailsService: UserDetailsService,
        private userService: UserService
    ) {
    }

    ngOnInit(): void {
        this.setupForm();
    }

    setupForm(): void {
       this.roleFormGroup = new FormGroup({
            admin: new FormControl(this.user.admin),
        });
    }

    submitUpdateUser(): void {
        const user = UserConstructor.newFromObj(
            this.user
        );

        user.admin = this.roleFormGroup.value.admin;

        this.userService.updateUser(this.user.userId, user).subscribe(
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
        return this.roleFormGroup.value.admin === this.user.admin;
    }

    resetRolesFormArrayButtonClick(): void {
        this.roleFormGroup.value.admin.setValue(
            this.user.admin
        );
    }

    onAdminClick(e: Event, templateAdd: ModalComponent, templateRemove: ModalComponent, checkboxValue: boolean): void {
        e.preventDefault();
        if (checkboxValue) {
            this.modalRef = templateRemove;
            templateRemove.open();
        } else {
            this.modalRef = templateAdd;
            templateAdd.open();
        }
    }

    confirmAdminChange(): void {
        this.roleFormGroup.patchValue({
            admin: !this.roleFormGroup.get('admin').value
        });

        this.modalRef.close();
    }

    declineAdminChange(): void {
        this.modalRef.close();
    }
}
