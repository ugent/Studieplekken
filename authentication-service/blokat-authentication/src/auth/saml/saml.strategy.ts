import { HttpException, Injectable, Logger } from "@nestjs/common";
import { PassportStrategy } from "@nestjs/passport";
import { assert } from "console";
import { Request } from "express";
import * as fs from "fs";
import { MultiSamlStrategy } from "passport-saml";
import { MetadataReader, toPassportConfig } from "passport-saml-metadata";
import {
  MultiSamlConfig,
  Profile,
  SamlConfig,
} from "passport-saml/lib/passport-saml/types";
import * as path from "path";
import { providerData } from "src/configModule/config";
import { ConfigService } from "../../configModule/config.service";

const AUTH_CREDENTIALS_DIR = "../../../config/auth/saml/";

@Injectable()
export class SamlStrategy extends PassportStrategy(MultiSamlStrategy) {
  /**
   * This is a strategy for handling SAML authentication, with multiple providers.
   * We subclass the MultiSamlStrategy to override the methods retrieving the settings.
   * We will use the SAML settings in the configuration to specify the settings for the SAML providers.
   *
   * Rough SAML flow is as follows:
   * - The frontend sends a request to the backend to log in with a specific IDP
   * - We retrieve the settings for the IDP from the request + configuration (we need to know which IDP to use, as we have multiple)
   * - We redirect the user to the IDP login page
   * - The user logs in, and the IDP redirects the user back to the callback url
   * - We retrieve the settings for the IDP from the request (using RelayState or the url)
   * - We validate the user's information with the IDP, and issue a token
   */

  constructor(private configService: ConfigService) {
    super(getMultiSamlConfig(configService));
  }

  validate(request: Request, profile: Profile) {
    const provider = getProviderConfiguration(this.configService, request);
    Logger.debug(profile);
    return provider.toSamlUser(profile);
  }
}

const getMultiSamlConfig: (a: ConfigService) => MultiSamlConfig = (c) => ({
  passReqToCallback: true,
  // See if the url includes the login url endpoints that are expected
  getSamlOptions: (req, cb) => {
  // Every request of this service, this function will get called
    // We need to return the correct SAML options for the requested provider
    // e.g. if the requested IDentity Provider is KULeuven, we need to return the SAML options for KULeuven

    const provider = getProviderConfiguration(c, req);

    Logger.debug(`Found provider for idp: ${provider.loginUrl}`);

    if (!provider) cb(new HttpException("Unsupported IDP", 400));
    else
      cb(
          null,
          createSamlOptionsFromConfig(provider, req.query.callbackUrl as string),
      );
  },
});

function getProviderConfiguration(
  configService: ConfigService,
  request: Request,
): providerData {
  // The frontend can choose an IDP to log in with, which will be stored in the request params.
  // If we have a redirect from a foreign IDP, we count on the RelayState to save the IDP.
  // RelayState is a way to preserve data between calls to the service:
  // - The frontend sends us a request to login with a specific IDP
  // - We redirect the user to the IDP
  // - The user is redirected again to this endpoint, this time with identity data
  // - We still need to know for which IDP the user is logging in, but we don't have control of the request to send it
  // - We need the IDP which is redirecting us to send it to us
  // - We can ask the IDP to send us any data we wish to be sent to us along with the user info (in the second call) using RelayState
  const search =
    request.params['idp'] || JSON.parse(request.body.RelayState).idp;
  if (!search) {
    Logger.warn(`IDP not included in request.`);
    return null;
  }
  const data: providerData | undefined = configService
    .getCurrentConfiguration()
    .auth.providers.find((prov) => search === prov.loginUrl);
  if (!data) {
    Logger.warn(`IDP ${search} is not a valid IDP.`);
    return null;
  }
  return data;
}

function createSamlOptionsFromConfig(
  config: providerData,
  callbackUrl?: string,
): SamlConfig {
  assert(!!config, "Config can't be null!");
  const privateKeyPath = path.join(__dirname, AUTH_CREDENTIALS_DIR, "key.pem");
  const decryptionPvkPath = path.join(
    __dirname,
    AUTH_CREDENTIALS_DIR,
    "key.pem",
  );
  if (!fs.existsSync(privateKeyPath)) {
    Logger.error(`Private key at ${privateKeyPath} not found.`);
    return null;
  }
  if (!fs.existsSync(decryptionPvkPath)) {
    Logger.error(`Decryption key at ${decryptionPvkPath} not found.`);
    return null;
  }
  return {
    ...readMetadata(AUTH_CREDENTIALS_DIR + config.metadataFile),
    callbackUrl: config.callbackUrl,
    issuer: config.issuer,
    privateKey: fs.readFileSync(privateKeyPath),
    decryptionPvk: fs.readFileSync(decryptionPvkPath),
    additionalParams: {
      RelayState: JSON.stringify({
        idp: config.loginUrl,
        callbackUrl: callbackUrl,
      }),
    },
    disableRequestedAuthnContext: true,
    identifierFormat: null,
  };
}

const metadataMap = new Map<string, string>();

export function readMetadata(localP: string) {
  if (!metadataMap.has(localP)) {
    const metadataPath = path.join(__dirname, AUTH_CREDENTIALS_DIR, localP);
    if (!fs.existsSync(metadataPath)) {
      Logger.error(`saml metadata file at ${metadataPath} not found.`);
      return null;
    }
    const data = fs.readFileSync(metadataPath).toString();
    metadataMap.set(localP, data);
  }
  try {
    const metadataReader = new MetadataReader(metadataMap.get(localP));
    return toPassportConfig(metadataReader);
  } catch (e) {
    Logger.error(`Failed to parse SAML metadata file of ${localP}.`);
    return null;
  }
}

export function getSamlMetadata() {
  // Get metadata from metadata.xml file
  return fs.readFileSync(
    path.join(__dirname, AUTH_CREDENTIALS_DIR, "metadata.xml"),
    "utf-8",
  );
}
