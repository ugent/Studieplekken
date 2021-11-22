export enum APPLICATION_TYPE {
  BLOK_AT,
  MINI_THERMIS,
}

export const environment = {
  production: true,
  applicationType: APPLICATION_TYPE.MINI_THERMIS,
  casFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/cas',
  hoGentFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/hogent',
  arteveldeHSFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/artevelde',
  lucaFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/luca',
  odiseeFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/odisee',
  stadGentFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/stadgent',
  accessToken: "pk.eyJ1IjoiYXVyaXNhdWRlbnRpcyIsImEiOiJja3M3cGdqN24xMnNsMm5zM2tlN2d4a3MxIn0.i69TQAR5E1VCJJXMP_2QlA",

  useExternalDashboard: true,
  externalDashboardUrl: "https://qa.stad.gent/nl/student-gent/studeren/bloklocaties-0"
};
