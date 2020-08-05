import {CustomDate} from './helpers/CustomDate';

export class Penalty {
  augentID: string;
  eventCode: number;
  timestamp: CustomDate;
  reservationDate: CustomDate;
  reservationLocation: string;
  receivedPoints: number;
}
