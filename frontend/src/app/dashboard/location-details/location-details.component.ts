import {Component, OnInit} from '@angular/core';
import {Location} from '../../shared/model/Location';
import {ActivatedRoute} from '@angular/router';
import {Observable} from 'rxjs';
import {LocationService} from '../../services/api/locations/location.service';
import {vars} from '../../../environments/environment';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {CalendarEvent} from 'angular-calendar';
import * as ClassicEditor from '@ckeditor/ckeditor5-build-classic';
import {TranslateService} from '@ngx-translate/core';
import {LocationTag} from '../../shared/model/LocationTag';
import {TagsService} from '../../services/api/tags/tags.service';
import {CalendarPeriodsService} from '../../services/api/calendar-periods/calendar-periods.service';
import { mapCalendarPeriodsToCalendarEvents } from 'src/app/shared/model/CalendarPeriod';
import { Timeslot } from 'src/app/shared/model/Timeslot';

@Component({
  selector: 'app-location-details',
  templateUrl: './location-details.component.html',
  styleUrls: ['./location-details.component.css']
})
export class LocationDetailsComponent implements OnInit {
  location: Observable<Location>;
  locationName: string;
  tags: LocationTag[];

  events: CalendarEvent[] = [];

  currentTimeslot: Timeslot;
  canReserve = false;

  editor = ClassicEditor;
  description = {
    show: '',
    english: '',
    dutch: ''
  };

  altImageUrl = vars.defaultLocationImage;

  currentLang: string;

  constructor(private locationService: LocationService,
              private tagsService: TagsService,
              private route: ActivatedRoute,
              private sanitizer: DomSanitizer,
              private translate: TranslateService,
              private calendarPeriodsService: CalendarPeriodsService) { }

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

    // if the browser language would change, the description needs to change
    this.translate.onLangChange.subscribe(
      () => {
        this.setDescriptionToShow();
        this.currentLang = this.translate.currentLang;
      }
    );
  }

  timeslotPicked(event: any): void {
    this.currentTimeslot = event;
    this.canReserve = true;
    console.log("Event received in LocationDetailsComponent");
    console.log(event);
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
}
