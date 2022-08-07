import { users } from ".prisma/client";
import { Injectable } from "@nestjs/common";
import { PrismaClientKnownRequestError } from "@prisma/client/runtime";
import { validate, validateOrReject } from "class-validator";
import { DbTokenService } from "src/db/db-token/db-token.service";
import { DbUserService } from "src/db/db-user/db-user.service";
import { HashedService } from "src/db/hasher/hash.service";
import { UnhashedRegisterBodyBase } from "./RegisterBodyInterface";

@Injectable()
export class RegisterFlowService {
  constructor(
    private hashService: HashedService,
    private usersDb: DbUserService,
    private tokenDb: DbTokenService,
  ) {}

  public async handleRegistration(body: UnhashedRegisterBodyBase) {
    const errors = [];
    const validationErrors = await validate(body);
    if (validationErrors) {
      validationErrors.forEach((v) => errors.push(v.toString()));
    }

    // Check if all values are filled in
    if (body.email && body.first_name && body.last_name && body.password) {
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

      // create user
      const user = await this.unhashedToUserData(body);

      // check token handed in body
      try {
        await this.tokenDb.checkToken(body.token, "REGISTRATION");
        const savedUser = await this.saveUser(user);
        await this.tokenDb.useToken(body.token, "REGISTRATION");
        return savedUser;
      } catch (error) {
        if (error instanceof PrismaClientKnownRequestError)
          errors.push("Email is already in use.");
        else errors.push("Invalid token.");
      }
    } else {
      errors.push("All fields need to be filled in.");
    }

    return { errors: errors };
  }

  private async unhashedToUserData(
    body: UnhashedRegisterBodyBase,
  ): Promise<users> {
    const { hash: hashed_password, salt } = await this.hashService.firstHash(
      body.password,
    );
    const user: users = {
      email: body.email,
      first_name: body.first_name,
      last_name: body.last_name,
      hashed_password,
      salt,
      user_id: undefined,
    };

    return user;
  }

  private async saveUser(user: users) {
    return this.usersDb.create(user);
  }
}
