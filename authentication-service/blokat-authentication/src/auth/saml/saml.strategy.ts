import { Injectable } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { Strategy } from 'passport-saml';
import { MetadataReader, toPassportConfig } from 'passport-saml-metadata';
import * as fs from 'fs';
import * as path from 'path';
import { ConfigService } from 'src/configModule/config.service';

const AUTH_CREDENTIALS_DIR = '../../../config/auth/saml/';

@Injectable()
export class SamlStrategy extends PassportStrategy(Strategy) {
  constructor(configService: ConfigService) {
    super({
      ...readMetadata(AUTH_CREDENTIALS_DIR + configService.getCurrentConfiguration().auth.metadataFile),
      // callbackUrl: 'https://studieplekken-dev.ugent.be/api/SSO/saml',
      // issuer: 'https://studieplekken-dev.ugent.be/api/metadata/saml',
      callbackUrl: configService.getCurrentConfiguration().auth.callbackUrl,
      issuer: configService.getCurrentConfiguration().auth.issuer,
      privateKey: fs.readFileSync(path.join(__dirname, AUTH_CREDENTIALS_DIR, 'key.pem')),
      decryptionPvk: fs.readFileSync(path.join(__dirname, AUTH_CREDENTIALS_DIR, 'key.pem')),
    });
  }

  validate(first: any) {
    return first.getAssertion('username');
  }
}

export function readMetadata(localP: string) {
  const data = fs.readFileSync(path.join(__dirname, localP)).toString();
  const metadataReader = new MetadataReader(data);
  return toPassportConfig(metadataReader);
}
