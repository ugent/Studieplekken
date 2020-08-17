import {CustomDate, CustomDateConstructor} from './helpers/CustomDate';
import {Time, TimeConstructor} from './helpers/Time';
import {Location, LocationConstructor} from './Location';

export interface CalendarDay {
  date: CustomDate;
  openingHour: Time;
  closingHour: Time;
  openForReservationDate: CustomDate;
}


export interface CalendarPeriod {
  location: Location;
  startsAt: string;
  endsAt: string;
  openingTime: string;
  closingTime: string;
  reservableFrom: string;
}


export class CalendarDayConstructor {
  static new(): CalendarDay {
    return {
      date: CustomDateConstructor.new(),
      openingHour: TimeConstructor.new(),
      closingHour: TimeConstructor.new(),
      openForReservationDate: CustomDateConstructor.new()
    };
  }

  static newFromObj(obj: CalendarDay): CalendarDay {
    return {
      date: CustomDateConstructor.newFromObj(obj.date),
      openingHour: TimeConstructor.newFromObj(obj.openingHour),
      closingHour: TimeConstructor.newFromObj(obj.closingHour),
      openForReservationDate: CustomDateConstructor.newFromObj(obj.openForReservationDate)
    };
  }
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

export function isStringValidDate(dateStr: string): boolean {
  const date = new Date(dateStr);
  return !isNaN(date.getTime());
}

export function isStringValidTimeWithoutSeconds(timeStr: string): boolean {
  const regexp = new RegExp('^[0-9][0-9]:[0-9][0-9]$');
  return regexp.test(timeStr);
}
