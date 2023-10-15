import { Location, LocationConstructor } from './Location';
import {
  isStringValidDateForDB,
  isStringValidDateTimeForDB,
} from '../extensions/validators/DateValidators';
import { CalendarEvent } from 'calendar-utils';

export interface CalendarPeriodForLockers {
  location: Location;
  startsAt: string;
  endsAt: string;
  reservableFrom: string;
}

export class CalendarPeriodForLockersConstructor {
  static new(): CalendarPeriodForLockers {
    return {
      location: LocationConstructor.new(),
      startsAt: '',
      endsAt: '',
      reservableFrom: '',
    };
  }

  static newFromObj(obj: CalendarPeriodForLockers): CalendarPeriodForLockers {
    return {
      location: LocationConstructor.newFromObj(obj.location),
      startsAt: obj.startsAt,
      endsAt: obj.endsAt,
      reservableFrom: obj.reservableFrom,
    };
  }
}

/**
 * Following checks are performed on the period:
 *
 * 1. Checking the formats of its members
 *   - format of startsAt and endsAt:             YYYY-MM-DD
 *   - format of reservableFrom:                  YYYY-MM-DD HH-MI
 *
 * 2. endsAt may not be before startsAt
 *
 * 3. closingTime may not be before openingTime
 */
export function isCalendarPeriodForLockersValid(
  period: CalendarPeriodForLockers
): boolean {
  if (period === null) {
    return false;
  }

  if (
    !(
      isStringValidDateForDB(period.startsAt) &&
      isStringValidDateForDB(period.endsAt) &&
      isStringValidDateTimeForDB(period.reservableFrom)
    )
  ) {
    return false;
  }

  // endsAt may not be before startsAt
  const startDate = new Date(period.startsAt);
  const endDate = new Date(period.endsAt);

  return endDate >= startDate;
}

export function calendarPeriodForLockersToCalendarEvent(
  period: CalendarPeriodForLockers
): CalendarEvent<CalendarPeriodForLockers> {
  return {
    title: '',
    start: new Date(period.startsAt),
    end: new Date(period.endsAt),
    meta: period,
  };
}
