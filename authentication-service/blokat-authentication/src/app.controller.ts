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

  @UseGuards(AuthGuard('local'))
  @Post('auth/login')
  async login(@Request() req: any) {
    return this.authService.issueToken(req.user);
  }

  @UseGuards(AuthGuard('saml'))
  @Get('auth/saml')
  async loginSaml(@Request() req: any) {
    return this.authService.issueToken(req.user);
  }

  @UseGuards(AuthGuard('saml'))
  @Post('auth/saml')
  async loginSamlGet(@Request() req: any, @Res() res: any) {
    const token : string = (await this.authService.issueToken(req.user)).access_token;
    const redirectUrl : string = `/`;
    return res.redirect(`${redirectUrl}?token=${token}`);
  }

  @Get()
  async home(@Request() req: any) {
    return await this.dbUsersService.userById('000170335535');
  }
}
