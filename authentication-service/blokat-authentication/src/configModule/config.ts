type NoHTTPSOptions = { enabled: false };
type HTTPSOptions = {
  enabled: true;
  certLocation: string;
  keyLocation: string;
};

export interface Configuration {
  auth: {
    callbackUrl: string;
    issuer: string;
    metadataFile: string;
  };

  https: NoHTTPSOptions | HTTPSOptions;

  database: {
    url: string,
    username: string,
    password: string,
    port: string
  }
}
