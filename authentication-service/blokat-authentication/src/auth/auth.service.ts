import { Injectable } from "@nestjs/common";
import { JwtService } from "@nestjs/jwt";
import {
  isSamlUser,
  missingSamlUserFields,
  SamlUser,
} from "src/configModule/config";

@Injectable()
export class AuthService {
  constructor(private jwtService: JwtService) {}

  async issueToken(user: SamlUser): Promise<{ access_token: string }> {
    if (!isSamlUser(user)) {
      throw new Error(
        `This user is not a qualified saml user. Missing user fields: ${missingSamlUserFields(
          user,
        )}`,
      );
    }

    const payload = {
      sub: user.id,
      fn: user.firstName,
      ln: user.lastName,
      email: user.email,
      ins: user.institution,
    };

    return Promise.resolve({
      access_token: this.jwtService.sign(payload),
    });
  }
}
