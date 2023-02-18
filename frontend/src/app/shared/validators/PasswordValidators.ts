import { UntypedFormGroup, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * The password needs to contain at least:
 *   - 1 capital letter
 *   - 1 number
 * And must be at least 8 characters long
 */
export const validPassword: ValidatorFn = (
  control: UntypedFormGroup
): ValidationErrors | null => {
  const password: string = control.get('password').value as string;
  return password.match(/^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$/gm)
    ? null
    : { passwordNotValid: true };
};

/**
 * Check whether the password and confirmation password match
 */
export const validateConfirmationPassword: ValidatorFn = (
  control: UntypedFormGroup
): ValidationErrors | null => {
  const password = control.get('password').value as string;
  const confirmation = control.get('confirmPassword').value as string;
  return password === confirmation
    ? null
    : { confirmationPasswordDoesNotMatch: true };
};
