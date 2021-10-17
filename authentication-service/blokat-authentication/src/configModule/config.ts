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

const dummySamlUser : SamlUser = {
  email: "",
  firstName: "",
  lastName: "",
  id: "",
  institution: Institution.UGENT
};

export function isSamlUser(obj: any): obj is SamlUser {
  for (const key in dummySamlUser) {
    if (!(key in obj)) {
      return false;
    }
  }
  return true;
}

export function missingSamlUserFields(obj: any): string[] {
  const missingFields = [];
  for (const key in dummySamlUser) {
    if (!(key in obj)) {
      missingFields.push(key);
    }
  }
  return missingFields;
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
    allowedClientCallbacks: string[]
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
