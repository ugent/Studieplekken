import { Logger, Module } from "@nestjs/common";
import { ConfigModule } from "../configModule/config.module";
import { DbUserService } from "./db-user/db-user.service";
import { DbService } from "./db.service";
import { HashedService } from "./hasher/hash.service";
import { DbTokenService } from "./db-token/db-token.service";

@Module({
  imports: [ConfigModule, Logger],
  providers: [DbUserService, DbService, HashedService, DbTokenService],
  exports: [DbUserService, HashedService, DbTokenService],
})
export class DbModule {
  /**
   * This module is responsible for handling the database & tables.
   * - The base service which instruments this module is the DbService,
   *   which is responsible for connecting to the database.
   *   This service is used by the other model-table services.
   * - The DbUserService is responsible for handling the User table. All related queries on this table
   *   are handled by this service.
   * - The DbTokenService is responsible for handling the Token table. All related queries on this table
   *  are handled by this service.
   */
}
