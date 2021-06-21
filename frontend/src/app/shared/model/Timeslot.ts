import * as moment from 'moment';
import { Moment } from 'moment';
import { CalendarPeriod } from './CalendarPeriod';

export class Timeslot {
  timeslotSequenceNumber: number;
  timeslotDate: Moment;
  amountOfReservations: number;
  seatCount: number;
  reservable: boolean;
  reservableFrom: Moment;
  locationId: number;
  openingHour: Moment;
  closingHour: Moment;

  constructor(
    timeslotSequenceNumber: number,
    timeslotDate: Moment,
    amountOfReservations: number,
    seatCount: number,
    reservable: boolean,
    reservableFrom: Moment,
    locationId: number,
    openingHour: Moment,
    closingHour: Moment
  ) {
    this.timeslotSequenceNumber = timeslotSequenceNumber;
    this.timeslotDate = timeslotDate;
    this.amountOfReservations = amountOfReservations;
    this.seatCount = seatCount;
    this.reservable = reservable;
    this.reservableFrom = reservableFrom;
    this.locationId = locationId;
    this.openingHour = openingHour;
    this.closingHour = closingHour;
  }

  static fromJSON(json: Timeslot): Timeslot {
    if (!json) {
      return null;
    }
    return new Timeslot(
      json.timeslotSequenceNumber,
      moment(json.timeslotDate),
      json.amountOfReservations,
      json.seatCount,
      json.reservable,
      moment(json.reservableFrom),
      json.locationId,
      moment(json.openingHour, "HH:mm:ss"),
      moment(json.closingHour, "HH:mm:ss")
    );
  }

  toJSON(): Record<string, unknown> {
    return {
      timeslotSequenceNumber: this.timeslotSequenceNumber,
      timeslotDate: this.timeslotDate.format('YYYY-MM-DD'),
      seatCount: this.seatCount,
    };
  }

  areReservationsLocked(): boolean {
    return !this.reservableFrom || this.reservableFrom.isAfter(moment());
  }
}


export const timeslotEquals: (t1: Timeslot, t2: Timeslot) => boolean = (
  t1,
  t2
) =>
  t1.timeslotSequenceNumber === t2.timeslotSequenceNumber

export const includesTimeslot: (l: Timeslot[], t: Timeslot) => boolean = (
  l,
  t
) => l.some((lt) => timeslotEquals(lt, t));