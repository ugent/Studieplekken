import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {LocationStat, LocationStatConstructor} from '../../../shared/model/LocationStat';
import {api} from '../endpoints';
import {map} from 'rxjs/internal/operators/map';
import {LocationOverviewStat, LocationOverviewStatConstructor} from '../../../shared/model/LocationOverviewStat';

@Injectable({
    providedIn: 'root',
})
export class StatsService {
    constructor(private http: HttpClient) {
    }

    getStats(): Observable<LocationStat[]> {
        return this.http.get<LocationStat[]>(api.getStats).pipe(map(x => x.map(LocationStatConstructor.newFromObj)));
    }

    getStatsAtDate(date: string): Observable<LocationStat[]> {
        return this.http.get<LocationStat[]>(api.getStatsAtDate.replace('{date}', date))
            .pipe(map(x => x.map(LocationStatConstructor.newFromObj)));
    }

    getStatsForLocationFromTo(locationId: number, from: string, to: string): Observable<LocationOverviewStat> {
        return this.http.get<LocationOverviewStat>(api.getStatsForLocation
            .replace('{locationId}', locationId.toString())
            .replace('{from}', from)
            .replace('{to}', to))
            .pipe(map(x => LocationOverviewStatConstructor.newFromObj(x)));
    }

}
