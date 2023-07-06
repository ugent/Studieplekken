import * as moment from 'moment';
import { Moment } from 'moment';

export interface LocationStat {
  locationId: number;
  locationName: string;
  open: boolean;
  reservable: boolean;
  numberOfSeats: number;
  numberOfTakenSeats: number;
  timeslotDate: Moment;
}

export class LocationStatConstructor {
  static new(): LocationStat {
    return {
      locationId: -1,
      locationName: '',
      open: false,
      reservable: false,
      numberOfSeats: 0,
      numberOfTakenSeats: 0,
      timeslotDate: null
    };
  }

  static newFromObj(obj: LocationStat): LocationStat {
    if (obj === null) {
      return null;
    }

    return {
      locationId: obj.locationId,
      locationName: obj.locationName,
      open: obj.open,
      reservable: obj.reservable,
      numberOfSeats: obj.numberOfSeats,
      numberOfTakenSeats: obj.numberOfTakenSeats,
      timeslotDate: obj.timeslotDate ? moment(obj.timeslotDate) : null
    };
  }
}
