import {Role, RoleConstructor} from './Role';

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

export class UserConstructor {
  static new(): User {
    return {
      augentID: '',
      firstName: '',
      lastName: '',
      mail: '',
      password: '',
      penaltyPoints: 0,
      roles: [],
      institution: ''
    };
  }

  static newFromObj(obj: User): User {
    if (obj === null) {
      return null;
    }

    return {
      augentID: obj.augentID,
      firstName: obj.firstName,
      lastName: obj.lastName,
      mail: obj.mail,
      password: obj.password,
      penaltyPoints: obj.penaltyPoints,
      roles: RoleConstructor.rolesFromStrings(obj.roles),
      institution: obj.institution
    };
  }
}
