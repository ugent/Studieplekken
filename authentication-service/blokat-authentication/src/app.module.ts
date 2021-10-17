import { Logger, Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { AuthModule } from './auth/auth.module';
import { DbModule } from './db/db.module';
import { ConfigModule } from './configModule/config.module';
import { LoggerModule } from './logger/logger.module';

@Module({
  imports: [AuthModule, DbModule, ConfigModule, LoggerModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
