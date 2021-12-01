import { Configuration, Institution } from 'src/configModule/config';

export const configuration: Configuration = {
  port: 8080,
  auth: {
    providers: [
      {
        loginUrl: 'okta',
        callbackUrl: 'https://localhost:8080/api/SSO/saml',
        issuer: 'https://localhost:8080',
        metadataFile: 'metadata-okta.xml',
        toSamlUser: (a: any) => ({
          firstName: a.firstName,
          lastName: a.lastName,
          email: a.email,
          id: a.nameID,
          institution: Institution.UGENT,
        }),
      },
    ],

    cas: {
      serverBaseURL: 'https://localhost:8080',
    },
    testEndpoint: true,
    allowedClientCallbacks: [
      'https://localhost:8086/login',
      'https://localhost:8080/login',
      'https://localhost:8087/login',
      'https://localhost:4200/login',
      'https://localhost:4200/auth/local/login'
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

  jwtKey: '***REMOVED***',
};
