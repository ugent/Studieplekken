import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {User} from '../../../../shared/model/User';
import {FormControl, FormGroup} from '@angular/forms';

@Component({
  selector: 'app-user-details-form',
  templateUrl: './user-details-form.component.html',
  styleUrls: ['./user-details-form.component.css']
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
    institution: new FormControl('')
  });

  constructor() { }

  ngOnInit(): void {
    this.formGroup.disable();

    this.userObs.subscribe(
      (next) => {
        this.setup(next);
      }
    );
  }

  setup(user: User): void {
    // setup formGroup for details of user
    this.formGroup.setValue({
      id: user.augentID,
      firstName: user.firstName,
      lastName: user.lastName,
      mail: user.mail,
      penaltyPoints: user.penaltyPoints,
      institution: user.institution
    });
    this.formGroup.disable();

    this.userQueryingError = false;
  }
}
