import {CustomDate, CustomDateConstructor} from './helpers/CustomDate';

export interface Penalty {
  augentID: string;
  eventCode: number;
  timestamp: CustomDate;
  reservationDate: CustomDate;
  reservationLocation: string;
  receivedPoints: number;
  remarks: string;
}

export class PenaltyConstructor {
  static new(): Penalty {
    return {
      augentID: '',
      eventCode: 0,
      timestamp: CustomDateConstructor.new(),
      reservationDate: CustomDateConstructor.new(),
      reservationLocation: '',
      receivedPoints: 0,
      remarks: ''
    };
  }

  static newFromObj(obj: Penalty): Penalty {
    if (obj === null) {
      return null;
    }

    return {
      augentID: obj.augentID,
      eventCode: obj.eventCode,
      timestamp: CustomDateConstructor.newFromObj(obj.timestamp),
      reservationDate: CustomDateConstructor.newFromObj(obj.reservationDate),
      reservationLocation: obj.reservationLocation,
      receivedPoints: obj.receivedPoints,
      remarks: obj.remarks
    };
  }
}
