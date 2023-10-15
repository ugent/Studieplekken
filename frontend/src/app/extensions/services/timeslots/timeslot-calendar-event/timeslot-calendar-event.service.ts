import {Injectable} from '@angular/core';
import {CalendarEvent} from 'angular-calendar';
import * as moment from 'moment';
import {LocationReservation} from 'src/app/model/LocationReservation';
import {Timeslot} from 'src/app/model/Timeslot';

export type TimeslotCalendarEvent = CalendarEvent<{
    timeslot?: Timeslot
}>;
@Injectable({
    providedIn: 'root'
})
export class TimeslotCalendarEventService {
    clicked: boolean;
    initial: boolean;

    constructor() {
        this.clicked = true;
        this.initial = true;
    }

    timeslotToCalendarEvent(timeslot: Timeslot, currentLang: string, locationReservations: LocationReservation[] = []): CalendarEvent {
        if (!timeslot.reservable) {
            return this.nonReservableToCalendarEvent(timeslot, currentLang);
        }

        if (timeslot.areReservationsLocked()) {
            return this.notYetReservableTimeslotToCalendarEvent(timeslot, currentLang);
        }

        return this.reservableTimeslotToCalendarEvent(timeslot, currentLang, locationReservations);
    }

    private nonReservableToCalendarEvent: (timeslot: Timeslot, currentLang: string) => CalendarEvent<{
        timeslot: Timeslot
    }> =
        (timeslot, currentLang) =>
            ({
                title: currentLang === 'nl' ?
                    `Geen reservatie nodig <br> ${timeslot.openingHour.format('HH:mm')} - ${timeslot.closingHour.format('HH:mm')}`
                    : `Requires no reservation <br> ${timeslot.openingHour.format('HH:mm')} - ${timeslot.closingHour.format('HH:mm')}`,
                start: timeslot.getStartMoment().toDate(),
                end: timeslot.getEndMoment().toDate(),
                meta: {
                    timeslot
                },
                cssClass: 'event not-reservable',
            })

    private notYetReservableTimeslotToCalendarEvent: (timeslot: Timeslot, currentLang: string) => CalendarEvent<{
        timeslot: Timeslot
    }> =
        (timeslot, currentLang) =>
            ({
                title: currentLang === 'nl' ?
                    `${timeslot.openingHour.format('HH:mm')} - ${timeslot.closingHour.format('HH:mm')} <br> Reserveren vanaf ${timeslot.reservableFrom.format('DD/MM/YYYY HH:mm')}`
                    : `${timeslot.openingHour.format('HH:mm')} - ${timeslot.closingHour.format('HH:mm')} <br> Reservable from ${timeslot.reservableFrom.format('DD/MM/YYYY HH:mm')}`,
                start: timeslot.getStartMoment().toDate(),
                end: timeslot.getEndMoment().toDate(),
                meta: {timeslot},
                cssClass: 'event not-reservable',
            })

    private reservableTimeslotToCalendarEvent(
        timeslot: Timeslot,
        currentLang: string,
        reservedTimeslots: LocationReservation[]
    ): CalendarEvent<{ timeslot: Timeslot }> {
        const reservation = reservedTimeslots.find(
            t => t.timeslot.timeslotSequenceNumber === timeslot.timeslotSequenceNumber
        );

        const isRandomReservationMoment = timeslot.amountOfReservations === 0 && timeslot.reservableFrom.isAfter(
            moment().subtract(10, 'minutes')
        );

        const randomMomentTitle =
            currentLang === 'en' ? 'Reservation queue is open' : 'Reservatiewachtlijn is open';

        let cssClass = 'event ';

        if (reservation) {
            if (!reservation.state) {
                cssClass += 'selected';
            } else {
                cssClass += reservation.state.toLowerCase();
            }
        }

        return {
            title: isRandomReservationMoment ? randomMomentTitle : `${timeslot.amountOfReservations} / ${timeslot.seatCount}`,
            start: timeslot.getStartMoment().toDate(),
            end: timeslot.getEndMoment().toDate(),
            meta: {
                timeslot
            },
            cssClass
        };
    }

    public suggestedTimeslotToCalendarEvent(timeslot: Timeslot, currentLang: string): CalendarEvent<{ timeslot: Timeslot }> {
        const calendarEvent = this.timeslotToCalendarEvent(timeslot, currentLang);

        calendarEvent.cssClass = (calendarEvent.cssClass || '') + ' calendar-event-suggestion';
        return calendarEvent;
    }
}
