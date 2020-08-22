import { Component, OnInit } from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {validateConfirmationPassword, validPassword} from '../../shared/validators/PasswordValidators';

@Component({
  selector: 'app-profile-change-password',
  templateUrl: './profile-change-password.component.html',
  styleUrls: ['./profile-change-password.component.css']
})
export class ProfileChangePasswordComponent implements OnInit {
  formGroup: FormGroup = new FormGroup({
    password: new FormControl('',
      [Validators.minLength(8)]),
    confirmPassword: new FormControl('')
  }, {validators: [validateConfirmationPassword, validPassword]});

  constructor() { }

  ngOnInit(): void {
  }

  submitChangePassword(data: {password: string, confirmPassword: string}): void {
    if (this.formGroup.valid) {
      // TODO
      console.log(data);
    }
  }

  cancelChangePassword(): void {
    this.formGroup.setValue({password: '', confirmPassword: ''});
  }

  disabledSubmitButton(data: {password: string, confirmPassword: string}): boolean {
    return data.password === '' || data.confirmPassword === '' || !this.formGroup.valid;
  }

  isPasswordValid(): boolean {
    return validPassword(this.formGroup) === null;
  }

  confirmPasswordMatches(): boolean {
    return this.formGroup.get('password').value === this.formGroup.get('confirmPassword').value;
  }
}
