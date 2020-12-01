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
import { Building } from '../shared/model/Building';
import { BuildingService } from '../services/api/buildings/buildings.service';

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

  // all buildings to select from
  buildings: Building[];

  // the tags that were selected to filter on
  selectedTags: LocationTag[];
  // the building that was selected to filter on
  selectedBuilding: Building;
  // the name that should be filtered
  locationSearch: string;

  tagFilterFormGroup = new FormGroup({
    filteredTags: new FormControl('')
  });

  buildingFilterFormGroup = new FormGroup({
    filteredBuilding: new FormControl('')
  });

  currentLang: string;

  successOnRetrievingLocations: boolean = undefined;

  showOpen = false;

  constructor(private locationService: LocationService,
              private tagsService: TagsService,
              private translate: TranslateService,
              private calendarPeriodService: CalendarPeriodsService,
              private buildingService: BuildingService) { }

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

        // retrieve the status for the locations, hereby invalidating the cache and forcing a reload.
        next.forEach(l => {
          this.calendarPeriodService.getStatusOfLocation(l.name, true).subscribe(
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

    this.buildingService.getAllBuildings().subscribe(
      (next) => {
        this.buildings = next;
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
  onTagsSelectionChange(event: MatSelectChange): void {
    this.selectedTags = event.value;
    this.displayFilterLocations();
  }

  /**
   * When the selection of building to filter on is changed
   */
  onBuildingSelectionChange(event: MatSelectChange): void {
    this.selectedBuilding = event.value;
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

      // console.log(this.selectedBuilding);
      // check that the location is in the selected building
      if (this.selectedBuilding !== undefined && location.building.buildingId !== this.selectedBuilding.buildingId) {
        return;
      }

      // only filter on tags when there is at least one selected
      if (!(this.selectedTags.length === 0)) {
        // only add when all the tags match
        for (const tag of this.selectedTags) {
          // if the filtered tag is not assigned to a certain location ...
          if (!location.assignedTags.some(t => t.tagId === tag.tagId)) {
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
    this.selectedBuilding = undefined;
    this.selectedTags = [];
    this.filteredTags.setValue([]);
    this.filteredBuilding.setValue('');
    this.locationSearch = '';
    this.showOpen = false;
    this.displayFilterLocations();
  }

  get filteredTags(): AbstractControl {
    return this.tagFilterFormGroup.get('filteredTags');
  }

  get filteredBuilding(): AbstractControl {
    return this.buildingFilterFormGroup.get('filteredBuilding');
  }
}
