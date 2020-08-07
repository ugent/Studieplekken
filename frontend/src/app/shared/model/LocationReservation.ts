import {User} from './User';
import {Location} from './Location';
import {CustomDate} from './helpers/CustomDate';

export class LocationReservation {
  user: User;
  location: Location;
  date: CustomDate;
  attended: boolean;
}
