import {Location} from './Location';
import {CalendarEvent} from 'angular-calendar';
import {includesTimeslot, Timeslot, timeslotEndHour, timeslotStartHour} from './Timeslot';
import { LocationReservation } from './LocationReservation';
import * as moment from 'moment';
import { Moment } from 'moment';
import {calendarEventTitleTemplate} from '../../app.constants';

export class CalendarPeriod {

  constructor(id: number, location: Location, startsAt: Moment, endsAt: Moment, openingTime: Moment, closingTime: Moment,
              reservable: boolean, reservableFrom: Moment, reservableTimeslotSize: number, timeslots: Timeslot[], lockedFrom: Moment) {
    this.id = id;
    this.location = location;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.openingTime = openingTime;
    this.closingTime = closingTime;
    this.reservableFrom = reservableFrom;
    this.reservable = reservable;
    this.reservableTimeslotSize = reservableTimeslotSize;
    this.timeslots = timeslots;
    this.lockedFrom = lockedFrom;
  }
  id: number;
  location: Location;
  startsAt: Moment;
  endsAt: Moment;
  openingTime: Moment;
  closingTime: Moment;
  reservable: boolean;
  reservableFrom: Moment;
  reservableTimeslotSize: number;
  timeslots: Timeslot[];
  lockedFrom: Moment;

  static fromJSON(json: any): CalendarPeriod {
    return new CalendarPeriod(
      json.id,
      json.location,
      moment(json.startsAt),
      moment(json.endsAt),
      moment(json.openingTime, 'HH:mm'),
      moment(json.closingTime, 'HH:mm'),
      json.reservable,
      moment(json.reservableFrom, 'YYYY-MM-DDTHH:mm:ss'),
      json.reservableTimeslotSize,
      json.timeslots.map(jsonT => Timeslot.fromJSON(jsonT)),
      moment(json.lockedFrom)
    );
  }

  isLocked(): boolean {
    return this.lockedFrom && this.lockedFrom.isBefore(moment());
  }

  areReservationsLocked(): boolean {
    return !this.reservableFrom || this.reservableFrom.isAfter(moment());
  }

  isValid(): boolean {
    return isCalendarPeriodValid(this);
  }

  toJSON(): object {
    return {
      id: this.id,
      location: this.location,
      startsAt: this.startsAt.format('YYYY-MM-DD'),
      endsAt: this.endsAt.format('YYYY-MM-DD'),
      openingTime: this.openingTime.format('HH:mm'),
      closingTime: this.closingTime.format('HH:mm'),
      reservableFrom: this.reservableFrom ? this.reservableFrom.format('YYYY-MM-DDTHH:mm:ss') : null,
      reservableTimeslotSize: this.reservableTimeslotSize,
      timeslots: this.timeslots,
      reservable: this.reservable,
      lockedFrom: this.lockedFrom
    };
  }
}

/**
 * Following checks are performed on the period:
 *
 * 1. Checking the formats of its members
 *   - format of startsAt and endsAt:             YYYY-MM-DD
 *   - format of openingTime and openingTime:     HH:MI
 *   - format of reservableFrom:                  YYYY-MM-DD HH-MI
 *
 * 2. endsAt may not be before startsAt
 *
 * 3. closingTime may not be before openingTime
 */
export function isCalendarPeriodValid(period: CalendarPeriod): boolean {
  // at least, these attributes may not be null (id may be null for adding a period)
  if (period === null || period.location === null || period.startsAt === null ||
      period.endsAt === null || period.openingTime === null || period.closingTime === null) {
    return false;
  }

  // if the period is set to be reservable, reservableTimeslotSize may not be 0, nor
  // may reservableFrom be null
  if (period.reservable && (period.reservableTimeslotSize <= 0 || period.reservableFrom === null)) {
    return false;
  }

  // of course, startsAt, endsAt must be valid and startsAt must be before endsAt
  if (!period.startsAt.isValid() || !period.endsAt.isValid() || period.startsAt.isAfter(period.endsAt)) {
    return false;
  }

  // if the period is reservable, the reservableFrom must be valid too
  if (period.reservable && !period.reservableFrom.isValid()) {
    return false;
  }

  // and finally, the opening time must be before the closing time
  return period.openingTime.isBefore(period.closingTime);
}



/**
 * Convert calendarPeriods to Calendar Events. This detects correctly whether the period is reservable or not (yet).
 */
export function mapCalendarPeriodsToCalendarEvents(periods: CalendarPeriod[],
                                                   currentLang: string,
                                                   reservedTimeslots: LocationReservation[] = []): CalendarEvent[]
{
  if (periods.length === 0) {
    return [];
  }
  return periods
          .map(period => period.reservable && !period.areReservationsLocked() ?
              mapReservableTimeslotsToCalendarEvents(period, reservedTimeslots) :
          period.reservable && period.areReservationsLocked() ?
              mapNotYetReservableTimeslotsToCalendarEvents(period, currentLang) :
              mapNotReservableCalendarPeriodToCalendarEvent(period, currentLang))
          .reduce((a, b) => [...a, ...b]);
}

