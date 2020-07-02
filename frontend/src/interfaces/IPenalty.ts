import {IDate} from "./IDate";

export interface IPenalty {
  augentID: string;
  eventCode: number;
  timestamp: IDate;
  reservationDate: IDate;
  reservationLocation: string;
  receivedPoints: number;
}
