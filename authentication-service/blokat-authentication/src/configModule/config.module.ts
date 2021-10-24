import { Module } from '@nestjs/common';
import { ConfigGuard } from './config.guard';
import { ConfigService } from './config.service';

@Module({
  providers: [ConfigService, ConfigGuard],
  exports: [ConfigService, ConfigGuard],
})
export class ConfigModule {}
