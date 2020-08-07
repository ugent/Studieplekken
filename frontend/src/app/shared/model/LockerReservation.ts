import {User, UserConstructor} from './User';
import {Locker, LockerConstructor} from './Locker';
import {CustomDate, CustomDateConstructor} from './helpers/CustomDate';

export interface LockerReservation {
  owner: User;
  locker: Locker;
  keyPickupDate: CustomDate;
  keyReturnedDate: CustomDate;
}

export class LockerReservationConstructor {
  static new(): LockerReservation {
    return {
      owner: UserConstructor.new(),
      locker: LockerConstructor.new(),
      keyPickupDate: CustomDateConstructor.new(),
      keyReturnedDate: CustomDateConstructor.new()
    };
  }

  static newFromObj(obj: LockerReservation): LockerReservation {
    if (obj === null) {
      return null;
    }

    return {
      owner: UserConstructor.newFromObj(obj.owner),
      locker: LockerConstructor.newFromObj(obj.locker),
      keyPickupDate: CustomDateConstructor.newFromObj(obj.keyPickupDate),
      keyReturnedDate: CustomDateConstructor.newFromObj(obj.keyReturnedDate)
    };
  }
}
