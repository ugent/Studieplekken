import { Component, OnInit } from '@angular/core';
import {CalendarEvent, CalendarMonthViewDay, CalendarView} from "angular-calendar";
import {Observable, Subject} from "rxjs";
import {LangChangeEvent, TranslateService} from "@ngx-translate/core";
import {registerLocaleData} from "@angular/common";
import localeNl from "@angular/common/locales/nl";
import {LocationReservationService} from "../../services/location-reservation.service";
import {ILocationReservation} from "../../interfaces/ILocationReservation";
import {AuthenticationService} from "../../services/authentication.service";
import {IDate} from "../../interfaces/IDate";
import {compareDates, compareDatesWithTime, CustomDate, dateToIDate} from "../../interfaces/CustomDate";
import {IDay} from "../../interfaces/IDay";
import {getDay, ILocation} from "../../interfaces/ILocation";
import {LocationService} from "../../services/location.service";
import {PenaltyService} from "../../services/penalty.service";

@Component({
  selector: 'app-my-calendar',
  templateUrl: './my-calendar.component.html',
  styleUrls: ['./my-calendar.component.css']
})
export class MyCalendarComponent implements OnInit {

  //exported function for html
  dateToIDate = dateToIDate;

  view: CalendarView = CalendarView.Month;
  viewDate: Date = new Date();
  refresh: Subject<any> = new Subject();

  events: CalendarEvent[] = [];

  selectedDay: IDate;
  cancelPoints: number;
  locale: string;
  reservations: ILocationReservation[] = [];
  displayCancel: any = 'none';

  constructor(private authenticationService: AuthenticationService, private locationReservationService: LocationReservationService,
              private translate: TranslateService, private penaltyService: PenaltyService) {
  }

  ngOnInit(): void {
    this.locationReservationService.getAllLocationReservationsOfUser(this.authenticationService.getCurrentUser().augentID).subscribe(value => {
      this.reservations = value;
      this.refresh.next();
    });
    this.locale = this.translate.currentLang;
    if (this.locale == 'nl'){
      registerLocaleData(localeNl);
    }
    this.translate.onLangChange.subscribe(lang=>{
      this.locale = lang;
    });
    this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
      this.locale = event.lang;
      if (this.locale == 'nl'){
        registerLocaleData(localeNl);
      }
    });
  }

  reserved(d: Date): boolean {
    if(this.reservations.findIndex(res => compareDates(res.date, dateToIDate(d)) === 0) < 0){
      return false;
    }
    return true;
  }

  getLocationNameForReservation(d: Date): string {
    let locationName: string = "";
    let index = this.reservations.findIndex(res => compareDates(res.date, dateToIDate(d)) === 0);
    if(index >= 0){
      locationName = this.reservations[index].location.name;
    }
    return locationName;
  }

  cancelLocationReservation(): void {
    let location: ILocation = this.reservations.find(res => compareDates(res.date, this.selectedDay) === 0).location;
    let day: IDay = getDay(location.calendar, this.selectedDay);
    this.locationReservationService.deleteLocationReservation(this.authenticationService.getCurrentUser().augentID, this.selectedDay, day.openingHour).subscribe(value => {
      this.ngOnInit();  //refresh
    });
  }

  beforeMonthViewRender({ body }: { body: CalendarMonthViewDay[] }): void {
    body.forEach(day => {
      delete day.cssClass;
      if(this.reserved(day.date)){
        day.cssClass = 'bg-info';
      }
    });
  }

  isPastOpeningHour(date: Date): boolean {
    let location: ILocation = this.reservations.find(res => compareDates(res.date, dateToIDate(date)) === 0).location;
    let day: IDay = getDay(location.calendar, dateToIDate(date));
    if (compareDates(dateToIDate(date), dateToIDate(new Date())) < 0 || (compareDates(dateToIDate(date), dateToIDate(new Date())) === 0 &&
      day.openingHour.hours * 60 + day.openingHour.minutes < new Date().getHours() * 60 + new Date().getMinutes())) {
      return true;
    }
    return false;
  }

  getCancelPoints(d: Date): void {
    const that = this;
    let date: IDate = dateToIDate(d);
    let location: ILocation = this.reservations.find(res => compareDates(res.date, date) === 0).location;
    let time = getDay(location.calendar, date).openingHour;
    date.hrs = time.hours;
    date.min = time.minutes;
    this.penaltyService.getCancelPoints(date).subscribe(value => {
      this.cancelPoints = value;
      this.displayCancel = 'block';
    })
  }
}
