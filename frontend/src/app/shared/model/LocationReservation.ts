import {User, UserConstructor} from './User';
import {Location, LocationConstructor} from './Location';
import {CustomDate, CustomDateConstructor} from './helpers/CustomDate';

export interface LocationReservation {
  user: User;
  location: Location;
  date: CustomDate;
  attended: boolean;
}

export class LocationReservationConstructor {
  static new(): LocationReservation {
    return {
      user: UserConstructor.new(),
      location: LocationConstructor.new(),
      date: CustomDateConstructor.new(),
      attended: false
    };
  }

  static newFromObj(obj: LocationReservation): LocationReservation {
    if (obj === null) {
      return null;
    }

    return {
      user: UserConstructor.newFromObj(obj.user),
      location: LocationConstructor.newFromObj(obj.location),
      date: CustomDateConstructor.newFromObj(obj.date),
      attended: obj.attended
    };
  }
}
