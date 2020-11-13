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

  // AUTHORITY
  buildings: '/api/building',
  building: '/api/building/{buildingId}',
  addBuilding: '/api/building',
  updateBuilding: '/api/building/{buildingId}',
  deleteBuilding: '/api/building/{buildingId}',

  // CALENDAR_PERIODS
  calendarPeriods: '/api/locations/calendar/{locationName}',
  locationStatus: '/api/locations/calendar/{locationName}/status',
  addCalendarPeriods: '/api/locations/calendar',
  updateCalendarPeriods: '/api/locations/calendar/{locationName}',
  deleteCalendarPeriods: '/api/locations/calendar',

  // CALENDAR_PERIODS_FOR_LOCKERS
  calendarPeriodsForLockers: '/api/locations/lockerCalendar/{locationName}',
  addCalendarPeriodsForLockers: '/api/locations/lockerCalendar',
  updateCalendarPeriodsForLockers: '/api/locations/lockerCalendar/{locationName}',
  deleteCalendarPeriodsForLockers: '/api/locations/lockerCalendar',

  // LOCATIONS
  locations: '/api/locations',
  location: '/api/locations/{locationName}',
  addLocation: '/api/locations',
  updateLocation: '/api/locations/{locationName}',
  deleteLocation: '/api/locations/{locationName}',
  numberOfReservations: '/api/locations/{locationName}/reservations/count',
  setupTagsForLocation: '/api/locations/tags/{locationName}',

  // LOCATION_RESERVATIONS
  locationReservationsOfUser: '/api/locations/reservations/user',
  locationReservationsOfLocation: '/api/locations/reservations/location',
  locationReservationsOfLocationFrom: '/api/locations/reservations/from',
  locationReservationsOfLocationUntil: '/api/locations/reservations/until',
  locationReservationsOfLocationFromAndUntil: '/api/locations/reservations/fromAndUntil',
  deleteLocationReservation: '/api/locations/reservations',

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
  hasUserAuthorities: '/api/account/{userId}/has/authorities',

  // LOCKERS
  lockersStatusesOfLocation: '/api/lockers/status/{locationName}',

  // PENALTY_BOOK
  penaltiesByUserId: '/api/penalties/{id}',
  addPenalty: '/api/penalties',
  deletePenalty: '/api/penalties',

  // PENALTY_EVENTS
  penaltyEvents: '/api/penalties/events',
  addPenaltyEvent: '/api/penalties/events',
  updatePenaltyEvent: '/api/penalties/events/{code}',
  deletePenaltyEvent: '/api/penalties/events',

  // TAGS
  tags: '/api/tags',
  addTag: '/api/tags',
  updateTag: '/api/tags',
  deleteTag: '/api/tags/{tagId}',

  // ROLES_USER_AUTHORITY
  usersInAuthority: '/api/authority/{authorityId}/users',
  authoritiesOfUser: '/api/authority/users/{userId}',
  addUserToAuthority: '/api/authority/{authorityId}/user/{userId}',
  deleteUserFromAuthority: '/api/authority/{authorityId}/user/{userId}'
};
