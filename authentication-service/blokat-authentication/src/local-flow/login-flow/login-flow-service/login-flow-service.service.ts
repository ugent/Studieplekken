import { Injectable } from '@nestjs/common';
import { AuthService } from 'src/auth/auth.service';
import { DbUserService } from 'src/db/db-user/db-user.service';
import { HashedService } from 'src/db/hasher/hash.service';
import {
  Institution,
  isSamlUser,
  missingSamlUserFields,
  SamlUser,
} from '../../../configModule/config';
import { validate, validateOrReject } from 'class-validator';

@Injectable()
export class LoginFlowService {
  constructor(
    private authService: AuthService,
    private usersDb: DbUserService,
    private hashedService: HashedService,
  ) {}
  /**
   * This handle validates the body, writes a token and returns it.
   * If it's not valid, throw a good error.
   */
  async handleLogin(
    body: any,
  ): Promise<{ access_token: string; errors: string[] }> {
    const errors = [];
    const validationErrors = await validate(body);
    if (validationErrors) {
      validationErrors.forEach((v) => errors.push(v.toString()));
    }
    // Get user from database
    const user: any = await this.usersDb.userByEmail(body.email);

    if (user) {
      // user has a valid email
      const userHash = await this.hashedService.hash(body.password, user.salt);
      if (user.hashed_password === userHash) {
        // translate user to SamlUser
        const samlUser: SamlUser = {
          id: user.user_id,
          email: user.email,
          institution: Institution.OTHER,
          firstName: user.first_name,
          lastName: user.last_name,
        };

        // Write token
        const token = await this.authService.issueToken(samlUser);
        return { access_token: token.access_token, errors: [] };
      }
    }
    return { access_token: '', errors: ['Invalid credentials'] };
  }
}
