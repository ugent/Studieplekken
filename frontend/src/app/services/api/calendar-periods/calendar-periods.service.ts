import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {CalendarPeriod} from '../../../shared/model/CalendarPeriod';
import {Pair} from '../../../shared/model/helpers/Pair';
import {Location} from '../../../shared/model/Location';
import {Observable, } from 'rxjs';
import { map } from 'rxjs/internal/operators/map';
import { filter } from 'rxjs/internal/operators/filter';
import {api} from '../endpoints';
import {LocationStatus} from '../../../app.constants';
import {Cache} from '../../../shared/cache/Cache';

@Injectable({
  providedIn: 'root'
})
export class CalendarPeriodsService {

  constructor(private http: HttpClient) {
  }

  // tslint:disable-next-line: max-line-length
  statusCache: Cache<number, Pair<LocationStatus, string>> = new Cache<number, Pair<LocationStatus, string>>(this.http, (arg: Location) => arg.locationId);

  getCalendarPeriodsOfLocation(locationId: number): Observable<CalendarPeriod[]> {
    return this.http.get<any>(api.calendarPeriods
      .replace('{locationId}', String(locationId)))
      .pipe(filter(s => !!s), map(ls => ls.map((s: any) => CalendarPeriod.fromJSON(s))));
  }

  /**
   * Retrieve the status of the location
   */
  getStatusOfLocation(locationId: number, invalidateCache: boolean = false): Observable<Pair<LocationStatus, string>> {
    const url = api.locationStatus.replace('{locationId}', String(locationId));
    return this.statusCache.getValue(locationId, url, invalidateCache);
  }

  addCalendarPeriods(calendarPeriods: CalendarPeriod[]): Observable<void> {
    return this.http.post<void>(api.addCalendarPeriods, calendarPeriods.map(s => s.toJSON()));
  }

  /**
   * Note: this function does not require from.length === to.length, as opposed to
   * the corresponding DAO method does. The comparison between from and to will happen in
   * the controller layer, and the correct add/delete/update methods to be called
   * will be invoked.
   */
  updateCalendarPeriod(locationId: number, from: CalendarPeriod[], to: CalendarPeriod): Observable<void> {
    const body = {previous: from, toUpdate: to};
    return this.http.put<void>(api.updateCalendarPeriods.replace('{locationId}', String(locationId)), body);
  }

  deleteCalendarPeriods(period: CalendarPeriod): Observable<void> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: period
    };
    return this.http.delete<void>(api.deleteCalendarPeriods, options);
  }
}
