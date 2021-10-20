import {
  Controller,
  Get,
  Logger,
  Post,
  Request,
  Res,
  UseGuards,
} from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { randomUUID } from 'crypto';
import { AuthService } from './auth/auth.service';
import {
  Institution,
  isSamlUser,
  missingSamlUserFields,
  SamlUser,
} from './configModule/config';
import { ConfigGuard } from './configModule/config.guard';
import { getConfig } from './configModule/config.service';

@Controller()
export class AppController {
  constructor(private authService: AuthService) {}

  /******* CAS ENDPOINTS  *********/

  @UseGuards(AuthGuard('cas'))
  @Get('auth/login/cas')
  async casLogin(@Request() req: any, @Res() res: any) {
    Logger.warn(`User did not get redirected by CAS authguard.`);
    return res.status(500).send();
  }

  @UseGuards(AuthGuard('cas'))
  @Get('auth/login/cas/:callbackURL')
  async casLoginCallback(@Request() req: any, @Res() res: any) {
    const samlUser = req.user;
    if (!isSamlUser(samlUser)) {
      const missingFields = missingSamlUserFields(samlUser);
      Logger.warn(
        `SAML-user in request was missing ${missingFields.join(',')}.`,
      );
      return res.status(400).send();
    }

    const token: string = (await this.authService.issueToken(samlUser))
      .access_token;

    try {
      const redirectUrl = req.params.callbackURL;
      if (redirectUrl && redirectUrl !== 'undefined') {
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

  @Get('auth/login/test')
  @UseGuards(ConfigGuard)
  async testEndpoint() {
    const id = randomUUID();
    const newTestUser: SamlUser = {
      id,
      email: `${id}@test.com`,
      institution: Institution.UGENT,
      firstName: 'test',
      lastName: 'test',
    };

    return await this.authService.issueToken(newTestUser);
  }

  @UseGuards(AuthGuard('saml'))
  @Get('auth/login/:idp')
  async loginSaml(@Request() req: any, @Res() res: any) {
    const samlUser = req.user;
    if (!isSamlUser(samlUser)) {
      const missingFields = missingSamlUserFields(samlUser);
      Logger.warn(
        `SAML-user in request was missing ${missingFields.join(',')}.`,
      );
      return res.status(400).send();
    }
    return await this.authService.issueToken(samlUser);
  }

  @UseGuards(AuthGuard('saml'))
  @Post('api/SSO/saml')
  async loginSamlGet(@Request() req: any, @Res() res: any) {
    const samlUser = req.user;
    if (!isSamlUser(samlUser)) {
      const missingFields = missingSamlUserFields(samlUser);
      Logger.warn(
        `SAML-user in request was missing ${missingFields.join(',')}.`,
      );
      return res.status(400).send();
    }

    const token: string = (await this.authService.issueToken(samlUser))
      .access_token;

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

  @Get()
  async home() {
    return 'online';
  }
}
