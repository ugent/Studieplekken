import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {CalendarPeriod} from '../../../shared/model/CalendarPeriod';

import {Observable} from 'rxjs';
import {api} from '../../../../environments/environment';
import { map } from 'rxjs/internal/operators/map';
import { filter } from 'rxjs/internal/operators/filter';

@Injectable({
  providedIn: 'root'
})
export class CalendarPeriodsService {

  constructor(private http: HttpClient) { }

  getCalendarPeriodsOfLocation(locationName: string): Observable<CalendarPeriod[]> {
    return this.http.get<any>(api.calendarPeriods
      .replace('{locationName}', locationName))
      .pipe(filter(s => !!s), map(ls => ls.map(s => CalendarPeriod.fromJSON(s))));
  }

  getCalendarPeriods(): Observable<CalendarPeriod[]> {
    return this.http.get<CalendarPeriod[]>(api.allCalendarPeriods);
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
  updateCalendarPeriods(locationName: string, from: CalendarPeriod[], to: CalendarPeriod[]): Observable<void> {
    const body = [from.map(s => s.toJSON()), to.map(s => s.toJSON())];
    return this.http.put<void>(api.updateCalendarPeriods.replace('{locationName}', locationName), body);
  }

  deleteCalendarPeriods(periods: CalendarPeriod[]): Observable<void> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: periods.map(s => s.toJSON())
    };
    return this.http.delete<void>(api.deleteCalendarPeriods, options);
  }
}
