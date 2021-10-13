// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export enum APPLICATION_TYPE {
  BLOK_AT,
  MINI_THERMIS,
}

export const environment = {
  production: false,
  applicationType: APPLICATION_TYPE.MINI_THERMIS,
  casFlowTriggerUrl: 'https://localhost:8080/auth/login/cas',
  hoGentFlowTriggerUrl: 'https://localhost:8080/auth/login/hogent',
  arteveldeHSFlowTriggerUrl: 'https://localhost:8080/auth/login/artevelde',
  accessToken: 'pk.eyJ1Ijoic21lc3NpZSIsImEiOiJja3JnMzR2ZXEwZG82MnVrd3l5NHFnYTk1In0.jER8bBqoIeiNrKX-HGlrZQ'
};
