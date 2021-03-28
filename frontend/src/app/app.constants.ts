export const defaultLocationImage = 'assets/images/default_location.jpg';
export const userWantsTLogInLocalStorageKey = 'userWantsTLogIn';

/*
 * The amount of milliseconds that a feedback div should be shown
 */
export const msToShowFeedback = 10000;

/*
 * The PenaltyEvent code for a manual entry
 */
export const penaltyEventCodeForManualEntry = 16663;

/*
 * The roles that are available in the application.
 * This is used for being able to manage the role(s)
 * of a certain user.
 *
 * Do not forget to translate the role if you would
 * be adding a role (<lang>.json -> general.roles)
 */
export const rolesArray = ['ADMIN'];

/*
 * The possible statuses of a location.
 */

export const locationStatusArray = [
  'OPEN',
  'CLOSED',
  'CLOSED_UPCOMING',
  'CLOSED_ACTIVE',
];

export enum LocationStatus {
  OPEN = 'OPEN',
  CLOSED = 'CLOSED',
  CLOSED_UPCOMING = 'CLOSED_UPCOMING',
  CLOSED_ACTIVE = 'CLOSED_ACTIVE',
}

/*
 * This variable maps all the supported languages to its
 * database representation (LANGUAGES.enum)
 */
export const languageAsEnum = {
  nl: 'DUTCH',
  en: 'ENGLISH',
};

export const defaultOpeningHour = 8;

export const defaultClosingHour = 17;

/*
 * These are templates used to translate the title of a Calendar Event used by Angular Calendar
 */
export const calendarEventTitleTemplate = {
  reservableFromNL: 'Reserveren vanaf {datetime}',
  reservableFromEN: 'Reservable from {datetime}',
  notReservableNL: 'Geen reservatie nodig',
  notReservableEN: 'No reservation required',
};
