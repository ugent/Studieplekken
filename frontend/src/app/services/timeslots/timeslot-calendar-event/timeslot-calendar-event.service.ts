import { Injectable } from '@angular/core';
import { CalendarEvent } from 'angular-calendar';
import * as moment from 'moment';
import { LocationReservation, LocationReservationState } from 'src/app/shared/model/LocationReservation';
import { Timeslot, includesTimeslot } from 'src/app/shared/model/Timeslot';

@Injectable({
  providedIn: 'root'
})

export class TimeslotCalendarEventService {
  clicked:boolean;
  initial:boolean;
  constructor() {
    this.clicked = true;
    this.initial = true;
  }


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

private reservableTimeslotToCalendarEvent(timeslot: Timeslot, currentLang: string, reservedTimeslots: LocationReservation[]):CalendarEvent<{ timeslot: Timeslot }> {

  const locationFull = timeslot.amountOfReservations >= timeslot.seatCount;
  const reservation = reservedTimeslots.find(t => t.timeslot.timeslotSequenceNumber === timeslot.timeslotSequenceNumber);
  const includedTimeSlot = includesTimeslot(
    reservedTimeslots.map((s) => s.timeslot),
    timeslot
  ) && reservation && reservation.state !== LocationReservationState.DELETED;
  return ({
    title: `${timeslot.amountOfReservations} / ${timeslot.seatCount}`,
    start: timeslot.getStartMoment().toDate(),
    end: timeslot.getEndMoment().toDate(),
    meta: { timeslot },
    color: includedTimeSlot
      // reserved -> dark blue, reserved full -> dark red
      ? (!locationFull ?

        ( reservation.state === LocationReservationState.PENDING ? { primary: '#F5512C', secondary: '#FC8A17' } :
        (reservation.state === LocationReservationState.REJECTED ? { primary: '#c53726', secondary: '#ed9987' } : { primary: '#00004d', secondary: '#133E7D' }) )
        :
        { primary: '#00004d', secondary: '#133E7D' })
      // unselected light pink, unselected light blue
      : (locationFull ? { primary: '#c53726', secondary: '#f4ded9' } : null),
    cssClass: includedTimeSlot
      ? 'calendar-event-reserved'
      : (locationFull ? 'calendar-event-full-not-reserved' : 'blue-text'),
  });
}

public suggestedTimeslotToCalendarEvent(timeslot: Timeslot, currentLang: string) {
    const calendarEvent = this.timeslotToCalendarEvent(timeslot, currentLang);

    calendarEvent.cssClass = (calendarEvent.cssClass || '') + " calendar-event-suggestion"
    return calendarEvent;
  }
}
