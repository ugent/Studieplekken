export interface Time {
  hrs: number;
  min: number;
  sec: number;
}

export class TimeConstructor {
  static new(): Time {
    return {
      hrs: 0,
      min: 0,
      sec: 0,
    };
  }

  static newFromObj(obj: Time): Time {
    return {
      hrs: obj.hrs,
      min: obj.min,
      sec: obj.sec,
    };
  }
}
