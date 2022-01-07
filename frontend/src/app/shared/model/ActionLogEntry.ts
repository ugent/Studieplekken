import { Moment } from 'moment';
import * as moment from 'moment';
import {User, UserConstructor} from './User';



export class ActionLogEntry {
    type: string;
    domain: string;
    domainId: number | undefined;
    user: User;
    userFullName: string;
    time: Moment;

    constructor(type: string, domain: string, domainId: number | undefined, user: User, time: Moment) {
        this.type = type;
        this.domain = domain;
        this.domainId = domainId? domainId : undefined;
        this.user = user;
        this.userFullName = user.firstName + ' ' + user.lastName;
        this.time = time;
    }

    static fromJSON(json: any): ActionLogEntry {
        return new ActionLogEntry(
            json.type,
            json.domain,
            json.domainId,
            UserConstructor.newFromObj(json.user),
            moment(json.time)
        );
    }
}