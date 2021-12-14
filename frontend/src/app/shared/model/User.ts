import { decode } from "html-entities";
import { Authority, AuthorityConstructor } from "./Authority";

export interface User {
  userId: string;
  firstName: string;
  lastName: string;
  mail: string;
  password: string;
  penaltyPoints: number;
  institution: string;
  admin: boolean;
  userAuthorities: Authority[];
  userVolunteer: unknown[];
}

export class UserConstructor {
  static new(): User {
    return {
      userId: '',
      firstName: '',
      lastName: '',
      mail: '',
      password: '',
      penaltyPoints: 0,
      institution: '',
      admin: false,
      userAuthorities: [],
      userVolunteer: []
    };
  }

  static newFromObj(obj: User): User {
    if (obj === null) {
      return null;
    }

    return {
      userId: obj.userId,
      firstName: decode(obj.firstName),
      lastName: decode(obj.lastName),
      mail: obj.mail,
      password: obj.password,
      penaltyPoints: obj.penaltyPoints,
      institution: obj.institution,
      admin: obj.admin,
      userAuthorities: obj.userAuthorities.map(v => AuthorityConstructor.newFromObj(v)),
      userVolunteer: obj.userVolunteer
    };
  }
}
