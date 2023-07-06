import { Component } from '@angular/core';
import { UntypedFormControl, UntypedFormGroup, Validators } from '@angular/forms';
import {
  validateConfirmationPassword,
  validPassword,
} from '../../../extensions/validators/PasswordValidators';
import { AuthenticationService } from '../../../extensions/services/authentication/authentication.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-profile-change-password',
  templateUrl: './profile-change-password.component.html',
  styleUrls: ['./profile-change-password.component.scss'],
})
export class ProfileChangePasswordComponent {
  formGroup: UntypedFormGroup = new UntypedFormGroup(
    {
      oldPassword: new UntypedFormControl('', Validators.required.bind(this)),
      password: new UntypedFormControl('', [Validators.minLength(8)]),
      confirmPassword: new UntypedFormControl(''),
    },
    { validators: [validateConfirmationPassword, validPassword] }
  );

  wrongOldPassword = false;
  otherErrorOnUpdating = false;

  constructor(private authenticationService: AuthenticationService) {}

  submitChangePassword(data: {
    oldPassword: string;
    password: string;
    confirmPassword: string;
  }): void {
    this.wrongOldPassword = false;
    this.otherErrorOnUpdating = false;

    if (this.formGroup.valid) {
      this.authenticationService
        .updatePassword(data.oldPassword, data.password)
        .subscribe(
          () => {
            this.successFullyUpdatedPasswordHandler();
          },
          (error: HttpErrorResponse) => {
            this.errorOnUpdatingPasswordHandler(error);
          }
        );
    }
  }

  cancelChangePassword(): void {
    this.wrongOldPassword = false;
    this.otherErrorOnUpdating = false;

    this.formGroup.setValue({
      oldPassword: '',
      password: '',
      confirmPassword: '',
    });
  }

  disabledSubmitButton(data: {
    oldPassword: string;
    password: string;
    confirmPassword: string;
  }): boolean {
    return (
      data.oldPassword === '' ||
      data.password === '' ||
      data.confirmPassword === '' ||
      !this.formGroup.valid
    );
  }

  isPasswordValid(): boolean {
    return validPassword(this.formGroup) === null;
  }

  confirmPasswordMatches(): boolean {
    return (
      this.formGroup.get('password').value ===
      this.formGroup.get('confirmPassword').value
    );
  }

  successFullyUpdatedPasswordHandler(): void {
    // reset form
    this.cancelChangePassword();
  }

  errorOnUpdatingPasswordHandler(error: HttpErrorResponse): void {
    // UNAUTHORIZED
    if (error.status === 401) {
      this.wrongOldPassword = true;
    } else {
      this.otherErrorOnUpdating = true;
    }
  }
}
