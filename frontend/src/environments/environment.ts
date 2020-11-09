// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export enum APPLICATION_TYPE {
  BLOK_AT,
  MINI_THERMIS
}

export const environment = {
  production: false,
  applicationType: APPLICATION_TYPE.MINI_THERMIS,
  casFlowTriggerUrl: 'https://localhost:8080/api/login/cas'
};
