import { Module } from '@nestjs/common';
import { ConfigModule } from 'src/configModule/config.module';
import { DbModule } from 'src/db/db.module';
import { TokenController } from './controller/token.controller';
import { BackendServiceGuard } from './internal-auth/service-jwt/backend-service.guard';

@Module({
  controllers: [TokenController],
  providers: [BackendServiceGuard],
  imports: [ConfigModule, DbModule]
})
export class ApiModule {}
