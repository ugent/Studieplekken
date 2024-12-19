import { User, UserConstructor } from './User';
import { Timeslot } from './Timeslot';
import { Moment } from 'moment';
import { Location, LocationConstructor } from './Location';
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
        public state: LocationReservationState = LocationReservationState.APPROVED,
        public createdAt: Moment = moment(),
        public location?: Location
    ) {
    }

    static fromJSON(json: LocationReservation): LocationReservation {
        if (!json) {
            return null;
        }
        return new LocationReservation(
            UserConstructor.newFromObj(json.user),
            Timeslot.fromJSON(json.timeslot),
            json.state,
            json.createdAt,
            LocationConstructor.newFromObj(json.location)
        );
    }

    public isApproved(): boolean {
        return this.state === LocationReservationState.APPROVED;
    }

    public isRejected(): boolean {
        return this.state === LocationReservationState.REJECTED;
    }

    public isAbsent(): boolean {
        return this.state === LocationReservationState.ABSENT;
    }

    public isPresent(): boolean {
        return this.state === LocationReservationState.PRESENT;
    }

    public isDeleted(): boolean {
        return this.state === LocationReservationState.DELETED;
    }

    public isPending(): boolean {
        return this.state === LocationReservationState.PENDING;
    }

    public isAccepted(): boolean {
        return this.isApproved() || this.isAbsent() || this.isPresent();
    }

    public isCanceled(): boolean {
        return this.isRejected() || this.isDeleted();
    }

    public isCommitted(): boolean {
        return this.isAbsent() || this.isPresent() || this.timeslot.isInPast();
    }
}
