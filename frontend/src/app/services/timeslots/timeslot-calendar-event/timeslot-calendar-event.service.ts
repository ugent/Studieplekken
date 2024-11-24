import {Injectable} from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
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
    public clicked: boolean;
    public initial: boolean;

    constructor(
        private translate: TranslateService
    ) {
        this.clicked = true;
        this.initial = true;
    }

    /**
     * Converts a given timeslot into a calendar event.
     * 
     * Depending on the state of the timeslot, it will be converted into one of the following:
     * - A non-reservable calendar event if the timeslot is not reservable.
     * - A not-yet-reservable calendar event if the timeslot's reservations are locked.
     * - A reservable calendar event if the timeslot is reservable.
     * 
     * @param timeslot - The timeslot to be converted.
     * @param currentLang - The current language to be used for the calendar event.
     * @param locationReservations - An optional array of location reservations associated with the timeslot.
     * @returns The converted calendar event.
     */
    public timeslotToCalendarEvent(timeslot: Timeslot, currentLang: string, locationReservations: LocationReservation[] = []): CalendarEvent {
        if (!timeslot.reservable) {
            return this.nonReservableToCalendarEvent(timeslot, currentLang);
        }

        if (timeslot.areReservationsLocked()) {
            return this.notYetReservableTimeslotToCalendarEvent(timeslot, currentLang);
        }

        return this.reservableTimeslotToCalendarEvent(
            timeslot,
            currentLang,
            locationReservations
        );
    }

    /**
     * Converts a non-reservable timeslot into a calendar event object.
     *
     * @param timeslot - The timeslot object containing the opening and closing hours.
     * @param currentLang - The current language for translation purposes.
     * @returns An object representing the calendar event with translated title, start and end times, CSS class, and metadata.
     */
    private nonReservableToCalendarEvent(timeslot: Timeslot, currentLang: string) {
        return {
            title: this.translate.instant('dashboard.locationDetails.calendar.timeslot.freeAdmission', {
                openingHour: timeslot.openingHour.format('HH:mm'),
                closingHour: timeslot.closingHour.format('HH:mm')
            }),
            start: timeslot.getStartMoment().toDate(),
            end: timeslot.getEndMoment().toDate(),
            cssClass: 'event not-reservable',
            meta: { timeslot },
        };
    }

    /**
     * Converts a not-yet-reservable timeslot into a calendar event.
     *
     * @param timeslot - The timeslot to convert.
     * @param currentLang - The current language code ('nl' for Dutch, other values for English).
     * @returns A CalendarEvent object containing the timeslot details.
     */
    private notYetReservableTimeslotToCalendarEvent(timeslot: Timeslot, currentLang: string): CalendarEvent<{ timeslot: Timeslot }> {
        return ({
            title: this.translate.instant('dashboard.locationDetails.calendar.timeslot.reservableFrom', {
                openingHour: timeslot.openingHour.format('HH:mm'),
                closingHour: timeslot.closingHour.format('HH:mm'),
                reservableFrom: timeslot.reservableFrom.format('DD/MM/YYYY HH:mm')
            }),
            start: timeslot.getStartMoment().toDate(),
            end: timeslot.getEndMoment().toDate(),
            meta: {timeslot},
            cssClass: 'event not-reservable',
        })
    }

    /**
     * Converts a reservable timeslot into a calendar event.
     *
     * @param timeslot - The timeslot to convert.
     * @param currentLang - The current language for localization.
     * @param reservedTimeslots - A list of reserved timeslots.
     * @returns A CalendarEvent object containing the timeslot details.
     */
    private reservableTimeslotToCalendarEvent(timeslot: Timeslot, currentLang: string, reservedTimeslots: LocationReservation[]): CalendarEvent<{ timeslot: Timeslot }> {
        const reservation = reservedTimeslots.find(
            t => t.timeslot.timeslotSequenceNumber === timeslot.timeslotSequenceNumber
        );

        const isRandomReservationMoment = timeslot.amountOfReservations === 0 && timeslot.reservableFrom.isAfter(
            moment().subtract(10, 'minutes')
        );

        const randomMomentTitle = currentLang === 'en' ? 'Reservation queue is open' : 'Reservatiewachtlijn is open';

        let cssClass = 'event ';

        if (reservation !== null) {            
            if (reservation.state === null || reservation.state === undefined) {
                cssClass += 'selected';
            } else {
                cssClass += reservation.state.toLowerCase();
            }
        }

        return {
            title: isRandomReservationMoment ? randomMomentTitle : `${timeslot.amountOfReservations} / ${timeslot.seatCount}`,
            start: timeslot.getStartMoment().toDate(),
            end: timeslot.getEndMoment().toDate(),
            meta: { timeslot },
            cssClass
        };
    }

    /**
     * Converts a suggested timeslot to a calendar event and adds a specific CSS class to indicate it is a suggestion.
     *
     * @param timeslot - The timeslot to be converted into a calendar event.
     * @param currentLang - The current language to be used for localization.
     * @returns A CalendarEvent object with the timeslot data and an additional CSS class for suggestions.
     */
    public suggestedTimeslotToCalendarEvent(timeslot: Timeslot, currentLang: string): CalendarEvent<{ timeslot: Timeslot }> {
        const calendarEvent = this.timeslotToCalendarEvent(timeslot, currentLang);
        calendarEvent.cssClass = (calendarEvent.cssClass || '') + ' calendar-event-suggestion';
        return calendarEvent;
    }
}
