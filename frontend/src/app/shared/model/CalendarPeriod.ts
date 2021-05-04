import { Location, LocationConstructor } from './Location';
import { CalendarEvent } from 'angular-calendar';
import {
  Timeslot,
  timeslotEndHour,
  timeslotEquals,
  timeslotStartHour,
} from './Timeslot';
import { LocationReservation } from './LocationReservation';
import * as moment from 'moment';
import { Moment } from 'moment';
import { calendarEventTitleTemplate } from '../../app.constants';

export class CalendarPeriod {
  constructor(
    id: number,
    location: Location,
    startsAt: Moment,
    endsAt: Moment,
    openingTime: Moment,
    closingTime: Moment,
    reservable: boolean,
    reservableFrom: Moment,
    timeslotLength: number,
    timeslots: Timeslot[],
    lockedFrom: Moment,
    seatCount: number
  ) {
    this.id = id;
    this.location = location;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.openingTime = openingTime;
    this.closingTime = closingTime;
    this.reservableFrom = reservableFrom;
    this.reservable = reservable;
    this.timeslotLength = timeslotLength;
    this.timeslots = timeslots;
    this.lockedFrom = lockedFrom;
    this.seatCount = seatCount;
  }
  id: number;
  location: Location;
  startsAt: Moment;
  endsAt: Moment;
  openingTime: Moment;
  closingTime: Moment;
  reservable: boolean;
  reservableFrom: Moment;
  timeslotLength: number;
  timeslots: Timeslot[];
  lockedFrom: Moment;
  seatCount: number;

  static fromJSON(json: CalendarPeriod): CalendarPeriod {
    return new CalendarPeriod(
      +json.id,
      LocationConstructor.newFromObj(json.location),
      moment(json.startsAt),
      moment(json.endsAt),
      moment(json.openingTime, 'HH:mm'),
      moment(json.closingTime, 'HH:mm'),
      json.reservable,
      moment(json.reservableFrom, 'YYYY-MM-DDTHH:mm:ss'),
      json.timeslotLength,
      json.timeslots.map((jsonT) => Timeslot.fromJSON(jsonT)),
      moment(json.lockedFrom),
      json.seatCount
    );
  }

  isLocked(): boolean {
    return this.lockedFrom && this.lockedFrom.isBefore(moment());
  }

  areReservationsLocked(): boolean {
    return !this.reservableFrom || this.reservableFrom.isAfter(moment());
  }

  isValid(): boolean {
    return isCalendarPeriodValid(this);
  }

