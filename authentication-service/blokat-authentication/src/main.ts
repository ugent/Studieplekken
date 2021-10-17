import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import * as fs from 'fs';
import * as path from 'path';
import { getConfig } from './configModule/config.service';
import { Logger } from '@nestjs/common';

async function bootstrap() {
  const config = getConfig();

  let httpsOptions = undefined;
  if (config.https.enabled) {
    const certLocation = path.join(__dirname, '..', config.https.certLocation);
    const keyLocation = path.join(__dirname, '..', config.https.keyLocation);
    if (!fs.existsSync(certLocation)) {
      Logger.error(`Certificate at ${certLocation} not found.`);
      return;
    }
    if (!fs.existsSync(keyLocation)) {
      Logger.error(`Key at ${keyLocation} not found.`);
      return;
    }
    httpsOptions = {
      httpsOptions: {
        cert: fs.readFileSync(certLocation),
        key: fs.readFileSync(keyLocation)
      }
    };
  }

  const app = await NestFactory.create(AppModule, {
    ...httpsOptions,
  });
  await app.listen(config.port);
}
bootstrap();
