import {ILocation} from './ILocation';
import {ILockerReservation} from './ILockerReservation';

export interface ILocker {
  id: string;
  number: number;
  location: string;
  studentLimit: number;
}
