import { Location, LocationConstructor } from './Location';
import {
  Timeslot,
} from './Timeslot';
import * as moment from 'moment';
import { Moment } from 'moment';

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
