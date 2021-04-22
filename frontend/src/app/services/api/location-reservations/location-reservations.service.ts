import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { api } from '../endpoints';
import { LocationReservation } from '../../../shared/model/LocationReservation';
import { combineLatest, Observable } from 'rxjs';
import { Timeslot } from 'src/app/shared/model/Timeslot';
import { map } from 'rxjs/internal/operators/map';
import { of } from 'rxjs/internal/observable/of';
import { Pair } from '../../../shared/model/helpers/Pair';
import { CalendarPeriod } from '../../../shared/model/CalendarPeriod';
import {Location} from '../../../shared/model/Location';

@Injectable({
  providedIn: 'root',
})
export class LocationReservationsService {
  constructor(private http: HttpClient) {}

  getLocationReservationsOfUser(id: string): Observable<LocationReservation[]> {
    if (id === '') {
      return of<LocationReservation[]>([]);
    }
    const params = new HttpParams().set('id', id);
    return this.http
      .get<LocationReservation[]>(api.locationReservationsOfUser, { params })
      .pipe(map((ls) => ls.map(LocationReservation.fromJSON.bind(this))));
  }

  getLocationReservationsWithCalendarPeriodsOfUser(
    userId: string
  ): Observable<Pair<LocationReservation, CalendarPeriod>[]> {
    return this.http
      .get<Pair<LocationReservation, CalendarPeriod>[]>(
        api.locationReservationsWithLocationOfUser.replace('{userId}', userId)
      )
      .pipe(map((value) => value.map((p) => this.createNewPair(p))));
  }

  createNewPair(
    pair: Pair<LocationReservation, CalendarPeriod>
  ): Pair<LocationReservation, CalendarPeriod> {
    return {
      first: LocationReservation.fromJSON(pair.first),
      second: CalendarPeriod.fromJSON(pair.second),
    };
  }

  getLocationReservationsOfTimeslot(
    timeslot: Timeslot
  ): Observable<LocationReservation[]> {
    return this.http
      .get<unknown[]>(
        api.locationReservationsOfLocation
          .replace('{calendarid}', `${timeslot.calendarId}`)
          .replace('{date}', timeslot.timeslotDate.format('YYYY-MM-DD'))
          .replace('{seqnr}', `${timeslot.timeslotSeqnr}`)
      )
      .pipe(map((ls) => ls.map(LocationReservation.fromJSON.bind(this))));
  }

  deleteLocationReservations(
    locationReservations: LocationReservation[]
  ): Observable<Array<void>> {
    return locationReservations.length > 0
      ? combineLatest(
          locationReservations.map((l) => this.deleteLocationReservation(l))
        )
      : of<Array<void>>([]);
  }

  deleteLocationReservation(
    locationReservation: LocationReservation
  ): Observable<void> {
    const options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
      }),
      body: locationReservation,
    };
    return this.http.delete<void>(api.deleteLocationReservation, options);
  }

  postLocationReservations(
    locationReservations: LocationReservation[]
  ): Observable<void[]> {
    return locationReservations.length > 0
      ? combineLatest(
          locationReservations.map((l) => this.postLocationReservation(l))
        )
      : of<Array<void>>([]);
  }

  postLocationReservation(
    locationReservation: LocationReservation
  ): Observable<void> {
    return this.http.post<void>(
      api.addLocationReservation,
      locationReservation.timeslot,
      { withCredentials: true }
    );
  }

  postLocationReservationAttendance(
    locationReservation: LocationReservation,
    attended: boolean
  ): Observable<void> {
    return this.http.post<void>(
      api.updateAttendance
        .replace('{userid}', locationReservation.user.augentID)
        .replace('{calendarid}', `${locationReservation.timeslot.calendarId}`)
        .replace(
          '{date}',
          locationReservation.timeslot.timeslotDate.format('YYYY-MM-DD')
        )
        .replace('{seqnr}', `${locationReservation.timeslot.timeslotSeqnr}`),
      { attended },
      { withCredentials: true }
    );
  }

  setAllNotScannedAsUnattended(
    location: Location
  ): Observable<void> {
    return this.http.put<void>(
      api.locationReservationsOfNotScannedUsers,
      location
    );
  }
}
