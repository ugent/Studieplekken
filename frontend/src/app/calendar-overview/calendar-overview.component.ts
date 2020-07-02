import {Component, OnInit} from '@angular/core';
import {CalendarEvent, CalendarMonthViewDay, CalendarView} from 'angular-calendar';
import {registerLocaleData, Time} from '@angular/common';
import localeNl from '@angular/common/locales/nl';
import {LangChangeEvent, TranslateService} from "@ngx-translate/core";
import {FormControl, FormGroup} from "@angular/forms";
import {getDay, ILocation, isOpen} from "../../interfaces/ILocation";
import {LocationService} from "../../services/location.service";
import {IDate} from "../../interfaces/IDate";
import {IDay} from "../../interfaces/IDay";
import {multipleDaysFormValidator, singleDayFormValidator} from "../shared/validators.directive";
import {compareDates, dateToIDate, dateToString, timeToString} from "../../interfaces/CustomDate";
import {Subject} from "rxjs";
import {AuthenticationService} from '../../services/authentication.service';

declare var $: any;

@Component({
  selector: 'app-calendar-overview',
  templateUrl: './calendar-overview.component.html',
  styleUrls: ['./calendar-overview.component.css']
})

export class CalendarOverviewComponent implements OnInit {

  //to use exported functions in html
  isOpen = isOpen;
  dateToIDate = dateToIDate;

  //to use calender library
  view: CalendarView = CalendarView.Month;
  viewDate: Date = new Date();
  events: CalendarEvent[] = [];

  //to refresh the page and calendar
  refresh: Subject<any> = new Subject();

  clickedDate: Date = new Date();
  currentDay: CalendarMonthViewDay;

  //current selected location and calendar
  location: ILocation;
  calendar: IDay[] = [];

  //all locations
  locations: ILocation[];
  locale: string;

  //show error messages
  messageSecondsVisible: number = 10;
  showErrorPastDaySubmit: boolean = false;
  showErrorPastDaySubmitMultipleDays: boolean = false;
  showErrorOpenForReservationDate: boolean = false;
  showSuccessfullySubmitted: boolean = false;
  showSuccessfullySubmittedMultipleDays: boolean = false;

  //save the information of the clicked day when editing its information for when the editing is cancelled
  openingHour: Time;
  closingHour: Time;
  openForReservationDate: IDate;
  openForReservationTime: Time;

  selectTypeOfForm: number = 0;

  singleDayForm = new FormGroup({
    open: new FormControl(''),
    openingHour: new FormControl(''),
    closingHour: new FormControl(''),
    openForReservationDate: new FormControl(''),
    openForReservationTime: new FormControl('')
  }, { validators: singleDayFormValidator });

  multipleDaysForm = new FormGroup({
    startDate: new FormControl(''),
    endDate: new FormControl(''),
    open: new FormControl(''),
    openingHour: new FormControl(''),
    closingHour: new FormControl(''),
    openForReservationDate: new FormControl(''),
    openForReservationTime: new FormControl('')
  }, { validators: multipleDaysFormValidator});

  constructor(private locationService: LocationService, private translate: TranslateService,
              private authenticationService: AuthenticationService) {
    this.locale = translate.currentLang;
    if (this.locale == 'nl'){
      registerLocaleData(localeNl);
    }
    translate.onLangChange.subscribe(lang=>{
      this.locale = lang;
    });
  }

  ngOnInit(): void {
    //this method fetches all the locations without the lockers for performance, because they are not needed here
    if(this.authenticationService.currentUserHasRole(this.authenticationService.roles.admin)){
      this.locationService.getAllLocationsWithoutLockers().subscribe(value => {
        this.locations = value;
        this.location = this.locations[0];
        this.calendar = this.location.calendar;
        this.setDayInfo(this.clickedDate);
        this.refresh.next();
      });
    } else { // a scan employee should only be able to change those locations for which he or she is responsible
      this.locations = [];
      this.locationService.getAllAuthenticatedLocations(this.authenticationService.getCurrentUser().mail).subscribe( locations => {
        for(let loc of locations){
          this.locationService.getLocationWithoutLockers(loc).subscribe(value => {
            this.locations.push(value);
            if(this.locations.length == 1){
              this.location = this.locations[0];
              this.calendar = this.location.calendar;
              this.setDayInfo(this.clickedDate);
              this.refresh.next();
            }
          })
        }
      })
    }


    this.translate.onLangChange.subscribe((event: LangChangeEvent) => {
      this.locale = event.lang;
      if (this.locale == 'nl'){
        registerLocaleData(localeNl);
      }
    });

    //this function subscribes the 'opening and closing hour' fields in the forms to the 'open' checkbox field
    this.subscribeToOpen();
    this.multipleDaysForm.controls['open'].setValue(false);
  }

