import { users } from ".prisma/client";
import { Injectable } from "@nestjs/common";
import { DbUserService } from "src/db/db-user/db-user.service";
import { HashedService } from "src/db/hasher/hash.service";
import { UnhashedRegisterBodyBase } from "./RegisterBodyInterface";

@Injectable()
export class RegisterFlowService {
  constructor(
    private hashService: HashedService,
    private usersDb: DbUserService,
  ) {}

  public async handleRegistration(body: UnhashedRegisterBodyBase) {
    const user = await this.unhashedToUserData(body);
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
}
