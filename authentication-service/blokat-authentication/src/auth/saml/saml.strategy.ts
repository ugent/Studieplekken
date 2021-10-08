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
      ...readMetadata('./metadata-okta.xml'),
      // callbackUrl: 'https://studieplekken-dev.ugent.be/api/SSO/saml',
      // issuer: 'https://studieplekken-dev.ugent.be/api/metadata/saml',
      callbackUrl: 'https://localhost:8087/api/SSO/saml',
      issuer: 'https://localhost:8087/api/metadata/saml',
      privateKey: fs.readFileSync(path.join(__dirname, './key.pem')),
      decryptionPvk: fs.readFileSync(path.join(__dirname, './key.pem')),
    });
    console.log(readMetadata('./sso-artevelde.xml'));
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
