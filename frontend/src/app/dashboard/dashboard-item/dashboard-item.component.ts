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

@Component({
  selector: 'app-dashboard-item',
  templateUrl: './dashboard-item.component.html',
  styleUrls: ['./dashboard-item.component.css'],
  providers: [DatePipe]
})
export class DashboardItemComponent implements OnInit, AfterViewInit {
  @Input() location: Location;
  @Input() status: Pair<LocationStatus, string>;

  occupation = 0;

  altImageUrl = defaultLocationImage;

  assignedTags: LocationTag[];

  currentLang: string;
  tagsInCurrentLang: string; // e.g. 'tag1, tag2, tag3', set by auxiliary setupTagsInCurrentLang()

  showProgressBar: boolean;

  statusInCurrentLang: string;

  constructor(private locationService: LocationService,
              private calendarPeriodsService: CalendarPeriodsService,
              private translate: TranslateService,
              private functionalityService: ApplicationTypeFunctionalityService,
              private datePipe: DatePipe) {
  }

  ngOnInit(): void {
    this.currentLang = this.translate.currentLang;
    this.translate.onLangChange.subscribe(
      () => {
        this.currentLang = this.translate.currentLang;
        this.setupTagsInCurrentLang();
        this.translateStatus();
      }
    );

    this.calendarPeriodsService.getStatusOfLocation(this.location.name).subscribe(
      next => {
        this.status = next;
        this.translateStatus();
      }
    );

    this.locationService.getNumberOfReservationsNow(this.location.name).subscribe(next => this.occupation = next);

    this.assignedTags = this.location.assignedTags;
    this.setupTagsInCurrentLang();

    this.showProgressBar = this.functionalityService.showReservationsFunctionality();
  }

  ngAfterViewInit(): void {
  }

  locationStatusColorClass(): string {
    if (this.status) {
      return this.status.first === LocationStatus.OPEN ? 'open' : 'closed';
    }
  }

  handleImageError(): void {
    this.location.imageUrl = defaultLocationImage;
  }

  setupTagsInCurrentLang(): void {
    if (this.assignedTags && this.assignedTags.length > 0) {
      this.tagsInCurrentLang = '';

      this.assignedTags.forEach(tag => {
        if (this.currentLang === 'nl') {
          this.tagsInCurrentLang += tag.dutch + ', ';
        } else {
          this.tagsInCurrentLang += tag.english + ', ';
        }
      });

      this.tagsInCurrentLang = this.tagsInCurrentLang.substr(0, this.tagsInCurrentLang.length - 2);
    } else {
      this.translate.get('general.notAvailableAbbreviation').subscribe(
        next => {
          this.tagsInCurrentLang = next;
        }, () => {
          this.tagsInCurrentLang = 'general.notAvailableAbbreviation';
        }
      );
    }
  }

  translateStatus(): void {
    // status.second format: "yyyy-MM-dd hh:mm"
    if (this.status) {
      switch (this.status.first) {
        case LocationStatus.OPEN: {
          const datetime = new Date(this.status.second);
          this.translate.get('dashboard.locationDetails.status.statusOpen').subscribe(
            next => {
              this.statusInCurrentLang = next.replace('{}', this.datePipe.transform(datetime, 'shortTime'));
            }, () => {
              this.statusInCurrentLang = 'general.notAvailableAbbreviation';
            }
          );
          break;
        }
        case LocationStatus.CLOSED: {
          this.translate.get('dashboard.locationDetails.status.statusClosed').subscribe(
            next => {
              this.statusInCurrentLang = next;
            }, () => {
              this.statusInCurrentLang = 'general.notAvailableAbbreviation';
            }
          );
          break;
        }
        case LocationStatus.CLOSED_ACTIVE: {
          const datetime = new Date(this.status.second);
          this.translate.get('dashboard.locationDetails.status.statusClosedActive').subscribe(
            next => {
              this.statusInCurrentLang = next.replace('{}', this.datePipe.transform(datetime, 'shortTime'));
            }, () => {
              this.statusInCurrentLang = 'general.notAvailableAbbreviation';
            }
          );
          break;
        }
        case LocationStatus.CLOSED_UPCOMING: {
          const datetime = new Date(this.status.second).toLocaleString();
          this.translate.get('dashboard.locationDetails.status.statusClosedUpcoming').subscribe(
            next => {
              this.statusInCurrentLang = next.replace('{}', datetime);
            }, () => {
              this.statusInCurrentLang = 'general.notAvailableAbbreviation';
            }
          );
          break;
        }
      }
    } else {
      this.translate.get('general.notAvailableAbbreviation').subscribe(
        next => {
          this.statusInCurrentLang = next;
        }, () => {
          this.statusInCurrentLang = 'general.notAvailableAbbreviation';
        }
      );
    }
  }
}
