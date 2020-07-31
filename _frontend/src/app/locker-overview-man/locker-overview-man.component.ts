import {Component, OnInit} from '@angular/core';
import {ILocker} from '../../interfaces/ILocker';
import {ILocation} from '../../interfaces/ILocation';
import {Subject} from 'rxjs';
import {transition, trigger, useAnimation} from '@angular/animations';
import {rowsAnimation} from '../animations';
import {ILockerReservation} from '../../interfaces/ILockerReservation';
import {LockerReservationService} from '../../services/locker-reservation.service';
import {LocationService} from '../../services/location.service';
import {FormControl, FormGroup} from '@angular/forms';
import {lockersFormValidator} from '../shared/validators.directive';
import {IDate} from '../../interfaces/IDate';
import {IUser} from '../../interfaces/IUser';
import {AuthenticationService} from '../../services/authentication.service';
import {TranslateService} from '@ngx-translate/core';

declare var $: any;

@Component({
  selector: 'app-locker-overview-man',
  templateUrl: './locker-overview-man.component.html',
  styleUrls: ['./locker-overview-man.component.css'],
  animations: [trigger('rowsAnimation', [
    transition('void => *', [
      useAnimation(rowsAnimation)
    ])
  ])]
})
export class LockerOverviewManComponent implements OnInit {

  refresh: Subject<any> = new Subject();

  linesOnPage = 20;
  lower: number;
  upper: number;
  numbers: number[];

  location: ILocation;
  locations: ILocation[] = [];
  results: ILocker[] = [];

  lockerReservations: ILockerReservation[] = [];

  lockersForm = new FormGroup({
    currentStartDate: new FormControl(''),
    currentEndDate: new FormControl(''),
    nextStartDate: new FormControl(''),
    nextEndDate: new FormControl('')
  }, {validators: lockersFormValidator});

  constructor(private lockerReservationService: LockerReservationService, private locationService: LocationService,
              private authenticationService: AuthenticationService) {

    // an admin has access to all locations
    if (this.authenticationService.currentUserHasRole(authenticationService.roles.admin)) {
      this.locationService.getAllLocationsWithoutLockersAndCalendar().subscribe(value => {
        this.locations = value;
        this.initLocation(this.locations[0]);
      });
    }
    // an employee should only have access to locations for which he or she is assigned
    else {
      this.locationService.getAllAuthenticatedLocations(this.authenticationService.getCurrentUser().mail).subscribe(locations => {
        for (let l of locations) {
          this.locationService.getLocationWithoutLockersAndCalendar(l).subscribe(value => {
            this.locations.push(value);
            if (this.locations.length == 1) {
              this.initLocation(value);
            }
          });
        }
      });
    }
  }

  /* om te veranderen naar een ugent style datepicker, gaf nog probelemen met het datum formaat
  initDatepickers(){
    const that = this;
    let locale = this.translate.currentLang;
    $(function () {
      $('#datetimepicker1').datetimepicker({
        format: 'DD-MM-YYYY',
        locale: locale
      });
      $('#datetimepicker2').datetimepicker({
        useCurrent: false,
        format: 'DD-MM-YYYY',
        locale: locale
      });
      // change second date so it should always be >= first date
      $("#datetimepicker1").on("dp.change", function (e) {
        let vals = $("#datetimepicker1").find("input").val();
        that.lockersForm.controls['nextStartDate'].setValue(vals);
        $('#datetimepicker2').data("DateTimePicker").minDate(e.date);
      });

      // change first date so it should always be <= second date
      $("#datetimepicker2").on("dp.change", function (e) {
        let vals = $("#datetimepicker2").find("input").val();
        that.lockersForm.controls['nextEndDate'].setValue(vals);
        $('#datetimepicker1').data("DateTimePicker").maxDate(e.date);
      });
    });
  }

   */
  initLocation(location: ILocation) {
    this.location = location;
    this.locationService.getLocationWithoutCalendar(this.location.name).subscribe(value => {
      this.results = value.lockers;
      this.numbers = Array(Math.ceil(this.results.length / this.linesOnPage)).fill(1).map((x, i) => i + 1);
      this.refresh.next();
    });
    this.lockersForm.controls['currentStartDate'].setValue(this.iDateToForm(this.location.startPeriodLockers));
    this.lockersForm.controls['currentEndDate'].setValue(this.iDateToForm(this.location.endPeriodLockers));
    this.lockerReservationService.getAllOngoingLockerReservationsOfLocation(this.location).subscribe(value => {
      this.lockerReservations = value;
      this.refresh.next();
    });
   // this.initDatepickers();
  };

