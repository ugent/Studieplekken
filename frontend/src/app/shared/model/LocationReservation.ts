import { User } from './User';
import { Timeslot } from './Timeslot';
import { Moment } from 'moment';
import * as moment from 'moment';

export class LocationReservation {
  user: User;
  timeslot: Timeslot;
  attended?: boolean;
  createdAt?: Moment;

  constructor(
    user: User,
    timeslot: Timeslot,
    attended?: boolean,
    createdAt?: Moment
  ) {
    this.user = user;
    this.timeslot = timeslot;
    this.attended = attended;
    this.createdAt = createdAt;
  }

  static fromJSON(json: LocationReservation): LocationReservation {
    return new LocationReservation(
      json.user,
      Timeslot.fromJSON(json.timeslot),
      json.attended,
      json.createdAt ? moment(json.createdAt) : null
    );
  }
}
