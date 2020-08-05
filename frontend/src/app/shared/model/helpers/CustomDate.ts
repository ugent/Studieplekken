export class CustomDate {
  year: number;
  month: number;
  day: number;
  hrs: number;
  min: number;
  sec: number;
}

export function toDateString(date: CustomDate): string {
  return date.year + '-' + date.month + '-' + date.day;
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


