import {Component, OnInit, TemplateRef} from '@angular/core';
import {objectExists} from '../../../extensions/util/GeneralFunctions';
import {Observable, of} from 'rxjs';
import {User, UserConstructor} from '../../../extensions/model/User';
import {UserService} from '../../../extensions/services/api/users/user.service';
import {catchError, tap} from 'rxjs/operators';
import {UntypedFormControl, UntypedFormGroup} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';

@Component({
    selector: 'app-admins-management',
    templateUrl: './admins-management.component.html',
    styleUrls: ['./admins-management.component.scss'],
})
export class AdminsManagementComponent implements OnInit {
    loading = true;
    adminsObs: Observable<User[]>;

    formGroup = new UntypedFormGroup({
        firstName: new UntypedFormControl(''),
        lastName: new UntypedFormControl(''),
    });
    neverSearched = true;
    filteredUsers: Observable<User[]>;

    errorOnRetrievingAdmins = false; // booleanId = 0

    constructor(
        private userService: UserService,
        private modalService: MatDialog,
    ) {
    }

    ngOnInit(): void {
        this.adminsObs = this.userService.getAdmins().pipe(
            tap(() => (this.loading = false)),
            catchError((e) => {
                this.errorOnRetrievingAdmins = !!e;
                return of<User[]>([]);
            })
        );
    }

    closeModal(): void {
        this.modalService.closeAll();
    }

    /*
     * If any of the input fields are not empty without trimming, enable the 'search' button
     */
    showAdd(template: TemplateRef<unknown>): void {
        this.modalService.open(template, {panelClass: ['cs--cyan', 'bigmodal']});
    }

    addAdmin(user: User): void {
        const clone = UserConstructor.newFromObj(user);
        clone.admin = true;
        this.userService
            .updateUser(user.userId, clone)
            .subscribe(
                () =>
                    (this.adminsObs = this.userService.getAdmins().pipe(
                        tap(() => (this.loading = false)),
                        catchError((e) => {
                            this.errorOnRetrievingAdmins = !!e;
                            return of<User[]>([]);
                        })
                    ))
            );

        this.modalService.closeAll();
    }
}
