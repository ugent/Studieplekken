import {decode} from 'html-entities';
import {Authority, AuthorityConstructor} from './Authority';

export class User {
    constructor(
        public userId: string = '',
        public firstName: string = '',
        public lastName: string = '',
        public mail: string = '',
        public password: string = '',
        public penaltyPoints: number = 0,
        public institution: string = '',
        public admin: boolean = false,
        public calendarId: string = '',
        public userAuthorities: Authority[] = [],
        public userVolunteer: unknown[] = [],
        public userSettings: UserSettings = {
            receiveMailConfirmation: false
        }
    ) {
    }

    /**
     * Check if the user is logged in
     *
     * @returns boolean
     */
    isLoggedIn(): boolean {
        return !!this.userId;
    }

    /**
     * Check if the user is an admin
     *
     * @returns boolean
     */
    isAdmin(): boolean {
        return this.isLoggedIn() && this.admin;
    }

    /**
     * Check if the user has any authority
     *
     * @returns boolean
     */
    isAuthority(): boolean {
        return this.isAdmin() || this.userAuthorities.length > 0;
    }

    /**
     * Check if the user is a scanner
     *
     * @returns boolean
     */
    isScanner(): boolean {
        return this.isAuthority() || this.userVolunteer.length > 0;
    }

    /**
     * Check if the user has a certain guard
     *
     * @param guard string
     * @returns boolean
     */
    hasGuard(guard: string): boolean {
        switch (guard) {
            case 'user':
                return this.isLoggedIn();
            case 'scanner':
                return this.isScanner();
            case 'authorities':
                return this.isAuthority();
            case 'admin':
                return this.isAdmin();
        }
        return false;
    }
}

export class UserSettings {
    receiveMailConfirmation: boolean;
}

export class UserConstructor {
    static new(): User {
        return new User();
    }

    static newFromObj(obj: User): User {
        if (obj === null) {
            return null;
        }

        return new User(
            obj.userId,
            decode(obj.firstName),
            decode(obj.lastName),
            obj.mail,
            obj.password,
            obj.penaltyPoints,
            obj.institution,
            obj.admin,
            obj.calendarId,
            obj.userAuthorities.map(v =>
                AuthorityConstructor.newFromObj(v)
            ),
            obj.userVolunteer,
            obj.userSettings
        );
    }
}
