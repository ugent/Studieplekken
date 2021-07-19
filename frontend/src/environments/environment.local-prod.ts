export enum APPLICATION_TYPE {
  BLOK_AT,
  MINI_THERMIS,
}

export const environment = {
  production: true,
  applicationType: APPLICATION_TYPE.MINI_THERMIS,
  casFlowTriggerUrl: 'https://localhost:8080/api/login/cas',
  hoGentFlowTriggerUrl: 'https://localhost:8080/api/login/saml?idp=https://idp.hogent.be/idp',
  arteveldeHSFlowTriggerUrl: 'https://localhost:8080/api/login/saml?idp=https://sts.windows.net/b6e080ea-adb9-4c79-9303-6dcf826fb854/',
};
