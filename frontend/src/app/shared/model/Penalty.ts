import * as moment from 'moment';
import { Moment } from 'moment';
import { LocationReservation } from './LocationReservation';
import { User, UserConstructor } from './User';

export class Penalty {
  points: number;
  createdAt: Moment;
  description: string;
  designee: User;
  issuer: User | null;
  locationReservation: LocationReservation | null;
  penaltyClass: string;

  static fromJSON(json: Record<string,any>): Penalty {
    const penalty = new Penalty();
    penalty.points = json.points;
    penalty.createdAt = moment(json.createdAt);
    penalty.description = json.description;
    penalty.designee = UserConstructor.newFromObj(json.designee);
    penalty.issuer = json.issuer ? UserConstructor.newFromObj(json.issuer) : null;
    penalty.locationReservation = json.locationReservation ? LocationReservation.fromJSON(json.locationReservation) : null;
    penalty.penaltyClass = json.penaltyClass;
    return penalty;
  }
}
