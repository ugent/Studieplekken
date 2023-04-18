import {
  Controller,
  Get,
  Logger,
  Post,
  Request,
  Res,
  UseGuards,
} from "@nestjs/common";
import { AuthGuard } from "@nestjs/passport";
import { randomUUID } from "crypto";
import { AuthService } from "./auth/auth.service";
import {
  Institution,
  isSamlUser,
  missingSamlUserFields,
  SamlUser,
} from "./configModule/config";
import { ConfigGuard } from "./configModule/config.guard";
import { getConfig } from "./configModule/config.service";
import { RealIP } from "nestjs-real-ip";
import { getSamlMetadata } from "./auth/saml/saml.strategy";

@Controller()
export class AppController {
  constructor(private authService: AuthService) {}

  /******* CAS ENDPOINTS  *********/

  /*
  The CAS endpoints are used for authenticating users using CAS. This is used for internal auth within UGent.
  The CAS endpoints are protected by the CAS strategy (see AuthGuard('cas')).
  This strategy can be found in auth/auth.module.ts.
  */

  @UseGuards(AuthGuard("cas"))
  @Get("auth/login/cas")
  async casLogin(@Request() req: any, @Res() res: any, @RealIP() ip: string) {
    const token = await this.authService.issueToken(req.user);
    Logger.log(`Written token for user ${req.user.id} and IP ${ip}`);

    return res.status(200).send(token);
  }

  @UseGuards(AuthGuard("cas"))
  @Get("auth/login/cas/:callbackURL")
  async casLoginCallback(
    @Request() req: any,
    @Res() res: any,
    @RealIP() ip: string,
  ) {
    const samlUser = req.user;
    if (!isSamlUser(samlUser)) {
      const missingFields = missingSamlUserFields(samlUser);
      Logger.warn(
        `SAML-user in request was missing ${missingFields.join(",")}.`,
      );
      return res
        .status(400)
        .json({
          error: `SAML-user in request was missing ${missingFields.join(",")}.`,
        })
        .send();
    }

    const token: string = (await this.authService.issueToken(samlUser))
      .access_token;

    Logger.log(`Written token for user ${samlUser.id} and IP ${ip}`);
    try {
      const redirectUrl = req.params.callbackURL;
      if (redirectUrl && redirectUrl !== "undefined") {
        const configuration = getConfig();
        const allowedCallbacks = configuration.auth.allowedClientCallbacks;
        if (allowedCallbacks.indexOf(redirectUrl) !== -1) {
          return res.redirect(`${redirectUrl}?token=${token}`);
        }
        Logger.warn(`Callback URL ${redirectUrl} is not allowed.`);
      }
      return res.send({ access_token: token });
    } catch {
      return res.send({ access_token: token });
    }
  }

  /******* SAML ENDPOINTS  *********/

  @UseGuards(AuthGuard("saml"))
  @Get("auth/login/:idp")
  async loginSaml(@Request() req: any, @Res() res: any, @RealIP() ip: string) {
    Logger.debug(`Someone is attempting to log in from ${req.params.idp}`);

    const samlUser = req.user;
    if (!isSamlUser(samlUser)) {
      const missingFields = missingSamlUserFields(samlUser);
      Logger.warn(
        `SAML-user in request was missing ${missingFields.join(",")}.`,
      );
      return res.status(400).send();
    }

    const token = await this.authService.issueToken(samlUser);
    Logger.log(`Written token for user ${req.user.id} and IP ${ip}`);

    return res.status(200).send(token);
  }

  @UseGuards(AuthGuard("saml"))
  @Post("api/SSO/saml")
  async loginSamlGet(
    @Request() req: any,
    @Res() res: any,
    @RealIP() ip: string,
  ) {
    Logger.debug(`Someone is returning from idp.`);
    Logger.debug(req.body.RelayState);

    const samlUser = req.user;
    if (!isSamlUser(samlUser)) {
      const missingFields = missingSamlUserFields(samlUser);
      Logger.warn(
        `SAML-user in request was missing ${missingFields.join(",")}.`,
      );
      return res.status(400).send();
    }

    const token: string = (await this.authService.issueToken(samlUser))
      .access_token;

    Logger.debug(`Issued token for user with id ${samlUser.id} and IP ${ip}`);

    try {
      const redirectUrl = JSON.parse(req.body.RelayState)?.callbackUrl;
      if (redirectUrl) {
        const configuration = getConfig();
        const allowedCallbacks = configuration.auth.allowedClientCallbacks;
        if (allowedCallbacks.indexOf(redirectUrl) !== -1) {
          return res.redirect(`${redirectUrl}?token=${token}`);
        }
        Logger.warn(`Callback URL ${redirectUrl} is not allowed.`);
      }
      return res.send({ access_token: token });
    } catch {
      return res.send({ access_token: token });
    }
  }

  /******* TEST ENDPOINTS  *********/

  @Get("auth/login/test")
  @UseGuards(ConfigGuard)
  async testEndpoint() {
    const id = randomUUID();
    const newTestUser: SamlUser = {
      id,
      email: `${id}@test.com`,
      institution: Institution.UGENT,
      firstName: "test",
      lastName: "test",
    };

    return await this.authService.issueToken(newTestUser);
  }

  @Get("api/metadata/saml")
  async getMetadata(@Request() req: any, @Res() res: any) {
    const metadata = getSamlMetadata();
    res.type("application/xml");
    return res.send(metadata);
  }

  @Get()
  async home() {
    return "online";
  }
}
