export interface Building {
  buildingId: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  institution: string;
}

export class BuildingConstructor {
  static new(): Building {
    return {
      buildingId: 0,
      name: '',
      address: '',
      latitude: 0,
      longitude: 0,
      institution: '',
    };
  }

  static newFromObj(obj: Building): Building {
    return {
      buildingId: obj.buildingId,
      name: obj.name,
      address: obj.address,
      latitude: obj.latitude,
      longitude: obj.longitude,
      institution: obj.institution,
    };
  }
}
