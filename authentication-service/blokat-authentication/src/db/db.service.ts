import { INestApplication, Injectable, OnModuleInit } from "@nestjs/common";
import { Prisma, PrismaClient } from "@prisma/client";
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

  async wipe() {
    const modelNames = Prisma.dmmf.datamodel.models.map((model) => model.name);

    return Promise.all(
      // @ts-ignore
      modelNames.map((modelName) => this[modelName.toLowerCase()].deleteMany()) 
    );
  }
}
