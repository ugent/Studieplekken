import { CalendarNativeDateFormatter, DateFormatterParams } from 'angular-calendar';
import { formatDate } from '@angular/common';

export class CustomDateFormatter extends CalendarNativeDateFormatter {

  public dayViewHour({ date, locale }: DateFormatterParams): string {
    return formatDate(date, 'HH:mm', locale);
  }

  public weekViewHour({ date, locale }: DateFormatterParams): string {
    return this.dayViewHour({ date, locale });
  }

}
