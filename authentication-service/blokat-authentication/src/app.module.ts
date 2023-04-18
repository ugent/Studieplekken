import { Module } from "@nestjs/common";
import { AppController } from "./app.controller";
import { AuthModule } from "./auth/auth.module";
import { DbModule } from "./db/db.module";
import { ConfigModule } from "./configModule/config.module";
import { LoggerModule } from "./logger/logger.module";
import { LocalFlowModule } from "./local-flow/local-flow.module";
import { ApiModule } from "./api/api.module";

@Module({
  imports: [
    AuthModule,
    DbModule,
    ConfigModule,
    LoggerModule,
    LocalFlowModule,
    ApiModule,
  ],
  controllers: [AppController],
  providers: [],
})
export class AppModule {
  /**
   * The App module is mainly responsible for the AppController, which handles the authentication flow for the majority of systems.
   * This module implements both SAML and CAS authentication strategies. You can configure these strategies using the config module.
   * The endpoints are found in app.controller.ts.
   */
}
