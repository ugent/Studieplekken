import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from '../../../extensions/services/authentication/authentication.service';
import {User, UserSettings} from '../../../extensions/model/User';
import {UntypedFormControl, UntypedFormGroup} from '@angular/forms';
import {Observable} from 'rxjs';
import {UserService} from 'src/app/extensions/services/api/users/user.service';

@Component({
    selector: 'app-profile-overview',
    templateUrl: './profile-overview.component.html',
    styleUrls: ['./profile-overview.component.scss'],
})
export class ProfileOverviewComponent implements OnInit {
    userObs: Observable<User>;
    formGroup: UntypedFormGroup;
    settingsFormGroup: UntypedFormGroup;

    constructor(
        private authenticationService: AuthenticationService,
        private userService: UserService
    ) {
        this.prepareEmptyFormGroup();
        this.authenticationService.user.subscribe((next) => {
            this.setupFormGroup(next);
        });
        this.userObs = this.authenticationService.user;
    }

    ngOnInit(): void {}

    prepareEmptyFormGroup(): void {
        this.formGroup = new UntypedFormGroup({
            userId: new UntypedFormControl({value: '', disabled: true}),
            firstName: new UntypedFormControl({value: '', disabled: true}),
            lastName: new UntypedFormControl({value: '', disabled: true}),
            mail: new UntypedFormControl({value: '', disabled: true}),
            penaltyPoints: new UntypedFormControl({value: 0, disabled: true}),
            institution: new UntypedFormControl({value: '', disabled: true}),
            password: new UntypedFormControl({value: '', disabled: true}),
            confirmPassword: new UntypedFormControl({value: '', disabled: true})
        });
        this.settingsFormGroup = new UntypedFormGroup({
            receiveMailConfirmation: new UntypedFormControl({value: true, disabled: false})
        });

    }

    setupFormGroup(user: User): void {
        this.formGroup.setValue({
            userId: user.userId,
            firstName: user.firstName,
            lastName: user.lastName,
            mail: user.mail,
            penaltyPoints: user.penaltyPoints,
            institution: user.institution,
            password: user.password,
            confirmPassword: user.password,
        });
        this.settingsFormGroup.setValue({
            receiveMailConfirmation: user.userSettings.receiveMailConfirmation
        });
        this.settingsFormGroup.valueChanges.subscribe((newSettings: UserSettings) => {
            this.saveUserSettings(newSettings);
        });
    }

    saveUserSettings(userSettings: UserSettings): void {
        this.userService.updateUserSettings(this.formGroup.get('userId').value, userSettings).subscribe();
    }

}
