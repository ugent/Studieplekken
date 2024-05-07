import {Component, Input, OnInit} from '@angular/core';
import {User, UserSettings} from '@/model/User';
import {FormControl, FormGroup} from '@angular/forms';
import {AuthenticationService} from 'src/app/services/authentication/authentication.service';
import {environment} from 'src/environments/environment';
import {UserService} from 'src/app/services/api/users/user.service';
import {Router} from '@angular/router';

@Component({
    selector: 'app-user-details-form',
    templateUrl: './user-details-form.component.html',
    styleUrls: ['./user-details-form.component.scss'],
})
export class UserDetailsFormComponent implements OnInit {
    @Input() user: User;

    protected detailsFormGroup: FormGroup;
    protected settingsFormGroup: FormGroup;

    constructor(
        private authenticationService: AuthenticationService,
        private userService: UserService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        this.setupFormGroups();

        this.settingsFormGroup.valueChanges.subscribe((newSettings: UserSettings) => {
            this.saveUserSettings(newSettings);
        });
    }

    setupFormGroups(): void {
        this.settingsFormGroup = new FormGroup({
            receiveMailConfirmation: new FormControl(this.user.userSettings.receiveMailConfirmation)
        });
        this.detailsFormGroup = new FormGroup({
            id: new FormControl(this.user.userId),
            firstName: new FormControl(this.user.firstName),
            lastName: new FormControl(this.user.lastName),
            mail: new FormControl(this.user.mail),
            penaltyPoints: new FormControl(this.user.penaltyPoints),
            institution: new FormControl(this.user.institution)
        });

        this.detailsFormGroup.disable();
    }

    showImpersonate(): boolean {
        return this.user.isAdmin() && !environment.production;
    }

    impersonate(user: User): void {
        this.authenticationService.substituteLogin(user.mail);

        void this.router.navigate(['/profile/overview']);
    }

    saveUserSettings(userSettings: UserSettings): void {
        this.userService.updateUserSettings(this.user.userId, userSettings).subscribe();
    }
}
