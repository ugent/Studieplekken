import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Location} from '../../../../shared/model/Location';
import {TranslateService} from '@ngx-translate/core';
import {LocationTag} from '../../../../shared/model/LocationTag';
import {MatSelectChange} from '@angular/material/select';
import {TagsService} from '../../../../services/api/tags/tags.service';
import {FormControl} from '@angular/forms';
import {LocationDetailsService} from "../../../../services/single-point-of-truth/location-details/location-details.service";

@Component({
  selector: 'app-location-tags-management',
  templateUrl: './location-tags-management.component.html',
  styleUrls: ['./location-tags-management.component.css']
})
export class LocationTagsManagementComponent implements OnInit {
  @Input() location: Observable<Location>;

  locationName: string;
  currentLang: string;

  tagsFormControl: FormControl = new FormControl([]);
  matSelectSelection: LocationTag[]; // this set upon a selectionChange() of the mat-selection
  tagsThatAreAllowed: LocationTag[]; // these tags are assignable to the location (retrieved from backend)
  tagsThatAreSelected: LocationTag[]; // these tags are actually set on the location (retrieved from backend)

  tagsSelectionIsUpdatable = false;

  successUpdatingTagsConfiguration: boolean = undefined;

  constructor(private translate: TranslateService,
              private tagsService: TagsService,
              private locationDetailsService: LocationDetailsService) { }

  ngOnInit(): void {
    this.currentLang = this.translate.currentLang;
    this.translate.onLangChange.subscribe(
      () => {
        this.currentLang = this.translate.currentLang;
        // do a setup so that the 'tagsThatAreSelected' are reset, which will trigger the template to rerender
        // this.setupTagsThatAreSelected(); // TODO: remove comment when #65 is finished
      }
    );

    // TODO when #65 is finished: remove setting the tagsThatAreAllowed to "all"
    //   and change the line "this.tagsThatAreSelected = next.tags;" in the subscription
    //   on the location to "this.tagsThatAreAllowed = next.tags;".
    this.tagsService.getAllTags().subscribe(
      (next) => {
        this.tagsThatAreAllowed = next;
      }
    );

    this.location.subscribe(
      (next) => {
        if (next.name !== '') {
          this.locationName = next.name;
          this.tagsThatAreSelected = next.tags; // TODO: replace tagsThatAreSelected by tagsThatAreAllowed when #65 is finished
          // this.setupTagsThatAreSelected(); // TODO: remove comment when #65 if finished

          this.tagsFormControl = new FormControl(this.tagsThatAreSelected);
        }
      }
    );
  }

  setupTagsThatAreSelected(): void {
    if (this.tagsThatAreAllowed === undefined || this.tagsThatAreAllowed === null) {
      return;
    }

    // TODO when issue #64 is finished: only add the LocationTag if value.assigned === true
    this.tagsThatAreSelected = this.tagsThatAreAllowed.map(value => value);
  }

  prepareUpdateTheTags(): void {
    this.tagsFormControl = new FormControl(this.tagsThatAreSelected);
    this.tagsSelectionIsUpdatable = false;
    this.successUpdatingTagsConfiguration = undefined;
  }

  updateTags(): void {
    this.successUpdatingTagsConfiguration = null;
    this.tagsService.assignTagsToLocation(this.locationName, this.matSelectSelection).subscribe(
      () => {
        this.successUpdatingTagsConfiguration = true;
        // reload the location
        this.locationDetailsService.loadLocation(this.locationName);
      }, () => {
        this.successUpdatingTagsConfiguration = false;
      }
    );
  }

  /**
   * Every time the selection is changed, we need to determine whether or not
   * the selection has changed opposed to the selection of tags that are selected.
   */
  selectionChanged(event: MatSelectChange): void {
    this.matSelectSelection = event.value;
    if (event.value.length !== this.tagsThatAreSelected.length) {
      this.tagsSelectionIsUpdatable = true;
    } else {
      const selection: LocationTag[] = event.value;
      for (const tag of this.tagsThatAreSelected) {
        if (selection.findIndex(v => v.tagId === tag.tagId) < 0) {
          this.tagsSelectionIsUpdatable = true;
          return;
        }
      }
      this.tagsSelectionIsUpdatable = false;
    }
  }

  /**
   * The MatSelectModule uses this method (because [compareWith] was set to "compareTagsInSelection")
   * to compare the objects in the value of the [formControl] with the objects that
   * were put in the [value] input of the <mat-option>.
   * If this function returns "true" the checkbox in the <mat-option> will be checked.
   * If the function returns "false" the checkbox will not check the checkbox
   */
  compareTagsInSelection(tag1: LocationTag, tag2: LocationTag): boolean {
    return tag1.tagId === tag2.tagId;
  }
}
