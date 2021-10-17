import { users } from '.prisma/client';
import { Injectable, Logger } from '@nestjs/common';
import { SamlUser } from 'src/configModule/config';
import { DbService } from '../db.service';

@Injectable()
export class DbUserService {
  constructor(
    private prisma: DbService
  ) {}

  public async userById(user_id: string) {
    return this.prisma.users.findUnique({
      where: { user_id },
    });
  }

  public async getOrCreateUserBySaml(samlUser: SamlUser): Promise<users> {
    const user = await this.prisma.users.findUnique({
      where: { user_id: samlUser.id },
    });

    if (user) return user;
    Logger.log('Adding user ' + samlUser.email + ' to the database.');

    return await this.prisma.users.create({
      data: {
        user_id: samlUser.id,
        mail: samlUser.email,
        first_name: samlUser.firstName,
        last_name: samlUser.lastName,
        institution: samlUser.institution,
      },
    });
  }
}
