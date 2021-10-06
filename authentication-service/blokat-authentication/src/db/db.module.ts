import { Module } from '@nestjs/common';
import { DbUserService } from './db-user/db-user.service';
import { DbService } from './db.service';

@Module({
  providers: [DbUserService, DbService],
  exports: [DbUserService],
})
export class DbModule {}
