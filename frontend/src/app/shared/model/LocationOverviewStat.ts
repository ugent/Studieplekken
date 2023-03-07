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

        const reservationsTotalPerHOI = new Map(Object.entries(obj.reservationsTotalPerHOI));
        const reservationsPerDay = new Map(Object.entries(obj.reservationsPerDay));
        const reservationsPerDayPerHOI = new Map([...Object.entries(obj.reservationsPerDayPerHOI)].sort((a, b) => {
            return new Date(a[0]).getTime() - new Date(b[0]).getTime();
        }).map(e => {
            return [e[0], JSON.parse(e[1])];
        }));

        return {
            locationId: obj.locationId,
            locationName: obj.locationName,
            reservationsTotal: obj.reservationsTotal,
            reservationsTotalPerHOI: reservationsTotalPerHOI,
            reservationsPerDay: reservationsPerDay,
            reservationsPerDayPerHOI: reservationsPerDayPerHOI
        };
    }
}
