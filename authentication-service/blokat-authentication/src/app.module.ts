import { Logger, Module } from "@nestjs/common";
import { AppController } from "./app.controller";
import { AppService } from "./app.service";
import { AuthModule } from "./auth/auth.module";
import { DbModule } from "./db/db.module";
import { ConfigModule } from "./configModule/config.module";
import { LoggerModule } from "./logger/logger.module";
import { LocalFlowModule } from "./local-flow/local-flow.module";

@Module({
  imports: [AuthModule, DbModule, ConfigModule, LoggerModule, LocalFlowModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
