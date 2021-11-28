import { users } from ".prisma/client";
import { Injectable } from "@nestjs/common";
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
    // Check password
    if (
      !Array.isArray(body.password) ||
      body.password[0] !== body.password[1]
    ) {
      throw new PasswordNoMatchError();
    } else {
      body.password = body.password[0];
    }

    // create user
    const user = await this.unhashedToUserData(body);

    // check token handed in body
    this.tokenDb.useToken(body.token);

    // save user
    try {
      return await this.saveUser(user);
    } catch (error) {
      throw new SaveUserError();
    }
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

  public async newToken(): Promise<string> {
    return (await this.tokenDb.createNewToken()).id;
  }
}

class PasswordNoMatchError extends Error {
  constructor() {
    super("The passwords do not match.");
  }
}

class SaveUserError extends Error {
  constructor() {
    super("Something went wrong when saving the user");
  }
}