  ngOnInit(): void {
    this.lower = 0;
    this.upper = this.linesOnPage;
  }

  newPage(i) {
    this.lower = (i - 1) * this.linesOnPage;
    this.upper = i * this.linesOnPage;
  }

  floor(i): number {
    return Math.floor(i);
  }

  keyPickedUp(locker: ILocker): void {
    let lockerReservation = this.lockerReservations.find(res => res.locker.number === locker.number);
    lockerReservation.keyPickedUp = true;
    this.lockerReservationService.updateLockerReservation(lockerReservation).subscribe(value => {
      this.refresh.next();
    });
  }

  keyBroughtBack(locker: ILocker): void {
    let index = this.lockerReservations.findIndex(res => res.locker.number === locker.number);
    let lockerReservation = this.lockerReservations[index];
    lockerReservation.keyBroughtBack = true;
    this.lockerReservationService.updateLockerReservation(lockerReservation).subscribe(value => {
      this.lockerReservations.splice(index, 1);
      this.refresh.next();
    });
  }

  changeLocation(): void {
    this.locationService.getLocationWithoutCalendar(this.location.name).subscribe(value => {
      this.results = value.lockers;
      this.numbers = Array(Math.ceil(this.results.length / this.linesOnPage)).fill(1).map((x, i) => i + 1);
      this.refresh.next();
    });
    this.results = this.location.lockers;
    this.lockerReservationService.getAllOngoingLockerReservationsOfLocation(this.location).subscribe(value => {
      this.lockerReservations = value;
      this.refresh.next();
    });
    this.lockersForm.controls['currentStartDate'].setValue(this.iDateToForm(this.location.startPeriodLockers));
    this.lockersForm.controls['currentEndDate'].setValue(this.iDateToForm(this.location.endPeriodLockers));
    this.lower = 0;
    this.upper = this.linesOnPage;
    //this.initDatepickers();
  }

  submit(): void {
    if (this.lockersForm.valid) {
      let tempDate = this.lockersForm.controls['nextStartDate'].value.split('-');
      let startDate: IDate = {'year': +tempDate[0], 'month': +tempDate[1], 'day': +tempDate[2], 'hrs': 0, 'min': 0, 'sec': 0};
      tempDate = this.lockersForm.controls['nextEndDate'].value.split('-');
      let endDate: IDate = {'year': +tempDate[0], 'month': +tempDate[1], 'day': +tempDate[2], 'hrs': 0, 'min': 0, 'sec': 0};

      this.location.startPeriodLockers = startDate;
      this.location.endPeriodLockers = endDate;
      this.locationService.saveLocation(this.location.name, this.location).subscribe(value => {
        this.lockersForm.controls['currentStartDate'].setValue(this.iDateToForm(this.location.startPeriodLockers));
        this.lockersForm.controls['currentEndDate'].setValue(this.iDateToForm(this.location.endPeriodLockers));
        this.lockersForm.controls['nextStartDate'].setValue('');
        this.lockersForm.controls['nextEndDate'].setValue('');
      });
    }
  }

  getStatus(locker: ILocker): string {
    let index = this.lockerReservations.findIndex(res => res.locker.number === locker.number);
    if (index < 0) {
      return 'available';
    }
    return 'reserved';
  }

  isOccupied(locker: ILocker): boolean {
    return this.lockerReservations.findIndex(res => res.locker.number === locker.number) >= 0;
  }

  getOwner(locker: ILocker): string {
    let index = this.lockerReservations.findIndex(res => res.locker.number === locker.number);
    if (index < 0) {
      return '';
    }
    let user: IUser = this.lockerReservations[index].owner;
    return user.firstName + ' ' + user.lastName;
  }

  isKeyPickedUp(locker: ILocker): boolean {
    return this.lockerReservations.find(res => res.locker.number === locker.number).keyPickedUp;
  }

  iDateToForm(date: IDate): string {
    let res = '';
    if (date === null || date === undefined) {
      return res;
    }
    res += date.year;
    res += '-';
    if (date.month < 10) {
      res += '0';
    }
    res += +date.month;
    res += '-';
    if (date.day < 10) {
      res += '0';
    }
    res += +date.day;
    return res;
  }

  cancel(event) {
    event.preventDefault();
    this.lockersForm.controls['nextStartDate'].setValue('');
    this.lockersForm.controls['nextEndDate'].setValue('');
  }

  // necessary to avoid selectpicker disappearing after switching pages
  ngAfterContentChecked(): void {
    $('#location').selectpicker('refresh');
  }
}
