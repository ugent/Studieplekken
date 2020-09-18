import {Component, OnInit} from '@angular/core';
import {Location} from '../../shared/model/Location';
import {ActivatedRoute} from '@angular/router';
import {Observable} from 'rxjs';
import {LocationService} from '../../services/api/locations/location.service';
import {vars} from '../../../environments/environment';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import {CalendarEvent} from 'angular-calendar';
import {CalendarPeriodsService} from '../../services/api/calendar-periods/calendar-periods.service';
import {mapCalendarPeriodsToCalendarEvents} from '../../shared/model/CalendarPeriod';

@Component({
  selector: 'app-location-details',
  templateUrl: './location-details.component.html',
  styleUrls: ['./location-details.component.css']
})
export class LocationDetailsComponent implements OnInit {
  location: Observable<Location>;

  events: CalendarEvent[] = [
    {
      start: new Date(),
      title: 'Test calendar event'
    }
  ];

  constructor(private locationService: LocationService,
              private route: ActivatedRoute,
              private sanitizer: DomSanitizer,
              private calendarPeriodsService: CalendarPeriodsService) { }

  ngOnInit(): void {
    const locationName = this.route.snapshot.paramMap.get('locationName');
    this.location = this.locationService.getLocation(locationName);

    this.location.subscribe(next => {

      // retrieve the calendar events
      this.calendarPeriodsService.getCalendarPeriodsOfLocation(next.name).subscribe(
        next2 => {
          this.events = mapCalendarPeriodsToCalendarEvents(next2);
        }
      );
    });
  }

  handleImageError(location: Location): void {
    location.imageUrl = vars.defaultLocationImage;
  }

  getGoogleMapsUrl(location: Location): SafeResourceUrl {
    const url = 'https://www.google.com/maps?q=' + location.address + '&output=embed';
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }
}
