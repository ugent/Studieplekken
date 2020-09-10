import { Component, OnInit } from '@angular/core';
import {Observable} from 'rxjs';
import {LocationTag, LocationTagConstructor} from '../../shared/model/LocationTag';
import {TagsService} from '../../services/api/tags/tags.service';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../../shared/animations/RowAnimation';
import {AbstractControl, FormControl, FormGroup, Validators} from '@angular/forms';

@Component({
  selector: 'app-tags-management',
  templateUrl: './tags-management.component.html',
  styleUrls: ['./tags-management.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class TagsManagementComponent implements OnInit {
  tagsObs: Observable<LocationTag[]>;

  tagFormGroup = new FormGroup({
    tagId: new FormControl({value: '', disabled: true}),
    dutch: new FormControl('', Validators.required),
    english: new FormControl('', Validators.required)
  });

  successGettingTags: boolean = undefined;
  successUpdatingTag: boolean = undefined;
  successDeletingTag: boolean = undefined;

  constructor(private tagsService: TagsService) { }

  ngOnInit(): void {
    this.tagsObs = this.tagsService.getAllTags();
    this.tagsObs.subscribe(
      () => {
        // Setting the 'successGettingTags' to true doesn't really do anything
        this.successGettingTags = true;
      }, () => {
        // But this does: gives feedback to the user
        this.successGettingTags = false;
      }
    );
  }

  validTagFormGroup(): boolean {
    return !this.tagFormGroup.invalid;
  }

  prepareFormGroup(locationTag: LocationTag): void {
    this.tagFormGroup.setValue({
      tagId: locationTag.tagId,
      dutch: locationTag.dutch,
      english: locationTag.english
    });
  }

  prepareUpdate(locationTag: LocationTag): void {
    // restore the feedback boolean
    this.successUpdatingTag = undefined;

    // prepare the tagFormGroup
    this.prepareFormGroup(locationTag);
  }

  updateTagInFormGroup(): void {
    this.successUpdatingTag = null;
    this.tagsService.updateTag(this.locationTag).subscribe(
      () => {
        this.successUpdatingTag = true;
        // and reload the tags
        this.tagsObs = this.tagsService.getAllTags();
      }, () => {
        this.successUpdatingTag = false;
      }
    );
  }

  prepareToDelete(locationTag: LocationTag): void {
    // restore the feedback boolean
    this.successDeletingTag = undefined;

    // prepare the tagFormGroup
    this.prepareFormGroup(locationTag);
  }

  deleteTagInFormGroup(): void {
    this.successDeletingTag = null;
    this.tagsService.deleteTag(this.locationTag).subscribe(
      () => {
        this.successDeletingTag = true;
        // and reload the tags
        this.tagsObs = this.tagsService.getAllTags();
      }, () => {
        this.successDeletingTag = false;
      }
    );
  }

  get tagId(): AbstractControl { return this.tagFormGroup.get('tagId'); }
  get dutch(): AbstractControl { return this.tagFormGroup.get('dutch'); }
  get english(): AbstractControl { return this.tagFormGroup.get('english'); }

  get locationTag(): LocationTag {
    return {
      tagId: this.tagId.value,
      dutch: this.dutch.value,
      english: this.english.value
    };
  }
}