  locationChanged(): void {
    this.calendar = this.location.calendar;
    this.setDayInfo(this.clickedDate);
  }


  dayClicked(date: CalendarMonthViewDay): void {
    this.setDayInfo(date.date);
    if(this.currentDay !== undefined){
      delete this.currentDay.cssClass;
    }
    this.clickedDate = date.date;
    this.currentDay = date;
    date.cssClass = 'bg-warning';
  }

  //this method fills in the singleDayForm with information of the clicked day
  setDayInfo(date: Date): void {

    //first set the variables that save this information to undefined
    //when a closed day is clicked, the fields will be empty
    this.openingHour = undefined;
    this.closingHour = undefined;
    this.openForReservationDate = undefined;
    this.openForReservationTime = undefined;

    if(this.location.calendar !== undefined){

      /*
      the interface IDay represents a calendar day of a location,
      it has variables like, openingHour, closingHour, openForReservationDate, and the date of that day
       */

      //this method will fetch the day object of the current calendar for the clicked date
      let day: IDay = getDay(this.location.calendar, dateToIDate(date));

      //fill in form
      if(day.openingHour === null) {
        this.singleDayForm.controls['open'].setValue(false);
      } else {
        this.openingHour = day.openingHour;
        this.closingHour = day.closingHour;
        this.openForReservationDate = day.openForReservationDate;
        this.openForReservationTime = {"hours": day.openForReservationDate.hrs, "minutes": day.openForReservationDate.min};
        this.singleDayForm.controls['open'].setValue(true);
      }
    }
  }

  onSubmitSingleDay(): void {
    if(this.singleDayForm.valid){

      //there's two more if tests here because the selected day is not in the form, testing if the open for reservation date for
      //example is before the selected day, needs to be done here.

      //check if selected day is not a past day
      if(compareDates(dateToIDate(this.clickedDate), dateToIDate(new Date())) > 0){

        //check if open for reservation date is before the selected day
        let openFRD: IDate;
        if(this.singleDayForm.controls['open'].value){
          let openForReservationDateString = this.singleDayForm.controls['openForReservationDate'].value.split('-');
          openFRD = {"year": +openForReservationDateString[0], "month": +openForReservationDateString[1], "day": +openForReservationDateString[2], "hrs": null, "min": null, "sec": null};
        }

        if(!this.singleDayForm.controls['open'].value || compareDates(dateToIDate(this.clickedDate), openFRD) > 0) {
          let date = dateToIDate(this.clickedDate);
          const open = this.singleDayForm.controls['open'].value;
          let day: IDay;
          if (open) {
            let time = this.singleDayForm.controls['openingHour'].value.split(':');
            let hrs = time[0];
            let min = time[1];
            let openingH: Time = {"hours": hrs, "minutes": min};
            time = this.singleDayForm.controls['closingHour'].value.split(':');
            hrs = time[0];
            min = time[1];
            let closingH: Time = {"hours": hrs, "minutes": min};

            let openForReservationDate = this.singleDayForm.controls['openForReservationDate'].value.split('-');
            let openForReservationTime = this.singleDayForm.controls['openForReservationTime'].value.split(':');
            let openForReservation: IDate = {
              "year": +openForReservationDate[0],
              "month": +openForReservationDate[1],
              "day": +openForReservationDate[2],
              "hrs": +openForReservationTime[0],
              "min": +openForReservationTime[1],
              "sec": 0
            };
            day = {
              "date": date,
              "openingHour": openingH,
              "closingHour": closingH,
              "openForReservationDate": openForReservation
            };
          }
          if (open) {
            this.locationService.addCalendarDays(this.location.name, {"days": [day]}).subscribe(value => {
              this.locationService.getLocation(this.location.name).subscribe(value => {
                this.showSuccessfullySubmitted = true;
                this.showAlert();
                this.location.calendar = value.calendar;
                this.calendar = this.location.calendar;
                this.locations.find(loc => loc.name === this.location.name).calendar = this.calendar;
                this.refresh.next();
                this.subscribeToOpen();
              });
            });
          } else {
            this.locationService.deleteCalendarDays(this.location.name, dateToString(date), dateToString(date)).subscribe(value => {
              this.locationService.getLocation(this.location.name).subscribe(value => {
                this.showSuccessfullySubmitted = true;
                this.showAlert();
                this.location.calendar = value.calendar;
                this.calendar = this.location.calendar;
                this.locations.find(loc => loc.name === this.location.name).calendar = this.calendar;
                this.refresh.next();
                this.subscribeToOpen();
              });
            });
          }
        } else {
          //show error message if open for reservation date is after selected day
          this.showErrorOpenForReservationDate = true;
          this.showAlert();
        }
      } else {
        //show error message if selected day is a past day
        this.showErrorPastDaySubmit = true;
        this.showAlert();
      }
    }
  }

