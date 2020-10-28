import {Location, LocationConstructor} from './Location';
import {
  isStringValidDateForDB,
  isStringValidDateTimeForDB,
  isStringValidTimeForDBWithoutSeconds
} from '../validators/DateValidators';
import {CalendarEvent} from 'angular-calendar';
import {Timeslot} from './Timeslot';

export interface CalendarPeriod {
  location: Location;
  startsAt: string;
  endsAt: string;
  openingTime: string;
  closingTime: string;
  reservable: boolean;
  reservableFrom: string;
  reservableTimeslotSize: number;
  timeslots: Timeslot[];
}

export class CalendarPeriodConstructor {
  static new(): CalendarPeriod {
    return {
      location: LocationConstructor.new(),
      startsAt: '',
      endsAt: '',
      openingTime: '',
      closingTime: '',
      reservableFrom: '',
      reservable: false,
      reservableTimeslotSize: 0,
      timeslots: []
    };
  }

  static newFromObj(obj: CalendarPeriod): CalendarPeriod {
    return {
      location: LocationConstructor.newFromObj(obj.location),
      startsAt: obj.startsAt,
      endsAt: obj.endsAt,
      openingTime: obj.openingTime,
      closingTime: obj.closingTime,
      reservableFrom: obj.reservableFrom,
      reservable: obj.reservable,
      reservableTimeslotSize: obj.reservableTimeslotSize,
      timeslots: obj.timeslots
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
  if (period === null) {
    return false;
  }

  // check if the formats of the strings are as they are supposed to be
  if (!(isStringValidDateForDB(period.startsAt) &&
        isStringValidDateForDB(period.endsAt) &&
        isStringValidTimeForDBWithoutSeconds(period.openingTime) &&
        isStringValidTimeForDBWithoutSeconds(period.closingTime) &&
        isStringValidDateTimeForDB(period.reservableFrom))) {
    return false;
  }

  // endsAt may not be before startsAt
  const startDate = new Date(period.startsAt);
  const endDate = new Date(period.endsAt);

  if (endDate < startDate) {
    return false;
  }

  // closing time may not be before opening time
  // this will be checked by creating a new Date() with two
  // times the same date, but with the times set to the values
  // of opening and closing time
  const openingTimeDate = new Date(period.startsAt + 'T' + period.openingTime);
  const closingTimeDate = new Date(period.startsAt + 'T' + period.closingTime);

  return closingTimeDate >= openingTimeDate;
}

/**
 * For each Timeslot that is attached to a CalendarPeriod provided in 'periods',
 * this method will create a CalendarEvent
 *
 * The starting and ending time for all CalendarEvents created from a timeslot, will
 * be the beginning and ending of the timeslot, calculated from the sequence number and the
 * timeslotdate.
 */
export function mapTimeslotsToCalendarEvents(periods: CalendarPeriod[]): CalendarEvent[] {
  const calendarEvents: CalendarEvent[] = [];

  for (const period of periods) {
    for (const timeslot of period.timeslots) {
      let beginDT = new Date(timeslot.timeslotDate + "T" + period.openingTime);
      beginDT.setTime(beginDT.getTime() + timeslot.timeslotSeqnr * period.reservableTimeslotSize * 60000);
      let endDT = new Date(timeslot.timeslotDate + "T" + period.openingTime);
      endDT.setTime(endDT.getTime() + (timeslot.timeslotSeqnr + 1) * period.reservableTimeslotSize * 60000);

      calendarEvents.push({
        title: timeslot.timeslotDate + " (Blok " + timeslot.timeslotSeqnr + ")",
        start: beginDT,
        end: endDT,
        meta: timeslot
      })
    }
  }

  return calendarEvents;
}
