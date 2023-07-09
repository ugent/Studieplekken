import {Injectable} from '@angular/core';
import {CalendarEvent} from 'angular-calendar';
import * as moment from 'moment';
import {LocationReservation, LocationReservationState} from 'src/app/extensions/model/LocationReservation';
import {includesTimeslot, Timeslot} from 'src/app/extensions/model/Timeslot';

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


    timeslotToCalendarEvent = (timeslot: Timeslot, currentLang: string, locationReservations: LocationReservation[] = []) =>
        !timeslot.reservable ? this.nonReservableToCalendarEvent(timeslot, currentLang) :
            timeslot.areReservationsLocked() ? this.notYetReservableTimeslotToCalendarEvent(timeslot, currentLang) :
                this.reservableTimeslotToCalendarEvent(timeslot, currentLang, locationReservations)

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
                meta: {timeslot},
                color: {primary: 'black', secondary: '#BEBEBE'},
                cssClass: 'calendar-event-NR',
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
                color: {primary: 'black', secondary: '#BEBEBE'},
                cssClass: 'calendar-event-NR',
            })

    private reservableTimeslotToCalendarEvent(timeslot: Timeslot, currentLang: string, reservedTimeslots: LocationReservation[]): CalendarEvent<{
        timeslot: Timeslot
    }> {

        const locationFull = timeslot.amountOfReservations >= timeslot.seatCount;
        const reservation = reservedTimeslots.find(t => t.timeslot.timeslotSequenceNumber === timeslot.timeslotSequenceNumber);
        const includedTimeSlot = includesTimeslot(
            reservedTimeslots.map((s) => s.timeslot),
            timeslot
        ) && reservation && reservation.state !== LocationReservationState.DELETED;

        const isRandomReservationMoment = timeslot.amountOfReservations === 0 && timeslot.reservableFrom.isAfter(moment().subtract(10, 'minutes'));
        const randomMomentTitle = currentLang === 'en' ? 'Reservation queue is open' : 'Reservatiewachtlijn is open';

        let color;

        if (includedTimeSlot) {
            if (!locationFull) {
                // Pending reservation colors.
                if (reservation.state === LocationReservationState.PENDING) {
                    color = {
                        primary: 'white',
                        secondary: '#feeee6'
                    };
                // Approved reservation colors.
                } else if (reservation.state !== LocationReservationState.REJECTED) {
                    color = {
                        primary: 'white',
                        secondary: '#007db3'
                    };
                }
            } else {
                // Location is full, therefore the reservation was rejected.
                if (reservation.state === LocationReservationState.REJECTED) {
                    color = {
                        primary: '#c53726',
                        secondary: '#ed9987'
                    };
                // Location is full, but the reservation is still pending.
                } else if (reservation.state === LocationReservationState.PENDING) {
                    color = {
                        primary: 'white',
                        secondary: '#feeee6'
                    };
                } else {
                    color = {
                        primary: 'white',
                        secondary: '#feeee6'
                    };
                }
            }
        } else if (locationFull) {
            color = {
                primary: '#c53726',
                secondary: '#f4ded9'
            };
        }


        return ({
            title: isRandomReservationMoment ? randomMomentTitle : `${timeslot.amountOfReservations} / ${timeslot.seatCount}`,
            start: timeslot.getStartMoment().toDate(),
            end: timeslot.getEndMoment().toDate(),
            meta: {timeslot},
            color,
            cssClass: reservation && reservation.isAccepted() ? 'accepted' : ''
        });
    }

    public suggestedTimeslotToCalendarEvent(timeslot: Timeslot, currentLang: string): CalendarEvent<{ timeslot: Timeslot }> {
        const calendarEvent = this.timeslotToCalendarEvent(timeslot, currentLang);

        calendarEvent.cssClass = (calendarEvent.cssClass || '') + ' calendar-event-suggestion';
        return calendarEvent;
    }
}
