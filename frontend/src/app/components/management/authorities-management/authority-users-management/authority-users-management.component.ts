import {Component, OnInit, TemplateRef} from '@angular/core';
import {
    AbstractControl,
    UntypedFormControl,
    UntypedFormGroup,
    Validators
} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatDialogRef} from '@angular/material/dialog/dialog-ref';
import {ActivatedRoute} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthoritiesService} from '@/services/api/authorities/authorities.service';
import {UserService} from '@/services/api/users/user.service';
import {
    AuthorityToManageService
} from '@/services/single-point-of-truth/authority-to-manage/authority-to-manage.service';
import {Authority} from '@/model/Authority';
import {User} from '@/model/User';

@Component({
    selector: 'app-authority-users-management',
    templateUrl: './authority-users-management.component.html',
    styleUrls: ['./authority-users-management.component.scss'],
})
export class AuthorityUsersManagementComponent implements OnInit {

    constructor(
        private authoritiesService: AuthoritiesService,
        private authorityToManageService: AuthorityToManageService,
        private route: ActivatedRoute,
        private userService: UserService,
        private modalService: MatDialog
    ) {
    }

    get firstName(): AbstractControl {
        return this.userSearchFormGroup.get('firstName');
    }

    get lastName(): AbstractControl {
        return this.userSearchFormGroup.get('lastName');
    }
    authority: Authority;

    usersInAuthorityObs: Observable<User[]>;

    userSearchFormGroup = new UntypedFormGroup({
        firstName: new UntypedFormControl('', Validators.required.bind(this)),
        lastName: new UntypedFormControl('', Validators.required.bind(this)),
    });

    selectedUserFormControl = new UntypedFormControl('', Validators.required.bind(this));

    userSearchResult: User[] = [];

    userPreparedToDelete: User;

    successRetrievingAuthority: boolean = undefined;
    successAddingAuthority: boolean = undefined;
    successSearchingUsers: boolean = undefined;
    successDeletingAuthority: boolean = undefined;

    isValidUserToAdd: boolean = undefined;

    addModal: MatDialogRef<unknown>;
    deleteModal: MatDialogRef<unknown>;

    protected readonly undefined = undefined;

    // *********************************
    // *   Add user to the authority   *
    // *********************************

    ngOnInit(): void {
        this.authority = this.authorityToManageService.authority;

        // It is possible that the authority to manage in not provided through the
        // AuthorityToManageService if the user refreshes the browser while being in this page,
        // or directly used the url to go to this page
        if (this.authority === undefined) {
            const id = this.route.snapshot.paramMap.get('authorityId');
            this.authoritiesService.getAuthority(Number(id)).subscribe((next) => {
                this.authority = next;
                this.setUsersObs(this.authority.authorityId);
            });
        } else {
            this.setUsersObs(this.authority.authorityId);
        }
    }

    closeModal(): void {
        this.modalService.closeAll();
    }

    prepareToAddUserToAuthority(template: TemplateRef<unknown>): void {
        this.successAddingAuthority = undefined;
        this.successSearchingUsers = undefined;
        this.isValidUserToAdd = undefined;
        this.userSearchFormGroup.setValue({
            firstName: '',
            lastName: '',
        });
        this.selectedUserFormControl.setValue('');
        this.userSearchResult = [];
        this.selectedUserFormControl.disable();
        this.addModal = this.modalService.open(template, {panelClass: ['cs--cyan', 'bigmodal']});

    }

    // **************************************
    // *   Delete user from the authority   *
    // **************************************

    searchForUserByFirstAndLastName(firstName: string, lastName: string): void {
        let usersObs: Observable<User[]>;

        if (firstName === '') {
            usersObs = this.userService.getUsersByLastName(lastName);
        } else if (lastName === '') {
            usersObs = this.userService.getUsersByFirstName(firstName);
        } else {
            usersObs = this.userService.getUsersByFirstAndLastName(
                firstName,
                lastName
            );
        }

        this.subscribeOnSearchedUsers(usersObs);
    }

    addUserToAuthority(user: User): void {
        const userId: string = user.userId;
        const authorityId = this.authority.authorityId;
        this.successAddingAuthority = null;

        this.authoritiesService.addUserToAuthority(userId, authorityId).subscribe(
            () => {
                this.successAddingAuthority = true;
                this.setUsersObs(authorityId);
                this.addModal.close();
            },
            () => {
                this.successAddingAuthority = false;
            }
        );
    }

    // *******************
    // *   Auxiliaries   *
    // *******************

    prepareToDeleteUserFromAuthority(
        user: User,
        template: TemplateRef<unknown>
    ): void {
        this.successDeletingAuthority = undefined;
        this.userPreparedToDelete = user;
        this.deleteModal = this.modalService.open(template, {panelClass: ['cs--cyan', 'bigmodal']});

    }

    deleteUserFromAuthority(userId: string, authorityId: number): void {
        this.successDeletingAuthority = null;
        this.authoritiesService
            .deleteUserFromAuthority(userId, authorityId)
            .subscribe(
                () => {
                    this.successDeletingAuthority = true;
                    this.setUsersObs(authorityId); // reload users data
                    this.deleteModal.close();
                },
                () => {
                    this.successDeletingAuthority = false;
                }
            );
    }

    setUsersObs(authorityId: number): void {
        this.usersInAuthorityObs = this.authoritiesService.getUsersFromAuthority(
            authorityId
        );
    }

    // ****************************
    // *   Form control getters   *
    // ****************************

    validForm(): boolean {
        return !this.firstName.invalid || !this.lastName.invalid;
    }

    subscribeOnSearchedUsers(usersObs: Observable<User[]>): void {
        usersObs.subscribe(
            (next) => {
                this.successSearchingUsers = true;
                this.userSearchResult = next;
                this.selectedUserFormControl.enable();
            },
            () => {
                this.successSearchingUsers = false;
            }
        );
    }
}
