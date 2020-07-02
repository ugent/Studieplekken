import {IUser} from './IUser';
import {ILocation} from './ILocation';
import {IDate} from './IDate';

export interface ILocationReservation {
  user: IUser;
  location: ILocation;
  date: IDate;
  attended: boolean
}
