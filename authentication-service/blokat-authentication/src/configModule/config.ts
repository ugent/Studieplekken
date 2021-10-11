import { Profile } from "passport-saml/lib/passport-saml";

type NoHTTPSOptions = { enabled: false };
type HTTPSOptions = {
  enabled: true;
  certLocation: string;
  keyLocation: string;
};

export interface SamlUser {
  email: string;
  firstName: string;
  lastName: string;
  id: string;
}

export type providerData = {
    loginUrl: string;
    callbackUrl: string;
    issuer: string;
    metadataFile: string;
    toSamlUser: (a: Profile) => SamlUser;
  };

export interface Configuration {
  auth: {
    providers: providerData[];
  }

  https: NoHTTPSOptions | HTTPSOptions;

  database: {
    url: string,
    username: string,
    password: string,
    port: string
  }
}
