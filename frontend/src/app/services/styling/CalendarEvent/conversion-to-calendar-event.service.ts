import { Injectable } from '@angular/core';
import {LocationReservation} from '../../../shared/model/LocationReservation';
import {CalendarEvent} from 'angular-calendar';
import {Timeslot,  timeslotEquals} from '../../../shared/model/Timeslot';
import {CalendarPeriod} from '../../../shared/model/CalendarPeriod';
import * as moment from 'moment';
import {Moment} from 'moment';

@Injectable({
  providedIn: 'root'
})
export class ConversionToCalendarEventService {

  constructor() { }

  /**
   * Convert calendarPeriods to Calendar Events. This detects correctly whether the period is reservable or not (yet).
   */
  mapCalendarPeriodsToCalendarEvents(
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
          ? this.mapReservableTimeslotsToCalendarEvents(period, locationReservations)
          : period.reservable && period.areReservationsLocked()
          ? this.mapNotYetReservableTimeslotsToCalendarEvents(period, currentLang)
          : this.mapNotReservableCalendarPeriodToCalendarEvent(period, currentLang)
      )
      .reduce((a, b) => [...a, ...b]);
  }

  /**
   * Convert a calendar period to calendar events but as a block instead of dividing each day into timeslots.
   */
  mapNotReservableCalendarPeriodToCalendarEvent(
    period: CalendarPeriod,
    currentLang: string
  ): CalendarEvent<{ calendarPeriod: CalendarPeriod; timeslot?: Timeslot }>[] {
    const calendarEvents: CalendarEvent<{
      calendarPeriod: CalendarPeriod;
    }>[] = [];

    const dateWithOpeningTime = this.dateWithTime(period.startsAt, period.openingTime);
    const dateWithClosingTime = this.dateWithTime(period.startsAt, period.closingTime);
    const lastDayWithOpeningTime = this.dateWithTime(
      period.endsAt,
      period.openingTime
    );

    let title: string;
    if (currentLang === 'nl') {
      title = `Geen reservatie nodig <br> ${period.openingTime.format('HH:mm')} - ${period.closingTime.format('HH:mm')}`;
    } else {
      title = `Requires no reservation <br> ${period.openingTime.format('HH:mm')} - ${period.closingTime.format('HH:mm')}`;
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
  mapNotYetReservableTimeslotsToCalendarEvents(
    period: CalendarPeriod,
    currentLang: string
  ): CalendarEvent<{ calendarPeriod: CalendarPeriod; timeslot: Timeslot }>[] {
    const calendarEvents: CalendarEvent<{
      calendarPeriod: CalendarPeriod;
      timeslot: Timeslot;
    }>[] = [];

    for (const timeslot of period.timeslots) {
      const startHour = timeslot.getStartMoment()
      const endHour = timeslot.getEndMoment()

      const beginDT = this.dateWithTime(
        timeslot.timeslotDate,
        startHour
      );
      const endDT = this.dateWithTime(
        timeslot.timeslotDate,
        endHour
      );

      let title: string;
      if (currentLang === 'nl') {
        title = `${startHour.format('HH:mm')} - ${endHour.format('HH:mm')} <br> Reserveren vanaf ${period.reservableFrom.format('DD/MM/YYYY HH:mm')}`;
      } else {
        title = `${startHour.format('HH:mm')} - ${endHour.format('HH:mm')} <br> Reservable from ${period.reservableFrom.format('DD/MM/YYYY HH:mm')}`;
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
  mapReservableTimeslotsToCalendarEvents(
    period: CalendarPeriod,
    locationReservations: LocationReservation[] = []
  ): CalendarEvent<{ calendarPeriod: CalendarPeriod; timeslot: Timeslot }>[] {
    const calendarEvents: CalendarEvent<{
      calendarPeriod: CalendarPeriod;
      timeslot: Timeslot;
    }>[] = [];

    for (const timeslot of period.timeslots) {
      const startHour = timeslot.getStartMoment()
      const endHour = timeslot.getEndMoment()

      const beginDT = this.dateWithTime(
        timeslot.timeslotDate,
        startHour
      );
      const endDT = this.dateWithTime(
        timeslot.timeslotDate,
        endHour
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

      const cssClass = this.clickableBasedOnTime(
        {
          calendarPeriod: period,
          timeslot,
        },
        locationReservations
      )
        ? ''
        : 'unclickable';

      const title = `${timeslot.amountOfReservations} / ${timeslot.seatCount} <br> ${startHour.format('HH:mm')} - ${endHour.format('HH:mm')}`;

      calendarEvents.push({
        title,
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
  clickableBasedOnTime(
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
    const startIsPast = timeslot.getStartMoment().isBefore(now);
    const endIsPast = timeslot.getEndMoment().isBefore(now);

    if (startIsPast && endIsPast) {
      return false;
    } else if (startIsPast) {
      return currentLR === undefined || currentLR.attended !== false;
    } else {
      return true;
    }
  }

  dateWithTime(date: Moment, time: Moment): Date {
    return new Date(date.format('YYYY-MM-DD') + 'T' + time.format('HH:mm'));
  }
}