  toJSON(): Record<string, unknown> {
    return {
      id: this.id,
      location: this.location,
      startsAt: this.startsAt.format('YYYY-MM-DD'),
      endsAt: this.endsAt.format('YYYY-MM-DD'),
      openingTime: this.openingTime.format('HH:mm'),
      closingTime: this.closingTime.format('HH:mm'),
      reservableFrom: this.reservableFrom
        ? this.reservableFrom.format('YYYY-MM-DDTHH:mm:ss')
        : null,
      timeslotLength: this.timeslotLength,
      timeslots: this.timeslots,
      reservable: this.reservable,
      lockedFrom: this.lockedFrom,
      seatCount: this.seatCount,
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
  // at least, these attributes may not be null (id may be null for adding a period)
  if (
    period === null ||
    period.location === null ||
    period.startsAt === null ||
    period.endsAt === null ||
    period.openingTime === null ||
    period.closingTime === null
  ) {
    return false;
  }

  // if the period is set to be reservable, timeslotLength may not be 0, nor may reservableFrom be null
  if (
    period.reservable &&
    (period.timeslotLength <= 0 ||
      period.timeslotLength === null ||
      period.timeslotLength === undefined ||
      period.timeslotLength >
        Math.abs(period.openingTime.diff(period.closingTime, 'minutes')) ||
      period.reservableFrom === null)
  ) {
    return false;
  }

  // of course, startsAt, endsAt must be valid and startsAt must be before endsAt
  if (
    !period.startsAt.isValid() ||
    !period.endsAt.isValid() ||
    period.startsAt.isAfter(period.endsAt)
  ) {
    return false;
  }

  // if the period is reservable, the reservableFrom must be valid too
  if (period.reservable && !period.reservableFrom.isValid()) {
    return false;
  }

  // and finally, the opening time must be before the closing time
  return period.openingTime.isBefore(period.closingTime);
}

/**
 * Convert calendarPeriods to Calendar Events. This detects correctly whether the period is reservable or not (yet).
 */
export function mapCalendarPeriodsToCalendarEvents(
  periods: CalendarPeriod[],
  currentLang: string,
  locationReservations: LocationReservation[] = []
): CalendarEvent<{ calendarPeriod: CalendarPeriod; timeslot?: Timeslot }>[] {
  if (periods.length === 0) {
    return [];
  }
  return periods
    .map((period) =>
      period.reservable && !period.areReservationsLocked()
        ? mapReservableTimeslotsToCalendarEvents(period, locationReservations)
        : period.reservable && period.areReservationsLocked()
        ? mapNotYetReservableTimeslotsToCalendarEvents(period, currentLang)
        : mapNotReservableCalendarPeriodToCalendarEvent(period, currentLang)
    )
    .reduce((a, b) => [...a, ...b]);
}

function dateWithTime(date: Moment, time: Moment): Date {
  return new Date(date.format('YYYY-MM-DD') + 'T' + time.format('HH:mm'));
}

/**
 * Convert a calendar period to calendar events but as a block instead of dividing each day into timeslots.
 */
function mapNotReservableCalendarPeriodToCalendarEvent(
  period: CalendarPeriod,
  currentLang: string
): CalendarEvent<{ calendarPeriod: CalendarPeriod; timeslot?: Timeslot }>[] {
  const calendarEvents: CalendarEvent<{
    calendarPeriod: CalendarPeriod;
  }>[] = [];

  const dateWithOpeningTime = dateWithTime(period.startsAt, period.openingTime);
  const dateWithClosingTime = dateWithTime(period.startsAt, period.closingTime);
  const lastDayWithOpeningTime = dateWithTime(
    period.endsAt,
    period.openingTime
  );

  let title: string;
  if (currentLang === 'nl') {
    title = calendarEventTitleTemplate.notReservableNL;
  } else {
    title = calendarEventTitleTemplate.notReservableEN;
  }

  while (dateWithOpeningTime <= lastDayWithOpeningTime) {
    calendarEvents.push({
      title,
      start: new Date(dateWithOpeningTime),
      end: new Date(dateWithClosingTime),
      meta: { calendarPeriod: period },
      color: { primary: 'black', secondary: '#BEBEBE' },
      cssClass: 'calendar-event-NR',
    });

    dateWithOpeningTime.setDate(dateWithOpeningTime.getDate() + 1);
    dateWithClosingTime.setDate(dateWithClosingTime.getDate() + 1);
  }

  return calendarEvents;
}

/**
 * Convert a CalendarPeriod which is not yet reservable (reservableFrom is in the future), to CalendarEvents.
 * Every timeslot of the CalendarPeriod will be represented by a CalendarEvent but will be greyed out
 * and have a title that notifies the user when the timeslot will be reservable.
 */
function mapNotYetReservableTimeslotsToCalendarEvents(
  period: CalendarPeriod,
  currentLang: string
): CalendarEvent<{ calendarPeriod: CalendarPeriod; timeslot: Timeslot }>[] {
  const calendarEvents: CalendarEvent<{
    calendarPeriod: CalendarPeriod;
    timeslot: Timeslot;
  }>[] = [];

  for (const timeslot of period.timeslots) {
    const beginDT = dateWithTime(
      timeslot.timeslotDate,
      timeslotStartHour(period, timeslot)
    );
    const endDT = dateWithTime(
      timeslot.timeslotDate,
      timeslotEndHour(period, timeslot)
    );

    let title: string;
    if (currentLang === 'nl') {
      title = calendarEventTitleTemplate.reservableFromNL.replace(
        '{datetime}',
        period.reservableFrom.format('DD/MM/YYYY HH:mm')
      );
    } else {
      title = calendarEventTitleTemplate.reservableFromEN.replace(
        '{datetime}',
        period.reservableFrom.format('DD/MM/YYYY HH:mm')
      );
    }

    calendarEvents.push({
      title,
      start: beginDT,
      end: endDT,
      meta: { calendarPeriod: period, timeslot },
      color: { primary: 'black', secondary: '#BEBEBE' },
      cssClass: 'calendar-event-NR',
    });
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
function mapReservableTimeslotsToCalendarEvents(
  period: CalendarPeriod,
  locationReservations: LocationReservation[] = []
): CalendarEvent<{ calendarPeriod: CalendarPeriod; timeslot: Timeslot }>[] {
  const calendarEvents: CalendarEvent<{
    calendarPeriod: CalendarPeriod;
    timeslot: Timeslot;
  }>[] = [];

  for (const timeslot of period.timeslots) {
    const beginDT = dateWithTime(
      timeslot.timeslotDate,
      timeslotStartHour(period, timeslot)
    );
    const endDT = dateWithTime(
      timeslot.timeslotDate,
      timeslotEndHour(period, timeslot)
    );

    const currentLR = locationReservations.find((value) =>
      timeslotEquals(value.timeslot, timeslot)
    );
    let color: { primary: string; secondary: string } = null;
    if (currentLR) {
      if (currentLR.attended === false) {
        color = { primary: '#751515', secondary: '#751515' };
      } else if (currentLR.attended === true) {
        color = { primary: '#0a5c0a', secondary: '#0a5c0a' };
      } else {
        color = { primary: '#133E7D', secondary: '#133E7D' };
      }
    }

    const cssClass = clickableBasedOnTime(
      {
        calendarPeriod: period,
        timeslot,
      },
      locationReservations
    )
      ? ''
      : 'unclickable';

    calendarEvents.push({
      title: `${timeslot.amountOfReservations} / ${timeslot.seatCount}`,
      start: beginDT,
      end: endDT,
      meta: { calendarPeriod: period, timeslot },
      color,
      cssClass: currentLR ? cssClass + ' calendar-event-reserved' : cssClass,
    });
  }

  return calendarEvents;
}

/**
 * If the selected timeslot is completely in the past (i.e. startHour and endHour are in the past),
 * then the user should not be allowed to click on the timeslot.
 *
 * If the the now() is within [startHour, endHour] (taking the timeslot date into account), then
 * in two occasions should the timeslot be clickable:
 *     - the user has not made a reservation (currentRL will be undefined)
 *     - the user was scanned as absent, then he shouldn't be able to delete this violation
 * Note that the timeslot also shouldn't be clickable if the timeslot is full, but that is checked elsewhere
 *
 * If now() is before both startHour and endHour, the timeslot must always be clickable
 */
export function clickableBasedOnTime(
  event: {
    calendarPeriod: CalendarPeriod,
    timeslot: Timeslot
  },
  locationReservations: LocationReservation[]
): boolean {
  const calendarPeriod = event.calendarPeriod;
  const timeslot = event.timeslot;
  const now = moment();

  const currentLR = locationReservations.find((value) =>
    timeslotEquals(value.timeslot, timeslot)
  );
  const startIsPast = timeslotStartHour(calendarPeriod, timeslot).isBefore(now);
  const endIsPast = timeslotEndHour(calendarPeriod, timeslot).isBefore(now);

  if (startIsPast && endIsPast) {
    return false;
  } else if (startIsPast) {
    return currentLR === undefined || currentLR.attended !== false;
  } else {
    return true;
  }
}
