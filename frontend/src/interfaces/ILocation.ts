import {ILocker} from './ILocker';
import {IDay} from "./IDay";
import {IDate} from "./IDate";
import {compareDates, compareDatesWithTime, dateToIDate} from "./CustomDate";

export interface ILocation {
  name: string;
  address: string;
  numberOfSeats: number;
  numberOfLockers: number;
  mapsFrame: string;
  descriptions: {};
  imageUrl: string;
  startPeriodLockers: IDate;
  endPeriodLockers: IDate;
}

export function getDay(days: IDay[], date: IDate): IDay {
  let d: IDay = {"date": date, "openingHour": null, "closingHour":null,
    "openForReservationDate": null};
  let index = days.findIndex(day => compareDates(d.date, day.date) === 0);
  if(index < 0){
    return d;
  }
  return days[index];
}

export function isOpen(days: IDay[], date: IDate): boolean {
  return getDay(days, date).openingHour !== null;
}

export function openForReservation(days: IDay[], date: IDate): boolean {
  let day: IDay = getDay(days, date);
  if(day.openForReservationDate === null || day.openForReservationDate === undefined){
    return false;
  }
  return compareDatesWithTime(day.openForReservationDate, dateToIDate(new Date())) <= 0;
}
