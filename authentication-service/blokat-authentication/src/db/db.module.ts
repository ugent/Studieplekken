import { Logger, Module } from '@nestjs/common';
import { ConfigModule } from '../configModule/config.module';

@Module({
  imports: [ConfigModule, Logger],
  providers: [],
  exports: [],
})
export class DbModule {}
