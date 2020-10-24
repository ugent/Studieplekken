import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import {DatePipe} from '@angular/common';
import {Location} from '../../shared/model/Location';
import {Pair} from '../../shared/model/helpers/Pair';
import {vars, LocationStatus} from '../../../environments/environment';
import {LocationService} from '../../services/api/locations/location.service';
import {CalendarPeriodsService} from '../../services/api/calendar-periods/calendar-periods.service';
import {TranslateService} from '@ngx-translate/core';
import {LocationTag} from '../../shared/model/LocationTag';
import {ApplicationTypeFunctionalityService} from '../../services/functionality/application-type/application-type-functionality.service';

@Component({
  selector: 'app-dashboard-item',
  templateUrl: './dashboard-item.component.html',
  styleUrls: ['./dashboard-item.component.css'],
  providers: [DatePipe]
})
export class DashboardItemComponent implements OnInit, AfterViewInit {
  @Input() location: Location;

  occupation: number;

  altImageUrl = vars.defaultLocationImage;

  assignedTags: LocationTag[];

  currentLang: string;
  tagsInCurrentLang: string; // e.g. 'tag1, tag2, tag3', set by auxiliary setupTagsInCurrentLang()

  showProgressBar: boolean;

  status: Pair<LocationStatus, string>;
  statusInCurrentLang: string;

  constructor(private locationService: LocationService,
              private calendarPeriodsService: CalendarPeriodsService,
              private translate: TranslateService,
              private functionalityService: ApplicationTypeFunctionalityService,
              private datepipe: DatePipe) {
  }

  ngOnInit(): void {
    this.locationService.getNumberOfReservations(this.location).subscribe(next => {
      this.occupation = Math.round(100 * next / this.location.numberOfSeats);
    });

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

    this.assignedTags = this.location.assignedTags;
    this.setupTagsInCurrentLang();

    this.showProgressBar = this.functionalityService.showReservationsFunctionality();
  }

  ngAfterViewInit(): void {
  }

  locationStatusColorClass(): string {
    if(this.status) {
      return this.status.first === LocationStatus.OPEN ? 'open' : 'closed';
    }
  }

  handleImageError(): void {
    this.location.imageUrl = vars.defaultLocationImage;
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
          this.tagsInCurrentLang = 'n/a';
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
              this.statusInCurrentLang = next.replace('{}', this.datepipe.transform(datetime, 'shortTime'));
            }, () => {
              this.statusInCurrentLang = 'n/a';
            }
          );
          break;
        }
        case LocationStatus.CLOSED: {
          this.translate.get('dashboard.locationDetails.status.statusClosed').subscribe(
            next => {
              this.statusInCurrentLang = next;
            }, () => {
              this.statusInCurrentLang = 'n/a';
            }
          );
          break;
        }
        case LocationStatus.CLOSED_ACTIVE: {
          const datetime = new Date(this.status.second);
          this.translate.get('dashboard.locationDetails.status.statusClosedActive').subscribe(
            next => {
              this.statusInCurrentLang = next.replace('{}', this.datepipe.transform(datetime, 'shortTime'));
            }, () => {
              this.statusInCurrentLang = 'n/a';
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
              this.statusInCurrentLang = 'n/a';
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
          this.statusInCurrentLang = 'n/a';
        }
      );
    }
  }
}
