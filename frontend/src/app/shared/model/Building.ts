export interface Building {
  buildingId: number;
  name: string;
  address: string;
}

export class BuildingConstructor {
  static new(): Building {
    return {
      buildingId: 0,
      name: '',
      address: '',
    };
  }

  static newFromObj(obj: Building): Building {
    return {
      buildingId: obj.buildingId,
      name: obj.name,
      address: obj.address,
    };
  }
}
