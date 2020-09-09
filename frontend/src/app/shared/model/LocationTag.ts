export interface LocationTag {
  tagId: number;
  dutch: string;
  english: string;
}

export class LocationTagConstructor {
  static new(): LocationTag {
    return {
      tagId: 0,
      dutch: '',
      english: ''
    };
  }

  static newFromObj(obj: LocationTag): LocationTag {
    return {
      tagId: obj.tagId,
      dutch: obj.dutch,
      english: obj.english
    };
  }
}
