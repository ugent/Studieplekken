import { Injectable } from "@nestjs/common";
import { HashedService } from "../../db/hasher/hash.service";
import { DbUserService } from "../../db/db-user/db-user.service";
import { DbTokenService } from "../../db/db-token/db-token.service";
import { UnhashedPasswordResetBodyBase } from "./PasswordResetBodyInterface";
import { validate } from "class-validator";

@Injectable()
export class PasswordResetFlowService {
  constructor(
    private hashService: HashedService,
    private usersDb: DbUserService,
    private tokenDb: DbTokenService,
  ) {}

  public async handlePasswordReset(body: UnhashedPasswordResetBodyBase) {
    const errors = [];
    const validationErrors = await validate(body);
    if (validationErrors) {
      validationErrors.forEach((v) => errors.push(v.toString()));
    }

    // Check if all values are filled in
    if (body.password) {
      // Check password
      if (
        !Array.isArray(body.password) ||
        body.password[0] !== body.password[1]
      ) {
        errors.push("The passwords do not match.");
      } else if (body.password[0].length < 8) {
        errors.push("Password is too short, must be at least 8 characters");
      } else {
        body.password = body.password[0];
      }

      if (errors.length > 0) {
        return { errors: errors };
      }

      // check token handed in body
      const token = await this.tokenDb.checkToken(body.token, "PASSWORD_RESET");

      // Get user from database by email in token
      const email = token.email;
      const user = await this.usersDb.userByEmail(email);
      console.log(user);
      const { hash: hashed_password, salt } = await this.hashService.firstHash(
        body.password,
      );
      console.log(hashed_password);
      user.hashed_password = hashed_password;
      user.salt = salt;

      await this.usersDb.updatePassword(email, hashed_password, salt);

      await this.tokenDb.useToken(body.token, "PASSWORD_RESET");

      return { errors: errors };
    }
  }
}
