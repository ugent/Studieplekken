import {User, UserConstructor} from './User';
import {Timeslot} from './Timeslot';
import {Moment} from 'moment';
import * as moment from 'moment';
import {LocationReservationsService} from "../services/api/location-reservations/location-reservations.service";

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
        user: User,
        timeslot: Timeslot,
        state: string,
        createdAt?: Moment
    ) {
        this.user = user;
        this.timeslot = timeslot;
        this.state = state as LocationReservationState;
        this.createdAt = createdAt;
    }
    user: User;
    timeslot: Timeslot;
    state: LocationReservationState;
    createdAt?: Moment;

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
}
