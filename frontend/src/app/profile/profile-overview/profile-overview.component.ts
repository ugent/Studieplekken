import {Component, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {AuthenticationService} from '../../services/authentication/authentication.service';
import {User} from '../../shared/model/User';
import {FormControl, FormGroup} from '@angular/forms';

@Component({
  selector: 'app-profile-overview',
  templateUrl: './profile-overview.component.html',
  styleUrls: ['./profile-overview.component.css']
})
export class ProfileOverviewComponent implements OnInit {
  user: User;
  formGroup: FormGroup;

  constructor(private authenticationService: AuthenticationService) {
    this.prepareEmptyFormGroup();
    authenticationService.user.subscribe(next => {
      this.setupFormGroup(next);
    });
  }

  ngOnInit(): void {
  }

  prepareEmptyFormGroup(): void {
    this.formGroup = new FormGroup({
      augentID: new FormControl({value: '', disabled: true}),
      firstName: new FormControl({value: '', disabled: true}),
      lastName: new FormControl({value: '', disabled: true}),
      mail: new FormControl({value: '', disabled: true}),
      penaltyPoints: new FormControl({value: 0, disabled: true}),
      institution: new FormControl({value: '', disabled: true}),
      password: new FormControl({value: '', disabled: true}),
      confirmPassword: new FormControl({value: '', disabled: true})
    });
  }

  setupFormGroup(user: User): void {
    this.formGroup.setValue({
      augentID: user.augentID,
      firstName: user.firstName,
      lastName: user.lastName,
      mail: user.mail,
      penaltyPoints: user.penaltyPoints,
      institution: user.institution,
      password: user.password,
      confirmPassword: user.password
    });
  }
}
