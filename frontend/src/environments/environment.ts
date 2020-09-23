// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export enum APPLICATION_TYPE {
  BLOK_AT,
  MINI_THERMIS
}

export const environment = {
  production: false,
  applicationType: APPLICATION_TYPE.MINI_THERMIS
};

export const api = {
  whoAmI: '/whoAmI',
  logout: '/logout',

  // CALENDAR_PERIODS
  calendarPeriods: '/api/locations/calendar/{locationName}',
  addCalendarPeriods: '/api/locations/calendar',
  updateCalendarPeriods: '/api/locations/calendar/{locationName}',
  deleteCalendarPeriods: '/api/locations/calendar',

  // CALENDAR_PERIODS_FOR_LOCKERS
  calendarPeriodsForLockers: '/api/locations/lockerCalendar/{locationName}',
  addCalendarPeriodsForLockers: '/api/locations/lockerCalendar',
  updateCalendarPeriodsForLockers: '/api/locations/lockerCalendar',
  deleteCalendarPeriodsForLockers: '/api/locations/lockerCalendar',

  // LOCATIONS
  locations: '/api/locations',
  location: '/api/locations/{locationName}',
  addLocation: '/api/locations',
  updateLocation: '/api/locations/{locationName}',
  deleteLocation: '/api/locations/{locationName}',
  numberOfReservations: '/api/locations/{locationName}/reservations/count',

  // LOCATION_RESERVATIONS
  locationReservationsOfUser: '/api/locations/reservations/user',
  locationReservationsOfLocation: '/api/locations/reservations/location',
  locationReservationsOfLocationFrom: '/api/locations/reservations/from',
  locationReservationsOfLocationUntil: '/api/locations/reservations/until',
  locationReservationsOfLocationFromAndUntil: '/api/locations/reservations/fromAndUntil',
  deleteLocationReservation: '/api/locations/reservations',

  // LOCKERS
  lockersStatusesOfLocation: '/api/lockers/status/{locationName}',

  // LOCKER_RESERVATIONS
  lockerReservationsOfUser: '/api/lockers/reservations/user',
  lockerReservationsOfLocation: '/api/lockers/reservations/location',
  updateLockerReservation: '/api/lockers/reservations',
  deleteLockerReservation: '/api/lockers/reservations',
  lockerReservationsByUserId: '/api/lockers/reservations/{userId}',

  // USERS
  userByAUGentId: '/api/account/id',
  userByBarcode: '/api/account/barcode',
  userByMail: '/api/account/mail',
  usersByFirstName: '/api/account/firstName',
  usersByLastName: '/api/account/lastName',
  usersByFirstAndLast: '/api/account/firstAndLastName',
  changePassword: '/api/account/password',
  updateUser: '/api/account/{id}',

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

  // LOCATION_TAGS
  assignTagsToLocation: '/api/tags/location/assign/{locationName}',
  tagsFromLocation: '/api/tags/location/{locationName}',
  assignedTagsFromLocation: '/api/tags/location/assign/{locationName}',
  reconfigureAllowedTags: '/api/tags/location/{locationName}'
};

export const vars = {
  defaultLocationImage: 'assets/images/default_location.jpg',
  casFlowTriggerUrl: 'https://localhost:8080/login/cas'
};

/*
 * The amount of milliseconds that a feedback div should be shown
 */
export const msToShowFeedback = 10000;

/*
 * The PenaltyEvent code for a manual entry
 */
export const penaltyEventCodeForManualEntry = 16663;

/*
 * The roles that are available in the application
 *
 * Important: make sure that the roles in 'rolesArray'
 * are put in the 'Role' enum as well!
 *
 * And, do not forget to translate the role if you would
 * be adding a role (<lang>.json -> general.roles)
 */
export const rolesArray = ['ADMIN', 'EMPLOYEE', 'STUDENT'];

export enum Role {
  STUDENT = 'STUDENT',
  EMPLOYEE = 'EMPLOYEE',
  ADMIN = 'ADMIN'
}

/*
 * This variable maps all the supported languages to its
 * database representation (LANGUAGES.enum)
 */
export const languageAsEnum = {
  nl: 'DUTCH',
  en: 'ENGLISH'
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
