export enum APPLICATION_TYPE {
    BLOK_AT,
    MINI_THERMIS,
  }
export const environment = {
    production: true,
    applicationType: APPLICATION_TYPE.MINI_THERMIS,
    casFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/api/login/cas',
    hoGentFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/api/login/saml?idp=https://idp.hogent.be/idp',
    oktaFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/api/login/saml?idp=http://www.okta.com/exk15hzmbtaSjzq1E5d7',
  };
