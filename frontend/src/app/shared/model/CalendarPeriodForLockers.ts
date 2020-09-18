import {Location, LocationConstructor} from './Location';
import {isStringValidDateForDB} from '../validators/DateValidators';

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
      reservableFrom: ''
    };
  }

  static newFromObj(obj: CalendarPeriodForLockers): CalendarPeriodForLockers {
    return {
      location: LocationConstructor.newFromObj(obj.location),
      startsAt: obj.startsAt,
      endsAt: obj.endsAt,
      reservableFrom: obj.reservableFrom
    };
  }
}

export function isCalendarPeriodForLockersValid(period: CalendarPeriodForLockers): boolean {
  if (period === null) {
    return false;
  }

  return isStringValidDateForDB(period.startsAt) &&
    isStringValidDateForDB(period.endsAt) &&
    isStringValidDateForDB(period.reservableFrom);
}

export function calendarPeriodForLockersToCalendarEvent(period: CalendarPeriodForLockers): any {
  return {
    title: '',
    start: new Date(period.startsAt),
    end: new Date(period.endsAt),
    meta: period
  };
}
