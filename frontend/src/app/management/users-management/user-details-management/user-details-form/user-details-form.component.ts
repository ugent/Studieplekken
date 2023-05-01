import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {User, UserSettings} from '../../../../shared/model/User';
import {UntypedFormControl, UntypedFormGroup} from '@angular/forms';
import {AuthenticationService} from 'src/app/services/authentication/authentication.service';
import {environment} from 'src/environments/environment';
import {UserService} from 'src/app/services/api/users/user.service';

@Component({
    selector: 'app-user-details-form',
    templateUrl: './user-details-form.component.html',
    styleUrls: ['./user-details-form.component.scss'],
})
export class UserDetailsFormComponent implements OnInit {
    @Input() userObs: Observable<User>;

    userQueryingError = false;

    formGroup = new UntypedFormGroup({
        id: new UntypedFormControl(''),
        firstName: new UntypedFormControl(''),
        lastName: new UntypedFormControl(''),
        mail: new UntypedFormControl(''),
        penaltyPoints: new UntypedFormControl(''),
        institution: new UntypedFormControl(''),
    });
    settingsFormGroup: UntypedFormGroup = new UntypedFormGroup({
        receiveMailConfirmation: new UntypedFormControl({value: true, disabled: false})
    });

    constructor(
        private authenticationService: AuthenticationService,
        private userService: UserService
    ) {
    }

    ngOnInit(): void {
        this.formGroup.disable();

        this.userObs.subscribe((next) => {
            this.setup(next);
        });
    }

    setup(user: User): void {
        // setup formGroup for details of user
        this.formGroup.setValue({
            id: user.userId,
            firstName: user.firstName,
            lastName: user.lastName,
            mail: user.mail,
            penaltyPoints: user.penaltyPoints,
            institution: user.institution,
        });
        this.formGroup.disable();

        this.settingsFormGroup.setValue({
            receiveMailConfirmation: user.userSettings.receiveMailConfirmation
        });
        // NOTE(ydndonck): This update functionality will never get triggered if the form is disabled.
        // But leaving it implemented here in case it is wanted in the future that admins can edit user
        // settings through this UI.
        this.settingsFormGroup.valueChanges.subscribe((newSettings: UserSettings) => {
            this.saveUserSettings(newSettings);
        });


        this.userQueryingError = false;
    }

    showImpersonate(): boolean {
        return this.authenticationService.isAdmin() && !environment.production;
    }

    impersonate(user): void {
        this.authenticationService.substituteLogin(user.mail);
    }

    saveUserSettings(userSettings: UserSettings): void {
        this.userService.updateUserSettings(this.formGroup.get('id').value, userSettings).subscribe();
    }

}
