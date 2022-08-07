import { Logger, Module } from "@nestjs/common";
import { ConfigModule } from "src/configModule/config.module";
import { DbModule } from "src/db/db.module";
import { LocalFlowController } from "./local-flow.controller";
import { RegisterFlowService } from "./register-flow/register-flow-service";
import { LoginFlowService } from "./login-flow/login-flow-service/login-flow-service.service";
import { AuthModule } from "src/auth/auth.module";
import { PasswordResetFlowService } from "./password-reset-flow/password-reset-flow-service";

@Module({
  imports: [ConfigModule, Logger, DbModule, AuthModule],
  providers: [RegisterFlowService, LoginFlowService, PasswordResetFlowService],
  exports: [],
  controllers: [LocalFlowController],
})
export class LocalFlowModule {}
