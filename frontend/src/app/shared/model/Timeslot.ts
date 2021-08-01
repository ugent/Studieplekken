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
      moment(json.closingHour, "HH:mm:ss")
    );
  }

  toJSON(): Record<string, unknown> {
    return {
      timeslotSeqnr: this.timeslotSequenceNumber,
      timeslotDate: this.timeslotDate.format('YYYY-MM-DD'),
      seatCount: this.seatCount,
    };
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


export const timeslotToCalendarEvent = (timeslot: Timeslot, currentLang: string, locationReservations: LocationReservation[] = []) =>
  !timeslot.reservable ? nonReservableToCalendarEvent(timeslot, currentLang) :
    timeslot.areReservationsLocked() ? notYetReservableTimeslotToCalendarEvent(timeslot, currentLang) :
      reservableTimeslotToCalendarEvent(timeslot, currentLang, locationReservations);


const nonReservableToCalendarEvent: (timeslot: Timeslot, currentLang: string) => CalendarEvent<{ timeslot: Timeslot }> =
  (timeslot, currentLang) =>
  ({
    title: currentLang == "nl" ? "Geen reservatie nodig" : "No reservation needed",
    start: timeslot.getStartMoment().toDate(),
    end: timeslot.getEndMoment().toDate(),
    meta: { timeslot },
    color: { primary: 'black', secondary: '#BEBEBE' },
    cssClass: 'calendar-event-NR',
  })

const notYetReservableTimeslotToCalendarEvent: (timeslot: Timeslot, currentLang: string) => CalendarEvent<{ timeslot: Timeslot }> =
  (timeslot, currentLang) =>
  ({
    title: currentLang == "nl" ? "Nog niet reserveerbaar" : "Not yet reservable",
    start: timeslot.getStartMoment().toDate(),
    end: timeslot.getEndMoment().toDate(),
    meta: { timeslot },
    color: { primary: 'black', secondary: '#BEBEBE' },
    cssClass: 'calendar-event-NR',
  });

const reservableTimeslotToCalendarEvent: (timeslot: Timeslot, currentLang: string, reservedTimeslots: LocationReservation[]) => CalendarEvent<{ timeslot: Timeslot }> =
  (timeslot, currentLang, reservedTimeslots) =>
  ({
    title: `${timeslot.amountOfReservations} / ${timeslot.seatCount}`,
    start: timeslot.getStartMoment().toDate(),
    end: timeslot.getEndMoment().toDate(),
    meta: { timeslot },
    color: includesTimeslot(
      reservedTimeslots.map((s) => s.timeslot),
      timeslot
    )
      ? { primary: '#00004d', secondary: '#133E7D' }
      : null,
    cssClass: includesTimeslot(
      reservedTimeslots.map((s) => s.timeslot),
      timeslot
    )
      ? 'calendar-event-reserved'
      : '',
  });