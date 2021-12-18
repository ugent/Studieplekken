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
export class DbModule {}
