export enum APPLICATION_TYPE {
  BLOK_AT,
  MINI_THERMIS,
}

export const environment = {
  production: true,
  applicationType: APPLICATION_TYPE.MINI_THERMIS,
  casFlowTriggerUrl: 'https://studieplekken.ugent.be/api/login/cas',
};
