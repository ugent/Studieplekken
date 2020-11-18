import { Component, OnInit } from '@angular/core';
import {LocationService} from '../services/api/locations/location.service';
import {Location} from '../shared/model/Location';
import {LocationTag} from '../shared/model/LocationTag';
import {TagsService} from '../services/api/tags/tags.service';
import {TranslateService} from '@ngx-translate/core';
import {AbstractControl, FormControl, FormGroup} from '@angular/forms';
import {MatSelectChange} from '@angular/material/select';
import { CalendarPeriodsService } from '../services/api/calendar-periods/calendar-periods.service';
import {LocationStatus} from '../app.constants';
import {Pair} from '../shared/model/helpers/Pair';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  locations: Location[];
  locationStatuses = new Map<string, Pair<LocationStatus, string>>();
  filteredLocations: Location[];
  filteredLocationsBackup: Location[];

  // all tags to select from
  tags: LocationTag[];

  // the tags that were selected to filter on
  selectedTags: LocationTag[];
  // the name that should be filtered
  locationSearch: string;

  filterFormGroup = new FormGroup({
    filteredTags: new FormControl('')
  });

  currentLang: string;

  successOnRetrievingLocations: boolean = undefined;

  showOpen = false;

  constructor(private locationService: LocationService,
              private tagsService: TagsService,
              private translate: TranslateService,
              private calendarPeriodService: CalendarPeriodsService) { }

  ngOnInit(): void {
    this.currentLang = this.translate.currentLang;
    this.translate.onLangChange.subscribe(
      () => {
        this.currentLang = this.translate.currentLang;
      }
    );

    this.selectedTags = [];

    this.successOnRetrievingLocations = null;
    this.locationService.getLocations().subscribe(
      (next) => {
        this.locations = next;
        this.filteredLocations = next;
        this.filteredLocationsBackup = next;
        this.successOnRetrievingLocations = true;

        // retrieve the status for the locations
        next.forEach(l => {
          this.calendarPeriodService.getStatusOfLocation(l.name).subscribe(
            next2 => {
              this.locationStatuses.set(l.name, next2);
            }
          );
        });
      }, () => {
        this.successOnRetrievingLocations = false;
      }
    );

    this.tagsService.getAllTags().subscribe(
      (next) => {
        this.tags = next;
      }
    );
  }

  /**
   * Used as a compareWith input on the tags-selection field in the filter
   * Tracks identities when checking for changes
   */
  compareTagsInSelection(tag1: LocationTag, tag2: LocationTag): boolean {
    return !tag1 || !tag2 ? false : tag1.tagId === tag2.tagId;
  }

  /**
   * When the selection of tags to filter on is changed
   */
  onSelectionChange(event: MatSelectChange): void {
    this.selectedTags = event.value;
    this.displayFilterLocations();
  }

  toggleShowOpen(): void {
    this.showOpen = !this.showOpen;
    this.displayFilterLocations();
  }

  /**
   * This will take into account:
   * - selectedTags
   * - locationSearch
   * - showOpen
   *
   * And filter only those locations that apply to all filters above
   */
  displayFilterLocations(): void {
    this.filteredLocations = [];

    this.locations.forEach(location => {
      // first check that the location name matches with the search bar
      if ((this.locationSearch !== undefined) && (!location.name.toLowerCase().includes(this.locationSearch.toLowerCase()))) {
        return;
      }

      // only filter on tags when there is at least one selected
      if (!(this.selectedTags.length === 0)) {
        // only add when all the tags match
        for (const tag of this.selectedTags) {
          // if the filtered tag is not assigned to a certain location ...
          if (location.assignedTags.filter(t => t.tagId === tag.tagId).length === 0) {
            return;
          }
        }
      }

      if (this.showOpen) {
        if (this.locationStatuses.get(location.name).first === LocationStatus.OPEN) {
          this.filteredLocations.push(location);
        }
      } else {
        this.filteredLocations.push(location);
      }
    });
  }

  onSearchEnter(): void {
    this.displayFilterLocations();
  }

  onClearSearch(): void {
    this.filteredLocations = this.locations;
    this.filteredTags.setValue([]);
    this.locationSearch = '';
    this.showOpen = false;
    this.displayFilterLocations();
  }

  get filteredTags(): AbstractControl {
    return this.filterFormGroup.get('filteredTags');
  }
}
