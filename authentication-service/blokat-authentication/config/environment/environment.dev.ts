import { Configuration } from "src/configModule/config";

export const configuration: Configuration = {
  auth: {
    callbackUrl: 'https://localhost:8087/api/SSO/saml',
    issuer: 'https://localhost:8087',
    metadataFile: "metadata-samltest.xml",
  },

  https: {
      enabled: true,
      certLocation: "config/auth/cert.pem",
      keyLocation: "config/auth/key.pem"
  },

  database: {
    username: ***REMOVED***,
    password: ***REMOVED***,
    url: "localhost",
    port: "5432",
  }
};