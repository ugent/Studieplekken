import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {CalendarPeriod} from '../../../shared/model/CalendarPeriod';
import {Pair} from '../../../shared/model/helpers/Pair';
import {Observable} from 'rxjs';
import {api, LocationStatus} from '../../../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class CalendarPeriodsService {

  constructor(private http: HttpClient) { }

  getCalendarPeriodsOfLocation(locationName: string): Observable<CalendarPeriod[]> {
    return this.http.get<CalendarPeriod[]>(api.calendarPeriods
      .replace('{locationName}', locationName));
  }

  getStatusOfLocation(locationName: string): Observable<Pair<LocationStatus, string>> {
    return this.http.get<Pair<LocationStatus, string>>(api.locationStatus
      .replace('{locationName}', locationName));
  }

  addCalendarPeriods(calendarPeriods: CalendarPeriod[]): Observable<void> {
    return this.http.post<void>(api.addCalendarPeriods, calendarPeriods);
  }

  /**
   * Note: this function does not require from.length === to.length, as opposed to
   * the corresponding DAO method does. The comparison between from and to will happen in
   * the controller layer, and the correct add/delete/update methods to be called
   * will be invoked.
   */
  updateCalendarPeriods(locationName: string, from: CalendarPeriod[], to: CalendarPeriod[]): Observable<void> {
    const body = [from, to];
    return this.http.put<void>(api.updateCalendarPeriods.replace('{locationName}', locationName), body);
  }

  deleteCalendarPeriods(periods: CalendarPeriod[]): Observable<void> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: periods
    };
    return this.http.delete<void>(api.deleteCalendarPeriods, options);
  }
}
