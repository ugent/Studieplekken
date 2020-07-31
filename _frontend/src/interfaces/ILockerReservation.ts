import {IUser} from './IUser';
import {ILocker} from './ILocker';
import {IDate} from './IDate';

export interface ILockerReservation {
  owner: IUser;
  locker: ILocker;
  keyPickedUp: boolean;
  keyBroughtBack: boolean;
  startDate: IDate;
  endDate: IDate;
}
