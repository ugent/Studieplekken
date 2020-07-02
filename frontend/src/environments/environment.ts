// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod=true` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

import {IRoles} from "../interfaces/IRoles";

export const environment = {
  production: false
};

/**
 *
 */
export const baseHref = '';

/**
 * Collection of the roles used in this application
 */
export const roles: IRoles = {
  admin: 'ADMIN',
  employee: 'EMPLOYEE',
  student: 'STUDENT'
};

/**
 * Collection of the languages supported by this application
 * This corresponds with the enum Language in the Backend (be.ugent.blok2.helpers.Language)
 */
export const appLanguages = {
  'en': "ENGLISH",
  'nl': "DUTCH"
};

/**
 *  Note the importance of the location of the translation, the order needs to be the same as how the
 *  languages are ordered within appLanguages: first 'en', then 'nl'.
 */
export const languageTranslations = {
  "ENGLISH": ["English", "Engels"],
  "DUTCH": ["Dutch", "Nederlands"]
};


export const institutions: String[] = [
  "UGent",
  "HoGent",
  "Artevelde Hogeschool"
];

/**
 * This constant defines the possible header values for the custom HTTP header Authentication-Type"
 * (used in authentication.service.ts)
 */
export const authenticationTypes = {
  augent: "AUGent-Email-Based-Authentication",
  ugent: "CAS-Based-Authentication"
}

//minimum length for passwords
export const minLengthPwd: number = 8;

/**
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.

/**
 * Keep all fetch request urls here
 */
export const urls = {
  newAccount: '/api/account/new',
  locations: '/api/locations',
  scanners: '/api/locations/scanners/',
  scanlocations: '/api/account/scanlocations/',
  locationReservation: '/api/location/reservations',
  lockerReservation: '/api/locker/reservations',
  userBarcode: '/api/barcode',
  accountExists: '/api/account/exists/',
  account: '/api/account/',
  accountEmail: "/api/account/email/",
  accountFirstName: '/api/account/firstName/',
  accountName: '/api/account/name/',
  accountRole: '/api/account/role/',
  session: '/api/account/session/',
  penaltyEvent: '/api/penalties',
  login: '/login',
  dashboard:'/dashboard',
  signin:'/signin',
  signout:'/signout',
  scan: '/scan',
  // when login fails, users are redirected to this url which will show the error messages
  // IF CHANGED HERE, THEN ALSO CHANGE IN THE RESOURCEFILE IN THE BACK-END
  loginfailurl: '/fail',
  accountMail: '/api/account/email/',
  accountVerify:'/api/account/verify',
  websocketsScan: '/scanning',
  scanPageForStudent: '/scanStudent/'
};
