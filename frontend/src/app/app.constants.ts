export const defaultLocationImage = 'assets/images/default_location.jpg';
export const defaultTeaserImages = ['assets/images/teaser/teaser1.jpg', 'assets/images/teaser/teaser2.jpg', 'assets/images/teaser/teaser3.jpg', 'assets/images/teaser/teaser4.jpg'];

export const userWantsTLogInLocalStorageKey = 'userWantsTLogIn';
export const authenticationWasExpiredUrlLSKey = 'authenticationWasExpiredUrl';

/*
 * The amount of milliseconds that a feedback div should be shown
 */
export const msToShowFeedback = 10000;

export const penaltyPointsLimit = 100;

export enum LocationStatus {
    OPEN = 'OPEN',
    CLOSED = 'CLOSED',
    CLOSED_UPCOMING = 'CLOSED_UPCOMING',
    CLOSED_ACTIVE = 'CLOSED_ACTIVE',
};

export enum Language {
    DUTCH = 'nl',
    ENGLISH = 'en'
}

export const defaultOpeningHour = 8;
export const defaultClosingHour = 17;
export const SEAT_COUNT_THRESHOLD = 10_000;

/**
 * Representative colors per institution.
 */
export const HOIColors = {
    HoGent: '#333',
    UGent: '#1E64C8',
    Arteveldehogeschool: '#f58732',
    KULeuven: '#6ac2ee',
    Odisee: '#1f416b',
    Luca: '#feb3d2'
};

export const institutions = [
    'UGent',
    'HoGent',
    'Arteveldehogeschool', 
    'StadGent',
    'Luca',
    'Odisee', 
    'Other'
];

export const contact = {
    'email': 'studieplekken@gentsestudentenraad.be',
    'address': 'Hoveniersberg 24, 9000 Gent',
    'building': 'De Therminal',
    'website': 'https://bloklocaties.stad.gent'
};