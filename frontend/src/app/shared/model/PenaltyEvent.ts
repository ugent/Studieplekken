export interface PenaltyEvent {
  code: number;
  points: number;
  publicAccessible: boolean;
  descriptions: Record<string, string>;
}

export class PenaltyEventConstructor {
  static new(): PenaltyEvent {
    return {
      code: 0,
      points: 0,
      publicAccessible: true,
      descriptions: {},
    };
  }

  static newFromObj(obj: PenaltyEvent): PenaltyEvent {
    return {
      code: obj.code,
      points: obj.points,
      publicAccessible: obj.publicAccessible,
      descriptions: obj.descriptions,
    };
  }
}
