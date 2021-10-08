import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import * as fs from 'fs';
import * as path from 'path';

async function bootstrap() {
  const app = await NestFactory.create(AppModule, {
    // httpsOptions: {
    //   cert: fs.readFileSync(path.join(__dirname, './cert.pem')),
    //   key: fs.readFileSync(path.join(__dirname, './key.pem')),
    // },
  });
  await app.listen(8087);
}
bootstrap();
