import { Controller, Request, Post, UseGuards, Get } from '@nestjs/common';
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
    return this.authService.login(req.user);
  }

  @Get()
  async home(@Request() req: any) {
    return await this.dbUsersService.userById('000170335535');
  }
}
