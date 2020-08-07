import {User} from './User';
import {Locker} from './Locker';
import {CustomDate} from './helpers/CustomDate';

export class LockerReservation {
  user: User;
  locker: Locker;
  keyPickupDate: CustomDate;
  keyReturnDate: CustomDate;
}
