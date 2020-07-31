import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "../../services/authentication.service";
import {IUser} from "../../interfaces/IUser";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {samePasswordsValidator} from "../shared/validators.directive";
import {BarcodeService} from "../../services/barcode.service";
import {minLengthPwd} from "../../environments/environment";

@Component({
  selector: 'app-edit-profile',
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.css']
})
export class EditProfileComponent implements OnInit {
  //parameters for validation
  minLengthPwd = minLengthPwd; //minimum length password, change its value in ../../environments/environment

  editPassword = false;
  errorMessageVisible = false;
  succesMessageVisible = false;
  messageSecondsVisible = 3;
  succesMessage: string;
  errorMessage: string;
  barcode: any;

  user: IUser;

  profileForm = new FormGroup({
    firstName: new FormControl({value: '', disabled: true}),
    lastName: new FormControl({value: '', disabled: true}),
    institution: new FormControl({value: '', disabled: true}),
    //roles: new FormControl({value: '', disabled: true}),
    identificationNumber: new FormControl({value: '', disabled: true}),
    penaltyPoints: new FormControl({value: '', disabled: true}),
    email: new FormControl({value: '', disabled: true}),
    password: new FormControl({value: '', disabled: true}),
    pwd: new FormControl('', Validators.minLength(this.minLengthPwd)),
    confPwd: new FormControl('')
  }, {validators: samePasswordsValidator});

  constructor(public authenticationService: AuthenticationService,
              private barcodeService: BarcodeService) {
  }

  ngOnInit(): void {
    // get user object
    this.authenticationService.getRequestUser();
    this.user = this.authenticationService.getCurrentUser();

    // show barcode
    if(this.user.barcode !== null) {
      this.barcodeService.getBarcodeImage(this.user.barcode)
        .subscribe(data => {
            this.createImageFromBlob(data);
          }
        );
    }

    this.profileForm.setValue({
      firstName: this.user.firstName,
      lastName: this.user.lastName,
      institution: this.user.institution,
      //roles: this.user.roles,
      identificationNumber: this.user.augentID,
      penaltyPoints: this.user.penaltyPoints,
      email: this.user.mail,
      password: "notshowing",
      pwd: '',
      confPwd: ''
    });
  }

  changeVisibilityPasswordChangeFields(): void {
    this.editPassword = !this.editPassword;
    this.profileForm.controls.pwd.reset();
    this.profileForm.controls.confPwd.reset();
  }

  showErrorMessage(message: string): void {
    this.errorMessageVisible = true;
    const that = this;
    this.errorMessage = message;
    setTimeout(() => {
      that.errorMessageVisible = false;
    }, this.messageSecondsVisible * 1000);
  }

  showSuccessMessage(message: string): void {
    this.succesMessageVisible = true;
    const that = this;
    this.succesMessage = message;
    setTimeout(() => {
      that.succesMessageVisible = false;
    }, this.messageSecondsVisible * 1000);
  }

  createImageFromBlob(image: Blob) {
    // show image from Blob object
    const reader = new FileReader();
    reader.addEventListener('load', () => {
      this.barcode = reader.result;
    }, false);

    if (image) {
      reader.readAsDataURL(image);
    }
  }

  saveChanges(form: any): void {
    // edit user details

    if (this.editPassword) {
      if (this.profileForm.valid) {
        // if his password has been changed and the form is valid, persist the change to the backend
        let user = this.authenticationService.getCurrentUser();
        let u = {
          lastName: user.lastName, firstName: user.firstName, mail: user.mail, password: form.confPwd,
          institution: user.institution, augentID: user.augentID, penaltyPoints: user.penaltyPoints,
          barcode: user.barcode, roles: user.roles
        } as IUser;
        this.authenticationService.updateOwnProfile(u);
        //profile.editSuccess is key in translate files
        this.showSuccessMessage("profile.editSuccess");
      } else {
        //profile.editFailed is key in translate files
        this.showErrorMessage("profile.editFailed");

        //when a field was left empty and untouched this loop will mark it as touched and this will trigger validation
        Object.keys(this.profileForm.controls).forEach(field => {
          const control = this.profileForm.get(field);
          control.markAsTouched({onlySelf: true});
        });
      }
    }
  }

}
