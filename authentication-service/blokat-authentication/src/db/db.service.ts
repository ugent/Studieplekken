import { INestApplication, Injectable, OnModuleInit } from '@nestjs/common';
import { PrismaClient } from '@prisma/client';
import { ConfigService } from 'src/configModule/config.service';

@Injectable()
export class DbService extends PrismaClient implements OnModuleInit {

  constructor(configService: ConfigService) {

    const dbConfig = configService.getCurrentConfiguration().database
    super({
      datasources: {
        db: {
          url: `postgresql://${dbConfig.username}:${dbConfig.password}@${dbConfig.url}:${dbConfig.port}/blokatugent?schema=public`,
        },
      },
    });
  }

  async onModuleInit() {
    await this.$connect();
  }

  async enableShutdownHooks(app: INestApplication) {
    this.$on('beforeExit', async () => {
      await app.close();
    });
  }
}
