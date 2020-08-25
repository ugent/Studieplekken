// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false
};

export const api = {
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
  updateLocation: '/api/locations/{locationName}',
  numberOfReservations: '/api/locations/{locationName}/reservations/count',

  // LOCATION_RESERVATIONS
  locationReservationsByUserId: '/api/locations/reservations/{userId}',

  // LOCKER_RESERVATIONS
  lockerReservationsByUserId: '/api/lockers/reservations/{userId}',

  // USERS
  user_by_mail: '/api/account/{mail}',

  // PENALTY_BOOK
  penalties_by_user_id: '/api/penalties/{userId}'
};

export const vars = {
  defaultLocationImage: 'assets/images/default_location.jpg'
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
