import {Location, LocationConstructor} from './Location';
import {CalendarEvent} from 'angular-calendar';
import {includesTimeslot, Timeslot, timeslotEndHour, timeslotStartHour} from './Timeslot';
import { LocationReservation } from './LocationReservation';
import * as moment from 'moment';
import { Moment } from 'moment';

export class CalendarPeriod {
  id: number;
  location: Location;
  startsAt: Moment;
  endsAt: Moment;
  openingTime: Moment;
  closingTime: Moment;
  reservable: boolean;
  reservableFrom: Moment;
  reservableTimeslotSize: number;
  timeslots: Timeslot[];

  constructor(id: number, location: Location, startsAt: Moment, endsAt: Moment, openingTime: Moment, closingTime: Moment,
              reservable: boolean, reservableFrom: Moment, reservableTimeslotSize: number, timeslots: Timeslot[]) {
    this.id = id;
    this.location = location;
    this.startsAt = startsAt;
    this.endsAt = endsAt;
    this.openingTime = openingTime;
    this.closingTime = closingTime;
    this.reservableFrom = reservableFrom;
    this.reservable = reservable;
    this.reservableTimeslotSize = reservableTimeslotSize;
    this.timeslots = timeslots;
  }

  static fromJSON(json: any): CalendarPeriod {
    return new CalendarPeriod(
      json.id,
      json.location,
      moment(json.startsAt),
      moment(json.endsAt),
      moment(json.openingTime, 'HH:mm'),
      moment(json.closingTime, 'HH:mm'),
      json.reservable,
      moment(json.reservableFrom, 'YYYY-MM-DDTHH:mm:ss'),
      json.reservableTimeslotSize,
      json.timeslots.map(jsonT => Timeslot.fromJSON(jsonT))
    );
  }

  toJSON(): object {
    return {
      location: this.location,
      startsAt: this.startsAt.format('YYYY-MM-DD'),
      endsAt: this.endsAt.format('YYYY-MM-DD'),
      openingTime: this.openingTime.format('HH:mm'),
      closingTime: this.closingTime.format('HH:mm'),
      reservableFrom: this.reservableFrom.format('YYYY-MM-DDTHH:mm:ss'),
      reservableTimeslotSize: this.reservableTimeslotSize,
      timeslots: this.timeslots,
      reservable: this.reservable
    }
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
  if (period === null) {
    return false;
  }


  if (period.startsAt.isAfter(period.endsAt)) {
    return false;
  }

  return period.openingTime.isBefore(period.closingTime);
}



/**
 * Convert calendarPeriods to Calendar Events. This detects correctly whether the period is reservable or not.
 * @param periods The to-convert periods.
 *
 */
export function mapCalendarPeriodsToCalendarEvents(periods: CalendarPeriod[],
                                                   reservedTimeslots: LocationReservation[] = []
                                                                                  ): CalendarEvent[]
{
  return periods
          .map(period => period.reservable ? mapTimeslotsToCalendarEvents(period, reservedTimeslots) : mapNRperiodToCalendarEvents(period))
          .reduce((a, b) => [...a, ...b]);
}

/**
 * Convert NON RESERVABLE calendar period to calendar events.
 * @param period The to-convert period
 */
function mapNRperiodToCalendarEvents(period: CalendarPeriod): CalendarEvent[] {
  const calendarEvents: CalendarEvent[] = [];

  const dateWithOpeningTime = new Date(period.startsAt.format('YYYY-MM-DD') + 'T' + period.openingTime.format('HH:mm'));
  const dateWithClosingTime = new Date(period.startsAt.format('YYYY-MM-DD') + 'T' + period.closingTime.format('HH:mm'));
  const lastDayWithOpeningTime = (new Date(period.endsAt.format('YYYY-MM-DD') + 'T' + period.openingTime.format('HH:mm')));

  while (dateWithOpeningTime <= lastDayWithOpeningTime) {
    calendarEvents.push({
      title: period.openingTime.format('HH:mm') + ' - ' + period.closingTime.format('HH:mm') + '  -  ' + '(open)',
      start: new Date(dateWithOpeningTime),
      end: new Date(dateWithClosingTime),
      meta: period,
      color: {primary: 'black', secondary: '#BEBEBE'},
      cssClass: 'calendar-event-NR',
    });

    dateWithOpeningTime.setDate(dateWithOpeningTime.getDate() + 1);
    dateWithClosingTime.setDate(dateWithClosingTime.getDate() + 1);
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
function mapTimeslotsToCalendarEvents(period: CalendarPeriod, reservedTimeslots: LocationReservation[] = []): CalendarEvent[] {
  const calendarEvents: CalendarEvent[] = [];

  for (const timeslot of period.timeslots) {
      const beginDT = new Date(timeslot.timeslotDate.format('YYYY-MM-DD') + 'T' + timeslotStartHour(period, timeslot.timeslotSeqnr));
      const endDT = new Date(timeslot.timeslotDate.format('YYYY-MM-DD') + 'T' + timeslotEndHour(period, timeslot.timeslotSeqnr));

      calendarEvents.push({
        title: timeslot.timeslotDate.format('DD-MM-YYYY') + ' (Blok ' + (timeslot.timeslotSeqnr + 1) + ')',
        start: beginDT,
        end: endDT,
        meta: timeslot,
        color: includesTimeslot(reservedTimeslots.map(s => s.timeslot), timeslot) ?
                                                         {primary: '#00004d', secondary: '#133E7D'} : null,
        cssClass: includesTimeslot(reservedTimeslots.map(s => s.timeslot), timeslot) ? 'calendar-event-reserved' : ''
      });

  }

  return calendarEvents;
}
