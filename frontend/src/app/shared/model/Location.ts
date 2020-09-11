import {LocationTag} from './LocationTag';
import {Authority, AuthorityConstructor} from './Authority';

export interface Location {
  name: string;
  address: string;
  numberOfSeats: number;
  numberOfLockers: number;
  imageUrl: string;
  authority: Authority;
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
      authority: AuthorityConstructor.new(),
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
      authority: AuthorityConstructor.newFromObj(obj.authority),
      descriptionDutch: obj.descriptionDutch,
      descriptionEnglish: obj.descriptionEnglish,
      tags: obj.tags
    };
  }
}
