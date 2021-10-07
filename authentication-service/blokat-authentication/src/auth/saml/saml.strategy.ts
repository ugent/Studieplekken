import { Injectable } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { Strategy } from 'passport-saml';
import { MetadataReader, toPassportConfig } from 'passport-saml-metadata';
import * as fs from 'fs';
import * as path from 'path';

@Injectable()
export class SamlStrategy extends PassportStrategy(Strategy) {
  constructor() {
    super({
      ...readMetadata('./sso-artevelde.xml'),
      callbackUrl: 'https://studieplekken-dev.ugent.be/api/SSO/saml',
      issuer: 'https://studieplekken-dev.ugent.be/api/metadata/saml',
      privateKey: fs.readFileSync(path.join(__dirname, './key.pem')),
      decryptionPvk: fs.readFileSync(path.join(__dirname, './key.pem')),
    });
    console.log(readMetadata('./sso-artevelde.xml'));
  }

  validate(first: any) {
    console.log(first.getAssertion('username'));
    return first.getAssertion('username');
  }
}

export function readMetadata(localP: string) {
  const data = fs.readFileSync(path.join(__dirname, localP)).toString();
  const metadataReader = new MetadataReader(data);
  return toPassportConfig(metadataReader);
}
