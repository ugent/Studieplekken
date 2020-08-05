import {Role} from './Role';

export class User {
  augentID: string;
  firstName: string;
  lastName: string;
  mail: string;
  password: string;
  penaltyPoints: number;
  roles: Role[];
  institution: string;

  constructor() {
    this.augentID = '';
    this.firstName = '';
    this.lastName = '';
    this.mail = '';
    this.password = '';
    this.penaltyPoints = 0;
    this.roles = [];
    this.institution = '';
  }
}
