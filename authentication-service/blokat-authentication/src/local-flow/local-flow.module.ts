import { Logger, Module } from "@nestjs/common";
import { ConfigModule } from "src/configModule/config.module";
import { DbModule } from "src/db/db.module";
import { LocalFlowController } from "./local-flow.controller";
import { RegisterFlowService } from "./register-flow/register-flow-service";

@Module({
  imports: [ConfigModule, Logger, DbModule],
  providers: [RegisterFlowService],
  exports: [],
  controllers: [LocalFlowController],
})
export class LocalFlowModule {}
