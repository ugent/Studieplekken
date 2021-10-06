import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { AuthModule } from './auth/auth.module';
import { DbModule } from './db/db.module';

@Module({
  imports: [AuthModule, DbModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
