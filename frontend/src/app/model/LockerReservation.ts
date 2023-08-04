import { User, UserConstructor } from './User';
import { Locker, LockerConstructor } from './Locker';
import { Moment } from 'moment';
import * as moment from 'moment';

export interface LockerReservation {
  owner: User;
  locker: Locker;
  keyPickupDate: Moment;
  keyReturnedDate: Moment;
}

export class LockerReservationConstructor {
  static new(): LockerReservation {
    return {
      owner: UserConstructor.new(),
      locker: LockerConstructor.new(),
      keyPickupDate: moment(),
      keyReturnedDate: moment(),
    };
  }

  static newFromObj(obj: LockerReservation): LockerReservation {
    if (obj === null) {
      return null;
    }

    return {
      owner: UserConstructor.newFromObj(obj.owner),
      locker: LockerConstructor.newFromObj(obj.locker),
      keyPickupDate: moment(obj.keyPickupDate),
      keyReturnedDate: moment(obj.keyReturnedDate),
    };
  }
}
