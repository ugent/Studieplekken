import {Location, LocationConstructor} from './Location';
import {
  isStringValidDateForDB,
  isStringValidDateTimeForDB,
  isStringValidTimeForDBWithoutSeconds
} from '../validators/DateValidators';
import {CalendarEvent} from 'angular-calendar';
import {includesTimeslot, Timeslot, timeslotEndHour, timeslotEquals, timeslotStartHour} from './Timeslot';
import { LocationReservation } from './LocationReservation';
import * as moment from 'moment';

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
        (!period.reservable || isStringValidDateTimeForDB(period.reservableFrom) || !period.reservableFrom || true))) {
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
 * Convert calendarPeriods to Calendar Events. This detects correctly whether the period is reservable or not.
 * @param periods The to-convert periods.
 * 
 */
export function mapCalendarPeriodsToCalendarEvents(periods: CalendarPeriod[],
                                                   reservedTimeslots: LocationReservation[] = []
                                                                                  ): CalendarEvent[]
{
  return periods
          .map(period => period.reservable ? mapTimeslotsToCalendarEvents(period, reservedTimeslots) : mapNRperiodToCalendarEvents(period))
          .reduce((a, b) => [...a, ...b]);
}

/**
 * Convert NON RESERVABLE calendar period to calendar events.
 * @param period The to-convert period
 */
function mapNRperiodToCalendarEvents(period: CalendarPeriod): CalendarEvent[] {
  const calendarEvents: CalendarEvent[] = [];

  const dateWithOpeningTime = new Date(period.startsAt + 'T' + period.openingTime);
  const dateWithClosingTime = new Date(period.startsAt + 'T' + period.closingTime);
  const lastDayWithOpeningTime = (new Date(period.endsAt + 'T' + period.openingTime));

  while (dateWithOpeningTime <= lastDayWithOpeningTime) {
    calendarEvents.push({
      title: moment(period.openingTime, 'HH:mm:ss').format('HH:mm') + ' - ' + moment(period.closingTime, 'HH:mm:ss').format('HH:mm') + '  -  ' + '(open)',
      start: new Date(dateWithOpeningTime),
      end: new Date(dateWithClosingTime),
      meta: period,
      color: {primary: 'black', secondary: '#BEBEBE'},
      cssClass: 'calendar-event-NR',
    });

    dateWithOpeningTime.setDate(dateWithOpeningTime.getDate() + 1);
    dateWithClosingTime.setDate(dateWithClosingTime.getDate() + 1);
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
function mapTimeslotsToCalendarEvents(period: CalendarPeriod, reservedTimeslots: LocationReservation[] = []): CalendarEvent[] {
  const calendarEvents: CalendarEvent[] = [];

  for (const timeslot of period.timeslots) {
      const beginDT = new Date(timeslot.timeslotDate + 'T' + timeslotStartHour(period, timeslot.timeslotSeqnr));
      const endDT = new Date(timeslot.timeslotDate + 'T' + timeslotEndHour(period, timeslot.timeslotSeqnr));

      calendarEvents.push({
        title: timeslot.timeslotDate + ' (Blok ' + (timeslot.timeslotSeqnr + 1) + ')',
        start: beginDT,
        end: endDT,
        meta: timeslot,
        color: includesTimeslot(reservedTimeslots.map(s => s.timeslot), timeslot) ?
                                                         {primary: '#00004d', secondary: '#133E7D'} : null,
        cssClass: includesTimeslot(reservedTimeslots.map(s => s.timeslot), timeslot) ? 'calendar-event-reserved' : ''
      });

  }

  return calendarEvents;
}
