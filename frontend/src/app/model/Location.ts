import {LocationTag} from './LocationTag';
import {Authority, AuthorityConstructor} from './Authority';
import {Building, BuildingConstructor} from './Building';
import {LocationStatus} from '../app.constants';
import {Pair} from './helpers/Pair';
import {Timeslot} from './Timeslot';

export interface Location {
    locationId: number;
    name: string;
    numberOfSeats: number;
    numberOfLockers: number;
    forGroup: boolean;
    imageUrl: string;
    authority: Authority;
    building: Building;
    descriptionDutch: string;
    descriptionEnglish: string;
    reminderDutch: string;
    reminderEnglish: string;
    assignedTags: LocationTag[];
    status: Pair<LocationStatus, string>;
    currentTimeslot?: Timeslot;
    institution: string;
    usesPenaltyPoints: boolean;
    tomorrowStillAvailable: boolean;
    openDuringWeek: boolean;
    openDuringWeekend: boolean;
    optionalNextUpcomingReservableTimeslot?: Timeslot;
    subscribed: boolean;
    hidden: boolean;
}

export class LocationConstructor {
    static new(): Location {
        return {
            locationId: -1,
            name: '',
            numberOfSeats: 10,
            numberOfLockers: 0,
            forGroup: false,
            imageUrl: '',
            authority: AuthorityConstructor.new(),
            building: BuildingConstructor.new(),
            descriptionDutch: '',
            descriptionEnglish: '',
            reminderDutch: '',
            reminderEnglish: '',
            assignedTags: [],
            status: {first: LocationStatus.CLOSED, second: ''},
            institution: 'Other',
            usesPenaltyPoints: false,
            tomorrowStillAvailable: false,
            openDuringWeek: false,
            openDuringWeekend: false,
            subscribed: false,
            hidden: false
        };
    }

    static newFromObj(obj: Location): Location {
        if (obj === null || obj === undefined) {
            return null;
        }

        return {
            locationId: obj.locationId,
            name: obj.name,
            numberOfSeats: obj.numberOfSeats,
            numberOfLockers: obj.numberOfLockers,
            imageUrl: obj.imageUrl,
            authority: AuthorityConstructor.newFromObj(obj.authority),
            building: BuildingConstructor.newFromObj(obj.building),
            descriptionDutch: obj.descriptionDutch,
            descriptionEnglish: obj.descriptionEnglish,
            reminderDutch: obj.reminderDutch,
            reminderEnglish: obj.reminderEnglish,
            assignedTags: obj.assignedTags,
            forGroup: obj.forGroup,
            status: {first: obj.status.first, second: obj.status.second},
            currentTimeslot: Timeslot.fromJSON(obj.currentTimeslot),
            institution: obj.institution,
            usesPenaltyPoints: obj.usesPenaltyPoints,
            tomorrowStillAvailable: obj.tomorrowStillAvailable,
            openDuringWeek: obj.openDuringWeek,
            openDuringWeekend: obj.openDuringWeekend,
            optionalNextUpcomingReservableTimeslot: Timeslot.fromJSON(obj.optionalNextUpcomingReservableTimeslot),
            subscribed: obj.subscribed,
            hidden: obj.hidden
        };
    }
}
