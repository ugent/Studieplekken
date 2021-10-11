import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import * as fs from 'fs';
import * as path from 'path';
import { getConfig } from './configModule/config.service';

async function bootstrap() {
  const config = getConfig();

  const httpsOptions =
    config.https.enabled === true
      ? {
          httpsOptions: {
            cert: fs.readFileSync(path.join(__dirname, "..", config.https.certLocation)),
            key: fs.readFileSync(path.join(__dirname, "..", config.https.keyLocation)),
          },
        }
      : undefined;

  const app = await NestFactory.create(AppModule, {
    ...httpsOptions
  });
  await app.listen(8087);
}
bootstrap();
