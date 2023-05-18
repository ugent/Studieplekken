import { NestFactory } from "@nestjs/core";
import { AppModule } from "./app.module";
import * as fs from "fs";
import * as path from "path";
import { getConfig } from "./configModule/config.service";
import { Logger } from "@nestjs/common";
import { NestExpressApplication } from "@nestjs/platform-express";
import { join } from "path";

async function bootstrap(): Promise<void> {
  /** Main function to launch the app.
   *
   * The app is a NestJS application, which is a wrapper around Express.
   * It provides a Service-based framework for structuring your code.
   * In this application, you can find the following modules:
   * - DbModule: Return the models representing the DB tables
   * - ConfigModule: Handle the different configuration environments for the app
   * - AuthModule: Handle the authentication strategies, such as CAS and SAML, as well as local.
   * - LocalFlowModule: Handle the API endpoints for the local authentication
   * - ApiModule: Handle the API endpoints used in direct communication with the Backend service
   * You can find the auth flow endpoints in app.controller.ts.
   *
   * Further documentation of each of the modules can be found in their respective folders.
   */
  const config = getConfig();

  let httpsOptions = undefined;
  if (config.https.enabled) {
    const certLocation = path.join(__dirname, "..", config.https.certLocation);
    const keyLocation = path.join(__dirname, "..", config.https.keyLocation);
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
        key: fs.readFileSync(keyLocation),
      },
    };
  }

  const app = await NestFactory.create<NestExpressApplication>(AppModule, {
    ...httpsOptions,
  });

  // Templating for local authentication
  app.useStaticAssets(join(__dirname, "..", "public"), {
    prefix: "/auth/local/",
  });
  app.setBaseViewsDir(join(__dirname, "..", "views"));
  app.setViewEngine("hbs");

  await app.listen(config.port);
}

bootstrap();
