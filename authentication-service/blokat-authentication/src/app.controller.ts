import { Controller, Request, Post, UseGuards, Get, Res } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { AuthService } from './auth/auth.service';
import { DbUserService } from './db/db-user/db-user.service';

@Controller()
export class AppController {
  constructor(
    private authService: AuthService,
    private dbUsersService: DbUserService,
  ) {}

  @UseGuards(AuthGuard('saml'))
  @Get('login/:idp')
  async loginSaml(@Request() req: any) {
    return await this.authService.issueToken(req.user);
  }

  @UseGuards(AuthGuard('saml'))
  @Post('api/SSO/saml')
  async loginSamlGet(@Request() req: any, @Res() res: any) {
    // No need to retrieve the actual user, only create if it does not exist
    await this.dbUsersService.getOrCreateUserBySaml(req.user);

    const token: string = (await this.authService.issueToken(req.user))
      .access_token;
    const redirectUrl = `/`;
    return res.redirect(`${redirectUrl}?token=${token}`);
  }

  @Get()
  async home() {
    return 'Online';
  }
}
