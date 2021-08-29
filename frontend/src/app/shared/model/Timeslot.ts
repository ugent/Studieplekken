import { Time } from '@angular/common';
import { CalendarEvent } from 'angular-calendar';
import * as moment from 'moment';
import { Moment } from 'moment';
import { CalendarPeriod } from './CalendarPeriod';
import { LocationReservation } from './LocationReservation';

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
  timeslotGroup: number;
  repeatable: boolean;

  constructor(
    timeslotSequenceNumber: number,
    timeslotDate: Moment,
    amountOfReservations: number,
    seatCount: number,
    reservable: boolean,
    reservableFrom: Moment,
    locationId: number,
    openingHour: Moment,
    closingHour: Moment,
    timeslotGroup: number,
    repeatable: boolean
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
    this.timeslotGroup = timeslotGroup;
    this.repeatable = repeatable;
  }

  static fromJSON(json: Record<string, any>): Timeslot {
    if (!json) {
      return null;
    }
    return new Timeslot(
      json.timeslotSeqnr,
      moment(json.timeslotDate),
      json.amountOfReservations,
      json.seatCount,
      json.reservable,
      moment(json.reservableFrom),
      json.locationId,
      moment(json.openingHour, "HH:mm:ss"),
      moment(json.closingHour, "HH:mm:ss"),
      json.timeslotGroup,
      json.repeatable
    );
  }

  toJSON(): Record<string, unknown> {
    return {
      timeslotSeqnr: this.timeslotSequenceNumber,
      timeslotDate: this.timeslotDate.format('YYYY-MM-DD'),
      seatCount: this.seatCount,
      openingHour: this.openingHour.format("HH:mm"),
      closingHour: this.closingHour.format("HH:mm"),
      reservable: this.reservable,
      reservableFrom: this.reservableFrom ? this.reservableFrom.toISOString():null,
      locationId: this.locationId,
      timeslotGroup: this.timeslotGroup,
      repeatable: this.repeatable
    };
  }

  isValid() {
    if(!this.timeslotDate || !this.openingHour || !this.closingHour) {
      return false;
    }

    if(this.reservable && !this.reservableFrom) {
      return false;
    }

    return true;
  }

  areReservationsLocked(): boolean {
    return !this.reservableFrom || this.reservableFrom.isAfter(moment());
  }

  getStartMoment() {
    return moment(
      this.timeslotDate.format('DD-MM-YYYY') +
      ' ' +
      this.openingHour.format('HH:mm'),
      'DD-MM-YYYY HH:mm'
    )
  }

  getEndMoment() {
    return moment(
      this.timeslotDate.format('DD-MM-YYYY') +
      ' ' +
      this.closingHour.format('HH:mm'),
      'DD-MM-YYYY HH:mm'
    )
  }

  isCurrent() {
    return this.getStartMoment().isBefore(moment()) && this.getEndMoment().isAfter(moment())
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