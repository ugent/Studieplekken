import { User } from './User';
import { Timeslot } from './Timeslot';
import { Moment } from 'moment';
import * as moment from 'moment';

export enum LocationReservationState {
  PENDING = "PENDING",
  REJECTED = "REJECTED",
  APPROVED = "APPROVED",
  PRESENT = "PRESENT",
  ABSENT = "ABSENT"
};

export class LocationReservation {
  user: User;
  timeslot: Timeslot;
  state: LocationReservationState;
  createdAt?: Moment;

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

  static fromJSON(json: LocationReservation): LocationReservation {
    return new LocationReservation(
      json.user,
      Timeslot.fromJSON(json.timeslot),
      json.state,
      json.createdAt ? moment(json.createdAt) : null
    );
  }
}
