import { users } from ".prisma/client";
import { Injectable } from "@nestjs/common";
import { DbService } from "../db.service";

@Injectable()
export class DbUserService {
  constructor(private prisma: DbService) {}

  public async userByEmail(email: string) {
    return this.prisma.users.findFirst({
      where: {
        email: {
          equals: email,
          mode: "insensitive",
        },
      },
    });
  }

  public async create(data: users) {
    return this.prisma.users.create({
      data,
    });
  }

  public async updatePassword(email: string, password: string, salt: string) {
    await this.prisma.users.updateMany({
      where: {
        email: {
          equals: email,
          mode: "insensitive",
        },
      },
      data: { hashed_password: password, salt: salt },
    });
  }
}
