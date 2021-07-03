import * as moment from 'moment';
import { Moment } from 'moment';

export interface Penalty {
  userId: string;
  eventCode: number;
  timestamp: Moment;
  reservationDate: Moment;
  reservationLocationId: number;
  receivedPoints: number;
  remarks: string;
}

export class PenaltyConstructor {
  static new(): Penalty {
    return {
      userId: '',
      eventCode: 0,
      timestamp: moment(),
      reservationDate: moment(),
      reservationLocationId: -1,
      receivedPoints: 0,
      remarks: '',
    };
  }

  static newFromObj(obj: Penalty): Penalty {
    if (obj === null) {
      return null;
    }

    return {
      userId: obj.userId,
      eventCode: obj.eventCode,
      timestamp: moment(obj.timestamp),
      reservationDate: moment(obj.reservationDate),
      reservationLocationId: obj.reservationLocationId,
      receivedPoints: obj.receivedPoints,
      remarks: obj.remarks,
    };
  }
}
