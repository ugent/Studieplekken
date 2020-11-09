import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {CalendarPeriodForLockers} from '../../../shared/model/CalendarPeriodForLockers';
import {api} from '../endpoints';

@Injectable({
  providedIn: 'root'
})
export class CalendarPeriodsForLockersService {

  constructor(private http: HttpClient) {
  }

  getCalendarPeriodsForLockersOfLocation(locationName: string): Observable<CalendarPeriodForLockers[]> {
    return this.http.get<CalendarPeriodForLockers[]>(api.calendarPeriodsForLockers
      .replace('{locationName}', locationName));
  }

  addCalendarPeriodsForLockers(calendarPeriodsForLockers: CalendarPeriodForLockers[]): Observable<void> {
    return this.http.post<void>(api.addCalendarPeriodsForLockers, calendarPeriodsForLockers);
  }

  updateCalendarPeriodsForLockers(locationName: string,
                                  from: CalendarPeriodForLockers[],
                                  to: CalendarPeriodForLockers[]): Observable<void> {
    const body = [from, to];
    return this.http.put<void>(api.updateCalendarPeriodsForLockers.replace('{locationName}', locationName), body);
  }

  deleteCalendarPeriodsForLockers(calendarPeriodsForLockers: CalendarPeriodForLockers[]): Observable<void> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      body: calendarPeriodsForLockers
    };
    return this.http.delete<void>(api.deleteCalendarPeriodsForLockers, options);
  }
}
