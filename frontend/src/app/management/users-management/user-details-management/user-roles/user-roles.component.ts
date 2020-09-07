import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {User, UserConstructor} from '../../../../shared/model/User';
import {msToShowFeedback, Role, rolesArray} from '../../../../../environments/environment';
import {FormArray, FormControl, FormGroup} from '@angular/forms';
import {UserDetailsService} from '../../../../services/single-point-of-truth/user-details/user-details.service';
import {UserService} from '../../../../services/api/users/user.service';

@Component({
  selector: 'app-user-roles',
  templateUrl: './user-roles.component.html',
  styleUrls: ['./user-roles.component.css']
})
export class UserRolesComponent implements OnInit {
  @Input() userObs: Observable<User>;

  userUpdatingSuccess: boolean = undefined;

  rolesFormGroup = new FormGroup({
    roles: new FormArray([])
  });

  roles = rolesArray;

  constructor(private userDetailsService: UserDetailsService,
              private userService: UserService) { }

  ngOnInit(): void {
    this.userObs.subscribe(
      (next) => {
        this.setup(next);
      }
    );
  }

  get rolesFormArray(): FormArray {
    return this.rolesFormGroup.controls.roles as FormArray;
  }

  setup(user: User): void {
    // setup rolesFormGroup for the roles of the user
    this.rolesFormArray.clear();
    rolesArray.forEach((r) => this.rolesFormArray
      .push(new FormControl(user.roles.includes(Role[r]))));

    // dont do: 'this.userUpdatingSuccess = undefined'
    // this is done either:
    //    (1): in construction (value is immediately set)
    //    (2): after update the roles of the user thanks to usage of setTimeout
    //         otherwise, the value would be reset here too fast to be able to have read the message
    //         because remember: the setup() is called in the next() of the observable that will
    //         be notified because of the call to this.userDetailsService.loadUser(string)
  }

  submitUpdateUser(user: User, roles: any): void {
    const clone = UserConstructor.newFromObj(user);
    clone.roles = this.convertBooleanArrayInRolesOfUser(roles);
    this.userService.updateUser(user.augentID, clone).subscribe(
      () => {
        this.successUpdatingUserHandler(user);
      }, () => {
        this.errorUpdatingUserHandler();
      }
    );
  }

  successUpdatingUserHandler(user: User): void {
    this.userDetailsService.loadUser(user.augentID);
    this.userUpdatingSuccess = true;
    setTimeout(() => this.userUpdatingSuccess = undefined, msToShowFeedback);
  }

  errorUpdatingUserHandler(): void {
    this.userUpdatingSuccess = false;
    setTimeout(() => this.userUpdatingSuccess = undefined, msToShowFeedback);
  }

  disableRoleUpdateButton(user: User, roles: boolean[], ignoreAllFalse = false): boolean {
    // Not all roles may be false!
    if (!ignoreAllFalse) {
      let countFalse = 0;
      roles.forEach(v => {
        if (!v) {
          countFalse++;
        }
      });

      if (countFalse === rolesArray.length) {
        return true;
      }
    }

    const convertedRoles = this.convertRolesOfUserInBooleanArray(user);
    for (let i = 0; i < rolesArray.length; i++) {
      if (roles[i] !== convertedRoles[i]) {
        return false;
      }
    }

    return true;
  }

  resetRolesFormArrayButtonClick(user: User): void {
    this.rolesFormArray.clear();
    rolesArray.forEach((r) => this.rolesFormArray
      .push(new FormControl(user.roles.includes(Role[r]))));
  }

  convertRolesOfUserInBooleanArray(user: User): boolean[] {
    const array = new Array<boolean>(rolesArray.length).fill(false);

    if (user.roles.includes(Role.ADMIN)) {
      array[0] = true;
    }

    if (user.roles.includes(Role.EMPLOYEE)) {
      array[1] = true;
    }

    if (user.roles.includes(Role.STUDENT)) {
      array[2] = true;
    }

    return array;
  }

  convertBooleanArrayInRolesOfUser(roles: any): Role[] {
    const array: Role[] = [];
    for (let i = 0; i < rolesArray.length; i++) {
      if (roles[i]) {
        array.push(Role[rolesArray[i]]);
      }
    }
    return array;
  }
}
