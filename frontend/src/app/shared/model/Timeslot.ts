import * as moment from 'moment';
import { CalendarPeriod } from './CalendarPeriod';

export interface Timeslot {
    timeslotSeqnr: number;
    timeslotDate: string;
    calendarId: number;
}


export function timeslotStartHour(calendarPeriod: CalendarPeriod, seqnr: number): string {
    const currentTime = moment(calendarPeriod.openingTime, 'hh:mm');

    currentTime.add(calendarPeriod.reservableTimeslotSize * seqnr, 'minutes');
    return currentTime.format('hh:mm');
}

export function timeslotEndHour(calendarPeriod: CalendarPeriod, seqnr: number): string {
    const currentTime = moment(calendarPeriod.openingTime, 'hh:mm');

    currentTime.add(calendarPeriod.reservableTimeslotSize * (seqnr + 1), 'minutes');
    return currentTime.format('hh:mm');
}

export const getTimeslotsOnDay: (calendarPeriod: CalendarPeriod, date: string) => Timeslot[] =
                     (calendarPeriod, date) => calendarPeriod.timeslots.filter(ts => ts.timeslotDate === date);
