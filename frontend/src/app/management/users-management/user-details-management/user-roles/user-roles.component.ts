import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { Observable } from 'rxjs';
import { User, UserConstructor } from '../../../../shared/model/User';
import { AbstractControl, FormControl, FormGroup } from '@angular/forms';
import { UserDetailsService } from '../../../../services/single-point-of-truth/user-details/user-details.service';
import { UserService } from '../../../../services/api/users/user.service';
import { msToShowFeedback } from '../../../../app.constants';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

@Component({
  selector: 'app-user-roles',
  templateUrl: './user-roles.component.html',
  styleUrls: ['./user-roles.component.scss'],
})
export class UserRolesComponent implements OnInit {
  @Input() userObs: Observable<User>;
  user: User;

  userUpdatingSuccess: boolean = undefined;

  roleFormGroup = new FormGroup({
    admin: new FormControl(''),
  });

  modalRef: BsModalRef;

  constructor(
    private userDetailsService: UserDetailsService,
    private userService: UserService,
    private modalService: BsModalService
  ) {}

  ngOnInit(): void {
    this.userObs.subscribe((next) => {
      this.user = next;
      this.admin.setValue(this.user.admin);
    });
  }

  submitUpdateUser(): void {
    const clone = UserConstructor.newFromObj(this.user);
    clone.admin = this.admin.value as boolean;
    this.userService.updateUser(this.user.userId, clone).subscribe(
      () => {
        this.successUpdatingUserHandler();
      },
      () => {
        this.errorUpdatingUserHandler();
      }
    );
  }

  successUpdatingUserHandler(): void {
    this.userDetailsService.loadUser(this.user.userId);
    this.userUpdatingSuccess = true;
    setTimeout(() => (this.userUpdatingSuccess = undefined), msToShowFeedback);
  }

  errorUpdatingUserHandler(): void {
    this.userUpdatingSuccess = false;
    setTimeout(() => (this.userUpdatingSuccess = undefined), msToShowFeedback);
  }

  disableRoleUpdateButton(): boolean {
    return this.admin.value === this.user.admin;
  }

  resetRolesFormArrayButtonClick(): void {
    this.admin.setValue(this.user.admin);
  }

  onAdminClick(event: Event, template: TemplateRef<unknown>): void {
    event.preventDefault();
    this.modalRef = this.modalService.show(template);
  }

  confirmAdminChange(): void {
    this.admin.setValue(!this.admin.value);
    this.modalRef.hide();
  }

  declineAdminChange(): void {
    this.modalRef.hide();
  }

  // ********************************************
  // *   Getters for roleFormGroup's controls   *
  // ********************************************
  get admin(): AbstractControl {
    return this.roleFormGroup.get('admin');
  }
}
