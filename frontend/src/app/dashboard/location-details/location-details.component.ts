import {Component, OnInit} from '@angular/core';
import {Location} from '../../shared/model/Location';
import {ActivatedRoute} from '@angular/router';
import {Observable} from 'rxjs';
import {LocationService} from '../../services/api/locations/location.service';
import {LocationStatus, vars} from '../../../environments/environment';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {CalendarEvent} from 'angular-calendar';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import {TranslateService} from '@ngx-translate/core';
import {LocationTag} from '../../shared/model/LocationTag';
import {TagsService} from '../../services/api/tags/tags.service';
import {CalendarPeriodsService} from '../../services/api/calendar-periods/calendar-periods.service';
import {mapCalendarPeriodsToCalendarEvents} from '../../shared/model/CalendarPeriod';
import { Pair } from 'src/app/shared/model/helpers/Pair';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-location-details',
  templateUrl: './location-details.component.html',
  styleUrls: ['./location-details.component.css'],
  providers: [DatePipe]
})
export class LocationDetailsComponent implements OnInit {
  location: Observable<Location>;
  locationName: string;
  tags: LocationTag[];

  events: CalendarEvent[] = [];

  editor = ClassicEditor;
  description = {
    show: '',
    english: '',
    dutch: ''
  };

  status: Pair<LocationStatus, string>;
  statusInCurrentLang: string;

  altImageUrl = vars.defaultLocationImage;

  currentLang: string;

  constructor(private locationService: LocationService,
              private tagsService: TagsService,
              private route: ActivatedRoute,
              private sanitizer: DomSanitizer,
              private translate: TranslateService,
              private calendarPeriodsService: CalendarPeriodsService,
              private datepipe: DatePipe) { }

  ngOnInit(): void {
    this.locationName = this.route.snapshot.paramMap.get('locationName');
    this.location = this.locationService.getLocation(this.locationName);
    this.currentLang = this.translate.currentLang;

    // when the location is loaded, setup the descriptions
    this.location.subscribe(next => {
      this.description.dutch = next.descriptionDutch;
      this.description.english = next.descriptionEnglish;
      this.setDescriptionToShow();

      this.tags = next.assignedTags;

      // retrieve the calendar periods and map them to calendar events used by Angular Calendar
      this.calendarPeriodsService.getCalendarPeriodsOfLocation(next.name).subscribe(
        next2 => {
          this.events = mapCalendarPeriodsToCalendarEvents(next2);
        }
      );
    });

    this.calendarPeriodsService.getStatusOfLocation(this.locationName).subscribe(
      next => {
        this.status = next;
        this.translateStatus();
      }
    )

    // if the browser language would change, the description needs to change
    this.translate.onLangChange.subscribe(
      () => {
        this.setDescriptionToShow();
        this.currentLang = this.translate.currentLang;
        this.translateStatus();
      }
    );
  }

  locationStatusColorClass(): string {
    return this.status.first === LocationStatus.OPEN ? 'open' : 'closed';
  }

  handleImageError(location: Location): void {
    location.imageUrl = vars.defaultLocationImage;
  }

  getGoogleMapsUrl(location: Location): SafeResourceUrl {
    const url = 'https://www.google.com/maps?q=' + location.address + '&output=embed';
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

  setDescriptionToShow(): void {
    const lang = this.translate.currentLang;

    // Depending on the browser language, return the description of the language.
    // Show the dutch description if the browser language is 'nl'.
    // Otherwise, show the english description.
    this.description.show = lang === 'nl' ? this.description.dutch : this.description.english;
  }

  translateStatus(): void {
    // status.second format: "yyyy-MM-dd hh:mm"
    if (this.status) {
      switch(this.status.first) {
        case LocationStatus.OPEN: {
          const datetime = new Date(this.status.second);
          this.translate.get('dashboard.locationDetails.status.statusOpen').subscribe(
            next => {
              this.statusInCurrentLang = next.replace("{}", this.datepipe.transform(datetime, 'shortTime'));
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
              this.statusInCurrentLang = next.replace("{}", this.datepipe.transform(datetime, 'shortTime'));
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
              this.statusInCurrentLang = next.replace("{}", datetime);
            }, () => {
              this.statusInCurrentLang = 'n/a';
            }
          )
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
