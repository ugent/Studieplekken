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
    const user = await this.unhashedToUserData(body);
    // TODO check token handed in body

    return await this.saveUser(user);
  }

  private async unhashedToUserData(
    body: UnhashedRegisterBodyBase,
  ): Promise<users> {
    const { hash: hashed_password, salt } = await this.hashService.firstHash(
      body.password,
    );
    const user: users = {
      ...body,
      hashed_password,
      salt,
      user_id: null,
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
