import { Controller, Request, Post, UseGuards, Get, Res } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { randomUUID } from 'crypto';
import { AuthService } from './auth/auth.service';
import { Institution, isSamlUser, missingSamlUserFields, SamlUser } from './configModule/config';
import { ConfigGuard } from './configModule/config.guard';
import { DbUserService } from './db/db-user/db-user.service';
import { Logger } from '@nestjs/common';
import { getConfig } from './configModule/config.service';

@Controller()
export class AppController {
  constructor(
    private authService: AuthService,
    private dbUsersService: DbUserService
  ) {}

  /******* CAS ENDPOINTS  *********/

  @UseGuards(AuthGuard('cas'))
  @Get('auth/login/cas')
  async casLogin(@Request() req: any, @Res() res: any) {
    const samlUser = req.user;
    if (!isSamlUser(samlUser)) {
      const missingFields = missingSamlUserFields(samlUser);
      Logger.warn(`SAML-user in request was missing ${missingFields.join(',')}.`);
      return res.status(400).send();
    }
    return res.send(await this.authService.issueToken(samlUser));
  }

  @UseGuards(AuthGuard('cas'))
  @Get('auth/login/cas/:callbackURL')
  async casLoginCallback(@Request() req: any, @Res() res: any) {
    const samlUser = req.user;
    if (!isSamlUser(samlUser)) {
      const missingFields = missingSamlUserFields(samlUser);
      Logger.warn(`SAML-user in request was missing ${missingFields.join(',')}.`);
      return res.status(400).send();
    }
    await this.dbUsersService.getOrCreateUserBySaml(samlUser);

    const token: string = (await this.authService.issueToken(samlUser))
      .access_token;

    try {
      const redirectUrl = req.params.callbackURL;
      if (redirectUrl && redirectUrl !== 'undefined') {
        const configuration = getConfig();
        const allowedCallbacks = configuration.auth.providers.map(prov => prov.callbackUrl);
        if (allowedCallbacks.indexOf(redirectUrl) !== -1) {
          return res.redirect(`${redirectUrl}?token=${token}`);
        }
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

    await this.dbUsersService.getOrCreateUserBySaml(newTestUser);
    return await this.authService.issueToken(newTestUser);
  }

  @UseGuards(AuthGuard('saml'))
  @Get('auth/login/:idp')
  async loginSaml(@Request() req: any, @Res() res: any) {
    const samlUser = req.user;
    if (!isSamlUser(samlUser)) {
      const missingFields = missingSamlUserFields(samlUser);
      Logger.warn(`SAML-user in request was missing ${missingFields.join(',')}.`);
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
      Logger.warn(`SAML-user in request was missing ${missingFields.join(',')}.`);
      return res.status(400).send();
    }
    // No need to retrieve the actual user, only create if it does not exist
    await this.dbUsersService.getOrCreateUserBySaml(samlUser);

    const token: string = (await this.authService.issueToken(samlUser))
      .access_token;

    try {
      const redirectUrl = JSON.parse(req.body.RelayState)?.callbackUrl;
      if (redirectUrl) {
        const configuration = getConfig();
        const allowedCallbacks = configuration.auth.providers.map(prov => prov.callbackUrl);
        if (allowedCallbacks.indexOf(redirectUrl) !== -1) {
          return res.redirect(`${redirectUrl}?token=${token}`);
        }
      } 
    } catch {
      return res.send({ access_token: token });
    }
  }

  @Get()
  async home() {
    return 'online';
  }
}
