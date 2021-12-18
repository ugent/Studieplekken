import { INestApplication, Injectable, OnModuleInit } from "@nestjs/common";
import { PrismaClient } from "@prisma/client";
import { ConfigService } from "../configModule/config.service";

@Injectable()
export class DbService extends PrismaClient implements OnModuleInit {
  constructor(configService: ConfigService) {
    const dbConfig = configService.getCurrentConfiguration().database;
    super({
      datasources: {
        db: {
          url: `postgresql://${dbConfig.username}:${dbConfig.password}@${dbConfig.url}:${dbConfig.port}/studieplekken_users?schema=public`,
        },
      },
    });
  }

  async onModuleInit() {
    await this.$connect();
  }

  async enableShutdownHooks(app: INestApplication) {
    this.$on("beforeExit", async () => {
      await app.close();
    });
  }
}
