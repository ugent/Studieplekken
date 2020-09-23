import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Location} from '../../../../shared/model/Location';
import {FormControl} from '@angular/forms';
import {LocationTag} from '../../../../shared/model/LocationTag';
import {TranslateService} from '@ngx-translate/core';
import {TagsService} from '../../../../services/api/tags/tags.service';
import {MatSelectChange} from '@angular/material/select';
import {matSelectionChanged} from '../../../../shared/GeneralFunctions';
import {LocationDetailsService} from '../../../../services/single-point-of-truth/location-details/location-details.service';

@Component({
  selector: 'app-location-allowed-tags',
  templateUrl: './location-allowed-tags.component.html',
  styleUrls: ['./location-allowed-tags.component.css']
})
export class LocationAllowedTagsComponent implements OnInit {
  @Input() location: Observable<Location>;

  locationName: string;
  currentLang: string;

  tagsFormControl: FormControl = new FormControl([]);
  matSelectSelection: LocationTag[]; // this set upon a selectionChange() of the mat-selection
  allPossibleTags: LocationTag[]; // these are all the tags that are in the application
  tagsThatAreAllowed: LocationTag[]; // these tags are assignable to the location (retrieved from backend)

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
      }
    );

    this.location.subscribe(
      (next) => {
        if (next.name !== '') {
          this.locationName = next.name;
          this.tagsThatAreAllowed = next.allowedTags;
          this.tagsFormControl = new FormControl(next.allowedTags);
        }
      }
    );

    this.tagsService.getAllTags().subscribe(
      next => {
        this.allPossibleTags = next;
      }
    );
  }

  prepareUpdateTheTags(): void {
    this.tagsFormControl = new FormControl(this.tagsThatAreAllowed);
    this.tagsSelectionIsUpdatable = false;
    this.successUpdatingTagsConfiguration = undefined;
  }

  updateTags(): void {
    this.successUpdatingTagsConfiguration = null;
    this.tagsService.reconfigureAllowedTagsOfLocation(this.locationName, this.matSelectSelection).subscribe(
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
    this.tagsSelectionIsUpdatable = matSelectionChanged(event, this.tagsThatAreAllowed);
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
