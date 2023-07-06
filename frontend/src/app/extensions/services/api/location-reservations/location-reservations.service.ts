import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { api } from '../endpoints';
import { LocationReservation } from '../../../model/LocationReservation';
import { combineLatest, Observable } from 'rxjs';
import { Timeslot } from 'src/app/extensions/model/Timeslot';
import { map } from 'rxjs/internal/operators/map';
import { of } from 'rxjs/internal/observable/of';
import { Pair } from '../../../model/helpers/Pair';
import * as moment from 'moment';
import { Moment } from 'moment';

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

  getLocationReservationsOfTimeslot(
    timeslotId: number
  ): Observable<LocationReservation[]> {
    return this.http
      .get<unknown[]>(
        api.locationReservationsOfLocation
          .replace('{seqnr}', `${timeslotId}`)
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
  ): Observable<Moment[]> {
    return locationReservations.length > 0
      ? combineLatest(
          locationReservations.map((l) => this.postLocationReservation(l))
        )
      : of<Array<Moment>>([]);
  }

  postLocationReservation(
    locationReservation: LocationReservation
  ): Observable<Moment> {
    return this.http.post<string>(
      api.addLocationReservation,
      locationReservation.timeslot,
      { withCredentials: true }
    ).pipe(map(str => {
      return moment(str);
    }));
  }

  postLocationReservationAttendance(
    locationReservation: LocationReservation,
    attended: boolean
  ): Observable<void> {
    return this.http.post<void>(
      api.updateAttendance
        .replace('{userid}', btoa(locationReservation.user.userId))
        .replace('{seqnr}', `${locationReservation.timeslot.timeslotSequenceNumber}`),
      { attended },
      { withCredentials: true }
    );
  }

  setAllNotScannedAsUnattended(
    timeslot: Timeslot
  ): Observable<void> {
    return this.http.put<void>(
      api.locationReservationsOfNotScannedUsers,
      timeslot
    );
  }
}
