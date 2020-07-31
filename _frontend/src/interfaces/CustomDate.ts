import {IDate} from "./IDate";
import {Time} from "@angular/common";

export class CustomDate implements IDate{
  day: number;
  hrs: number;
  min: number;
  month: number;
  sec: number;
  year: number;


  constructor(){
    const d = new Date();
    this.hrs = d.getHours();
    this.min = d.getMinutes();
    this.sec = d.getSeconds();
    this.day = d.getUTCDate();
    this.month = d.getMonth()+1;
    this.year = d.getFullYear();
  }

  nextDay(): void {
    const numberOfDays = 32 - new Date(this.year, this.month, 32).getDate();
    if(this.day === numberOfDays){
      this.day = 1;
      if(this.month === 12) {
        this.month = 1;
        this.year += 1;
      }
      else {
        this.month += 1;
      }
    }
  }
}

export function getToday(): CustomDate {
  let date;
  date = new Date();
  let custom;
  custom = new CustomDate();
  custom.day = date.getUTCDate();
  custom.month = date.getMonth() + 1;
  custom.year = date.getFullYear();
  custom.hrs = 0;
  custom.min = 0;
  custom.sec = 0;
  return custom;
}

export function dateToIDate(date: Date): IDate {
  return {"year": date.getFullYear(),
    "month": date.getMonth()+1,
    "day": date.getDate(),
    "hrs": date.getHours(),
    "min": date.getMinutes(),
    "sec": date.getSeconds()};
}

export function format(length: number, value: number): string {
  if(value.toString().length<length){
    let res = '';
    for(let i=0; i<length-value.toString().length; i++){
      res += '0';
    }
    return res + value.toString();
  }
  return value.toString().substr(0, length);
}

export function dateToString(date: IDate): string {
  return format(4, date.year) + "-" + format(2, date.month) + "-"
    + format(2, date.day) + "T" + format(2, date.hrs)
    + ":" + format(2, date.min) + ":" + format(2, date.sec);
}

export function timeToString(time: Time): string {
  let res = "";
  if(time !== null && time !== undefined){
    if(+time.hours < 10){
      res += "0";
    }
    res += +time.hours + ":";
    if(+time.minutes < 10){
      res += "0";
    }
    res += +time.minutes;
  }
  return res;
}

export function isPast(date: IDate): boolean {
  let today: IDate = new CustomDate();
  return compareDates(today, date) > 0;
}

export function isDate(date: IDate): boolean {
  let today: IDate = new CustomDate();
  return today.year === date.year && today.month === date.month && today.day === date.day;
}

export function compareDates(date1: IDate, date2: IDate): number {
  return date1.year*404 + date1.month*31 + date1.day -
    (date2.year*404 + date2.month*31 + date2.day);
}

export function compareDatesWithTime(date1: IDate, date2: IDate): number {
  return date1.year*404*31*24*60*60 + date1.month*31*24*60*60 + date1.day*24*60*60 + date1.hrs*60*60 + date1.min*60 + date1.sec -
    (date2.year*404*31*24*60*60 + date2.month*31*24*60*60 + date2.day*24*60*60 + date2.hrs*60*60 + date2.min*60 + date2.sec);
}