/**
 * Convert a calendar period to calendar events but as a block instead of dividing each day into timeslots.
 */
function mapNotReservableCalendarPeriodToCalendarEvent(period: CalendarPeriod, currentLang: string): CalendarEvent[] {
  const calendarEvents: CalendarEvent[] = [];

  const dateWithOpeningTime = new Date(period.startsAt.format('YYYY-MM-DD') + 'T' + period.openingTime.format('HH:mm'));
  const dateWithClosingTime = new Date(period.startsAt.format('YYYY-MM-DD') + 'T' + period.closingTime.format('HH:mm'));
  const lastDayWithOpeningTime = (new Date(period.endsAt.format('YYYY-MM-DD') + 'T' + period.openingTime.format('HH:mm')));

  let title: string;
  if (currentLang === 'nl') {
    title = calendarEventTitleTemplate.notReservableNL;
  } else {
    title = calendarEventTitleTemplate.notReservableEN;
  }

  while (dateWithOpeningTime <= lastDayWithOpeningTime) {
    calendarEvents.push({
      title,
      start: new Date(dateWithOpeningTime),
      end: new Date(dateWithClosingTime),
      meta: {calendarPeriod: period},
      color: {primary: 'black', secondary: '#BEBEBE'},
      cssClass: 'calendar-event-NR',
    });

    dateWithOpeningTime.setDate(dateWithOpeningTime.getDate() + 1);
    dateWithClosingTime.setDate(dateWithClosingTime.getDate() + 1);
  }


  return calendarEvents;
}

/**
 * Convert a CalendarPeriod which is not yet reservable (reservableFrom is in the future), to CalendarEvents.
 * Every timeslot of the CalendarPeriod will be represented by a CalendarEvent but will be greyed out
 * and have a title that notifies the user when the timeslot will be reservable.
 */
function mapNotYetReservableTimeslotsToCalendarEvents(period: CalendarPeriod, currentLang: string): CalendarEvent[] {
  const calendarEvents: CalendarEvent[] = [];

  for (const timeslot of period.timeslots) {
    const beginDT = new Date(timeslot.timeslotDate.format('YYYY-MM-DD') + 'T' + timeslotStartHour(period, timeslot).format('HH:mm'));
    const endDT = new Date(timeslot.timeslotDate.format('YYYY-MM-DD') + 'T' + timeslotEndHour(period, timeslot.timeslotSeqnr));

    let title: string;
    if (currentLang === 'nl') {
      title = calendarEventTitleTemplate.reservableFromNL.replace('{datetime}', period.reservableFrom.format('DD/MM/YYYY HH:mm'));
    } else {
      title = calendarEventTitleTemplate.reservableFromEN.replace('{datetime}', period.reservableFrom.format('DD/MM/YYYY HH:mm'));
    }

    calendarEvents.push({
      title,
      start: beginDT,
      end: endDT,
      meta: {calendarPeriod: period, timeslot},
      color: {primary: 'black', secondary: '#BEBEBE'},
      cssClass: 'calendar-event-NR',
    });

  }

  return calendarEvents;
}


/**
 * For each Timeslot that is attached to a CalendarPeriod provided in 'periods',
 * this method will create a CalendarEvent
 *
 * The starting and ending time for all CalendarEvents created from a timeslot, will
 * be the beginning and ending of the timeslot, calculated from the sequence number and the
 * timeslotdate.
 */
function mapReservableTimeslotsToCalendarEvents(period: CalendarPeriod, reservedTimeslots: LocationReservation[] = []): CalendarEvent[] {
  const calendarEvents: CalendarEvent[] = [];

  for (const timeslot of period.timeslots) {
      const beginDT = new Date(timeslot.timeslotDate.format('YYYY-MM-DD') + 'T' + timeslotStartHour(period, timeslot).format('HH:mm'));
      const endDT = new Date(timeslot.timeslotDate.format('YYYY-MM-DD') + 'T' + timeslotEndHour(period, timeslot).format('HH:mm'));

      calendarEvents.push({
        title: `${timeslot.amountOfReservations} / ${period.location.numberOfSeats}`,
        start: beginDT,
        end: endDT,
        meta: {calendarPeriod: period, timeslot},
        color: includesTimeslot(reservedTimeslots.map(s => s.timeslot), timeslot) ?
                                                         {primary: '#00004d', secondary: '#133E7D'} : null,
        cssClass: includesTimeslot(reservedTimeslots.map(s => s.timeslot), timeslot) ? 'calendar-event-reserved' : ''
      });

  }

  return calendarEvents;
}
