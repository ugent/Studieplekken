import { Component, OnInit } from '@angular/core';
import {LocationService} from '../services/api/locations/location.service';
import {Location} from '../shared/model/Location';
import {LocationTag} from '../shared/model/LocationTag';
import {TagsService} from '../services/api/tags/tags.service';
import {TranslateService} from '@ngx-translate/core';
import {FormControl} from '@angular/forms';
import {MatSelectChange} from '@angular/material/select';
import {AuthenticationService} from "../services/authentication/authentication.service";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  locations: Location[];
  filteredLocations: Location[];

  enableClearButton = false;
  backupFilterForAfterClear: Location[];

  tags: LocationTag[];
  matSelectFormControl: FormControl;

  currentLang: string;

  successOnRetrievingLocations: boolean = undefined;

  locationSearch: string;

  constructor(private locationService: LocationService,
              private tagsService: TagsService,
              private translate: TranslateService,
              private authenticationService: AuthenticationService) { }

  ngOnInit(): void {
    // some
    this.authenticationService.whoAmI().subscribe();

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
        this.backupFilterForAfterClear = next;
        this.successOnRetrievingLocations = true;
      }, () => {
        this.successOnRetrievingLocations = false;
      }
    );

    this.tagsService.getAllTags().subscribe(
      (next) => {
        this.tags = next;
        this.matSelectFormControl = new FormControl(next);
      }
    );
  }

  compareTagsInSelection(tag1: LocationTag, tag2: LocationTag): boolean {
    return tag1.tagId === tag2.tagId;
  }

  onSelectionChange(event: MatSelectChange): void {
    const value: LocationTag[] = event.value;
    this.filteredLocations = [];

    this.locations.forEach(location => {
      for (const tag of location.assignedTags) {
        if (value.find(v => v.tagId === tag.tagId)) {
          this.filteredLocations.push(location);
          break;
        }
      }
    });

    this.backupFilterForAfterClear = this.filteredLocations;
  }

  onSearchEnter(): void {
    this.filteredLocations = [];
    for (const location of this.backupFilterForAfterClear) {
      if (location.name.toUpperCase().includes(this.locationSearch.toUpperCase())) {
        this.filteredLocations.push(location);
      }
    }

    this.enableClearButton = true;
  }

  onClearSearch(): void {
    this.filteredLocations = this.backupFilterForAfterClear;
    this.enableClearButton = false;
    this.locationSearch = '';
  }
}
