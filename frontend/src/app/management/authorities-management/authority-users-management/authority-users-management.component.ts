import {Component, ElementRef, OnInit, TemplateRef, ViewChild} from '@angular/core';
import {AuthoritiesService} from '../../../services/api/authorities/authorities.service';
import {Observable} from 'rxjs';
import {User} from '../../../shared/model/User';
import {Authority} from '../../../shared/model/Authority';
import {ActivatedRoute} from '@angular/router';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../../shared/animations/RowAnimation';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';
import {UserService} from '../../../services/api/users/user.service';
import {AuthorityToManageService} from '../../../services/single-point-of-truth/authority-to-manage/authority-to-manage.service';
import {BsModalService, BsModalRef} from 'ngx-bootstrap/modal';

@Component({
  selector: 'app-authority-users-management',
  templateUrl: './authority-users-management.component.html',
  styleUrls: ['./authority-users-management.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class AuthorityUsersManagementComponent implements OnInit {
  authority: Authority;

  usersInAuthorityObs: Observable<User[]>;

  userSearchFormGroup = new FormGroup({
    firstName: new FormControl('', Validators.required),
    lastName: new FormControl('', Validators.required)
  });

  selectedUserFormControl = new FormControl('', Validators.required);

  userSearchResult: User[] = [];

  userPreparedToDelete: User;

  successRetrievingAuthority: boolean = undefined;
  successAddingAuthority: boolean = undefined;
  successSearchingUsers: boolean = undefined;
  successDeletingAuthority: boolean = undefined;

  isValidUserToAdd: boolean = undefined;

  addModal: BsModalRef;
  deleteModal: BsModalRef;

  constructor(private authoritiesService: AuthoritiesService,
              private authorityToManageService: AuthorityToManageService,
              private route: ActivatedRoute,
              private userService: UserService,
              private modalService: BsModalService) { }

  ngOnInit(): void {
    this.authority = this.authorityToManageService.authority;

    // It is possible that the authority to manage in not provided through the
    // AuthorityToManageService if the user refreshes the browser while being in this page,
    // or directly used the url to go to this page
    if (this.authority === undefined) {
      const id = this.route.snapshot.paramMap.get('authorityId');
      this.authoritiesService.getAuthority(Number(id)).subscribe(
        next => {
          this.authority = next;
          this.setUsersObs(this.authority.authorityId);
        }
      );
    } else {
      this.setUsersObs(this.authority.authorityId);
    }

  }

  closeModal(): void {
    this.modalService.hide();
  }

  // *********************************
  // *   Add user to the authority   *
  // *********************************

  prepareToAddUserToAuthority(template: TemplateRef<any>): void {
    this.successAddingAuthority = undefined;
    this.successSearchingUsers = undefined;
    this.isValidUserToAdd = undefined;
    this.userSearchFormGroup.setValue({
      firstName: '',
      lastName: ''
    });
    this.selectedUserFormControl.setValue('');
    this.userSearchResult = [];
    this.selectedUserFormControl.disable();
    this.addModal = this.modalService.show(template);
  }

  searchForUserByFirstAndLastName(firstName: string, lastName: string): void {
    let usersObs: Observable<User[]>;

    if (firstName === '') {
      usersObs = this.userService.getUsersByLastName(lastName);
    } else if (lastName === '') {
      usersObs = this.userService.getUsersByFirstName(firstName);
    } else {
      usersObs = this.userService.getUsersByFirstAndLastName(firstName, lastName);
    }

    this.subscribeOnSearchedUsers(usersObs);
  }

  addUserToAuthority(): void {
    const userId: string = this.selectedUserFormControl.value;
    const authorityId = this.authority.authorityId;
    this.successAddingAuthority = null;

    this.authoritiesService.addUserToAuthority(userId, authorityId).subscribe(
      () => {
        this.successAddingAuthority = true;
        this.setUsersObs(authorityId);
        this.addModal.hide();
      }, () => {
        this.successAddingAuthority = false;
      }
    );
  }

  // **************************************
  // *   Delete user from the authority   *
  // **************************************

  prepareToDeleteUserFromAuthority(user: User, template: TemplateRef<any>): void {
    this.successDeletingAuthority = undefined;
    this.userPreparedToDelete = user;
    this.deleteModal = this.modalService.show(template);
  }

  deleteUserFromAuthority(userId: string, authorityId: number): void {
    this.successDeletingAuthority = null;
    this.authoritiesService.deleteUserFromAuthority(userId, authorityId).subscribe(
      () => {
        this.successDeletingAuthority = true;
        this.setUsersObs(authorityId); // reload users data
        this.deleteModal.hide();
      }, () => {
        this.successDeletingAuthority = false;
      }
    );
  }

  // *******************
  // *   Auxiliaries   *
  // *******************

  setUsersObs(authorityId: number): void {
    this.usersInAuthorityObs = this.authoritiesService.getUsersFromAuthority(authorityId);
  }

  validForm(): boolean {
    return !this.firstName.invalid || !this.lastName.invalid;
  }

  subscribeOnSearchedUsers(usersObs: Observable<User[]>): void {
    usersObs.subscribe(
      next => {
        this.successSearchingUsers = true;
        this.userSearchResult = next;
        this.selectedUserFormControl.enable();
      }, () => {
        this.successSearchingUsers = false;
      }
    );
  }

  // ****************************
  // *   Form control getters   *
  // ****************************

  get firstName(): AbstractControl {
    return this.userSearchFormGroup.get('firstName');
  }

  get lastName(): AbstractControl {
    return this.userSearchFormGroup.get('lastName');
  }

}
