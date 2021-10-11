import { HttpException, Injectable } from '@nestjs/common';
import { PassportStrategy } from '@nestjs/passport';
import { assert } from 'console';
import { Request } from 'express';
import * as fs from 'fs';
import { MultiSamlStrategy } from 'passport-saml';
import { MetadataReader, toPassportConfig } from 'passport-saml-metadata';
import {
  MultiSamlConfig,
  Profile,
  SamlConfig,
} from 'passport-saml/lib/passport-saml/types';
import * as path from 'path';
import { providerData } from 'src/configModule/config';
import { ConfigService } from '../../configModule/config.service';

const AUTH_CREDENTIALS_DIR = '../../../config/auth/saml/';

@Injectable()
export class SamlStrategy extends PassportStrategy(MultiSamlStrategy) {
  constructor(private configService: ConfigService) {
    super(getMultiSamlConfig(configService));
  }

  validate(request: Request, profile: Profile, verified: any) {
    const provider = getProviderConfiguration(this.configService, request);
    return provider.toSamlUser(profile);
  }
}

const getMultiSamlConfig: (a: ConfigService) => MultiSamlConfig = (c) => ({
  passReqToCallback: true,
  // See if the url includes the login url endpoints that are expected
  getSamlOptions: (req, cb) => {
    // Idp is determined by either the query param or the relay state

    // Find the correct configuration for this provider
    const provider = getProviderConfiguration(c, req);

    if (!provider) cb(new HttpException("Unsupported IDP", 400));
    else cb(null, createSamlOptionsFromConfig(provider));
  },
});

function getProviderConfiguration(configService: ConfigService, request: Request): providerData {
    const search = request.params['idp'] || request.body.RelayState;
  return configService
      .getCurrentConfiguration()
      .auth.providers.find((prov) => search === prov.loginUrl);
}

function createSamlOptionsFromConfig(config: providerData): SamlConfig {
  assert(!!config, "Config can't be null!");
  return {
    ...readMetadata(AUTH_CREDENTIALS_DIR + config.metadataFile),
    callbackUrl: config.callbackUrl,
    issuer: config.issuer,
    privateKey: fs.readFileSync(
      path.join(__dirname, AUTH_CREDENTIALS_DIR, 'key.pem'),
    ),
    decryptionPvk: fs.readFileSync(
      path.join(__dirname, AUTH_CREDENTIALS_DIR, 'key.pem'),
    ),
    additionalParams: {
      RelayState: config.loginUrl,
    },
  };
}

const metadataMap = new Map<string, string>();

export function readMetadata(localP: string) {
  if(!metadataMap.has(localP)) {
  const data = fs
    .readFileSync(path.join(__dirname, AUTH_CREDENTIALS_DIR, localP))
    .toString();

    metadataMap.set(localP, data);
  }
  const metadataReader = new MetadataReader(metadataMap.get(localP));
  return toPassportConfig(metadataReader);
}
