import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CalendarPeriodForLockers } from '@/model/CalendarPeriodForLockers';
import { api } from '../endpoints';

@Injectable({
  providedIn: 'root',
})
export class CalendarPeriodsForLockersService {
  constructor(private http: HttpClient) {}

  getCalendarPeriodsForLockersOfLocation(
    locationId: number
  ): Observable<CalendarPeriodForLockers[]> {
    return this.http.get<CalendarPeriodForLockers[]>(
      api.calendarPeriodsForLockers.replace('{locationId}', String(locationId))
    );
  }

  addCalendarPeriodsForLockers(
    calendarPeriodsForLockers: CalendarPeriodForLockers[]
  ): Observable<void> {
    return this.http.post<void>(
      api.addCalendarPeriodsForLockers,
      calendarPeriodsForLockers
    );
  }

  updateCalendarPeriodsForLockers(
    locationId: number,
    from: CalendarPeriodForLockers[],
    to: CalendarPeriodForLockers[]
  ): Observable<void> {
    const body = [from, to];
    return this.http.put<void>(
      api.updateCalendarPeriodsForLockers.replace(
        '{locationId}',
        String(locationId)
      ),
      body
    );
  }

  deleteCalendarPeriodsForLockers(
    calendarPeriodsForLockers: CalendarPeriodForLockers[]
  ): Observable<void> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
      body: calendarPeriodsForLockers,
    };
    return this.http.delete<void>(api.deleteCalendarPeriodsForLockers, options);
  }
}
