import * as moment from 'moment';
import { Moment } from 'moment';

export class Timeslot {
    constructor(
        public timeslotSequenceNumber: number = 0,
        public timeslotDate: Moment = moment(),
        public amountOfReservations: number = 0,
        public seatCount: number = 10,
        public reservable: boolean = false,
        public reservableFrom: Moment = moment(),
        public locationId: number = 0,
        public openingHour: Moment = moment().hour(8).minute(0),
        public closingHour: Moment = moment().hour(22).minute(0),
        public timeslotGroup?: number,
        public repeatable: boolean = false
    ) {
    }

    /**
     * Creates a new Timeslot instance from a partial Timeslot object.
     * 
     * @param timeslot - A partial object containing properties of a Timeslot.
     * @returns A new Timeslot instance populated with the provided properties.
     */
    public static fromObject(timeslot: Partial<Timeslot>): Timeslot {
        return new Timeslot(
            timeslot.timeslotSequenceNumber,
            moment(timeslot.timeslotDate),
            timeslot.amountOfReservations,
            timeslot.seatCount,
            timeslot.reservable,
            moment(timeslot.reservableFrom),
            timeslot.locationId,
            moment(timeslot.openingHour),
            moment(timeslot.closingHour),
            timeslot.timeslotGroup,
            timeslot.repeatable
        );
    }

    /**
     * Creates an instance of Timeslot from a JSON object.
     *
     * @param json - The JSON object to convert.
     * @returns A new Timeslot instance or null if the input JSON is falsy.
     */
    public static fromJSON(json: Record<string, any>): Timeslot {
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

    /**
     * Converts the Timeslot instance to a JSON object.
     * 
     * @returns {Record<string, unknown>} A JSON representation of the Timeslot instance.
     * 
     * The returned JSON object includes the following properties:
     * - `timeslotSeqnr`: The sequence number of the timeslot.
     * - `timeslotDate`: The date of the timeslot formatted as 'YYYY-MM-DD'.
     * - `seatCount`: The number of seats available in the timeslot.
     * - `openingHour`: The opening hour of the timeslot formatted as 'HH:mm'.
     * - `closingHour`: The closing hour of the timeslot formatted as 'HH:mm'.
     * - `reservable`: A boolean indicating if the timeslot is reservable.
     * - `reservableFrom`: The date and time from which the timeslot is reservable, formatted as 'YYYY-MM-DDTHH:mm', or null if not applicable.
     * - `locationId`: The ID of the location associated with the timeslot.
     * - `timeslotGroup`: The group to which the timeslot belongs.
     * - `repeatable`: A boolean indicating if the timeslot is repeatable.
     */
    public toJSON(): Record<string, unknown> {
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

    /**
     * Checks if the timeslot is valid.
     * 
     * A timeslot is considered valid if:
     * - `timeslotDate`, `openingHour`, and `closingHour` are all defined.
     * - If the timeslot is reservable, `reservableFrom` must be defined and valid.
     * 
     * @returns {boolean} `true` if the timeslot is valid, otherwise `false`.
     */
    public isValid(): boolean {
        if (!this.timeslotDate || !this.openingHour || !this.closingHour) {
            return false;
        }

        return !(this.reservable && (!this.reservableFrom || !this.reservableFrom.isValid()));
    }

    /**
     * Determines if reservations are locked based on the `reservableFrom` property.
     * 
     * @returns {boolean} - Returns `true` if `reservableFrom` is not set or if the current 
     * time is after `reservableFrom`, indicating that reservations are locked.
     * Otherwise, returns `false`.
     */
    public areReservationsLocked(): boolean {
        return !this.reservableFrom || this.reservableFrom.isAfter(moment());
    }

    /**
     * Returns the start moment of the timeslot.
     * 
     * This method combines the date and opening hour of the timeslot
     * to create a Moment object representing the start time.
     * 
     * @returns {Moment} The start moment of the timeslot.
     */
    public getStartMoment(): Moment {
        return moment(
            this.timeslotDate.format('DD-MM-YYYY') +
            ' ' +
            this.openingHour.format('HH:mm'),
            'DD-MM-YYYY HH:mm'
        );
    }

    /**
     * Calculates and returns the end moment of the timeslot.
     * Combines the timeslot date and closing hour to create a Moment object.
     *
     * @returns {Moment} The end moment of the timeslot.
     */
    public getEndMoment(): Moment {
        return moment(
            this.timeslotDate.format('DD-MM-YYYY') +
            ' ' +
            this.closingHour.format('HH:mm'),
            'DD-MM-YYYY HH:mm'
        );
    }

    /**
     * Determines if the current timeslot is active.
     * 
     * This method checks if the current time is between the start and end moments of the timeslot.
     * 
     * @returns {boolean} True if the current time is within the timeslot, otherwise false.
     */
    public isCurrent(): boolean {
        return this.getStartMoment().isBefore(moment()) && this.getEndMoment().isAfter(moment());
    }

    /**
     * Determines if the timeslot is in the past.
     *
     * @returns {boolean} True if the end moment of the timeslot is before the current moment, otherwise false.
     */
    public isInPast(): boolean {
        return this.getEndMoment().isBefore(moment());
    }

    /**
     * Sanitizes the timeslot by setting invalid `reservableFrom` and `timeslotDate` properties to null.
     * 
     * This method checks if the `reservableFrom` and `timeslotDate` properties are valid.
     * If either property is invalid, it sets that property to null.
     * 
     * @remarks
     * This method assumes that the `isValid` method is available on the `reservableFrom` and `timeslotDate` properties.
     */
    public sanitize(): void {
        if (!this.reservableFrom || !this.reservableFrom.isValid()) {
            this.reservableFrom = null;
        }

        if (!this.timeslotDate || !this.timeslotDate.isValid()) {
            this.timeslotDate = null;
        }
    }

    /**
     * Sets the date for the timeslot.
     *
     * @param date - The date to set, represented as a Moment object.
     * @returns The updated Timeslot instance.
     */
    public setDate(date: Moment): Timeslot {
        this.timeslotDate = date;
        return this;
    }

    /**
     * Sets the location ID for the timeslot.
     * 
     * @param id - The ID of the location to be set.
     * @returns The updated Timeslot instance.
     */
    public setLocationId(id: number): Timeslot {
        this.locationId = id;
        return this;
    }

    /**
     * Sets the opening hour for the timeslot.
     *
     * @param hour - The moment object representing the opening hour.
     * @returns The updated Timeslot instance.
     */
    public setOpeningHour(hour: Moment): Timeslot {
        this.openingHour = hour;
        return this;
    }
}

// TODO(EwoutV): incorporate the following functions into the Timeslot class.
export const timeslotEquals: (t1: Timeslot, t2: Timeslot) => boolean = (
    t1,
    t2
) =>
    t1.timeslotSequenceNumber === t2.timeslotSequenceNumber;

export const includesTimeslot: (l: Timeslot[], t: Timeslot) => boolean = (
    l,
    t
) => l.some((lt) => timeslotEquals(lt, t));
