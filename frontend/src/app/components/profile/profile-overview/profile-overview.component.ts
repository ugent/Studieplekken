import {Component, Input, OnInit} from '@angular/core';
import {User, UserSettings} from '../../../model/User';
import {FormControl, FormGroup} from '@angular/forms';
import {UserService} from '../../../extensions/services/api/users/user.service';

@Component({
    selector: 'app-profile-overview',
    templateUrl: './profile-overview.component.html',
    styleUrls: ['./profile-overview.component.scss'],
})
export class ProfileOverviewComponent implements OnInit {

    @Input() user: User;

    protected settingsFormGroup: FormGroup;

    constructor(
        private userService: UserService
    ) {
    }

    ngOnInit(): void {
        this.setupForm();
    }

    setupForm(): void {
        this.settingsFormGroup = new FormGroup({
            receiveMailConfirmation: new FormControl(this.user.userSettings.receiveMailConfirmation)
        });
    }

    storeUpdate(userSettings: UserSettings = this.settingsFormGroup.value): void {
        this.userService.updateUserSettings(
            this.user.userId, userSettings
        ).subscribe();
    }
}
