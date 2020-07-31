import { Directive } from '@angular/core';
import {AbstractControl, FormGroup, ValidationErrors, ValidatorFn} from "@angular/forms";
import {compareDates, CustomDate, getToday} from '../../interfaces/CustomDate';
import {IDate} from "../../interfaces/IDate";

@Directive({
  selector: '[appValidators]'
})
export class ValidatorsDirective {

  constructor() { }

}
//checks whether the password and confirmation passwords are the same
//used on registration page and edit profile page
export const samePasswordsValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  const pwd = control.get('pwd');
  const confPwd = control.get('confPwd');

  return pwd && confPwd && pwd.value !== confPwd.value ? { 'passwordsDifferent': true } : null;
};

export const singleDayFormValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  let valid = true;

  //unmark all formcontrols as dirty (set to pristine)
  control.get('openingHour').markAsPristine();
  control.get('closingHour').markAsPristine();
  control.get('openForReservationDate').markAsPristine();
  control.get('openForReservationTime').markAsPristine();
  control.get('open').markAsPristine();

  /*
  If checkbox open is set to true, check all filled in fields
   */

  if(control.get('open').value === true){
    const openingHour = control.get('openingHour').value;
    const closingHour = control.get('closingHour').value;
    const openForReservationDate = control.get('openForReservationDate').value;
    const openForReservationTime = control.get('openForReservationTime').value;

    /*
    Check if all fields are filled in correctly, if not, mark as dirty
     */

    if(!/\d\d:\d\d/.test(openingHour)){
      valid = false;
      control.get('openingHour').markAsDirty();
    }
    if(!/\d\d:\d\d/.test(closingHour)){
      valid = false;
      control.get('closingHour').markAsDirty();
    }
    if(!/\d\d\d\d-\d\d-\d\d/.test(openForReservationDate)){
      valid = false;
      control.get('openForReservationDate').markAsDirty();
    }
    if(!/\d\d:\d\d/.test(openForReservationTime)){
      valid = false;
      control.get('openForReservationTime').markAsDirty();
    }

    /*
    Check if opening hour comes before closing hour, if not, mark 'open' as dirty, this makes it easy to check in the html form
    because if we would mark openingHour or closingHour as dirty, we wouldn't know if the formcontrol was marked as dirty because
    it wasn't filled in correctly, or if the opening hour didn't come before closing hour.

    By marking 'open' as dirty, (open can not be wrongly filled in), we can seperate the two errors
     */

    if(valid || (/\d\d:\d\d/.test(openingHour) && /\d\d:\d\d/.test(closingHour))){
      // closing hour should be later than opening hour
      let time = openingHour.split(':');
      const o: number = (+time[0])*60 + (+time[1]);
      time = closingHour.split(':');
      const c = (+time[0])*60 + (+time[1]);
      if(o >= c){
        valid = false;
        control.get('open').markAsDirty();
      }
    }
  }
  return !valid ? { 'times': true } : null;
};

