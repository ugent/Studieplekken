import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Pair } from '../../../shared/model/helpers/Pair';
import { Location } from '../../../shared/model/Location';
import { Observable } from 'rxjs';
import { map } from 'rxjs/internal/operators/map';
import { filter } from 'rxjs/internal/operators/filter';
import { api } from '../endpoints';
import { LocationStatus } from '../../../app.constants';
import { Cache } from '../../../shared/cache/Cache';
import { Timeslot } from 'src/app/shared/model/Timeslot';

@Injectable({
  providedIn: 'root',
})
export class TimeslotsService {
  constructor(private http: HttpClient) {}

  // tslint:disable-next-line: max-line-length
  statusCache: Cache<number, Pair<LocationStatus, string>> = new Cache<
    number,
    Pair<LocationStatus, string>
  >(this.http, (arg: Location) => arg.locationId);

  getTimeslotsOfLocation(
    locationId: number
  ): Observable<Timeslot[]> {
    return this.http
      .get<Timeslot[]>(
        api.timeslots.replace('{locationId}', String(locationId))
      )
      .pipe(
        filter((s) => !!s),
        map((ls) => ls.map((s) => Timeslot.fromJSON(s)))
      );
  }

  /**
   * Retrieve the status of the location
   */
  getStatusOfLocation(
    locationId: number,
    invalidateCache: boolean = false
  ): Observable<Pair<LocationStatus, string>> {
    const url = api.locationStatus.replace('{locationId}', String(locationId));
    return this.statusCache.getValue(locationId, url, invalidateCache);
  }

  addTimeslot(calendarPeriods: Timeslot): Observable<void> {
    return this.http.post<void>(
      api.addTimeslots,
      calendarPeriods.toJSON()
    );
  }


  setRepeatable(timeslot: Timeslot, repeatable: boolean) {
      return this.http.put<Timeslot>(
        api.setRepeatable.replace("{timeslotId}", `${timeslot.timeslotSequenceNumber}`),
        {repeatable}
      )
  }

  deleteTimeslot(period: Timeslot): Observable<void> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
      body: period,
    };
    return this.http.delete<void>(api.deleteTimeslot, options);
  }
}
