import { Component, OnInit } from '@angular/core';
import {LocationService} from '../services/api/locations/location.service';
import {Location} from '../shared/model/Location';
import {LocationTag} from '../shared/model/LocationTag';
import {TagsService} from '../services/api/tags/tags.service';
import {TranslateService} from '@ngx-translate/core';
import {AbstractControl, FormControl, FormGroup} from '@angular/forms';
import {MatSelectChange} from '@angular/material/select';
import {AuthenticationService} from '../services/authentication/authentication.service';
import { CalendarPeriodsService } from '../services/api/calendar-periods/calendar-periods.service';
import {LocationStatus} from '../app.constants';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  locations: Location[];
  filteredLocations: Location[];
  filteredLocationsBackup: Location[];

  tags: LocationTag[];

  filterFormGroup = new FormGroup({
    filteredTags: new FormControl('')
  });

  currentLang: string;

  successOnRetrievingLocations: boolean = undefined;

  locationSearch: string;

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

    this.successOnRetrievingLocations = null;
    this.locationService.getLocations().subscribe(
      (next) => {
        this.locations = next;
        this.filteredLocations = next;
        this.filteredLocationsBackup = next;
        this.successOnRetrievingLocations = true;
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

  compareTagsInSelection(tag1: LocationTag, tag2: LocationTag): boolean {
    return !tag1 || !tag2 ? false : tag1.tagId === tag2.tagId;
  }

  onSelectionChange(event: MatSelectChange): void {
    const value: LocationTag[] = event.value;

    // If no tags to filter are selected, show all locations
    if (value.length === 0) {
      this.filteredLocations = this.locations;
    } else {
      this.filteredLocations = [];

      this.locations.forEach(location => {
        for (const tag of value) {
          // if the filtered tag is not assigned to a certain location ...
          if (location.assignedTags.find(v => v.tagId === tag.tagId) === undefined) {
            return; // ... then check if next location may be added to the filtered locations
          }
        }
        // if all selected tags in the filter were found in the location, push the location
        // else, the lambda was already returned and we wouldn't have gotten here.
        this.filteredLocations.push(location);
      });
    }


    this.filteredLocationsBackup = this.filteredLocations;

    this.displayOpen();
  }

  toggleShowOpen(): void {
    this.showOpen = !this.showOpen;
    this.displayOpen();
  }

  displayOpen(): void {
    if (this.showOpen){
      this.filteredLocations = [];
      this.filteredLocations.forEach(location => {
        this.calendarPeriodService.getStatusOfLocation(location.name).subscribe(
          (next) => {
            if (next.first === LocationStatus.OPEN) {
              this.filteredLocations.push(location);
            }
          }
        );
      });
    } else {
      this.filteredLocations = this.filteredLocationsBackup;
    }
  }

  onSearchEnter(): void {
    this.filteredLocations = [];
    for (const location of this.filteredLocationsBackup) {
      if (location.name.toUpperCase().includes(this.locationSearch.toUpperCase())) {
        this.filteredLocations.push(location);
      }
    }
    this.displayOpen();
  }

  onClearSearch(): void {
    this.filteredLocations = this.locations;
    this.filteredTags.setValue([]);
    this.locationSearch = '';
    this.showOpen = false;
    this.displayOpen();
  }

  get filteredTags(): AbstractControl {
    return this.filterFormGroup.get('filteredTags');
  }
}
