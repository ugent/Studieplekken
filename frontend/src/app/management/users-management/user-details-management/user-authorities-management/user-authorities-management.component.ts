import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { AuthoritiesService } from '../../../../services/api/authorities/authorities.service';
import { UserDetailsService } from '../../../../services/single-point-of-truth/user-details/user-details.service';
import { Authority } from '../../../../shared/model/Authority';
import { User } from '../../../../shared/model/User';

@Component({
  selector: 'app-user-authorities-management',
  templateUrl: './user-authorities-management.component.html',
  styleUrls: ['./user-authorities-management.component.scss'],
})
export class UserAuthoritiesManagementComponent implements OnInit {
  @Input() userObs: Observable<User>;
  userId: string; // set by subscription on `userObs`
  userFirstName: string; // set by subscription on `userObs`
  userLastName: string; // set by subscription on `userObs`

  authoritiesOfUser: Authority[]; // the authorities assigned to a user
  allAuthorities: Authority[]; // all authorities in the application
  addableAuthorities: Authority[]; // the authorities in the application, that are not yet assigned to the user

  authoritiesFormControl = new FormControl('', Validators.required.bind(this));

  authorityPreparedToDelete: Authority;

  successOnAddingAuthorityToUser: boolean = undefined;
  successOnDeletingAuthorityForUser: boolean = undefined;

  constructor(
    private authoritiesService: AuthoritiesService,
    private userDetailsService: UserDetailsService,
    private modalService: MatDialog
  ) { }

  ngOnInit(): void {
    this.userObs.subscribe((next) => {
      // make sure that a request is only sent if the user is valid
      if (next.userId !== '') {
        this.userId = next.userId;
        this.userFirstName = next.firstName;
        this.userLastName = next.lastName;

        this.authoritiesService
          .getAuthoritiesOfUser(next.userId)
          .subscribe((next2) => {
            this.authoritiesOfUser = next2;
            this.addableAuthorities = this.calculateAddableAuthorities(
              this.allAuthorities,
              this.authoritiesOfUser
            );
          });
      }
    });

    this.authoritiesService.getAllAuthorities().subscribe((next) => {
      this.allAuthorities = next;
      this.addableAuthorities = this.calculateAddableAuthorities(
        this.allAuthorities,
        this.authoritiesOfUser
      );
    });
  }

  // *****************************************
  // *   Adding an authority from the user   *
  // *****************************************

  prepareToAddAnAuthorityToUser(template: TemplateRef<unknown>): void {
    this.successOnAddingAuthorityToUser = undefined;
    this.modalService.open(template, { panelClass: ["cs--cyan", "bigmodal"] });
  }

  addAuthorityFromForm(): void {
    this.successOnAddingAuthorityToUser = null;

    const userId = this.userId;
    const authorityId = this.authoritiesFormControl.value as number;

    this.authoritiesService.addUserToAuthority(userId, authorityId).subscribe(
      () => {
        this.successOnAddingAuthorityToUser = true;
        this.userDetailsService.loadUser(userId);
        this.modalService.closeAll();
      },
      () => {
        this.successOnAddingAuthorityToUser = false;
      }
    );
  }

  // *******************************************
  // *   Deleting an authority from the user   *
  // *******************************************

  prepareToDeleteAuthorityForUser(
    authority: Authority,
    template: TemplateRef<unknown>
  ): void {
    this.successOnDeletingAuthorityForUser = undefined;
    this.authorityPreparedToDelete = authority;
    this.modalService.open(template, { panelClass: ["cs--cyan", "bigmodal"] });
  }

  deleteAuthorityFromUser(userId: string, authorityId: number): void {
    this.successOnDeletingAuthorityForUser = null;
    this.authoritiesService
      .deleteUserFromAuthority(userId, authorityId)
      .subscribe(
        () => {
          this.successOnDeletingAuthorityForUser = true;
          this.userDetailsService.loadUser(userId);
          this.modalService.closeAll();
        },
        () => {
          this.successOnDeletingAuthorityForUser = false;
        }
      );
  }

  // *******************
  // *   Auxiliaries   *
  // *******************

  validForm(): boolean {
    return !this.authoritiesFormControl.invalid;
  }

  clearForm(): void {
    this.authoritiesFormControl.setValue('');
  }

  closeModal(): void {
    this.modalService.closeAll();
  }

  /**
   * Make sure that only authorities that aren't already assigned to the user, are shown
   * in the addAuthorityToUserModal.
   */
  calculateAddableAuthorities(
    allAuthorities: Authority[],
    authoritiesOfUser: Authority[]
  ): Authority[] {
    if (allAuthorities === undefined || authoritiesOfUser === undefined) {
      return undefined;
    }

    // If the lengths match, all authorities are already assigned to the user
    /*if (allAuthorities.length === authoritiesOfUser.length) {
      return [];
    }

    // If user has no authorities, all application authorities are assignable
    if (authoritiesOfUser.length === 0) {
      return allAuthorities;
    }*/

    return allAuthorities.filter((value) => {
      // if `value` is not included in authoritiesOfUser, add `value` to the return value
      return (
        authoritiesOfUser.findIndex(
          (value2) => value2.authorityId === value.authorityId
        ) < 0
      );
    });
  }
}
