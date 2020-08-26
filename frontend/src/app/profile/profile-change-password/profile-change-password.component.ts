import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {validateConfirmationPassword, validPassword} from '../../shared/validators/PasswordValidators';
import {AuthenticationService} from '../../services/authentication/authentication.service';
import {HttpErrorResponse} from '@angular/common/http';

@Component({
  selector: 'app-profile-change-password',
  templateUrl: './profile-change-password.component.html',
  styleUrls: ['./profile-change-password.component.css']
})
export class ProfileChangePasswordComponent implements OnInit {
  formGroup: FormGroup = new FormGroup({
    oldPassword: new FormControl('', Validators.required),
    password: new FormControl('',
      [Validators.minLength(8)]),
    confirmPassword: new FormControl('')
  }, {validators: [validateConfirmationPassword, validPassword]});

  wrongOldPassword = false;
  otherErrorOnUpdating = false;

  constructor(private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
  }

  submitChangePassword(data: {oldPassword: string, password: string, confirmPassword: string}): void {
    this.wrongOldPassword = false;
    this.otherErrorOnUpdating = false;

    if (this.formGroup.valid) {
      this.authenticationService.updatePassword(data.oldPassword, data.password).subscribe(
        () => {
          this.successFullyUpdatedPasswordHandler();
        }, (error: HttpErrorResponse) => {
          this.errorOnUpdatingPasswordHandler(error);
        }
      );
    }
  }

  cancelChangePassword(): void {
    this.wrongOldPassword = false;
    this.otherErrorOnUpdating = false;

    this.formGroup.setValue({oldPassword: '', password: '', confirmPassword: ''});
  }

  disabledSubmitButton(data: {oldPassword: string, password: string, confirmPassword: string}): boolean {
    return data.oldPassword === '' || data.password === '' || data.confirmPassword === '' || !this.formGroup.valid;
  }

  isPasswordValid(): boolean {
    return validPassword(this.formGroup) === null;
  }

  confirmPasswordMatches(): boolean {
    return this.formGroup.get('password').value === this.formGroup.get('confirmPassword').value;
  }

  successFullyUpdatedPasswordHandler(): void {
    console.log('successfully updated password');
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
