import {LocationTag} from './LocationTag';

export interface Location {
  name: string;
  address: string;
  numberOfSeats: number;
  numberOfLockers: number;
  imageUrl: string;
  authorityId: number;
  descriptionDutch: string;
  descriptionEnglish: string;

  tags: LocationTag[];
}

export class LocationConstructor {
  static new(): Location {
    return {
      name: '',
      address: '',
      numberOfSeats: 0,
      numberOfLockers: 0,
      imageUrl: '',
      authorityId: 0,
      descriptionDutch: '',
      descriptionEnglish: '',
      tags: []
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
      imageUrl: obj.imageUrl,
      authorityId: obj.authorityId,
      descriptionDutch: obj.descriptionDutch,
      descriptionEnglish: obj.descriptionEnglish,
      tags: obj.tags
    };
  }
}