  cancel(event) {
    event.preventDefault();
    this.setDayInfo(this.clickedDate);
  }

  //Display opening hours in calendar
  getOpeningHoursToString(date: Date): string {
    let day: IDay = getDay(this.calendar, dateToIDate(date));
    return timeToString(day.openingHour) + " - " + timeToString(day.closingHour);
  }

  //Display reservation date in calendar
  getReservationHoursToString(date: Date): string {
    let day = getDay(this.calendar, dateToIDate(date));
    if(day.openForReservationDate !== null){
      return day.openForReservationDate.day + "/" + day.openForReservationDate.month + "/" + day.openForReservationDate.year + "  -  " +
        timeToString({"hours": day.openForReservationDate.hrs, "minutes": day.openForReservationDate.min});
    }
    return "";
  }

  onSubmitMultipleDays(): void {
    if(this.multipleDaysForm.valid){

      //check if selected day is not a past day
      let tempDate = this.multipleDaysForm.controls['startDate'].value.split('-');
      let start: IDate = {
        "year": +tempDate[0],
        "month": +tempDate[1],
        "day": +tempDate[2],
        "hrs": 0,
        "min": 0,
        "sec": 0
      };
      if(compareDates(start, dateToIDate(new Date())) > 0) {
        tempDate = this.multipleDaysForm.controls['endDate'].value.split('-');
        let end: IDate = {"year": tempDate[0], "month": tempDate[1], "day": tempDate[2], "hrs": 0, "min": 0, "sec": 0};

        if (this.multipleDaysForm.controls['open'].value) {
          let time = this.multipleDaysForm.controls['openingHour'].value.split(':');
          let hrs = time[0];
          let min = time[1];
          let openingH: Time = {"hours": hrs, "minutes": min};
          time = this.multipleDaysForm.controls['closingHour'].value.split(':');
          hrs = time[0];
          min = time[1];
          let closingH: Time = {"hours": hrs, "minutes": min};

          let openForReservationDate = this.multipleDaysForm.controls['openForReservationDate'].value.split('-');
          let openForReservationTime = this.multipleDaysForm.controls['openForReservationTime'].value.split(':');
          let openForReservation: IDate = {
            "year": +openForReservationDate[0], "month": +openForReservationDate[1], "day": +openForReservationDate[2],
            "hrs": +openForReservationTime[0], "min": +openForReservationTime[1], "sec": 0
          };

          let stdate: Date = new Date(start.year, start.month - 1, start.day);
          let edate: Date = new Date(end.year, end.month - 1, end.day);

          let toAdd: IDay[] = [];

          for (stdate; stdate <= edate; stdate.setDate(stdate.getDate() + 1)) {
            let date: IDate = dateToIDate(stdate);
            let day: IDay = {
              "date": date,
              "openingHour": openingH,
              "closingHour": closingH,
              "openForReservationDate": openForReservation
            };
            toAdd.push(day);
          }
          this.locationService.addCalendarDays(this.location.name, {"days": toAdd}).subscribe(v => {
            this.locationService.getLocation(this.location.name).subscribe(value => {
              this.showSuccessfullySubmittedMultipleDays = true;
              this.showAlert();
              this.location.calendar = value.calendar;
              this.calendar = this.location.calendar;
              this.locations.find(loc => loc.name === this.location.name).calendar = this.calendar;
              this.refresh.next();
              this.subscribeToOpen();
            });
          });
        } else {
          this.locationService.deleteCalendarDays(this.location.name, dateToString(start), dateToString(end)).subscribe(v => {
            this.locationService.getLocation(this.location.name).subscribe(value => {
              this.showSuccessfullySubmittedMultipleDays = true;
              this.showAlert();
              this.location.calendar = value.calendar;
              this.calendar = this.location.calendar;
              this.locations.find(loc => loc.name === this.location.name).calendar = this.calendar;
              this.refresh.next();
              this.subscribeToOpen();
            });
          });
        }
      } else {
        this.showErrorPastDaySubmitMultipleDays = true;
        this.showAlert();
      }
    }
  }

  cancelMultipleDays(event): void {
    event.preventDefault();
    this.multipleDaysForm.reset();
  }

