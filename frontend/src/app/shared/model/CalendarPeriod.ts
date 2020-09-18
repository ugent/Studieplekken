import {Location, LocationConstructor} from './Location';
import {
  isStringValidDateForDB,
  isStringValidDateTimeForDB,
  isStringValidTimeForDBWithoutSeconds
} from '../validators/DateValidators';
import {CalendarEvent} from 'angular-calendar';

export interface CalendarPeriod {
  location: Location;
  startsAt: string;
  endsAt: string;
  openingTime: string;
  closingTime: string;
  reservableFrom: string;
}

export class CalendarPeriodConstructor {
  static new(): CalendarPeriod {
    return {
      location: LocationConstructor.new(),
      startsAt: '',
      endsAt: '',
      openingTime: '',
      closingTime: '',
      reservableFrom: ''
    };
  }

  static newFromObj(obj: CalendarPeriod): CalendarPeriod {
    return {
      location: LocationConstructor.newFromObj(obj.location),
      startsAt: obj.startsAt,
      endsAt: obj.endsAt,
      openingTime: obj.openingTime,
      closingTime: obj.closingTime,
      reservableFrom: obj.reservableFrom
    };
  }
}

/**
 * Following checks are performed on the period:
 *
 * 1. Checking the formats of its members
 *   - format of startsAt and endsAt:             new Date(member) != NaN
 *   - format of openingTime and openingTime:     HH:MI
 *   - format of reservableFrom:                  new Date(member) != NaN
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
 * For each period provided in 'periods', this method will create a CalendarEvent
 * object for every day within the period.
 *
 * The starting and ending time for all CalendarEvents created from a period, will
 * be 'period.openingTime' and 'period.closingTime' respectively.
 *
 * An example period could be:
 *
 *      const period = {
 *        location: ...,                   // irrelevant for this example
 *        startsAt: '2020-01-01',
 *        endsAt: '2020-01-05',
 *        openingTime: '09:00',
 *        closingTime: '12:00',
 *        reservableFrom: ...              // irrelevant for this example, and not used in MiniThermis
 *      }
 *
 * If the period above would be part of the 'periods' parameter, following
 * CalendarEvent-objects would be created and pushed in the return-array:
 *
 *      1. { title: '09:00 - 12:00', start: new Date('2020-01-01T09:00'), end: new Date('2020-01-01T12:00'), meta: period }
 *      2. { title: '09:00 - 12:00', start: new Date('2020-01-02T09:00'), end: new Date('2020-01-02T12:00'), meta: period }
 *      3. { title: '09:00 - 12:00', start: new Date('2020-01-03T09:00'), end: new Date('2020-01-03T12:00'), meta: period }
 *      4. { title: '09:00 - 12:00', start: new Date('2020-01-04T09:00'), end: new Date('2020-01-04T12:00'), meta: period }
 *      5. { title: '09:00 - 12:00', start: new Date('2020-01-05T09:00'), end: new Date('2020-01-05T12:00'), meta: period }
 */
export function mapCalendarPeriodsToCalendarEvents(periods: CalendarPeriod[]): CalendarEvent[] {
  const calendarEvents: CalendarEvent[] = [];

  for (const period of periods) {
    const dateWithOpeningTime = new Date(period.startsAt + 'T' + period.openingTime);
    const dateWithClosingTime = new Date(period.startsAt + 'T' + period.closingTime);
    const lastDayWithOpeningTime = (new Date(period.endsAt + 'T' + period.openingTime));

    while (dateWithOpeningTime.toLocaleDateString() !== lastDayWithOpeningTime.toLocaleDateString()) {
      calendarEvents.push({
        title: period.openingTime + ' - ' + period.closingTime,
        start: new Date(dateWithOpeningTime),
        end: new Date(dateWithClosingTime),
        meta: period
      });

      dateWithOpeningTime.setDate(dateWithOpeningTime.getDate() + 1);
      dateWithClosingTime.setDate(dateWithClosingTime.getDate() + 1);
    }

    // add the final day too
    calendarEvents.push({
      title: period.openingTime + ' - ' + period.closingTime,
      start: new Date(dateWithOpeningTime),
      end: new Date(dateWithClosingTime),
      meta: period
    });
  }

  return calendarEvents;
}
