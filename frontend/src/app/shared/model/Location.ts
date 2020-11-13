import {LocationTag} from './LocationTag';
import {Authority, AuthorityConstructor} from './Authority';
import {Building, BuildingConstructor} from './Building';

export interface Location {
  name: string;
  numberOfSeats: number;
  numberOfLockers: number;
  forGroup: boolean;
  imageUrl: string;
  authority: Authority;
  building: Building;
  descriptionDutch: string;
  descriptionEnglish: string;
  assignedTags: LocationTag[];
}

export class LocationConstructor {
  static new(): Location {
    return {
      name: '',
      numberOfSeats: 0,
      numberOfLockers: 0,
      forGroup: false,
      imageUrl: '',
      authority: AuthorityConstructor.new(),
      building: BuildingConstructor.new(),
      descriptionDutch: '',
      descriptionEnglish: '',
      assignedTags: []
    };
  }

  static newFromObj(obj: Location): Location {
    if (obj === null) {
      return null;
    }

    return {
      name: obj.name,
      numberOfSeats: obj.numberOfSeats,
      numberOfLockers: obj.numberOfLockers,
      imageUrl: obj.imageUrl,
      authority: AuthorityConstructor.newFromObj(obj.authority),
      building: BuildingConstructor.newFromObj(obj.building),
      descriptionDutch: obj.descriptionDutch,
      descriptionEnglish: obj.descriptionEnglish,
      assignedTags: obj.assignedTags,
      forGroup: obj.forGroup,
    };
  }
}
