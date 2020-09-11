import { Component, OnInit } from '@angular/core';
import {LocationService} from '../services/api/locations/location.service';
import {Location} from '../shared/model/Location';
import {LocationTag} from '../shared/model/LocationTag';
import {TagsService} from '../services/api/tags/tags.service';
import {TranslateService} from '@ngx-translate/core';
import {FormControl} from '@angular/forms';
import {MatSelectChange} from '@angular/material/select';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  locations: Location[];
  filteredLocations: Location[];

  tags: LocationTag[];
  matSelectFormControl: FormControl;

  currentLang: string;

  successOnRetrievingLocations: boolean = undefined;

  constructor(private locationService: LocationService,
              private tagsService: TagsService,
              private translate: TranslateService) { }

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
      for (const tag of location.tags) {
        if (value.find(v => v.tagId === tag.tagId)) {
          this.filteredLocations.push(location);
          break;
        }
      }
    });
  }
}
