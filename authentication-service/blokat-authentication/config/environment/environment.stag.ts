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
          email: a.nameID,
          id: a[
            'http://schemas.microsoft.com/identity/claims/objectidentifier'
          ],
          institution: Institution.ARTEVELDE,
        }),
      },
      {
        loginUrl: 'luca',
        callbackUrl: 'https://studieplekken-dev.ugent.be/api/SSO/saml',
        issuer: 'https://studieplekken-dev.ugent.be/api/metadata/saml',
        metadataFile: 'sso-kuleuven-off.xml',
        toSamlUser: (a: any) => ({
          firstName: a['urn:oid:2.5.4.42'],
          lastName: a['urn:oid:2.5.4.4'],
          email: a['mail'],
          id: a['urn:mace:kuleuven.be:dir:attribute-def:KULMoreUnifiedUID'],
          institution: Institution.LUCA,
        }),
      },
      {
        loginUrl: 'odisee',
        callbackUrl: 'https://studieplekken-dev.ugent.be/api/SSO/saml',
        issuer: 'https://studieplekken-dev.ugent.be/api/metadata/saml',
        metadataFile: 'sso-kuleuven-off.xml',
        toSamlUser: (a: any) => ({
          firstName: a['urn:oid:2.5.4.42'],
          lastName: a['urn:oid:2.5.4.4'],
          email: a['mail'],
          id: a['urn:mace:kuleuven.be:dir:attribute-def:KULMoreUnifiedUID'],
          institution: Institution.ODISEE,
        }),
      },
      {
        loginUrl: 'kuleuven',
        callbackUrl: 'https://studieplekken-dev.ugent.be/api/SSO/saml',
        issuer: 'https://studieplekken-dev.ugent.be/api/metadata/saml',
        metadataFile: 'sso-kuleuven-off.xml',
        toSamlUser: (a: any) => ({
          firstName: a['urn:oid:2.5.4.42'],
          lastName: a['urn:oid:2.5.4.4'],
          email: a['mail'],
          id: a['urn:mace:kuleuven.be:dir:attribute-def:KULMoreUnifiedUID'],
          institution: Institution.KU_LEUVEN,
        }),
      },
      {
        loginUrl: 'stadgent',
        callbackUrl: 'https://studieplekken-dev.ugent.be/api/SSO/saml',
        issuer: 'https://studieplekken-dev.ugent.be/api/metadata/saml',
        metadataFile: 'metadata-stadgent.xml',
        toSamlUser: (a: any) => ({
          firstName:
            a[
              'http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname'
            ],
          lastName:
            a['http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname'],
          email: a.nameID,
          id: a[
            'http://schemas.microsoft.com/identity/claims/objectidentifier'
          ],
          institution: Institution.STAD_GENT,
        }),
      },
    ],
    cas: {
      serverBaseURL: 'https://studieplekken-dev.ugent.be',
    },

    testEndpoint: true,
    allowedClientCallbacks: [
      'https://studieplekken-dev.ugent.be/login',
      'https://localhost:8086/login',
      'https://localhost:8080/login',
      'https://localhost:8087/login',
      'https://localhost:4200/login',
      'https://bloklocaties.stad.gent/login',
    ],
  },

  https: {
    enabled: false,
  },

  database: {
    username: 'blokat',
    password: '***REMOVED***',
    url: 'user-db',
    port: '5432',
  },

  jwtKey: '***REMOVED***',
};
