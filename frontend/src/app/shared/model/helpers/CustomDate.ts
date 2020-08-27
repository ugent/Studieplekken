export interface CustomDate {
  year: number;
  month: number;
  day: number;
  hrs: number;
  min: number;
  sec: number;
}
export class CustomDateConstructor {
  static new(): CustomDate {
    return {
      year: 0,
      month: 0,
      day: 0,
      hrs: 0,
      min: 0,
      sec: 0
    };
  }

  static newFromObj(obj: CustomDate): CustomDate {
    if (obj === null) {
      return null;
    }

    return {
      year: obj.year,
      month: obj.month,
      day: obj.day,
      hrs: obj.hrs,
      min: obj.min,
      sec: obj.sec
    };
  }
}

export function compareDates(date1: CustomDate, date2: CustomDate): number {
  const d1 = Number.parseInt(toIntegerString(date1), 10);
  const d2 = Number.parseInt(toIntegerString(date2), 10);
  return (d1 < d2) ? -1 : (d1 > d2) ? 1 : 0;
}

export function nowAsCustomDate(): CustomDate {
  const date = new Date();
  const customDate = CustomDateConstructor.new();

  customDate.year = date.getFullYear();
  customDate.month = date.getMonth() + 1; // months in Date are from 0-11
  customDate.day = date.getDate();
  customDate.hrs = date.getHours();
  customDate.min = date.getMinutes();
  customDate.sec = date.getSeconds();
  return customDate;
}

export function toDateString(date: CustomDate): string {
  const d = customDateToTypeScriptDate(date);
  const dateString = d.toLocaleDateString();

  // format or dateString: D-M-YYYY or M-D-YYYY
  return formatDateString(dateString);
}

export function toTimeString(date: CustomDate): string {
  return date.hrs + ':' + date.min + ':' + date.sec;
}

export function toDateTimeString(date: CustomDate): string {
  return toDateString(date) + 'T'
    + toTimeString(date);
}

export function toDateTimeViewString(date: CustomDate): string {
  return toDateString(date) + ' '
    + toTimeString(date);
}

/*
 * This transforms the CustomDate to a concatenation of the attributes
 * in the following format (PostgreSQL style): YYYYMMDDHH24MISS
 * This makes it possible to just transform this string to a number
 * before comparing it to another date
 */
export function toIntegerString(date: CustomDate): string {
  return date.year + ('' + date.month).padStart(2, '0') + ('' + date.day).padStart(2, '0') + ''
    + date.hrs + '' + date.min + '' + date.sec;
}

export function customDateToTypeScriptDate(date: CustomDate): Date {
  return new Date(date.year,
    date.month - 1, // months in Date are from 0-11
    date.day, date.hrs, date.min, date.sec);
}

export function typeScriptDateToCustomDate(date: Date): CustomDate {
  return {
    year: date.getFullYear(),
    month: date.getMonth() + 1,
    day: date.getDate(),
    hrs: date.getHours(),
    min: date.getMinutes(),
    sec: date.getSeconds()
  };
}

/*
 * Make sure that a string formatted as e.g. M-D-YYYY or YYYY-M-D becomes
 * MM-DD-YYYY or YYYY-MM-DD
 */
export function formatDateString(dateString: string): string {
  let ret = '';
  const dateParts = dateString.split('-');

  dateParts.forEach(part => {
    if (part.length === 1) {
      ret += '0';
    }
    ret += part + '-';
  });

  return ret.substr(0, ret.length - 1);
}
