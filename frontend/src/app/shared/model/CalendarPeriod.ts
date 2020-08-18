import {Location, LocationConstructor} from './Location';
import {isStringValidDate, isStringValidTimeWithoutSeconds} from '../validators/DateValidators';

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

export function isCalendarPeriodValid(period: CalendarPeriod): boolean {
  if (period === null) {
    return false;
  }

  return isStringValidDate(period.startsAt) &&
    isStringValidDate(period.endsAt) &&
    isStringValidTimeWithoutSeconds(period.openingTime) &&
    isStringValidTimeWithoutSeconds(period.closingTime) &&
    isStringValidDate(period.reservableFrom);
}

export function calendarPeriodToCalendarEvent(period: CalendarPeriod): any {
  return {
    title: period.openingTime + ' - ' + period.closingTime,
    start: new Date(period.startsAt),
    end: new Date(period.endsAt),
    meta: period
  };
}
