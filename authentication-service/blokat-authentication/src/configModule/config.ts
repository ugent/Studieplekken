import { Profile } from 'passport-saml/lib/passport-saml';

type NoHTTPSOptions = { enabled: false };
type HTTPSOptions = {
  enabled: true;
  certLocation: string;
  keyLocation: string;
};

export enum Institution {
  HOGENT = 'HoGent',
  UGENT = 'UGent',
  ARTEVELDE = 'Arteveldehogeschool',
}

export interface SamlUser {
  email: string;
  firstName: string;
  lastName: string;
  id: string;
  institution: Institution;
}

export type providerData = {
  loginUrl: string;
  callbackUrl: string;
  issuer: string;
  metadataFile: string;
  toSamlUser: (a: Profile) => SamlUser;
};

export interface Configuration {
  port: number;
  auth: {
    providers: providerData[];
    cas: {
      serverBaseURL: string;
    };

    testEndpoint: boolean;
  };

  https: NoHTTPSOptions | HTTPSOptions;

  database: {
    url: string;
    username: string;
    password: string;
    port: string;
  };

  jwtKey: string;
}
