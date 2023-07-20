import {User, UserConstructor} from './User';
import {Timeslot, timeslotEquals} from './Timeslot';
import {Moment} from 'moment';
import * as moment from 'moment';

export enum LocationReservationState {
    PENDING = 'PENDING',
    REJECTED = 'REJECTED',
    APPROVED = 'APPROVED',
    PRESENT = 'PRESENT',
    ABSENT = 'ABSENT',
    DELETED = 'DELETED'
}

export class LocationReservation {

    constructor(
        public user: User,
        public timeslot: Timeslot,
        public state?: LocationReservationState,
        public createdAt?: Moment
    ) {
    }

    static fromJSON(json: LocationReservation): LocationReservation {
        return new LocationReservation(
            UserConstructor.newFromObj(json.user),
            Timeslot.fromJSON(json.timeslot),
            json.state,
            json.createdAt ? moment(json.createdAt) : null
        );
    }

    /**
     * Check whether the reservation has been accepted.
     */
    public isAccepted(): boolean {
        return this.state !== LocationReservationState.PENDING && this.state !== LocationReservationState.REJECTED;
    }

    /**
     * Check whether the reservation has been canceled.
     */
    public isCanceled(): boolean {
        return this.state === LocationReservationState.REJECTED || this.state === LocationReservationState.DELETED;
    }

    public equals(other: LocationReservation): boolean {
        return timeslotEquals(this.timeslot, other.timeslot) && this.state === other.state;
    }
}
