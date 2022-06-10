import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { User, UserSettings } from '../../shared/model/User';
import { FormControl, FormGroup } from '@angular/forms';
import { ApplicationTypeFunctionalityService } from '../../services/functionality/application-type/application-type-functionality.service';
import { Observable } from 'rxjs';
import { UserService } from 'src/app/services/api/users/user.service';

@Component({
  selector: 'app-profile-overview',
  templateUrl: './profile-overview.component.html',
  styleUrls: ['./profile-overview.component.scss'],
})
export class ProfileOverviewComponent implements OnInit {
  userObs: Observable<User>;
  formGroup: FormGroup;
  settingsFormGroup: FormGroup;

  showPenaltyPoints: boolean;

  constructor(
    private authenticationService: AuthenticationService,
    private functionalityService: ApplicationTypeFunctionalityService,
    private userService: UserService
  ) {
    this.prepareEmptyFormGroup();
    this.authenticationService.user.subscribe((next) => {
      this.setupFormGroup(next);
    });
    this.userObs = this.authenticationService.user;
  }

  ngOnInit(): void {
    this.showPenaltyPoints = this.functionalityService.showPenaltyFunctionality();
  }

  prepareEmptyFormGroup(): void {
    this.formGroup = new FormGroup({
      userId: new FormControl({ value: '', disabled: true }),
      firstName: new FormControl({ value: '', disabled: true }),
      lastName: new FormControl({ value: '', disabled: true }),
      mail: new FormControl({ value: '', disabled: true }),
      penaltyPoints: new FormControl({ value: 0, disabled: true }),
      institution: new FormControl({ value: '', disabled: true }),
      password: new FormControl({ value: '', disabled: true }),
      confirmPassword: new FormControl({ value: '', disabled: true })
    });
    this.settingsFormGroup = new FormGroup({
      receiveMailConfirmation: new FormControl({value: true, disabled: false})
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
