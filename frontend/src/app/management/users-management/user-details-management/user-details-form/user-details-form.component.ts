import { Component, Input, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { User, UserSettings } from '../../../../shared/model/User';
import { FormControl, FormGroup } from '@angular/forms';
import { ApplicationTypeFunctionalityService } from '../../../../services/functionality/application-type/application-type-functionality.service';
import { AuthenticationService } from 'src/app/services/authentication/authentication.service';
import { environment } from 'src/environments/environment';
import { UserService } from 'src/app/services/api/users/user.service';

@Component({
  selector: 'app-user-details-form',
  templateUrl: './user-details-form.component.html',
  styleUrls: ['./user-details-form.component.scss'],
})
export class UserDetailsFormComponent implements OnInit {
  @Input() userObs: Observable<User>;

  userQueryingError = false;

  formGroup = new FormGroup({
    id: new FormControl(''),
    firstName: new FormControl(''),
    lastName: new FormControl(''),
    mail: new FormControl(''),
    penaltyPoints: new FormControl(''),
    institution: new FormControl(''),
  });
  settingsFormGroup: FormGroup = new FormGroup({
    receiveMailConfirmation: new FormControl({value: true, disabled: false})
  });

  showPenaltyPoints: boolean;

  constructor(
    private functionalityService: ApplicationTypeFunctionalityService,
    private authenticationService: AuthenticationService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.formGroup.disable();

    this.userObs.subscribe((next) => {
      this.setup(next);
    });

    this.showPenaltyPoints = this.functionalityService.showPenaltyFunctionality();
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
