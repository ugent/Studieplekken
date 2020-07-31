import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { CalendarEvent, CalendarMonthViewDay, CalendarView } from "angular-calendar";
import { LangChangeEvent, TranslateService } from "@ngx-translate/core";
import { registerLocaleData } from "@angular/common";
import localeNl from "@angular/common/locales/nl";
import { AuthenticationService } from "../../services/authentication.service";
import { getDay, ILocation, isOpen, openForReservation } from "../../interfaces/ILocation";
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { LocationReservationService } from "../../services/location-reservation.service";
import { LocationService } from "../../services/location.service";
import { ILocationReservation } from "../../interfaces/ILocationReservation";
import { IDay } from "../../interfaces/IDay";
import {dateToIDate, timeToString, compareDates, compareDatesWithTime} from "../../interfaces/CustomDate";
import { BehaviorSubject, Subject } from "rxjs";
import { LockerReservationService } from "../../services/locker-reservation.service";
import {IDate} from "../../interfaces/IDate";
import {PenaltyService} from "../../services/penalty.service";
import {ILocationReservationResponse} from "../../interfaces/ILocationReservationResponse";
import {appLanguages, languageTranslations} from "../../environments/environment";

@Component({
  selector: 'app-location',
  templateUrl: './location.component.html',
  styleUrls: [
    './location.component.css']
})
export class LocationComponent implements OnInit {

  //to use exported functions in html
  isOpen = isOpen;
  dateToIDate = dateToIDate;
  openForReservation = openForReservation;

  //to use the calendar
  view: CalendarView = CalendarView.Month;
  viewDate: Date = new Date();
  days: CalendarMonthViewDay[] = [];
  events: CalendarEvent[] = [];

  //to refresh the calendar and the page
  refresh: Subject<any> = new Subject();

  //the current location
  location = new BehaviorSubject<ILocation>(null);
  locationForSendingRequests: ILocation;
  maps: SafeResourceUrl;
  locationReservationsOfUser: ILocationReservation[] = [];
  locationReservationsOfLocation: ILocationReservation[] = [];

  //this is the max penalty points, if a user has more penalty points then this variable, he is not allowed to reserve for the study location
  MAX_PENALTY_POINTS: number;

  //this is how long the error message will be visible
  messageSecondsVisible: number = 10;
  showErrorToManyPoints: boolean = false;

  //this is then number of penalty points of the logged in user
  penaltyPoints: number = 0;

  //this is how many penalty points the user will get when he cancels his reservation, it is calculated when pressed on the
  //cancel button in the calender, then saved into this variable, and then showed in a popup via this variable
  //the amount of penalty points the user will get is calculated on the backend, so a request is sent to the backed.
  //when the response is here, the popup is showed.
  cancelPoints: number;

  //the number of lockers that are in use in this location
  numberOfLockersInUse: number = 0;

  locale: string;
  nearestDay = new BehaviorSubject(null);

  //selected days to reserve
  selectedDays: IDate[] = [];

  //used to display the different popups
  displayCancel: any = 'none';
  displayLockerConfirmation = 'none';
  displayLocationReservationResponse = 'none';
  displayError = 'none';
  //displayCaptcha: any = 'none';


  //this is the day for which the user wants to cancel his reservation, the date needs to be saved so the cancel method knows
  //which reservation to cancel
  selectedDay: IDate;

  //this variable shows if a user is allowed to reserve a locker, a user only allowed to reserve a locker if
  //he has no other ongoing locker reservations
  allowedToReserveLocker: boolean;

  //this is the HttpResponse of the submission of location reservations
  //it has a list of succesfull reservations
  //and a list of failed reservations
  response: ILocationReservationResponse;

  appLanguages: {};
  languageTranslations: {};

  constructor(private route: ActivatedRoute, private locationService: LocationService, private locationReservationService: LocationReservationService,
    private sanitizer: DomSanitizer, private authenticationService: AuthenticationService, public translate: TranslateService,
    private lockerReservationService: LockerReservationService, private penaltyService: PenaltyService) {
    this.appLanguages = appLanguages;
    this.languageTranslations = languageTranslations;
  }

