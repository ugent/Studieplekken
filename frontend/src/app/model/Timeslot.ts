import * as moment from 'moment';
import {Moment} from 'moment';

export class Timeslot {
    constructor(
        public timeslotSequenceNumber: number = 0,
        public timeslotDate: Moment = moment(),
        public amountOfReservations: number = 0,
        public seatCount: number = 0,
        public reservable: boolean = false,
        public reservableFrom: Moment = null,
        public locationId: number = 0,
        public openingHour: Moment = null,
        public closingHour: Moment = null,
        public timeslotGroup?: number,
        public repeatable: boolean = false
    ) {
    }

    static fromJSON(json: Record<string, any>): Timeslot {
        if (!json) {
            return null;
        }
        const t = new Timeslot(
            json.timeslotSeqnr,
            moment(json.timeslotDate),
            json.amountOfReservations,
            json.seatCount,
            json.reservable,
            moment(json.reservableFrom),
            json.locationId,
            moment(json.openingHour, 'HH:mm:ss'),
            moment(json.closingHour, 'HH:mm:ss'),
            json.timeslotGroup,
            json.repeatable
        );

        t.sanitize();
        return t;
    }

    toJSON(): Record<string, unknown> {
        return {
            timeslotSeqnr: this.timeslotSequenceNumber,
            timeslotDate: this.timeslotDate.format('YYYY-MM-DD'),
            seatCount: this.seatCount,
            openingHour: this.openingHour.format('HH:mm'),
            closingHour: this.closingHour.format('HH:mm'),
            reservable: this.reservable,
            reservableFrom: this.reservableFrom ? this.reservableFrom.format('YYYY-MM-DDTHH:mm') : null,
            locationId: this.locationId,
            timeslotGroup: this.timeslotGroup,
            repeatable: this.repeatable
        };
    }

    isValid(): boolean {
        if (!this.timeslotDate || !this.openingHour || !this.closingHour) {
            return false;
        }

        return !(this.reservable && (!this.reservableFrom || !this.reservableFrom.isValid()));
    }

    areReservationsLocked(): boolean {
        return !this.reservableFrom || this.reservableFrom.isAfter(moment());
    }

    getStartMoment(): Moment {
        return moment(
            this.timeslotDate.format('DD-MM-YYYY') +
            ' ' +
            this.openingHour.format('HH:mm'),
            'DD-MM-YYYY HH:mm'
        );
    }

    getEndMoment(): Moment {
        return moment(
            this.timeslotDate.format('DD-MM-YYYY') +
            ' ' +
            this.closingHour.format('HH:mm'),
            'DD-MM-YYYY HH:mm'
        );
    }

    isCurrent(): boolean {
        return this.getStartMoment().isBefore(moment()) && this.getEndMoment().isAfter(moment());
    }

    isInPast(): boolean {
        return this.getEndMoment().isBefore(moment());
    }

    sanitize(): void {
        if (!this.reservableFrom || !this.reservableFrom.isValid()) {
            this.reservableFrom = null;
        }

        if (!this.timeslotDate || !this.timeslotDate.isValid()) {
            this.timeslotDate = null;
        }
    }

    setDate(date: Moment): Timeslot {
        this.timeslotDate = date;
        return this;
    }

    setLocationId(id: number): Timeslot {
        this.locationId = id;
        return this;
    }

    setOpeningHour(hour: Moment): Timeslot {
        this.openingHour = hour;
        return this;
    }
}


export const timeslotEquals: (t1: Timeslot, t2: Timeslot) => boolean = (
    t1,
    t2
) =>
    t1.timeslotSequenceNumber === t2.timeslotSequenceNumber;

export const includesTimeslot: (l: Timeslot[], t: Timeslot) => boolean = (
    l,
    t
) => l.some((lt) => timeslotEquals(lt, t));
