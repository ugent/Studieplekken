export interface InstitutionOverviewStat {
    institution: string;
    outgoingStudentsPerHOI: Map<string, number>;
    incomingStudentsPerHOI: Map<string, number>;
    reservationsPerDay: Map<string, number>;
}

export class InstitutionOverviewStatConstructor {
    static new(): InstitutionOverviewStat {
        return {
            institution: '',
            outgoingStudentsPerHOI: new Map<string, number>(),
            incomingStudentsPerHOI: new Map<string, number>(),
            reservationsPerDay: new Map<string, number>(),
        };
    }

    static newFromObj(obj: InstitutionOverviewStat): InstitutionOverviewStat {
        if (obj === null) {
            return null;
        }

        return {
            institution: obj.institution,
            outgoingStudentsPerHOI: obj.outgoingStudentsPerHOI,
            incomingStudentsPerHOI: obj.incomingStudentsPerHOI,
            reservationsPerDay: obj.reservationsPerDay,
        };
    }
}
