export const api = {
  whoAmI: '/api/whoAmI',
  logout: '/api/logout',

  // AUTHORITY
  authorities: '/api/authority',
  authority: '/api/authority/{authorityId}',
  addAuthority: '/api/authority',
  updateAuthority: '/api/authority/{authorityId}',
  deleteAuthority: '/api/authority/{authorityId}',
  locationsInAuthoritiesOfUser: '/api/authority/users/{userId}/locations',

  // CALENDAR_PERIODS
  calendarPeriods: '/api/locations/calendar/{locationId}',
  locationStatus: '/api/locations/calendar/{locationId}/status',
  addTimeslots: '/api/locations/timeslots',
  deleteTimeslot: '/api/locations/timeslots',
  updateTimeslot: '/api/locations/timeslots',
  setRepeatable: '/api/locations/timeslots/{timeslotId}/repeatable',

  // TIMESLOTS
  timeslots: '/api/locations/timeslots/{locationId}',
  timeslotDetails: '/api/locations/timeslots/details/{timeslotId}',


  // CALENDAR_PERIODS_FOR_LOCKERS
  calendarPeriodsForLockers: '/api/locations/lockerCalendar/{locationId}',
  addCalendarPeriodsForLockers: '/api/locations/lockerCalendar',
  updateCalendarPeriodsForLockers: '/api/locations/lockerCalendar/{locationId}',
  deleteCalendarPeriodsForLockers: '/api/locations/lockerCalendar',

  // LOCATIONS
  locations: '/api/locations',
  locationsUnapproved: '/api/locations/unapproved',
  location: '/api/locations/{locationId}',
  addLocation: '/api/locations',
  updateLocation: '/api/locations/{locationId}',
  approveLocation: '/api/locations/{locationId}/approval',
  deleteLocation: '/api/locations/{locationId}',
  setupTagsForLocation: '/api/locations/tags/{locationId}',
  allReservableFroms: '/api/locations/nextReservableFroms',
  locationVolunteers: '/api/locations/{locationId}/volunteers',
  addLocationVolunteer: '/api/locations/{locationId}/volunteers/{userId}',

  // LOCATION_RESERVATIONS
  locationReservationsOfUser: '/api/locations/reservations/user',
  locationReservationsOfLocation:
    '/api/locations/reservations/timeslot/{seqnr}',
  locationReservationsOfLocationFrom: '/api/locations/reservations/from',
  locationReservationsOfLocationUntil: '/api/locations/reservations/until',
  locationReservationsOfLocationFromAndUntil:
    '/api/locations/reservations/fromAndUntil',
  addLocationReservation: '/api/locations/reservations',
  deleteLocationReservation: '/api/locations/reservations',
  updateAttendance:
    `/api/locations/reservations/{userid}/{seqnr}/attendance`,
  locationReservationsWithLocationOfUser:
    '/api/locations/reservations/{userId}',
  locationReservationsOfNotScannedUsers:
    '/api/locations/reservations/not-scanned',

  // USER_LOCATION_SUBSCRIPTIONS
  userLocationSubscriptions: '/api/locations/{locationId}/subscriptions',

  // AUTHORITY
  buildings: '/api/building',
  building: '/api/building/{buildingId}',
  addBuilding: '/api/building',
  updateBuilding: '/api/building/{buildingId}',
  deleteBuilding: '/api/building/{buildingId}',

  // LOCKER_RESERVATIONS
  lockerReservationsOfUser: '/api/lockers/reservations/user',
  lockerReservationsOfLocation: '/api/lockers/reservations/location',
  updateLockerReservation: '/api/lockers/reservations',
  deleteLockerReservation: '/api/lockers/reservations',

  // USERS
  userByAUGentId: '/api/account/id',
  userByBarcode: '/api/account/barcode',
  usersByFirstName: '/api/account/firstName',
  usersByLastName: '/api/account/lastName',
  usersByFirstAndLast: '/api/account/firstAndLastName',
  changePassword: '/api/account/password',
  updateUser: '/api/account/{userId}',
  updateUserSettings: '/api/account/{userId}/settings',
  hasUserAuthorities: '/api/account/{userId}/has/authorities',
  hasUserVolunteered: '/api/account/{userId}/has/volunteered',
  getAdmins: '/api/account/admins',
  getManageableLocations: '/api/account/{userId}/manageable/locations',

  // CALENDAR ICAL
  calendarLink: '/api/ical/{userId}/{calendarId}',

  // LOCKERS
  lockersStatusesOfLocation: '/api/lockers/status/{locationId}',

  // PENALTY_BOOK
  penaltiesByUserId: '/api/penalties/{id}',
  addPenalty: '/api/penalties',
  deletePenalty: '/api/penalties',

  // PENALTY_EVENTS
  penaltyEvents: '/api/penalties/events',
  addPenaltyEvent: '/api/penalties/events',
  updatePenaltyEvent: '/api/penalties/events/{code}',
  deletePenaltyEvent: '/api/penalties/events',
  getAllPenalties: '/api/penalties',

  // TAGS
  tags: '/api/tags',
  addTag: '/api/tags',
  updateTag: '/api/tags',
  deleteTag: '/api/tags/{tagId}',

  // ROLES_USER_AUTHORITY
  usersInAuthority: '/api/authority/{authorityId}/users',
  authoritiesOfUser: '/api/authority/users/{userId}',
  addUserToAuthority: '/api/authority/{authorityId}/user/{userId}',
  deleteUserFromAuthority: '/api/authority/{authorityId}/user/{userId}',

  // MISCELLANEOUS QUERIES
  openingHoursOverview: '/api/locations/overview/opening/{year}/{weekNr}',

  // Scanning functionality
  scanningLocations: '/api/scan/locations',
  usersToScanAtLocation: '/api/scan/users/{locationId}',

  // Action log
  actions: '/api/actions',

  // STATS
  getStats: 'api/stats',
  getStatsAtDate: 'api/stats/{date}',
  getStatsForLocation: 'api/stats/locations/{locationId}/from/{from}/to/{to}',
  getStatsForInstitution: 'api/stats/institutions/{institutionLocations}/students/{institutionStudents}/from/{from}/to/{to}',

  // TOKENS
  tokens: '/api/tokens',
};
