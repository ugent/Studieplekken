import { Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { DbUserService } from 'src/db/db-user/db-user.service';

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

  async login(user: any) {
    const payload = { username: user.username, sub: user.userId };

    return {
      access_token: this.jwtService.sign(payload),
    };
  }
}
