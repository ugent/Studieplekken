import { Component, OnInit } from '@angular/core';
import { AuthenticationService } from '../../services/authentication/authentication.service';
import { User } from '../../shared/model/User';
import { FormControl, FormGroup } from '@angular/forms';
import { ApplicationTypeFunctionalityService } from '../../services/functionality/application-type/application-type-functionality.service';

@Component({
  selector: 'app-profile-overview',
  templateUrl: './profile-overview.component.html',
  styleUrls: ['./profile-overview.component.scss'],
})
export class ProfileOverviewComponent implements OnInit {
  user: User;
  formGroup: FormGroup;

  showPenaltyPoints: boolean;

  constructor(
    private authenticationService: AuthenticationService,
    private functionalityService: ApplicationTypeFunctionalityService
  ) {
    this.prepareEmptyFormGroup();
    authenticationService.user.subscribe((next) => {
      this.setupFormGroup(next);
    });
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
      confirmPassword: new FormControl({ value: '', disabled: true }),
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
  }
}
