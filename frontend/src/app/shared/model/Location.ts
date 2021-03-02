import {LocationTag} from './LocationTag';
import {Authority, AuthorityConstructor} from './Authority';
import {Building, BuildingConstructor} from './Building';
import {LocationStatus} from '../../app.constants';
import {Pair} from './helpers/Pair';

export interface Location {
  locationId: number;
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
  status: Pair<LocationStatus, string>;
}

export class LocationConstructor {
  static new(): Location {
    return {
      locationId: -1,
      name: '',
      numberOfSeats: 0,
      numberOfLockers: 0,
      forGroup: false,
      imageUrl: '',
      authority: AuthorityConstructor.new(),
      building: BuildingConstructor.new(),
      descriptionDutch: '',
      descriptionEnglish: '',
      assignedTags: [],
      status: {first: LocationStatus.CLOSED, second: ''}
    };
  }

  static newFromObj(obj: Location): Location {
    if (obj === null) {
      return null;
    }

    return {
      locationId: obj.locationId,
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
      status: {first: obj.status.first, second: obj.status.second}
    };
  }
}
