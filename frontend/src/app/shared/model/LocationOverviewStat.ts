export interface LocationOverviewStat {
    locationId: number;
    locationName: string;
    reservationsTotal: number;
    reservationsTotalPerHOI: Map<string, number>;
    reservationsPerDay: Map<string, number>;
    reservationsPerDayPerHOI: Map<string, Map<string, number>>;
}

export class LocationOverviewStatConstructor {
    static new(): LocationOverviewStat {
        return {
            locationId: -1,
            locationName: '',
            reservationsTotal: 0,
            reservationsTotalPerHOI: new Map<string, number>(),
            reservationsPerDay: new Map<string, number>(),
            reservationsPerDayPerHOI: new Map<string, Map<string, number>>()
        };
    }

    static newFromObj(obj: LocationOverviewStat): LocationOverviewStat {
        if (obj === null) {
            return null;
        }
        // Foreach entry of reservationsPerDayPerHOI, json deserialize the inner map
        for (const key of Object.keys(obj.reservationsPerDayPerHOI)) {
            const value = obj.reservationsPerDayPerHOI[key];
            obj.reservationsPerDayPerHOI[key] = JSON.parse(value);
        }

        return {
            locationId: obj.locationId,
            locationName: obj.locationName,
            reservationsTotal: obj.reservationsTotal,
            reservationsTotalPerHOI: obj.reservationsTotalPerHOI,
            reservationsPerDay: obj.reservationsPerDay,
            reservationsPerDayPerHOI: obj.reservationsPerDayPerHOI
        };
    }
}
