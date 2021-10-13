import { Configuration, Institution } from 'src/configModule/config';

export const configuration: Configuration = {
  port: 8087,
  auth: {
    providers: [
      {
        loginUrl: 'hogent',
        callbackUrl: 'https://studieplekken-dev.ugent.be/api/SSO/saml',
        issuer: 'https://studieplekken-dev.ugent.be/api/metadata/saml',
        metadataFile: 'sso-hogent.xml',
        toSamlUser: (a: any) => ({
          firstName: a.first_name,
          lastName: a.last_name,
          email: a.email,
          id: a.user_name,
          institution: Institution.HOGENT,
        }),
      },
      {
        loginUrl: 'artevelde',
        callbackUrl: 'https://studieplekken-dev.ugent.be/api/SSO/saml',
        issuer: 'https://studieplekken-dev.ugent.be/api/metadata/saml',
        metadataFile: 'sso-artevelde.xml',
        toSamlUser: (a: any) => ({
          firstName:
            a[
              'http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname'
            ],
          lastName:
            a['http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname'],
          email: a.email,
          id: a[
            'http://schemas.microsoft.com/identity/claims/objectidentifier'
          ],
          institution: Institution.ARTEVELDE,
        }),
      },
    ],
    cas: {
      serverBaseURL: 'https://studieplekken-dev.ugent.be',
    },
  },

  https: {
    enabled: false,
  },

  database: {
    username: 'blokat',
    password: '***REMOVED***',
    url: 'db',
    port: '5432',
  },
};
