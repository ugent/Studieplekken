import { Component, OnInit } from '@angular/core';
import {IUser} from "../../interfaces/IUser";
import {LocationReservationService} from "../../services/location-reservation.service";
import {ILocationReservation} from "../../interfaces/ILocationReservation";
import {IDate} from "../../interfaces/IDate";
import {compareDates, CustomDate, dateToIDate, timeToString} from "../../interfaces/CustomDate";
import {AuthenticationService} from "../../services/authentication.service";
import {transition, trigger, useAnimation} from "@angular/animations";
import {rowsAnimation} from "../animations";
import {IDay} from "../../interfaces/IDay";
import {getDay, ILocation} from "../../interfaces/ILocation";
import {PenaltyService} from "../../services/penalty.service";

@Component({
  selector: 'app-location-overview',
  templateUrl: './location-overview.component.html',
  styleUrls: ['./location-overview.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class LocationOverviewComponent implements OnInit {

  results: ILocationReservation[] = [];
  linesOnPage = 20;
  lower: number;
  upper: number;
  numbers: number[];

  cancelPoints: number;
  displayCancel: any = 'none';
  selectedDay: IDate;

  constructor(private locationReservationService: LocationReservationService, public authenticationService: AuthenticationService,
              private penaltyService: PenaltyService) {
  }

  ngOnInit(): void {
    this.lower = 0;
    this.upper = 19;
    this.locationReservationService.getAllLocationReservationsOfUser(this.authenticationService.getCurrentUser().augentID).subscribe((value) => {
      this.results = value;
      this.sortResults();
      this.numbers = Array(Math.ceil(this.results.length / 20)).fill(1).map((x, i) => i + 1);
    });
  }

  cancelLocationReservation(): void {
    let location: ILocation = this.results.find(res => compareDates(res.date, this.selectedDay) === 0).location;
    let day = getDay(location.calendar, this.selectedDay);
    this.locationReservationService.deleteLocationReservation(this.authenticationService.getCurrentUser().augentID, this.selectedDay, day.openingHour).subscribe(value => {
      this.locationReservationService.getAllLocationReservationsOfUser(this.authenticationService.getCurrentUser().augentID).subscribe(value => {
        this.results = value;
        this.sortResults();
        this.numbers = Array(Math.ceil(this.results.length / 20)).fill(1).map((x, i) => i + 1);
      })
    });
  }

  isPastOpeningHour(date: IDate): boolean {
    let location: ILocation = this.results.find(res => compareDates(res.date, date) === 0).location;
    let day: IDay = getDay(location.calendar, date);
    if (compareDates(date, dateToIDate(new Date())) < 0 || (compareDates(date, dateToIDate(new Date())) === 0 &&
      day.openingHour.hours * 60 + day.openingHour.minutes < new Date().getHours() * 60 + new Date().getMinutes())) {
      return true;
    }
    return false;
  }

  newPage(i) {
    this.lower = (i - 1) * 20;
    this.upper = (i - 1) * 20 + 19;
  }

  floor(i): number {
    return Math.floor(i);
  }

  sortResults(): void {
    this.results.sort((res1, res2) => {
      return compareDates(res2.date, res1.date);
    });
  }

  isFutureDate(date: IDate): boolean {
    //let today: IDate = new CustomDate();
    //return date.year * 365 + date.month * 31 + date.day - today.year * 365 - today.month * 31 - today.day >= 0 ? true : false;
    return compareDates(date, dateToIDate(new Date())) >= 0;
  }

  //Display opening hours

  getOpeningHoursToString(res: ILocationReservation): string {
    let day: IDay = getDay(res.location.calendar, res.date);
    return timeToString(day.openingHour) + " - " + timeToString(day.closingHour);
  }

  getCancelPoints(date: IDate): void {
    const that = this;
    let location: ILocation = this.results.find(res => compareDates(res.date, date) === 0).location;
    let time = getDay(location.calendar, date).openingHour;
    date.hrs = time.hours;
    date.min = time.minutes;
    this.penaltyService.getCancelPoints(date).subscribe(value => {
      this.cancelPoints = value;
      this.displayCancel = 'block';
    })
  }
}
