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
}
