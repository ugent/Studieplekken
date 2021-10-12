import { Injectable } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { SamlUser } from 'src/configModule/config';

@Injectable()
export class AuthService {
  constructor(private jwtService: JwtService) {}

  async issueToken(user: SamlUser): Promise<{ access_token: string }> {
    const payload = { sub: user.id };

    return Promise.resolve({
      access_token: this.jwtService.sign(payload),
    });
  }
}
