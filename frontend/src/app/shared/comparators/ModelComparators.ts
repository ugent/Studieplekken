import {Location} from '../model/Location';
import {CustomDate} from '../model/helpers/CustomDate';
import {CalendarPeriod} from '../model/CalendarPeriod';
import {CalendarPeriodForLockers} from '../model/CalendarPeriodForLockers';

export function equalCustomDates(date1: CustomDate, date2: CustomDate): boolean {
  if (date1 === null && date2 == null) {
    return true;
  }

  return date1.year === date2.year &&
    date1.month === date2.month &&
    date1.day === date2.day &&
    date1.hrs === date2.hrs &&
    date1.min === date2.min &&
    date1.sec === date2.sec;
}

export function equalLocations(location1: Location, location2: Location): boolean {
  if (location1 === null && location2 == null) {
    return true;
  }

  return location1.name === location2.name &&
    location1.numberOfSeats === location2.numberOfSeats &&
    location1.numberOfLockers === location2.numberOfLockers &&
    location1.building.address === location2.building.address &&
    location1.imageUrl === location2.imageUrl;
}

export function equalCalendarPeriods(period1: CalendarPeriod, period2: CalendarPeriod): boolean {
  if (period1 === null && period2 == null) {
    return true;
  }

  return equalLocations(period1.location, period2.location) &&
    period1.startsAt === period2.startsAt &&
    period1.endsAt === period2.endsAt &&
    period1.openingTime === period2.openingTime &&
    period1.closingTime === period2.closingTime &&
    period1.reservableFrom === period2.reservableFrom;
}

export function equalCalendarPeriodsForLockers(period1: CalendarPeriodForLockers,
                                               period2: CalendarPeriodForLockers): boolean {
  if (period1 === null && period2 == null) {
    return true;
  }

  return equalLocations(period1.location, period2.location) &&
    period1.startsAt === period2.startsAt &&
    period1.endsAt === period2.endsAt &&
    period1.reservableFrom === period2.reservableFrom;
}
