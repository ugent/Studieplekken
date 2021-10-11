import { Configuration } from "src/configModule/config";

export const configuration: Configuration = {
  auth: {
    providers: [
      {
        loginUrl: "okta",
        callbackUrl: 'https://localhost:8087/api/SSO/saml',
        issuer: 'https://localhost:8087',
        metadataFile: 'metadata-okta.xml',
        toSamlUser: (a: any) => ({ firstName: a.firstName, lastName: a.lastName, email: a.email, id: a.nameID })
      },
    ],
  },

  https: {
    enabled: true,
    certLocation: 'config/auth/cert.pem',
    keyLocation: 'config/auth/key.pem',
  },

  database: {
    username: ***REMOVED***,
    password: ***REMOVED***,
    url: 'localhost',
    port: '5432',
  },
};