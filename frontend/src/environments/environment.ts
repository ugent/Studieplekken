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
  casFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/cas',
  hoGentFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/hogent',
  arteveldeHSFlowTriggerUrl:
    'https://studieplekken-dev.ugent.be/auth/login/artevelde',
  lucaFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/luca',
  odiseeFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/odisee',
  stadGentFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/stadgent',
  kulFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/kuleuven',
  otherFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/local/login',



  accessToken:
    'pk.eyJ1Ijoic21lc3NpZSIsImEiOiJja3JnMzR2ZXEwZG82MnVrd3l5NHFnYTk1In0.jER8bBqoIeiNrKX-HGlrZQ',

  useExternalDashboard: false,
  externalDashboardUrl: "https://qa.stad.gent/nl/student-gent/studeren/bloklocaties-0",
  showStagingWarning: false

};
