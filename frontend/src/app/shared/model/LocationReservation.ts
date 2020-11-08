import {User, UserConstructor} from './User';
import { Timeslot } from './Timeslot';

export interface LocationReservation {
  user: User;
  timeslot: Timeslot;
  attended?: boolean;
  createdAt?: string;
}

export class LocationReservationConstructor {
  static new(): LocationReservation {
    return {
      user: UserConstructor.new(),
      attended: false,
      createdAt: '',
      timeslot: null
    };
  }

  static newFromObj(obj: LocationReservation): LocationReservation {
    if (obj === null) {
      return null;
    }

    return {
      user: UserConstructor.newFromObj(obj.user),
      attended: obj.attended,
      timeslot: obj.timeslot,
      createdAt: obj.createdAt
    };
  }
}
