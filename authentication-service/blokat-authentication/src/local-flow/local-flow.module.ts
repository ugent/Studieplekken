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
export class LocalFlowModule {
  /**
   * The LocalFlow module is responsible for handling the local authentication flow.
   * This is a flow where you can register a new user, login, and reset your password.
   * We generally prefer using the CAS or SAML authentication flows, but this flow is necessary for
   * external organisations which have no access to SAML.
   * 
   * Accounts can only be registered using a token provided by an administrator (generated with this service).
   */
}
