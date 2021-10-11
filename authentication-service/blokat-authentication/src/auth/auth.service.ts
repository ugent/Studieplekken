import { Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { users } from '@prisma/client';
import { SamlUser } from 'src/configModule/config';
import { DbUserService } from '../db/db-user/db-user.service';

@Injectable()
export class AuthService {
  constructor(
    private usersService: DbUserService,
    private jwtService: JwtService,
  ) {}

  async validateUser(username: string, pass: string): Promise<any> {
    const user = await this.usersService.userById(username);
    return user || null;
  }

  async issueToken(user: SamlUser): Promise<{access_token: string}> {
    const payload = { sub: user.id };

    return Promise.resolve({
      access_token: this.jwtService.sign(payload),
    });
  }
}
