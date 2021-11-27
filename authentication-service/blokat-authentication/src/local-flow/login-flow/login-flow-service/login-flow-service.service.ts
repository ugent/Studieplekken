import { Injectable } from "@nestjs/common";
import { AuthService } from "src/auth/auth.service";
import { DbUserService } from "src/db/db-user/db-user.service";
import { HashedService } from "src/db/hasher/hash.service";
import {
  Institution,
  isSamlUser,
  missingSamlUserFields,
  SamlUser,
} from "../../../configModule/config";

@Injectable()
export class LoginFlowService {
  constructor(
    private authService: AuthService,
    private usersDb: DbUserService,
    private hashedService: HashedService,
  ) {}
  /**
   * This handle validates the body, writes a token and returns it.
   * If it's not valid, throw a good error.
   */
  async handleLogin(body: any): Promise<{ access_token: string }> {
    console.log("enters login");
    // Get user from database
    const user: any = await this.usersDb.userByEmail(body.email);
    console.log(user);
    if (user) {
      // user has a valid email
      const userHash = await this.hashedService.hash(body.password, user.salt);
      console.log(userHash);
      if (user.hashed_password === userHash) {
        console.log(user);
        // translate user to SamlUser
        const samlUser: SamlUser = {
          id: user.user_id,
          email: user.email,
          institution: Institution.STAD_GENT,
          firstName: user.first_name,
          lastName: user.last_name,
        };

        console.log("hello");
        // Write token
        const token = await this.authService.issueToken(samlUser);
        console.log(token);
        return token;
      } else {
        console.log("invalid pass");
        throw new InvalidPasswordError();
      }
    } else {
      console.log("email not found");
      throw new EmailNotFoundError();
    }
  }
}

class EmailNotFoundError extends Error {
  constructor() {
    super("Email is not valid.");
  }
}

class InvalidPasswordError extends Error {
  constructor() {
    super("Given password is not valid.");
  }
}
