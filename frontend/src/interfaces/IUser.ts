import {CustomDate} from "./CustomDate";


export interface IUser {
  augentID: string;
  lastName: string;
  firstName: string;
  mail: string;
  password: string;
  institution: string;
  roles: string[];
  penaltyPoints: number;
  barcode: string;
}
