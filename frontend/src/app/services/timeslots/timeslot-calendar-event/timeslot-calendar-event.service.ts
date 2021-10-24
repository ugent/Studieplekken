import { Injectable } from '@angular/core';
import { CalendarEvent } from 'angular-calendar';
import * as moment from 'moment';
import { LocationReservation } from 'src/app/shared/model/LocationReservation';
import { Timeslot, includesTimeslot } from 'src/app/shared/model/Timeslot';

@Injectable({
  providedIn: 'root'
})
export class TimeslotCalendarEventService {

  constructor() { }

  
timeslotToCalendarEvent = (timeslot: Timeslot, currentLang: string, locationReservations: LocationReservation[] = []) =>
!timeslot.reservable ? this.nonReservableToCalendarEvent(timeslot, currentLang) :
  timeslot.areReservationsLocked() ? this.notYetReservableTimeslotToCalendarEvent(timeslot, currentLang) :
    this.reservableTimeslotToCalendarEvent(timeslot, currentLang, locationReservations);


private nonReservableToCalendarEvent: (timeslot: Timeslot, currentLang: string) => CalendarEvent<{ timeslot: Timeslot }> =
(timeslot, currentLang) =>
({
  title: currentLang == "nl" ? 
      `Geen reservatie nodig <br> ${timeslot.openingHour.format('HH:mm')} - ${timeslot.closingHour.format('HH:mm')}`
       : `Requires no reservation <br> ${timeslot.openingHour.format('HH:mm')} - ${timeslot.closingHour.format('HH:mm')}`,
  start: timeslot.getStartMoment().toDate(),
  end: timeslot.getEndMoment().toDate(),
  meta: { timeslot },
  color: { primary: 'black', secondary: '#BEBEBE' },
  cssClass: 'calendar-event-NR',
})

private notYetReservableTimeslotToCalendarEvent: (timeslot: Timeslot, currentLang: string) => CalendarEvent<{ timeslot: Timeslot }> =
(timeslot, currentLang) =>
({
  title: currentLang == "nl" ? 
  `${timeslot.openingHour.format('HH:mm')} - ${timeslot.closingHour.format('HH:mm')} <br> Reserveren vanaf ${timeslot.reservableFrom.format('DD/MM/YYYY HH:mm')}`
   : `${timeslot.openingHour.format('HH:mm')} - ${timeslot.closingHour.format('HH:mm')} <br> Reservable from ${timeslot.reservableFrom.format('DD/MM/YYYY HH:mm')}`,
  start: timeslot.getStartMoment().toDate(),
  end: timeslot.getEndMoment().toDate(),
  meta: { timeslot },
  color: { primary: 'black', secondary: '#BEBEBE' },
  cssClass: 'calendar-event-NR',
});

private reservableTimeslotToCalendarEvent: (timeslot: Timeslot, currentLang: string, reservedTimeslots: LocationReservation[]) => CalendarEvent<{ timeslot: Timeslot }> =
(timeslot, currentLang, reservedTimeslots) =>
({
  title: `${timeslot.amountOfReservations} / ${timeslot.seatCount}`,
  start: timeslot.getStartMoment().toDate(),
  end: timeslot.getEndMoment().toDate(),
  meta: { timeslot },
  color: includesTimeslot(
    reservedTimeslots.map((s) => s.timeslot),
    timeslot
  )
    ? { primary: '#00004d', secondary: '#133E7D' }
    : null,
  cssClass: includesTimeslot(
    reservedTimeslots.map((s) => s.timeslot),
    timeslot
  )
    ? 'calendar-event-reserved'
    : '',
});

  suggestedTimeslotToCalendarEvent(timeslot: Timeslot, currentLang: string) {
    const calendarEvent = this.timeslotToCalendarEvent(timeslot, currentLang);

    calendarEvent.cssClass = (calendarEvent.cssClass || '') + " calendar-event-suggestion"
    return calendarEvent;
  }
}
