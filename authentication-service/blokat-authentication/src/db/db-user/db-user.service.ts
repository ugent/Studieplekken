import { Injectable } from '@nestjs/common';
import { DbService } from '../db.service';

@Injectable()
export class DbUserService {
  constructor(private prisma: DbService) {}

  public async userById(user_id: string) {
    return this.prisma.users.findUnique({
      where: { user_id },
    });
  }
}
