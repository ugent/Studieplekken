import * as moment from 'moment';
import { CalendarPeriod } from './CalendarPeriod';

export interface Timeslot {
    timeslotSeqnr: number;
    timeslotDate: string;
    calendarId: number;
}


export function timeslotStartHour(calendarPeriod: CalendarPeriod, seqnr: number): string {
    const currentTime = moment(calendarPeriod.openingTime, 'HH:mm');

    currentTime.add(calendarPeriod.reservableTimeslotSize * seqnr, 'minutes');
    return currentTime.format('HH:mm');
}

export function timeslotEndHour(calendarPeriod: CalendarPeriod, seqnr: number): string {
    const currentTime = moment(calendarPeriod.openingTime, 'HH:mm');

    currentTime.add(calendarPeriod.reservableTimeslotSize * (seqnr + 1), 'minutes');
    return currentTime.format('HH:mm');
}

export const getTimeslotsOnDay: (calendarPeriod: CalendarPeriod, date: string) => Timeslot[] =
                     (calendarPeriod, date) => calendarPeriod.timeslots.filter(ts => ts.timeslotDate === date);

export const timeslotEquals: (t1: Timeslot, t2: Timeslot) => boolean = (t1, t2) =>
                                             t1.timeslotSeqnr === t2.timeslotSeqnr
                                             && t1.calendarId === t2.calendarId
                                              && t1.timeslotDate === t2.timeslotDate;

export const includesTimeslot: (l: Timeslot[], t: Timeslot) => boolean = (l, t) => l.some(lt => timeslotEquals(lt, t));