export const multipleDaysFormValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  let valid = true;

  //unmark all as dirty (set to pristine)
  control.get('startDate').markAsPristine();
  control.get('endDate').markAsPristine();
  control.get('openingHour').markAsPristine();
  control.get('closingHour').markAsPristine();
  control.get('openForReservationDate').markAsPristine();
  control.get('openForReservationTime').markAsPristine();
  control.get('open').markAsPristine();

  /*
  First check if startDate and endDate are filled in correctly, if not, mark as dirty
   */

  if(!/\d\d\d\d-\d\d-\d\d/.test(control.get('startDate').value)){
    valid = false;
    control.get('startDate').markAsDirty();

  }
  if(!/\d\d\d\d-\d\d-\d\d/.test(control.get('endDate').value)){
    valid = false;
    control.get('endDate').markAsDirty();
  }

  if(valid){

    // startdate should be earlier than enddate
    const startDate = control.get('startDate').value;
    const endDate = control.get('endDate').value;
    let date = startDate.split('-');
    const s = +date[0]*404 + (+date[1])*31 + (+date[2]);
    date = endDate.split('-');
    const e = +date[0]*404 + (+date[1])*31 + (+date[2]);
    if(s > e){
      valid = false;

      //if startdate comes after enddate, startDate gets marked dirty, this makes it complicated in the html form, because
      //there are two reasons why startDate can be marked as dirty, we need to check in the html which one we're talking about
      control.get('startDate').markAsDirty();
    }
  }

  if(control.get('open').value === true){

    /*
    If the checkbox open is set to true, check all other fields
     */

    const openingH = control.get('openingHour').value;
    const closingH = control.get('closingHour').value;
    const openForReservationDate = control.get('openForReservationDate').value;
    const openForReservationTime = control.get('openForReservationTime').value;

    /*
    Check if fields are filled in correctly, if not, mark as dirty
     */

    if(!/\d\d:\d\d/.test(openingH)){
      valid = false;
      control.get('openingHour').markAsDirty();
    }
    if(!/\d\d:\d\d/.test(closingH)){
      valid = false;
      control.get('closingHour').markAsDirty();
    }
    if(!/\d\d\d\d-\d\d-\d\d/.test(openForReservationDate)){
      valid = false;
      control.get('openForReservationDate').markAsDirty();
    }
    if(!/\d\d:\d\d/.test(openForReservationTime)){
      valid = false;
      control.get('openForReservationTime').markAsDirty();
    }

    /*
    Check if opening hour comes before closing hour, we mark the checkbox 'open' as dirty if not
     */

    if(valid || (/\d\d:\d\d/.test(openingH) && /\d\d:\d\d/.test(closingH))){
      let time = openingH.split(':');
      const o: number = (+time[0])*60 + (+time[1]);
      time = closingH.split(':');
      const c = (+time[0])*60 + (+time[1]);
      if(o >= c){
        valid = false;
        control.get('open').markAsDirty();
      }
    }

    /*
    check if open for reservation date comes before start date, if not, mark openForReservationDate as dirty
     */

    if(valid || /\d\d\d\d-\d\d-\d\d/.test(control.get('startDate').value) && /\d\d\d\d-\d\d-\d\d/.test(openForReservationDate)){
      let dateString = control.get('startDate').value.split('-');
      let startDate: IDate = {"year": +dateString[0],
                              "month": +dateString[1],
                              "day": +dateString[2], "hrs": null, "min": null, "sec": null};
      dateString = openForReservationDate.split('-');
      let openForResDate: IDate = {"year": +dateString[0],
                                   "month": +dateString[1],
                                   "day": +dateString[2], "hrs": null, "min": null, "sec": null};
      if(compareDates(startDate, openForResDate) <= 0){
        valid = false;
        control.get('openForReservationDate').markAsDirty();
      }
    }
  }
  return !valid ? { 'times': true } : null;
};

export const lockersFormValidator: ValidatorFn = (control: FormGroup): ValidationErrors | null => {
  let valid = true;
  const startDate = control.get('nextStartDate').value;
  const endDate = control.get('nextEndDate').value;
  if(!/\d\d\d\d-\d\d-\d\d/.test(startDate) || !/\d\d\d\d-\d\d-\d\d/.test(endDate)){
    valid = false;
  }
  if(valid){
    let date = startDate.split('-');
    const s = +date[0]*404 + (+date[1])*31 + (+date[2]);
    date = endDate.split('-');
    const e = +date[0]*404 + (+date[1])*31 + (+date[2]);
    if(s > e){
      valid = false;
    }

    // cant make a new period with a starting date earlier then today;
    let d = getToday();
    const today = d.year*404 + d.month*31 + d.day;
    if(today > s || today > e){
       valid =  false;
    }
  }
  return !valid ? { 'times': true } : null;
};

