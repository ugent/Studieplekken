import {Role} from '../../../environments/environment';

export class RoleConstructor {
  static newRoleFromString(value: string): Role {
    switch (value) {
      case Role.ADMIN:
        return Role.ADMIN;
      case Role.EMPLOYEE:
        return Role.ADMIN;
      case Role.STUDENT:
        return Role.STUDENT;
      default:
        return null;
    }
  }

  static rolesFromStrings(str: string[]): Role[] {
    const roles = [];

    str.forEach(s => {
      roles.push(RoleConstructor.newRoleFromString(s));
    });

    return roles;
  }
}
