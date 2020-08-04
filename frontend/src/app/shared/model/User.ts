import {Role} from './Role';

export interface User {
  augentID: string;
  firstName: string;
  lastName: string;
  mail: string;
  password: string;
  penaltyPoints: number;
  roles: Role[];
  institution: string;
}
