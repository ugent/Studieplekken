import { Component, OnInit } from '@angular/core';
import {AuthoritiesService} from '../../../services/api/authorities/authorities.service';
import {Observable} from 'rxjs';
import {User} from '../../../shared/model/User';
import {AuthorityToManageService} from '../../../services/single-point-of-truth/authority-to-manage/authority-to-manage.service';
import {Authority} from '../../../shared/model/Authority';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-authority-users-management',
  templateUrl: './authority-users-management.component.html',
  styleUrls: ['./authority-users-management.component.css']
})
export class AuthorityUsersManagementComponent implements OnInit {
  authority: Authority;
  usersObs: Observable<User[]>;

  successRetrievingAuthority: boolean = undefined;
  successAddingAuthority: boolean = undefined;
  successDeletingAuthority: boolean = undefined;

  constructor(private authoritiesService: AuthoritiesService,
              private authorityToManageService: AuthorityToManageService,
              private route: ActivatedRoute) { }

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

  // *********************************
  // *   Add user to the authority   *
  // *********************************

  prepareToAddUserToAuthority(): void {
    this.successAddingAuthority = undefined;
    // todo
  }

  addUserToAuthority(): void {
    // todo
  }

  // **************************************
  // *   Delete user from the authority   *
  // **************************************

  prepareToDeleteUserFromAuthority(): void {
    this.successDeletingAuthority = undefined;
    // todo
  }

  deleteUserFromAuthority(): void {
    // todo
  }

  // *******************
  // *   Auxiliaries   *
  // *******************

  setUsersObs(authorityId: number): void {
    this.usersObs = this.authoritiesService.getUsersFromAuthority(authorityId);
  }

}