  //shows the error for a few seconds and then makes it disappear
  showAlert(): void {
    const that = this;
    setTimeout(() => {
      that.showErrorPastDaySubmit = false;
      that.showSuccessfullySubmitted = false;
      that.showSuccessfullySubmittedMultipleDays = false;
      that.showErrorOpenForReservationDate = false;
      that.showErrorPastDaySubmitMultipleDays = false;
    }, this.messageSecondsVisible * 1000);
  }

  /*
  * Enabling and disabling the components like this might seem a lot of duplicate code and you might think it could also be done with a variable
  * and using "disabled" in the html, but this wont work since it's a reactive form.
  * */
  subscribeToOpen(): void {
    this.singleDayForm.controls['open'].valueChanges.subscribe(v => {
      if(v){
        this.singleDayForm.controls['openingHour'].enable();
        this.singleDayForm.controls['closingHour'].enable();
        this.singleDayForm.controls['openForReservationDate'].enable();
        this.singleDayForm.controls['openForReservationTime'].enable();
        this.singleDayForm.controls['openingHour'].setValue(timeToString(this.openingHour));
        this.singleDayForm.controls['closingHour'].setValue(timeToString(this.closingHour));
        this.singleDayForm.controls['openForReservationDate'].setValue(this.iDateToString(this.openForReservationDate));
        this.singleDayForm.controls['openForReservationTime'].setValue(timeToString(this.openForReservationTime));
      } else {
        this.singleDayForm.controls['openingHour'].disable();
        this.singleDayForm.controls['closingHour'].disable();
        this.singleDayForm.controls['openForReservationDate'].disable();
        this.singleDayForm.controls['openForReservationTime'].disable();
        this.singleDayForm.controls['openingHour'].setValue(null);
        this.singleDayForm.controls['closingHour'].setValue(null);
        this.singleDayForm.controls['openForReservationDate'].setValue(null);
        this.singleDayForm.controls['openForReservationTime'].setValue(null);
      }
      this.singleDayForm.controls['openingHour'].markAsUntouched();
      this.singleDayForm.controls['closingHour'].markAsUntouched();
      this.singleDayForm.controls['open'].markAsUntouched();
      this.singleDayForm.controls['openForReservationDate'].markAsUntouched();
      this.singleDayForm.controls['openForReservationTime'].markAsUntouched();
    });
    this.multipleDaysForm.controls['open'].valueChanges.subscribe(v => {
      this.multipleDaysForm.controls['startDate'].markAsUntouched();
      this.multipleDaysForm.controls['endDate'].markAsUntouched();
      this.multipleDaysForm.controls['openingHour'].markAsUntouched();
      this.multipleDaysForm.controls['closingHour'].markAsUntouched();
      this.multipleDaysForm.controls['openForReservationDate'].markAsUntouched();
      this.multipleDaysForm.controls['openForReservationTime'].markAsUntouched();
      this.multipleDaysForm.controls['open'].markAsUntouched();
      if(v){
        this.multipleDaysForm.controls['openingHour'].enable();
        this.multipleDaysForm.controls['closingHour'].enable();
        this.multipleDaysForm.controls['openForReservationDate'].enable();
        this.multipleDaysForm.controls['openForReservationTime'].enable();
      } else {
        this.multipleDaysForm.controls['openingHour'].disable();
        this.multipleDaysForm.controls['closingHour'].disable();
        this.multipleDaysForm.controls['openForReservationDate'].disable();
        this.multipleDaysForm.controls['openForReservationTime'].disable();
        this.multipleDaysForm.controls['openingHour'].setValue(null);
        this.multipleDaysForm.controls['closingHour'].setValue(null);
        this.multipleDaysForm.controls['openForReservationDate'].setValue(null);
        this.multipleDaysForm.controls['openForReservationTime'].setValue(null);
      }
    });
  }

  isValidOpenForReservationDate() {
    return /\d\d\d\d-\d\d-\d\d/.test(this.multipleDaysForm.controls['openForReservationDate'].value)
  }

  isValidStartDate(){
    return /\d\d\d\d-\d\d-\d\d/.test(this.multipleDaysForm.controls['startDate'].value);
  }


  //format the IDate to fill in the form, different from normal toString for IDate!!!!
  iDateToString(date: IDate): string {
    let res = "";
    if(date === null || date === undefined){
      return res;
    }
    res += date.year;
    res += "-";
    if(date.month < 10){
      res += "0";
    }
    res += +date.month;
    res += "-";
    if(date.day < 10){
      res += "0";
    }
    res += +date.day;
    return res;
  }

  // necessary to avoid selectpicker disappearing after switching pages
  ngAfterContentChecked(): void {
    $('#location').selectpicker('refresh');
  }
}
