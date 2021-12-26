import { Moment } from 'moment';
import * as moment from 'moment';
import {User, UserConstructor} from './User';



export class ActionLogEntry {
    type: string;
    description: string;
    user: User;
    userFullName: string;
    time: Moment;

    constructor(type: string, description: string, user: User, time: Moment) {
        this.type = type;
        this.description = description;
        this.user = user;
        this.userFullName = user.firstName + ' ' + user.lastName;
        this.time = time;

    }

    static fromJSON(json: any): ActionLogEntry {
        return new ActionLogEntry(
            json.type,
            json.description,
            UserConstructor.newFromObj(json.user),
            moment(json.time)
        );
    }
}