import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Location} from '../../shared/model/Location';
import {LocationService} from '../../services/api/locations/location.service';
import {Pair} from '../../shared/model/helpers/Pair';
import {TranslateService} from '@ngx-translate/core';
import {LocationTag} from '../../shared/model/LocationTag';
import {ApplicationTypeFunctionalityService} from '../../services/functionality/application-type/application-type-functionality.service';
import {defaultLocationImage, LocationStatus} from '../../app.constants';
import {CalendarPeriodsService} from '../../services/api/calendar-periods/calendar-periods.service';
import {Moment} from 'moment';
import { Observable} from 'rxjs';

@Component({
  selector: 'app-dashboard-item',
  templateUrl: './dashboard-item.component.html',
  styleUrls: ['./dashboard-item.component.css', '../location.css'],
  providers: [DatePipe]
})
export class DashboardItemComponent implements OnInit, AfterViewInit {
  @Input() location: Location;
  @Input() status: Pair<LocationStatus, string>;
  @Input() nextReservableFrom: Moment;

  occupation = 0;

  imageUrlErrorOccurred = false;
  altImageUrl = defaultLocationImage;

  assignedTags: LocationTag[];

  currentLang: string;
  tagsInCurrentLang: string[]; // e.g. 'tag1, tag2, tag3', set by auxiliary setupTagsInCurrentLang()

  showProgressBar: boolean;

  statusInCurrentLang: string;
  occupationObs: Observable<number>;
  showLockersManagement: boolean;

  /* Subscriptions */
  constructor(private locationService: LocationService,
              private calendarPeriodsService: CalendarPeriodsService,
              private translate: TranslateService,
              private functionalityService: ApplicationTypeFunctionalityService) {
  }

  ngOnInit(): void {
    this.currentLang = this.translate.currentLang;
    this.translate.onLangChange.subscribe(
      () => {
        this.currentLang = this.translate.currentLang;
        this.setupTagsInCurrentLang();
      }
    );

    this.occupationObs = this.locationService.getNumberOfReservationsNow(this.location.name);

    this.assignedTags = this.location.assignedTags;
    this.setupTagsInCurrentLang();

    this.showProgressBar = this.functionalityService.showReservationsFunctionality();
    this.showLockersManagement = this.functionalityService.showLockersManagementFunctionality();
  }

  ngAfterViewInit(): void {
  }

  locationStatusColorClass(): string {
    if (this.status) {
      return this.status.first === LocationStatus.OPEN ? 'open' : 'closed';
    }
  }

  handleImageError(): void {
    this.imageUrlErrorOccurred = true;
  }

  setupTagsInCurrentLang(): void {
    this.tagsInCurrentLang = [];
    if (this.assignedTags && this.assignedTags.length > 0) {
      this.assignedTags.forEach(tag => {
        if (this.currentLang === 'nl') {
          this.tagsInCurrentLang.push(tag.dutch);
        } else {
          this.tagsInCurrentLang.push(tag.english);
        }
      });
    }
  }

}
