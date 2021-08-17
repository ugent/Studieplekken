import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSelectChange } from '@angular/material/select';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { LocationService } from '../../../../services/api/locations/location.service';
import { TagsService } from '../../../../services/api/tags/tags.service';
import { LocationDetailsService } from '../../../../services/single-point-of-truth/location-details/location-details.service';
import { matSelectionChanged } from '../../../../shared/GeneralFunctions';
import { Location } from '../../../../shared/model/Location';
import { LocationTag } from '../../../../shared/model/LocationTag';

@Component({
  selector: 'app-location-tags-management',
  templateUrl: './location-tags-management.component.html',
  styleUrls: ['./location-tags-management.component.scss'],
})
export class LocationTagsManagementComponent implements OnInit {
  @Input() location: Observable<Location>;

  locationName: string;
  locationId: number;
  currentLang: string;

  tagsFormControl: FormControl = new FormControl([]);
  matSelectSelection: LocationTag[]; // this set upon a selectionChange() of the mat-selection
  allTags: LocationTag[]; // these tags are assignable to the location (retrieved from backend)
  tagsThatAreSelected: LocationTag[]; // these tags are actually set on the location (retrieved from backend)

  tagsSelectionIsUpdatable = false;

  successUpdatingTagsConfiguration: boolean = undefined;

  constructor(
    private translate: TranslateService,
    private tagsService: TagsService,
    private locationService: LocationService,
    private locationDetailsService: LocationDetailsService,
    private modalService: MatDialog
  ) {}

  ngOnInit(): void {
    this.currentLang = this.translate.currentLang;
    this.translate.onLangChange.subscribe(() => {
      this.currentLang = this.translate.currentLang;
    });

    this.tagsService.getAllTags().subscribe((next) => {
      this.allTags = next;
    });

    this.location.subscribe((next) => {
      if (next.name !== '') {
        this.locationId = next.locationId;
        this.locationName = next.name;
        this.tagsThatAreSelected = next.assignedTags;
        this.tagsFormControl = new FormControl(this.tagsThatAreSelected);
      }
    });
  }

  prepareUpdateTheTags(template: TemplateRef<unknown>): void {
    this.tagsFormControl = new FormControl(this.tagsThatAreSelected);
    this.tagsSelectionIsUpdatable = false;
    this.successUpdatingTagsConfiguration = undefined;
    this.modalService.open(template, {panelClass: ["cs--cyan" ,"bigmodal"]});
  }

  updateTags(): void {
    this.successUpdatingTagsConfiguration = null;
    this.locationService
      .setupTagsForLocation(this.locationId, this.matSelectSelection)
      .subscribe(
        () => {
          this.successUpdatingTagsConfiguration = true;
          // reload the location
          this.locationDetailsService.loadLocation(this.locationId);
          this.modalService.closeAll();
        },
        () => {
          this.successUpdatingTagsConfiguration = false;
        }
      );
  }

  closeModal(): void {
    this.modalService.closeAll();
  }

  /**
   * Every time the selection is changed, we need to determine whether or not
   * the selection has changed opposed to the selection of tags that are selected.
   */
  selectionChanged(event: MatSelectChange): void {
    this.matSelectSelection = event.value as LocationTag[];
    this.tagsSelectionIsUpdatable = matSelectionChanged(
      event,
      this.tagsThatAreSelected
    );
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
