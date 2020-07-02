import {IDate} from "./IDate";
import {Time} from "@angular/common";

export interface IDay {
  date: IDate;
  openingHour: Time;
  closingHour: Time;
  openForReservationDate: IDate;
}
