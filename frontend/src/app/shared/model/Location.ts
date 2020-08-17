export interface Location {
  name: string;
  address: string;
  numberOfSeats: number;
  numberOfLockers: number;
  imageUrl: string;
}

export class LocationConstructor {
  static new(): Location {
    return {
      name: '',
      address: '',
      numberOfSeats: 0,
      numberOfLockers: 0,
      imageUrl: ''
    };
  }

  static newFromObj(obj: Location): Location {
    if (obj === null) {
      return null;
    }

    return {
      name: obj.name,
      address: obj.address,
      numberOfSeats: obj.numberOfSeats,
      numberOfLockers: obj.numberOfLockers,
      imageUrl: obj.imageUrl
    };
  }
}
