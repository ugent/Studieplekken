import {Location, LocationConstructor} from './Location';

export interface Locker {
  number: number;
  location: Location;
}

export class LockerConstructor {
  static new(): Locker {
    return {
      number: 0,
      location: LocationConstructor.new()
    };
  }

  static newFromObj(obj: {number: number, location: Location}): Locker {
    if (obj === null) {
      return null;
    }

    return {
      number: obj.number,
      location: LocationConstructor.newFromObj(obj.location)
    };
  }
}
