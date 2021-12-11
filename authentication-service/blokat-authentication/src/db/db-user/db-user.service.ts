import { users } from ".prisma/client";
import { Injectable } from "@nestjs/common";
import { DbService } from "../db.service";

@Injectable()
export class DbUserService {
  constructor(private prisma: DbService) {}

  public async userByEmail(email: string) {
    return this.prisma.users.findUnique({
      where: { email },
    });
  }

  public async create(data: users) {
    return this.prisma.users.create({
      data,
    });
  }
}
