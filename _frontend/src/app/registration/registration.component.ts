import { Component, OnInit } from '@angular/core';
import { Location } from '@angular/common'
import {
  FormControl,
  FormGroup,
  Validators
} from '@angular/forms';
import {minLengthPwd} from "../../environments/environment";
import {Router} from "@angular/router";
import {VerificationService} from "../../services/verification.service";
import {samePasswordsValidator} from "../shared/validators.directive";
import {UserService} from "../../services/user.service";
import {IUser} from "../../interfaces/IUser";

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})
export class RegistrationComponent implements OnInit {

  //parameters for validation
  minLengthPwd = minLengthPwd; //minimum length password, see environment.ts
  disabled = false;

  displayFail = 'none';
  httpStatus: number;

  // Note: if you change any of these keywords, then you should change the
  // form's formControlName attribute accordingly (in registration.component.html)
  r = new FormGroup({
    email: new FormControl('', Validators.required),
    pwd: new FormControl('', [Validators.required, Validators.minLength(this.minLengthPwd)]),
    confPwd: new FormControl('', Validators.required),
  }, { validators: samePasswordsValidator });

  constructor(private router: Router,
              private verificationService: VerificationService,
              private managementService: UserService,
              private location: Location) { }

  ngOnInit(): void { }

  async onSubmit(newUser) {
    if (this.r.valid) {
      let n = {email: newUser.email, password: newUser.pwd};
      this.verificationService.setNewUser(n);

      this.disabled = true;

      // the backend will only use email and password attributes of the User instance. But Spring
      // needs a complete User in order to be able to map the HTTP-request to the correct Controller and method
      let user: IUser = {
        augentID: null,
        lastName: null,
        firstName: null,
        mail: newUser.email,
        password: newUser.pwd,
        institution: null,
        roles: null,
        penaltyPoints: null,
        barcode: null,
      };

      await this.managementService.addUser(user).subscribe(n => {
        this.r.reset();
        this.router.navigate(['verification']).then();
      }, error => {
        // Two possible HTTP status codes: HttpStatus.CONFLICT (409) when an account with the given email address
        // already exists, and HttpStatus.FORBIDDEN (403) when the LDAP doesn't recognize the given email address
        // which means the email address does not belong to an institution within the Association UGent
        // Based on the value of this.httpStatus, the correct pop-up will be shown.
        this.httpStatus = error.status;
        this.displayFail = 'block';
      });
    } else {
      // when a field was left empty and untouched this loop will mark it as touched and this will trigger validation
      Object.keys(this.r.controls).forEach(field => {
        const control = this.r.get(field);
        control.markAsTouched({ onlySelf: true });
      });
    }
  }

  cancel(event){
    event.preventDefault();
    this.r.reset();
    this.location.back();
  }
}
