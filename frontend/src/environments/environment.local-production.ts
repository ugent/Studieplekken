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
    kulFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/login/kuleuven',
    otherFlowTriggerUrl: 'https://studieplekken-dev.ugent.be/auth/local/login',

    useExternalDashboard: false,
    externalDashboardUrl: '',
    showStagingWarning: false
};