  ngOnInit(): void {

    //get the name of the location out of the url
    const name = this.route.snapshot.paramMap.get('name');

    //fetch the location object by name
    this.locationService.getLocation(name).subscribe(value => {
      this.maps = this.sanitizeURL(value.mapsFrame);
      this.location.next(value);
      this.locationForSendingRequests = JSON.parse(JSON.stringify(this.location.getValue()));
      this.locationForSendingRequests.lockers = [];
      this.locationForSendingRequests.calendar = [];

      //calculate the time untill the next reservation opens
      this.nearestDay.next(this.getNearestDay(this.location.getValue().calendar));

      //get the number of lockers in use of this location
      this.lockerReservationService.getNumberOfLockersInUseOfLocation(this.location.getValue()).subscribe(value => {
        this.numberOfLockersInUse = value;
      });
    });
    const that = this;

    //fetch the MAX PENALTY POINTS
    this.locationReservationService.MAX_PENALTY_POINTS.subscribe(value => {
      that.MAX_PENALTY_POINTS = value;
    });
    let user = this.authenticationService.getCurrentUser();
    if (!user.roles.includes(this.authenticationService.roles.employee)) {
      this.penaltyPoints = user.penaltyPoints;
    }
    this.locale = this.translate.currentLang;
    if (this.locale == 'nl') {
      registerLocaleData(localeNl);
    }
    this.translate.onLangChange.subscribe(lang => {
      this.locale = lang;
    });
    this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
      this.locale = event.lang;
      if (this.locale == 'nl') {
        registerLocaleData(localeNl);
      }
    });

    //get location reservations of the user to show on the calendar
    this.locationReservationService.getAllLocationReservationsOfUser(this.authenticationService.getCurrentUser().augentID).subscribe(value => {
      this.locationReservationsOfUser = value;
      this.refresh.next();
    });

    //get all location reservations of this location
    //this is used to check if a day is full
    this.locationReservationService.getAllLocationReservationsOfLocationByName(name).subscribe(value => {
      this.locationReservationsOfLocation = value;
      this.refresh.next();
    });

    //check if the user has ongoing locker reservations, if so, the user is not allowed to make another locker reservation
    this.lockerReservationService.getAllLockerReservationsOfUser(user.augentID).subscribe(value => {
      if(value.findIndex(res => res.keyBroughtBack === false) < 0){
        this.allowedToReserveLocker = true;
      } else{
        this.allowedToReserveLocker = false;
      }
      this.refresh.next();
    });


  }

  private sanitizeURL(url: string) {
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

  cancelLocationReservation(): void {
    let day: IDay = getDay(this.location.getValue().calendar, this.selectedDay);
    this.locationReservationService.deleteLocationReservation(this.authenticationService.getCurrentUser().augentID, day.date, day.openingHour).subscribe(value => {
      this.ngOnInit();  //refresh
    });
  }

  //this method is called before rendering the calendar
  beforeMonthViewRender({ body }: { body: CalendarMonthViewDay[] }): void {
    this.days = body;
    body.forEach(day => {
      delete day.cssClass;
      if (this.alreadyReservedOnThisLocation(day.date)) {
        day.cssClass = 'bg-info';
      } else if (day.isPast || !isOpen(this.location.getValue().calendar, dateToIDate(day.date))) {
        day.cssClass = 'closed';
        if(day.inMonth){
          day.cssClass = 'other-month';
        }
      }
    });
  }

  //checks if the user has already reserved on this location for a certain date
  alreadyReservedOnThisLocation(date: Date): boolean {
    if (this.locationReservationsOfUser === null) {
      return false;
    }
    return this.locationReservationsOfUser.some(d => {
      return compareDates(dateToIDate(date), d.date) === 0 && this.location.getValue().name === d.location.name;
    });
  }

  //check if the user has already reserved on another location for a certain date
  alreadyReservedOnOtherLocation(date: Date): boolean {
    if (this.locationReservationsOfUser === null) {
      return false;
    }
    return this.locationReservationsOfUser.some(d => {
      return compareDates(dateToIDate(date), d.date) === 0 && this.location.getValue().name !== d.location.name;
    });
  }

  //Display opening hours in calendar
  getOpeningHoursToString(date: IDate): string {
    let day = getDay(this.location.getValue().calendar, date);
    return timeToString(day.openingHour) + " - " + timeToString(day.closingHour);
  }

  //this method is used when a user has already reserved on another location for a certain date,
  //the name of that location is then showed on the calendar, this method returns the name of that location
  getLocationReservationName(date: Date): string {
    let locationName: string = "";
    this.locationReservationsOfUser.forEach(d => {
      if (compareDates(dateToIDate(date), d.date) === 0) {
        locationName = d.location.name;
      }
    });
    return locationName;
  }

  //returns the first next day to open reservations
  getNearestDay(days: IDay[]): IDay {
    if (days.length >= 1) {
      let index = days.findIndex(day => compareDatesWithTime(day.openForReservationDate, dateToIDate(new Date())) >= 0);
      if(index < 0){
        return null;
      }
      let nearest: IDay = days[index];
      for (let i = 1; i < days.length; i++) {
        if(compareDatesWithTime(days[i].openForReservationDate, dateToIDate(new Date())) >= 0 &&
          compareDatesWithTime(days[i].openForReservationDate, nearest.openForReservationDate) < 0){
          nearest = days[i];
        }
      }
      return nearest;
    }
    return null;
  }

  reserveLocker(): void {
    this.lockerReservationService.addLockerReservation(this.location.getValue().name, this.authenticationService.getCurrentUser().augentID).subscribe(value => {
      this.ngOnInit();
    });
  }

  //this method shows a warning that the user has to many penalty points to reserve, this warning is showed for a few seconds
  //and then disappears
  showToManyPoints(): void {
    this.showErrorToManyPoints = true;
    const that = this;
    setTimeout(() => {
      that.showErrorToManyPoints = false;
    }, this.messageSecondsVisible * 1000);
  }

  //this method gets the number of penalty points the user will get if he cancels a certain location reservation
  getCancelPoints(d: Date): void {
    const that = this;
    let date: IDate = dateToIDate(d);
    let time = getDay(this.location.getValue().calendar, date).openingHour;
    date.hrs = time.hours;
    date.min = time.minutes;
    this.penaltyService.getCancelPoints(date).subscribe(value => {
      this.cancelPoints = value;
      this.displayCancel = 'block';
    })
  }

  //checks if the current date is past the opening hour of a certain date
  isPastOpeningHour(date: IDate): boolean {
    let day: IDay = getDay(this.location.getValue().calendar, date);
    if(day.openingHour !== null && (compareDates(date, dateToIDate(new Date())) < 0 || (compareDates(date, dateToIDate(new Date())) === 0 &&
      day.openingHour.hours*60 + day.openingHour.minutes < new Date().getHours()*60 + new Date().getMinutes()))){
      return true;
    }
    return false;
  }

  //check if the location is full on a certain date
  isFull(date: IDate): boolean {
    let count = 0;
    this.locationReservationsOfLocation.forEach(res => {
      if(compareDates(res.date, date) === 0){
        count++;
      }
    });
    if(count === this.location.getValue().numberOfSeats){
      return true;
    }
    return false;
  }

  //checks if lockers are open for reservation
  lockerReservationsAreOpen(): boolean {
    return this.location.getValue().startPeriodLockers !== null && this.location.getValue().endPeriodLockers !== null &&
      this.location.getValue().startPeriodLockers !== undefined && this.location.getValue().endPeriodLockers !== undefined &&
      compareDates(dateToIDate(new Date), this.location.getValue().startPeriodLockers) >= 0 &&
      compareDates(dateToIDate(new Date), this.location.getValue().endPeriodLockers) < 0;
  }

  //this method adds a day to the selected days
  addDate(date: IDate){
    if(this.authenticationService.getCurrentUser().penaltyPoints >= this.MAX_PENALTY_POINTS){
      this.showToManyPoints();
    } else {
      if(this.selectedDays.findIndex(day => compareDates(date, day) === 0) < 0) {
        this.selectedDays.push(date);
        this.sortSelectedDays();
        this.refresh.next();
      }
    }
  }

  //submit the location reservations
  submitReservations(): void {
    let reservations: ILocationReservation[] = [];
    this.selectedDays.forEach(day => {
      let reservation = {"user": this.authenticationService.getCurrentUser(), "location": this.locationForSendingRequests, "date": day, "attended": null};
      reservations.push(reservation);
    });

    this.locationReservationService.addLocationReservations(reservations).subscribe(value => {
      this.response = value;
      this.displayLocationReservationResponse = 'block';
      this.selectedDays = [];
      this.ngOnInit();
    });
  }

  //this method removes a date from the 'selectedDays' list
  unselect(date: IDate): void {
    this.selectedDays.splice(this.selectedDays.findIndex(day => compareDates(day, date) === 0), 1);
    this.refresh.next();
  }

  //this method checks if a date is already in the 'selectedDays' list
  isSelected(date: IDate): boolean {
    return this.selectedDays.findIndex(day => compareDates(day, date) === 0) > -1;
  }

  sortSelectedDays() {
    this.selectedDays.sort((a, b) => {
      let _a = a.year * 10000 + a.month * 100 + a.day;
      let _b = b.year * 10000 + b.month * 100 + b.day;
      return _a - _b;
    });
  }
}
