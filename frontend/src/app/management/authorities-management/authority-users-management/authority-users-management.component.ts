import { Component, OnInit, TemplateRef } from '@angular/core';
import { AuthoritiesService } from '../../../services/api/authorities/authorities.service';
import { Observable } from 'rxjs';
import { User } from '../../../shared/model/User';
import { Authority } from '../../../shared/model/Authority';
import { ActivatedRoute } from '@angular/router';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { UserService } from '../../../services/api/users/user.service';
import { AuthorityToManageService } from '../../../services/single-point-of-truth/authority-to-manage/authority-to-manage.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

@Component({
  selector: 'app-authority-users-management',
  templateUrl: './authority-users-management.component.html',
  styleUrls: ['./authority-users-management.component.css'],
})
export class AuthorityUsersManagementComponent implements OnInit {
  authority: Authority;

  usersInAuthorityObs: Observable<User[]>;

  userSearchFormGroup = new FormGroup({
    firstName: new FormControl('', Validators.required.bind(this)),
    lastName: new FormControl('', Validators.required.bind(this)),
  });

  selectedUserFormControl = new FormControl('', Validators.required.bind(this));

  userSearchResult: User[] = [];

  userPreparedToDelete: User;

  successRetrievingAuthority: boolean = undefined;
  successAddingAuthority: boolean = undefined;
  successSearchingUsers: boolean = undefined;
  successDeletingAuthority: boolean = undefined;

  isValidUserToAdd: boolean = undefined;

  addModal: BsModalRef;
  deleteModal: BsModalRef;

  constructor(
    private authoritiesService: AuthoritiesService,
    private authorityToManageService: AuthorityToManageService,
    private route: ActivatedRoute,
    private userService: UserService,
    private modalService: BsModalService
  ) {}

  get firstName(): AbstractControl {
    return this.userSearchFormGroup.get('firstName');
  }

  get lastName(): AbstractControl {
    return this.userSearchFormGroup.get('lastName');
  }

  // *********************************
  // *   Add user to the authority   *
  // *********************************

  ngOnInit(): void {
    this.authority = this.authorityToManageService.authority;

    // It is possible that the authority to manage in not provided through the
    // AuthorityToManageService if the user refreshes the browser while being in this page,
    // or directly used the url to go to this page
    if (this.authority === undefined) {
      const id = this.route.snapshot.paramMap.get('authorityId');
      this.authoritiesService.getAuthority(Number(id)).subscribe((next) => {
        this.authority = next;
        this.setUsersObs(this.authority.authorityId);
      });
    } else {
      this.setUsersObs(this.authority.authorityId);
    }
  }

  closeModal(): void {
    this.modalService.hide();
  }

  prepareToAddUserToAuthority(template: TemplateRef<unknown>): void {
    this.successAddingAuthority = undefined;
    this.successSearchingUsers = undefined;
    this.isValidUserToAdd = undefined;
    this.userSearchFormGroup.setValue({
      firstName: '',
      lastName: '',
    });
    this.selectedUserFormControl.setValue('');
    this.userSearchResult = [];
    this.selectedUserFormControl.disable();
    this.addModal = this.modalService.show(template);
  }

  // **************************************
  // *   Delete user from the authority   *
  // **************************************

  searchForUserByFirstAndLastName(firstName: string, lastName: string): void {
    let usersObs: Observable<User[]>;

    if (firstName === '') {
      usersObs = this.userService.getUsersByLastName(lastName);
    } else if (lastName === '') {
      usersObs = this.userService.getUsersByFirstName(firstName);
    } else {
      usersObs = this.userService.getUsersByFirstAndLastName(
        firstName,
        lastName
      );
    }

    this.subscribeOnSearchedUsers(usersObs);
  }

  addUserToAuthority(): void {
    const userId: string = this.selectedUserFormControl.value as string;
    const authorityId = this.authority.authorityId;
    this.successAddingAuthority = null;

    this.authoritiesService.addUserToAuthority(userId, authorityId).subscribe(
      () => {
        this.successAddingAuthority = true;
        this.setUsersObs(authorityId);
        this.addModal.hide();
      },
      () => {
        this.successAddingAuthority = false;
      }
    );
  }

  // *******************
  // *   Auxiliaries   *
  // *******************

  prepareToDeleteUserFromAuthority(
    user: User,
    template: TemplateRef<unknown>
  ): void {
    this.successDeletingAuthority = undefined;
    this.userPreparedToDelete = user;
    this.deleteModal = this.modalService.show(template);
  }

  deleteUserFromAuthority(userId: string, authorityId: number): void {
    this.successDeletingAuthority = null;
    this.authoritiesService
      .deleteUserFromAuthority(userId, authorityId)
      .subscribe(
        () => {
          this.successDeletingAuthority = true;
          this.setUsersObs(authorityId); // reload users data
          this.deleteModal.hide();
        },
        () => {
          this.successDeletingAuthority = false;
        }
      );
  }

  setUsersObs(authorityId: number): void {
    this.usersInAuthorityObs = this.authoritiesService.getUsersFromAuthority(
      authorityId
    );
  }

  // ****************************
  // *   Form control getters   *
  // ****************************

  validForm(): boolean {
    return !this.firstName.invalid || !this.lastName.invalid;
  }

  subscribeOnSearchedUsers(usersObs: Observable<User[]>): void {
    usersObs.subscribe(
      (next) => {
        this.successSearchingUsers = true;
        this.userSearchResult = next;
        this.selectedUserFormControl.enable();
      },
      () => {
        this.successSearchingUsers = false;
      }
    );
  }
}
