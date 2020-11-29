import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {CalendarPeriod} from '../../../shared/model/CalendarPeriod';
import {Pair} from '../../../shared/model/helpers/Pair';
import {Observable} from 'rxjs';
import { map } from 'rxjs/internal/operators/map';
import { filter } from 'rxjs/internal/operators/filter';
import {api} from '../endpoints';
import {LocationStatus} from '../../../app.constants';

@Injectable({
  providedIn: 'root'
})
export class CalendarPeriodsService {

  constructor(private http: HttpClient) {
  }

  getCalendarPeriodsOfLocation(locationName: string): Observable<CalendarPeriod[]> {
    return this.http.get<any>(api.calendarPeriods
      .replace('{locationName}', locationName))
      .pipe(filter(s => !!s), map(ls => ls.map(s => CalendarPeriod.fromJSON(s))));
  }

  getStatusOfLocation(locationName: string): Observable<Pair<LocationStatus, string>> {
    return this.http.get<Pair<LocationStatus, string>>(api.locationStatus
      .replace('{locationName}', locationName));
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
  updateCalendarPeriod(locationName: string, from: CalendarPeriod[], to: CalendarPeriod): Observable<void> {
    const body = {previous: from, toUpdate: to};
    return this.http.put<void>(api.updateCalendarPeriods.replace('{locationName}', locationName), body);
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
