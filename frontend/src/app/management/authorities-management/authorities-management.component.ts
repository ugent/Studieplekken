import { Component, OnInit } from '@angular/core';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../shared/animations/RowAnimation';
import {Observable} from 'rxjs';
import {Authority} from '../../shared/model/Authority';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';
import {AuthoritiesService} from '../../services/api/authorities/authorities.service';
import {AuthorityToManageService} from '../../services/single-point-of-truth/authority-to-manage/authority-to-manage.service';

@Component({
  selector: 'app-authorities-management',
  templateUrl: './authorities-management.component.html',
  styleUrls: ['./authorities-management.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class AuthoritiesManagementComponent implements OnInit {
  authoritiesObs: Observable<Authority[]>;

  authorityFormGroup = new FormGroup({
    authorityId: new FormControl({value: '', disabled: true}),
    authorityName: new FormControl('', Validators.required),
    description: new FormControl('', Validators.required)
  });

  successGettingAuthorities: boolean = undefined;
  successAddingAuthority: boolean = undefined;
  successUpdatingAuthority: boolean = undefined;
  successDeletingAuthority: boolean = undefined;

  constructor(private authoritiesService: AuthoritiesService,
              private authorityToMangeService: AuthorityToManageService) { }

  ngOnInit(): void {
    this.authoritiesObs = this.authoritiesService.getAllAuthorities();

    this.successGettingAuthorities = null;
    this.authoritiesObs.subscribe(
      () => {
        // Setting the 'successGettingAuthorities' to true doesn't really do anything
        this.successGettingAuthorities = true;
      }, () => {
        // But this does: gives feedback to the user
        this.successGettingAuthorities = false;
      }
    );
  }

  prepareFormGroup(authority: Authority): void {
    this.authorityFormGroup.setValue({
      authorityId: authority.authorityId,
      authorityName: authority.authorityName,
      description: authority.description
    });
  }

  /********************
   *   CRUD: Create   *
   ********************/

  prepareAdd(): void {
    // reset the feedback boolean
    this.successAddingAuthority = undefined;

    // prepare the authorityFormGroup, note that the authorityId won't be shown
    // because this is automatically added by the database
    this.authorityFormGroup.setValue({
      authorityId: '',
      authorityName: '',
      description: ''
    });
  }

  addTag(): void {
    this.successAddingAuthority = null;
    this.authoritiesService.addAuthority(this.authority).subscribe(
      () => {
        this.successAddingAuthority = true;
        // and reload the tags
        this.authoritiesObs = this.authoritiesService.getAllAuthorities();
      }, () => {
        this.successAddingAuthority = false;
      }
    );
  }

  /********************
   *   CRUD: Update   *
   ********************/

  prepareUpdate(authority: Authority): void {
    // reset the feedback boolean
    this.successUpdatingAuthority = undefined;

    // prepare the tagFormGroup
    this.prepareFormGroup(authority);
  }

  updateTagInFormGroup(): void {
    this.successUpdatingAuthority = null;
    this.authoritiesService.updateAuthority(this.authorityId.value, this.authority).subscribe(
      () => {
        this.successUpdatingAuthority = true;
        // and reload the tags
        this.authoritiesObs = this.authoritiesService.getAllAuthorities();
      }, () => {
        this.successUpdatingAuthority = false;
      }
    );
  }

  /********************
   *   CRUD: Delete   *
   ********************/

  prepareToDelete(authority: Authority): void {
    // reset the feedback boolean
    this.successDeletingAuthority = undefined;

    // prepare the tagFormGroup
    this.prepareFormGroup(authority);
  }

  deleteTagInFormGroup(): void {
    this.successDeletingAuthority = null;
    this.authoritiesService.deleteAuthority(this.authority.authorityId).subscribe(
      () => {
        this.successDeletingAuthority = true;
        // and reload the tags
        this.authoritiesObs = this.authoritiesService.getAllAuthorities();
      }, () => {
        this.successDeletingAuthority = false;
      }
    );
  }

  /*************************
   *   Auxiliary getters   *
   *************************/

  get authorityId(): AbstractControl { return this.authorityFormGroup.get('authorityId'); }
  get authorityName(): AbstractControl { return this.authorityFormGroup.get('authorityName'); }
  get description(): AbstractControl { return this.authorityFormGroup.get('description'); }

  get authority(): Authority {
    return {
      authorityId: this.authorityId.value,
      authorityName: this.authorityName.value,
      description: this.description.value
    };
  }

  validTagFormGroup(): boolean {
    return !this.authorityFormGroup.invalid;
  }

  setAuthorityToManage(authority: Authority): void {
    this.authorityToMangeService.authority = authority;
  }
}
